import fs from 'fs';
import path from 'path';
import crypto from 'crypto';
import { fileURLToPath } from 'url';
import { logger } from '../utils/logger.js';
import { config } from '../config/index.js';
import { query } from '../db/index.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

export interface PresignedUploadResult {
  sessionId: string;
  uploadUrl: string;
  method: string;
  headers: Record<string, string>;
  key: string;
  sessionToken: string;
  publicUrl: string;
  expiresIn: number;
}

export interface StorageProvider {
  save(filename: string, buffer: Buffer, contentType: string): Promise<{ key: string; url: string }>;
  saveStream(filename: string, stream: NodeJS.ReadableStream): Promise<{ key: string; url: string; size: number }>;
  delete(key: string): Promise<void>;
  getUrl(key: string): string;
  exists(key: string): Promise<boolean>;
  getLocalPath(key: string): string;
  getFileSize(key: string): Promise<number>;
  createPresignedUpload(
    userId: string,
    filename: string,
    contentType: string,
    maxSizeBytes: number,
    expiresInSeconds?: number
  ): Promise<PresignedUploadResult>;
}

const ALLOWED_VIDEO_MIME_TYPES = new Set([
  'video/mp4',
  'video/webm',
  'video/quicktime',
  'video/x-matroska',
  'video/3gpp',
  'video/avi',
  'video/mpeg',
]);

const MAX_VIDEO_SIZE_BYTES = 100 * 1024 * 1024; // 100 MB

export class LocalStorageProvider implements StorageProvider {
  private baseDir: string;
  private publicPrefix: string;
  private tokenSecret: string;

  constructor(baseDir?: string, publicPrefix: string = '/storage') {
    this.baseDir = baseDir || path.resolve(process.cwd(), 'server', 'storage');
    this.publicPrefix = publicPrefix;
    this.tokenSecret = config.jwt.accessSecret || 'tashan_local_storage_secret_key_83921';

    // Ensure storage subdirectories exist
    if (!fs.existsSync(this.baseDir)) {
      fs.mkdirSync(this.baseDir, { recursive: true });
    }
    const subdirs = ['videos', 'thumbnails', 'avatars', 'sounds', 'raw_videos', 'temp'];
    for (const sub of subdirs) {
      const p = path.join(this.baseDir, sub);
      if (!fs.existsSync(p)) {
        fs.mkdirSync(p, { recursive: true });
      }
    }
  }

  getLocalPath(key: string): string {
    const cleanKey = key.replace(/^\/storage\//, '').replace(/^\/cdn\//, '');
    return path.join(this.baseDir, cleanKey);
  }

  async exists(key: string): Promise<boolean> {
    const filePath = this.getLocalPath(key);
    return fs.existsSync(filePath);
  }

  async getFileSize(key: string): Promise<number> {
    const filePath = this.getLocalPath(key);
    if (!fs.existsSync(filePath)) return 0;
    const stat = await fs.promises.stat(filePath);
    return stat.size;
  }

  async save(filename: string, buffer: Buffer, _contentType: string): Promise<{ key: string; url: string }> {
    const filePath = this.getLocalPath(filename);
    const dir = path.dirname(filePath);
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
    }
    await fs.promises.writeFile(filePath, buffer);
    return {
      key: filename,
      url: this.getUrl(filename),
    };
  }

  async saveStream(filename: string, stream: NodeJS.ReadableStream): Promise<{ key: string; url: string; size: number }> {
    const filePath = this.getLocalPath(filename);
    const dir = path.dirname(filePath);
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
    }

    return new Promise((resolve, reject) => {
      const writeStream = fs.createWriteStream(filePath);
      let bytesWritten = 0;

      stream.on('data', (chunk) => {
        bytesWritten += chunk.length;
      });

      stream.pipe(writeStream);

      writeStream.on('finish', () => {
        resolve({
          key: filename,
          url: this.getUrl(filename),
          size: bytesWritten,
        });
      });

      writeStream.on('error', (err) => {
        reject(err);
      });
    });
  }

  async delete(key: string): Promise<void> {
    const filePath = this.getLocalPath(key);
    if (fs.existsSync(filePath)) {
      await fs.promises.unlink(filePath).catch(() => {});
    }
  }

  getUrl(key: string): string {
    if (key.startsWith('http://') || key.startsWith('https://')) {
      return key;
    }
    const cleanKey = key.replace(/^\/+/, '').replace(/^storage\//, '').replace(/^cdn\//, '');
    return `${this.publicPrefix}/${cleanKey.replace(/\\/g, '/')}`;
  }

  async createPresignedUpload(
    userId: string,
    filename: string,
    contentType: string,
    maxSizeBytes: number,
    expiresInSeconds: number = 900 // 15 minutes default
  ): Promise<PresignedUploadResult> {
    const extMatch = filename.match(/\.([0-9a-z]+)$/i);
    const ext = extMatch ? extMatch[1].toLowerCase() : 'mp4';
    const timestamp = Date.now();
    const randomHex = crypto.randomBytes(8).toString('hex');
    const sessionId = `ups_${randomHex}`;
    const key = `raw_videos/usr_${userId.slice(0, 16)}_${timestamp}_${randomHex}.${ext}`;

    const expiresAt = Date.now() + expiresInSeconds * 1000;

    // Create HMAC signature
    const payload = `${sessionId}:${userId}:${key}:${contentType}:${maxSizeBytes}:${expiresAt}`;
    const signature = crypto.createHmac('sha256', this.tokenSecret).update(payload).digest('hex');
    const sessionToken = Buffer.from(JSON.stringify({
      sessionId,
      userId,
      key,
      contentType,
      maxSizeBytes,
      expiresAt,
      sig: signature,
    })).toString('base64url');

    // Register upload session in database for tracking and recovery
    await query(
      `INSERT INTO upload_sessions (id, user_id, storage_key, filename, content_type, max_size_bytes, expires_at, status)
       VALUES ($1, $2, $3, $4, $5, $6, $7, 'CREATED')
       ON CONFLICT (id) DO NOTHING`,
      [sessionId, userId, key, filename, contentType, maxSizeBytes, new Date(expiresAt)]
    ).catch((err) => {
      logger.warn('[Storage] Could not insert upload session into DB', err);
    });

    const uploadUrl = `/storage/upload-direct/${sessionToken}`;

    return {
      sessionId,
      uploadUrl,
      method: 'PUT',
      headers: {
        'Content-Type': contentType,
      },
      key,
      sessionToken,
      publicUrl: this.getUrl(key),
      expiresIn: expiresInSeconds,
    };
  }

  verifyPresignedToken(token: string): {
    valid: boolean;
    error?: string;
    session?: {
      sessionId: string;
      userId: string;
      key: string;
      contentType: string;
      maxSizeBytes: number;
      expiresAt: number;
    };
  } {
    try {
      const decoded = JSON.parse(Buffer.from(token, 'base64url').toString('utf8'));
      if (!decoded.sessionId || !decoded.userId || !decoded.key || !decoded.expiresAt || !decoded.sig) {
        return { valid: false, error: 'Malformed token payload' };
      }

      if (Date.now() > decoded.expiresAt) {
        return { valid: false, error: 'Upload session has expired' };
      }

      const expectedPayload = `${decoded.sessionId}:${decoded.userId}:${decoded.key}:${decoded.contentType}:${decoded.maxSizeBytes}:${decoded.expiresAt}`;
      const expectedSignature = crypto.createHmac('sha256', this.tokenSecret).update(expectedPayload).digest('hex');

      if (!crypto.timingSafeEqual(Buffer.from(decoded.sig), Buffer.from(expectedSignature))) {
        return { valid: false, error: 'Invalid token signature' };
      }

      return {
        valid: true,
        session: {
          sessionId: decoded.sessionId,
          userId: decoded.userId,
          key: decoded.key,
          contentType: decoded.contentType,
          maxSizeBytes: decoded.maxSizeBytes,
          expiresAt: decoded.expiresAt,
        },
      };
    } catch {
      return { valid: false, error: 'Failed to decode upload token' };
    }
  }
}

export class S3StorageProvider implements StorageProvider {
  private bucket: string;
  private cdnBaseUrl: string;
  private localFallback: LocalStorageProvider;

  constructor(bucket: string = process.env.S3_BUCKET || 'tashan-media', cdnBaseUrl: string = process.env.CDN_BASE_URL || '') {
    this.bucket = bucket;
    this.cdnBaseUrl = cdnBaseUrl || `https://${bucket}.s3.amazonaws.com`;
    this.localFallback = new LocalStorageProvider();
  }

  getLocalPath(key: string): string {
    return this.localFallback.getLocalPath(key);
  }

  async exists(key: string): Promise<boolean> {
    return this.localFallback.exists(key);
  }

  async getFileSize(key: string): Promise<number> {
    return this.localFallback.getFileSize(key);
  }

  async save(filename: string, buffer: Buffer, contentType: string): Promise<{ key: string; url: string }> {
    return this.localFallback.save(filename, buffer, contentType);
  }

  async saveStream(filename: string, stream: NodeJS.ReadableStream): Promise<{ key: string; url: string; size: number }> {
    return this.localFallback.saveStream(filename, stream);
  }

  async delete(key: string): Promise<void> {
    return this.localFallback.delete(key);
  }

  getUrl(key: string): string {
    if (key.startsWith('http://') || key.startsWith('https://')) return key;
    if (this.cdnBaseUrl) {
      return `${this.cdnBaseUrl}/${key.replace(/^\/+/, '')}`;
    }
    return this.localFallback.getUrl(key);
  }

  async createPresignedUpload(
    userId: string,
    filename: string,
    contentType: string,
    maxSizeBytes: number,
    expiresInSeconds?: number
  ): Promise<PresignedUploadResult> {
    return this.localFallback.createPresignedUpload(userId, filename, contentType, maxSizeBytes, expiresInSeconds);
  }
}

export class StorageService {
  private provider: StorageProvider;
  public readonly localStorageProvider: LocalStorageProvider;

  constructor() {
    this.localStorageProvider = new LocalStorageProvider();
    const providerType = process.env.STORAGE_PROVIDER || 'local';
    if (providerType === 's3' && process.env.S3_BUCKET) {
      this.provider = new S3StorageProvider();
    } else {
      this.provider = this.localStorageProvider;
    }
  }

  validateVideoUpload(contentType: string, sizeBytes: number): { valid: boolean; error?: string } {
    if (!ALLOWED_VIDEO_MIME_TYPES.has(contentType.toLowerCase())) {
      return {
        valid: false,
        error: `Unsupported video MIME type: ${contentType}. Supported types: ${Array.from(ALLOWED_VIDEO_MIME_TYPES).join(', ')}`,
      };
    }
    if (sizeBytes > MAX_VIDEO_SIZE_BYTES) {
      return {
        valid: false,
        error: `File size ${Math.round(sizeBytes / (1024 * 1024))}MB exceeds maximum allowed limit of 100MB`,
      };
    }
    if (sizeBytes <= 0) {
      return {
        valid: false,
        error: 'File size must be greater than 0 bytes',
      };
    }
    return { valid: true };
  }

  async createUploadSession(
    userId: string,
    filename: string,
    contentType: string,
    sizeBytes: number
  ): Promise<PresignedUploadResult> {
    const validation = this.validateVideoUpload(contentType, sizeBytes);
    if (!validation.valid) {
      throw new Error(validation.error);
    }

    return this.provider.createPresignedUpload(userId, filename, contentType, sizeBytes, 900);
  }

  async updateUploadSessionStatus(sessionId: string, status: string, bytesUploaded?: number): Promise<void> {
    await query(
      `UPDATE upload_sessions
       SET status = $1, bytes_uploaded = COALESCE($2, bytes_uploaded), updated_at = NOW()
       WHERE id = $3`,
      [status, bytesUploaded || null, sessionId]
    ).catch(() => {});
  }

  async getUploadSession(sessionId: string) {
    const res = await query(
      `SELECT id, user_id, storage_key, filename, content_type, max_size_bytes, bytes_uploaded, status, expires_at
       FROM upload_sessions
       WHERE id = $1`,
      [sessionId]
    );
    return res.rows[0] || null;
  }

  async uploadFile(filename: string, buffer: Buffer, contentType: string): Promise<{ key: string; url: string }> {
    return this.provider.save(filename, buffer, contentType);
  }

  async deleteFile(key: string): Promise<void> {
    return this.provider.delete(key);
  }

  getFileUrl(key: string): string {
    return this.provider.getUrl(key);
  }

  getLocalPath(key: string): string {
    return this.provider.getLocalPath(key);
  }

  async exists(key: string): Promise<boolean> {
    return this.provider.exists(key);
  }

  async getFileSize(key: string): Promise<number> {
    return this.provider.getFileSize(key);
  }
}

export const storageService = new StorageService();
