import dotenv from 'dotenv';
import path from 'path';

dotenv.config();

export const config = {
  env: process.env.NODE_ENV || 'development',
  port: parseInt(process.env.PORT || '3000', 10),
  host: process.env.HOST || '0.0.0.0',
  databaseUrl: process.env.DATABASE_URL || '',
  jwt: {
    accessSecret: process.env.JWT_ACCESS_SECRET || 'tashan_prod_access_secret_key_change_in_production_3892749281',
    refreshSecret: process.env.JWT_REFRESH_SECRET || 'tashan_prod_refresh_secret_key_change_in_production_8492048192',
    accessExpiresIn: process.env.JWT_ACCESS_EXPIRES_IN || '1h',
    refreshExpiresInDays: parseInt(process.env.JWT_REFRESH_EXPIRES_DAYS || '30', 10),
  },
  cors: {
    allowedOrigins: (process.env.CORS_ALLOWED_ORIGINS || '*').split(',').map((s) => s.trim()),
  },
  rateLimit: {
    windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS || '60000', 10), // 1 minute
    maxRequests: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS || '180', 10),
  },
  storage: {
    provider: process.env.STORAGE_PROVIDER || 'local', // 'local' or 's3'
    localDir: path.resolve(process.env.STORAGE_LOCAL_DIR || './storage'),
    publicBaseUrl: process.env.STORAGE_PUBLIC_BASE_URL || 'http://localhost:3000/cdn',
    s3: {
      endpoint: process.env.S3_ENDPOINT || '',
      bucket: process.env.S3_BUCKET || 'tashan-media',
      region: process.env.S3_REGION || 'auto',
      accessKeyId: process.env.S3_ACCESS_KEY_ID || '',
      secretAccessKey: process.env.S3_SECRET_ACCESS_KEY || '',
      publicUrl: process.env.S3_PUBLIC_URL || '',
    },
  },
  worker: {
    pollIntervalMs: parseInt(process.env.WORKER_POLL_INTERVAL_MS || '2000', 10),
    maxConcurrentJobs: parseInt(process.env.WORKER_MAX_CONCURRENT_JOBS || '2', 10),
    ffmpegPath: process.env.FFMPEG_PATH || 'ffmpeg',
  },
  admin: {
    secretKey: process.env.ADMIN_SECRET_KEY || 'tashan_admin_master_secret_key_999182',
  },
};
