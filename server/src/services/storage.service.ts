import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { logger } from '../utils/logger.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

export interface StorageProvider {
  save(filename: string, buffer: Buffer, contentType: string): Promise<{ key: string; url: string }>;
  delete(key: string): Promise<void>;
  getUrl(key: string): string;
}

export class LocalStorageProvider implements StorageProvider {
  private baseDir: string;
  private publicPrefix: string;

  constructor(baseDir?: string, publicPrefix: string = '/storage') {
    this.baseDir = baseDir || path.resolve(process.cwd(), 'storage');
    this.publicPrefix = publicPrefix;

    // Ensure storage subdirectories exist
    if (!fs.existsSync(this.baseDir)) {
      fs.mkdirSync(this.baseDir, { recursive: true });
    }
    const subdirs = ['videos', 'thumbnails', 'avatars', 'sounds'];
    for (const sub of subdirs) {
      const p = path.join(this.baseDir, sub);
      if (!fs.existsSync(p)) {
        fs.mkdirSync(p, { recursive: true });
      }
    }
  }

  async save(filename: string, buffer: Buffer, _contentType: string): Promise<{ key: string; url: string }> {
    const filePath = path.join(this.baseDir, filename);
    const dir = path.dirname(filePath);
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
    }
    await fs.promises.writeFile(filePath, buffer);
    return {
      key: filename,
      url: `${this.publicPrefix}/${filename.replace(/\\/g, '/')}`,
    };
  }

  async delete(key: string): Promise<void> {
    const filePath = path.join(this.baseDir, key);
    if (fs.existsSync(filePath)) {
      await fs.promises.unlink(filePath).catch(() => {});
    }
  }

  getUrl(key: string): string {
    if (key.startsWith('http://') || key.startsWith('https://') || key.startsWith('/')) {
      return key;
    }
    return `${this.publicPrefix}/${key.replace(/\\/g, '/')}`;
  }
}

export class S3StorageProvider implements StorageProvider {
  private bucket: string;
  private cdnBaseUrl: string;

  constructor(bucket: string = process.env.S3_BUCKET || 'tashan-media', cdnBaseUrl: string = process.env.CDN_BASE_URL || '') {
    this.bucket = bucket;
    this.cdnBaseUrl = cdnBaseUrl || `https://${bucket}.s3.amazonaws.com`;
  }

  async save(filename: string, _buffer: Buffer, _contentType: string): Promise<{ key: string; url: string }> {
    // In production with AWS/R2 credentials:
    // await s3Client.send(new PutObjectCommand({ Bucket: this.bucket, Key: filename, Body: buffer, ContentType: contentType }))
    logger.info(`[S3 Storage] Upload simulated for ${filename} to bucket ${this.bucket}`);
    return {
      key: filename,
      url: `${this.cdnBaseUrl}/${filename}`,
    };
  }

  async delete(key: string): Promise<void> {
    logger.info(`[S3 Storage] Delete simulated for ${key} in bucket ${this.bucket}`);
  }

  getUrl(key: string): string {
    if (key.startsWith('http://') || key.startsWith('https://')) return key;
    return `${this.cdnBaseUrl}/${key}`;
  }
}

export class StorageService {
  private provider: StorageProvider;

  constructor() {
    const providerType = process.env.STORAGE_PROVIDER || 'local';
    if (providerType === 's3' && process.env.S3_BUCKET) {
      this.provider = new S3StorageProvider();
    } else {
      this.provider = new LocalStorageProvider();
    }
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
}

export const storageService = new StorageService();
