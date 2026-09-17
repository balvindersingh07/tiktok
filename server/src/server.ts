import { Hono } from 'hono';
import { serve } from '@hono/node-server';
import { cors } from 'hono/cors';
import { logger as honoLogger } from 'hono/logger';
import { prettyJSON } from 'hono/pretty-json';
import path from 'path';
import fs from 'fs';

import { config } from './config/index.js';
import { apiRouter } from './routes/index.js';
import { healthRouter } from './routes/health.js';
import { errorHandler } from './middleware/errorHandler.js';
import { runMigrations } from './db/migrate.js';
import { seedInitialDataIfNeeded } from './db/seed.js';
import { startWorker } from './workers/ffmpegWorker.js';
import { logger } from './utils/logger.js';

export const app = new Hono();

// Global Middlewares
app.use('*', honoLogger());
app.use('*', prettyJSON());
app.use(
  '*',
  cors({
    origin: (origin) => origin || '*',
    allowMethods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS'],
    allowHeaders: ['Content-Type', 'Authorization', 'X-Requested-With'],
    exposeHeaders: ['Content-Length', 'X-Total-Count'],
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
    endpoints: {
      health: '/health',
      apiHealth: '/api/health',
      auth: '/api/auth',
      feed: '/api/feed',
      videos: '/api/videos',
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

// Storage static file serving
app.get('/storage/*', async (c) => {
  const reqPath = c.req.path.replace(/^\/storage\//, '');
  const filePath = path.join(config.storage.localDir, reqPath);
  if (fs.existsSync(filePath)) {
    const stat = fs.statSync(filePath);
    const data = fs.readFileSync(filePath);
    const mime = filePath.endsWith('.mp4') ? 'video/mp4' : 'image/jpeg';
    return c.body(data, 200, {
      'Content-Type': mime,
      'Content-Length': stat.size.toString(),
      'Cache-Control': 'public, max-age=31536000',
    });
  }
  return c.text('Not found', 404);
});

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
const HONO_PORT = Number(process.env.HONO_PORT || (process.env.PORT && process.env.PORT !== '3000' && process.env.PORT !== '8080' ? process.env.PORT : 3001));
const HOST = config.host || '0.0.0.0';

export async function initializeBackend() {
  try {
    await runMigrations();
    await seedInitialDataIfNeeded();
    startWorker(3000);
    logger.info('Tashan Backend initialization complete.');
  } catch (err) {
    logger.error('Backend initialization error', err);
  }
}

// Auto-run if executed as server entry
initializeBackend().catch((err) => logger.error('Init error', err));

if (process.env.NODE_ENV !== 'test' && !process.env.VITE_EMBEDDED && process.env.RUN_STANDALONE_SERVER === 'true') {
  serve(
    {
      fetch: app.fetch,
      port: HONO_PORT,
      hostname: HOST,
    },
    (info) => {
      logger.info(`🚀 Tashan Hono Backend listening on http://${info.address}:${info.port}`);
    }
  );
}

export default app;
