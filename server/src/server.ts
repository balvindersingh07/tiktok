import { Hono } from 'hono';
import { serve } from '@hono/node-server';
import { cors } from 'hono/cors';
import { logger as honoLogger } from 'hono/logger';
import { prettyJSON } from 'hono/pretty-json';
import path from 'path';
import fs from 'fs';
import { Readable } from 'stream';

import { config } from './config/index.js';
import { apiRouter } from './routes/index.js';
import { healthRouter } from './routes/health.js';
import { errorHandler } from './middleware/errorHandler.js';
import { runMigrations } from './db/migrate.js';
import { seedInitialDataIfNeeded } from './db/seed.js';
import { startWorker, stopWorker } from './workers/ffmpegWorker.js';
import { storageService } from './services/storage.service.js';
import { logger } from './utils/logger.js';

export const app = new Hono();

// Global Middlewares
app.use('*', honoLogger());
app.use('*', prettyJSON());

// Security Headers Middleware
app.use('*', async (c, next) => {
  await next();
  c.header('X-Content-Type-Options', 'nosniff');
  c.header('X-Frame-Options', 'SAMEORIGIN');
  c.header('X-XSS-Protection', '1; mode=block');
  c.header('Referrer-Policy', 'strict-origin-when-cross-origin');
});

// CORS Middleware
app.use(
  '*',
  cors({
    origin: (origin) => origin || '*',
    allowMethods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS'],
    allowHeaders: ['Content-Type', 'Authorization', 'X-Requested-With', 'Range'],
    exposeHeaders: ['Content-Length', 'Content-Range', 'Accept-Ranges', 'X-Total-Count'],
    credentials: true,
  })
);

// Base / Root Route
app.get('/', (c) => {
  return c.json({
    service: 'Tashan Video Platform - Production Backend API',
    version: '1.0.0',
    framework: 'Hono',
    status: 'online',
    database: 'PostgreSQL',
    storage: {
      provider: config.storage.provider,
      cdn: config.storage.publicBaseUrl,
      directUploads: true,
    },
    endpoints: {
      health: '/health',
      apiHealth: '/api/health',
      auth: '/api/auth',
      feed: '/api/feed',
      videos: '/api/videos',
      uploadSession: '/api/videos/upload-session',
      confirmUpload: '/api/videos/confirm-upload',
      directUpload: '/storage/upload-direct/:sessionToken',
      users: '/api/users',
      notifications: '/api/notifications',
      messages: '/api/messages',
      sounds: '/api/sounds',
      search: '/api/search',
      analytics: '/api/analytics',
      drafts: '/api/drafts',
    },
  });
});

// 1. Direct Presigned Object Storage Upload (PUT /storage/upload-direct/:token)
// Directly receives streaming video binary from client/Android without passing through API controllers
app.put('/storage/upload-direct/:token', async (c) => {
  const token = c.req.param('token');
  const verification = storageService.localStorageProvider.verifyPresignedToken(token);

  if (!verification.valid || !verification.session) {
    return c.json(
      {
        success: false,
        error: verification.error || 'Invalid or expired upload token',
      },
      403
    );
  }

  const session = verification.session;
  const targetPath = storageService.getLocalPath(session.key);
  const dir = path.dirname(targetPath);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }

  const rawBody = c.req.raw.body;
  if (!rawBody) {
    return c.json({ success: false, error: 'Empty request body stream' }, 400);
  }

  try {
    const writeStream = fs.createWriteStream(targetPath);
    let bytesWritten = 0;
    const reader = rawBody.getReader();

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      bytesWritten += value.length;

      if (bytesWritten > session.maxSizeBytes) {
        writeStream.destroy();
        await fs.promises.unlink(targetPath).catch(() => {});
        return c.json(
          {
            success: false,
            error: `File size exceeds allowed limit of ${session.maxSizeBytes} bytes`,
          },
          413
        );
      }
      writeStream.write(Buffer.from(value));
    }

    await new Promise((resolve, reject) => {
      writeStream.end(resolve);
      writeStream.on('error', reject);
    });

    await storageService.updateUploadSessionStatus(session.sessionId, 'UPLOADED', bytesWritten);
    logger.info(`[Direct Storage] Upload completed for ${session.key} (${bytesWritten} bytes)`);

    return c.json({
      success: true,
      sessionId: session.sessionId,
      key: session.key,
      sizeBytes: bytesWritten,
      publicUrl: storageService.getFileUrl(session.key),
      message: 'Direct object upload completed successfully',
    });
  } catch (err: any) {
    logger.error('[Direct Storage] Streaming upload failed', err);
    await fs.promises.unlink(targetPath).catch(() => {});
    return c.json({ success: false, error: 'Upload stream failure: ' + err.message }, 500);
  }
});

// 2. High-Performance Remote Media & CDN Serving with HTTP Byte-Range Requests
const handleMediaServing = async (c: any) => {
  const reqPath = c.req.path.replace(/^\/(storage|cdn)\//, '');
  const filePath = storageService.getLocalPath(reqPath);

  if (!fs.existsSync(filePath)) {
    return c.text('Media not found', 404);
  }

  const stat = await fs.promises.stat(filePath);
  const fileSize = stat.size;
  const ext = path.extname(filePath).toLowerCase();

  const mimeMap: Record<string, string> = {
    '.mp4': 'video/mp4',
    '.webm': 'video/webm',
    '.mov': 'video/quicktime',
    '.jpg': 'image/jpeg',
    '.jpeg': 'image/jpeg',
    '.png': 'image/png',
    '.webp': 'image/webp',
    '.mp3': 'audio/mpeg',
    '.aac': 'audio/aac',
    '.m4a': 'audio/mp4',
  };
  const contentType = mimeMap[ext] || 'application/octet-stream';

  const range = c.req.header('range');
  if (range && range.startsWith('bytes=')) {
    const parts = range.replace(/bytes=/, '').split('-');
    const start = parseInt(parts[0], 10);
    const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;

    if (start >= fileSize || end >= fileSize || start > end) {
      return c.text('Requested range not satisfiable', 416, {
        'Content-Range': `bytes */${fileSize}`,
      });
    }

    const chunksize = end - start + 1;
    const nodeStream = fs.createReadStream(filePath, { start, end });
    const webStream = Readable.toWeb(nodeStream);

    return c.body(webStream as any, 206, {
      'Content-Range': `bytes ${start}-${end}/${fileSize}`,
      'Accept-Ranges': 'bytes',
      'Content-Length': chunksize.toString(),
      'Content-Type': contentType,
      'Cache-Control': 'public, max-age=31536000',
    });
  }

  const nodeStream = fs.createReadStream(filePath);
  const webStream = Readable.toWeb(nodeStream);

  return c.body(webStream as any, 200, {
    'Content-Length': fileSize.toString(),
    'Content-Type': contentType,
    'Accept-Ranges': 'bytes',
    'Cache-Control': 'public, max-age=31536000',
  });
};

app.get('/storage/*', handleMediaServing);
app.get('/cdn/*', handleMediaServing);

// Health Checks
app.route('/health', healthRouter);

// API Sub-routes
app.route('/api', apiRouter);

// 404 Handler
app.notFound((c) => {
  return c.json(
    {
      success: false,
      error: 'Not Found',
      message: `Route ${c.req.method} ${c.req.path} does not exist`,
    },
    404
  );
});

// Global Error Handler
app.onError((err, c) => errorHandler(err, c));

// Port configuration
const HONO_PORT = Number(
  process.env.HONO_PORT ||
    (process.env.PORT && process.env.PORT !== '3000' && process.env.PORT !== '8080'
      ? process.env.PORT
      : 3001)
);
const HOST = config.host || '0.0.0.0';

let isBackendInitializing = false;
let isBackendInitialized = false;

export async function initializeBackend() {
  if (isBackendInitialized) return;
  if (isBackendInitializing) {
    while (isBackendInitializing) {
      await new Promise((r) => setTimeout(r, 50));
    }
    return;
  }
  isBackendInitializing = true;
  try {
    await runMigrations();
    await seedInitialDataIfNeeded();
    // Start background FFmpeg video processing worker
    startWorker(config.worker.pollIntervalMs || 2000);
    isBackendInitialized = true;
    logger.info('Tashan Backend initialization complete.');
  } catch (err) {
    logger.error('Backend initialization error', err);
  } finally {
    isBackendInitializing = false;
  }
}

// Auto-run only if executed directly as standalone server entry, NOT inside Vite embedded plugin
if (
  !process.env.VITE_EMBEDDED &&
  (process.env.RUN_STANDALONE_SERVER === 'true' || process.argv[1]?.includes('server.ts'))
) {
  initializeBackend().catch((err) => logger.error('Init error', err));
}

if (
  process.env.NODE_ENV !== 'test' &&
  !process.env.VITE_EMBEDDED &&
  (process.env.RUN_STANDALONE_SERVER === 'true' || process.argv[1]?.includes('server.ts'))
) {
  const server = serve(
    {
      fetch: app.fetch,
      port: HONO_PORT,
      hostname: HOST,
    },
    (info) => {
      logger.info(`🚀 Tashan Hono Backend listening on http://${info.address}:${info.port}`);
    }
  );

  const gracefulShutdown = () => {
    logger.info('Shutting down Tashan Hono server gracefully...');
    stopWorker();
    server.close(() => {
      logger.info('Server closed');
      process.exit(0);
    });
  };

  process.on('SIGTERM', gracefulShutdown);
  process.on('SIGINT', gracefulShutdown);
}

export default app;
