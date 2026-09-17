import fs from 'fs';
import path from 'path';
import { execFile } from 'child_process';
import { promisify } from 'util';
import { query } from '../db/index.js';
import { logger } from '../utils/logger.js';
import { storageService } from '../services/storage.service.js';

const execFileAsync = promisify(execFile);

let isRunning = false;
let workerTimeout: NodeJS.Timeout | null = null;

interface ProbedMetadata {
  width: number;
  height: number;
  durationSeconds: number;
  aspectRatio: string;
  bitrate: number;
  codec: string;
}

export async function probeVideoMetadata(filePath: string): Promise<ProbedMetadata> {
  try {
    const { stdout } = await execFileAsync('ffprobe', [
      '-v', 'error',
      '-select_streams', 'v:0',
      '-show_entries', 'stream=width,height,codec_name,duration:format=duration,bit_rate',
      '-of', 'json',
      filePath,
    ]);

    const data = JSON.parse(stdout);
    const stream = data.streams?.[0] || {};
    const format = data.format || {};

    const width = stream.width ? parseInt(stream.width, 10) : 720;
    const height = stream.height ? parseInt(stream.height, 10) : 1280;
    const durationSeconds = Math.max(
      1,
      Math.round(parseFloat(stream.duration || format.duration || '15'))
    );
    const bitrate = format.bit_rate ? parseInt(format.bit_rate, 10) : 1500000;
    const codec = stream.codec_name || 'h264';

    // Calculate aspect ratio string
    const ratio = width / height;
    let aspectRatio = '9:16';
    if (Math.abs(ratio - 16 / 9) < 0.1) aspectRatio = '16:9';
    else if (Math.abs(ratio - 1) < 0.1) aspectRatio = '1:1';
    else if (Math.abs(ratio - 4 / 3) < 0.1) aspectRatio = '4:3';

    return {
      width,
      height,
      durationSeconds,
      aspectRatio,
      bitrate,
      codec,
    };
  } catch (err) {
    logger.warn(`[FFmpeg Worker] ffprobe failed for ${filePath}, using sensible defaults`, err);
    return {
      width: 720,
      height: 1280,
      durationSeconds: 15,
      aspectRatio: '9:16',
      bitrate: 1500000,
      codec: 'h264',
    };
  }
}

export async function extractThumbnail(inputPath: string, outputPath: string, durationSeconds: number): Promise<void> {
  const seekTime = Math.min(1.0, Math.max(0.2, durationSeconds / 4)).toFixed(2);
  const dir = path.dirname(outputPath);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }

  await execFileAsync('ffmpeg', [
    '-y',
    '-ss', seekTime,
    '-i', inputPath,
    '-vframes', '1',
    '-q:v', '2',
    '-vf', 'scale=720:-2',
    outputPath,
  ]);
}

export async function transcodeVideo(inputPath: string, outputPath: string, targetHeight?: number): Promise<void> {
  const dir = path.dirname(outputPath);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }

  const args = [
    '-y',
    '-i', inputPath,
  ];

  if (targetHeight && targetHeight > 0) {
    args.push('-vf', `scale=-2:${targetHeight}`);
  }

  args.push(
    '-c:v', 'libx264',
    '-preset', 'fast',
    '-crf', '23',
    '-pix_fmt', 'yuv420p',
    '-movflags', '+faststart',
    '-c:a', 'aac',
    '-b:a', '128k',
    '-ar', '44100',
    outputPath
  );

  await execFileAsync('ffmpeg', args);
}

export async function recoverStuckJobs(): Promise<void> {
  try {
    // Look for jobs that have been PROCESSING for more than 5 minutes
    const stuckRes = await query(
      `SELECT id, video_id, attempts, max_attempts
       FROM video_processing_jobs
       WHERE status = 'PROCESSING'
         AND started_at < NOW() - INTERVAL '5 minutes'`
    );

    for (const job of stuckRes.rows) {
      if (job.attempts >= job.max_attempts) {
        logger.warn(`[FFmpeg Worker] Job ${job.id} exceeded max attempts, marking FAILED`);
        await query(
          `UPDATE video_processing_jobs
           SET status = 'FAILED', error_message = 'Job timed out after multiple attempts', updated_at = NOW()
           WHERE id = $1`,
          [job.id]
        );
        await query(
          `UPDATE videos SET status = 'FAILED', updated_at = NOW() WHERE id = $1`,
          [job.video_id]
        );
      } else {
        logger.info(`[FFmpeg Worker] Re-queuing stuck job ${job.id}`);
        await query(
          `UPDATE video_processing_jobs
           SET status = 'PENDING', updated_at = NOW()
           WHERE id = $1`,
          [job.id]
        );
      }
    }
  } catch (err) {
    logger.error('[FFmpeg Worker] Error in recoverStuckJobs', err);
  }
}

export async function cleanupOrphanMedia(): Promise<void> {
  try {
    // Clean up temporary files older than 24 hours
    const tempDir = storageService.getLocalPath('temp');
    if (fs.existsSync(tempDir)) {
      const files = await fs.promises.readdir(tempDir);
      const now = Date.now();
      for (const file of files) {
        const fullPath = path.join(tempDir, file);
        const stat = await fs.promises.stat(fullPath);
        if (now - stat.mtimeMs > 24 * 60 * 60 * 1000) {
          await fs.promises.unlink(fullPath).catch(() => {});
        }
      }
    }
  } catch (err) {
    logger.warn('[FFmpeg Worker] Orphan media cleanup notice', err);
  }
}

export async function processNextJob(): Promise<boolean> {
  let activeJobId: string | null = null;
  let activeVideoId: string | null = null;

  try {
    // Recover any stuck jobs first
    await recoverStuckJobs();

    // Fetch next pending job
    const res = await query(
      `SELECT id, video_id, source_storage_key, attempts, max_attempts
       FROM video_processing_jobs
       WHERE status = 'PENDING'
       ORDER BY created_at ASC
       LIMIT 1`
    );

    if (res.rows.length === 0) {
      return false;
    }

    const job = res.rows[0];
    activeJobId = job.id;
    activeVideoId = job.video_id;

    logger.info(`[FFmpeg Worker] 🎬 Processing job ${job.id} (attempt ${job.attempts + 1}) for video ${job.video_id}`);

    await query(
      `UPDATE video_processing_jobs
       SET status = 'PROCESSING', started_at = NOW(), attempts = attempts + 1, updated_at = NOW()
       WHERE id = $1`,
      [job.id]
    );

    await query(
      `UPDATE videos
       SET status = 'PROCESSING', updated_at = NOW()
       WHERE id = $1`,
      [job.video_id]
    );

    const inputPath = storageService.getLocalPath(job.source_storage_key);

    if (!fs.existsSync(inputPath)) {
      throw new Error(`Source media file not found on disk at ${inputPath}`);
    }

    // Step 1: Probe video metadata with ffprobe
    const metadata = await probeVideoMetadata(inputPath);
    logger.info(`[FFmpeg Worker] Probed metadata for video ${job.video_id}: ${metadata.width}x${metadata.height}, ${metadata.durationSeconds}s`);

    // Step 2: Generate poster thumbnail
    const thumbKey = `thumbnails/thumb_${job.video_id}.jpg`;
    const thumbPath = storageService.getLocalPath(thumbKey);
    await extractThumbnail(inputPath, thumbPath, metadata.durationSeconds);
    const thumbnailUrl = storageService.getFileUrl(thumbKey);
    logger.info(`[FFmpeg Worker] Thumbnail generated: ${thumbnailUrl}`);

    // Step 3: Transcode to web/mobile optimized MP4 with +faststart
    const outputKey = `videos/proc_${job.video_id}.mp4`;
    const outputPath = storageService.getLocalPath(outputKey);
    await transcodeVideo(inputPath, outputPath);
    const processedVideoUrl = storageService.getFileUrl(outputKey);
    logger.info(`[FFmpeg Worker] Transcoded master MP4: ${processedVideoUrl}`);

    // Step 4: If resolution is 1080p+, create 720p mobile variation
    if (metadata.height > 720) {
      const output720pKey = `videos/proc_${job.video_id}_720p.mp4`;
      const output720pPath = storageService.getLocalPath(output720pKey);
      await transcodeVideo(inputPath, output720pPath, 720).catch((e) => {
        logger.warn('[FFmpeg Worker] 720p downscale skipped', e);
      });
    }

    // Step 5: Update Video record with processed media URLs and probed metadata
    await query(
      `UPDATE videos
       SET status = 'READY',
           video_url = $1,
           thumbnail_url = $2,
           cover_res_name = $3,
           width = $4,
           height = $5,
           duration_seconds = $6,
           aspect_ratio = $7,
           updated_at = NOW()
       WHERE id = $8`,
      [
        processedVideoUrl,
        thumbnailUrl,
        thumbnailUrl,
        metadata.width,
        metadata.height,
        metadata.durationSeconds,
        metadata.aspectRatio,
        job.video_id,
      ]
    );

    // Step 6: Mark job completed
    await query(
      `UPDATE video_processing_jobs
       SET status = 'COMPLETED', completed_at = NOW(), updated_at = NOW()
       WHERE id = $1`,
      [job.id]
    );

    logger.info(`[FFmpeg Worker] ✅ Video ${job.video_id} processing completed successfully! Ready for streaming.`);
    return true;
  } catch (err: any) {
    logger.error(`[FFmpeg Worker] ❌ Job ${activeJobId} failed`, err);

    if (activeJobId && activeVideoId) {
      const errMessage = err?.message ? String(err.message).slice(0, 500) : 'Unknown processing failure';

      const jobCheck = await query(
        'SELECT attempts, max_attempts FROM video_processing_jobs WHERE id = $1',
        [activeJobId]
      );
      const attempts = jobCheck.rows[0]?.attempts || 1;
      const maxAttempts = jobCheck.rows[0]?.max_attempts || 3;

      if (attempts >= maxAttempts) {
        await query(
          `UPDATE video_processing_jobs
           SET status = 'FAILED', error_message = $1, updated_at = NOW()
           WHERE id = $2`,
          [errMessage, activeJobId]
        );

        await query(
          `UPDATE videos
           SET status = 'FAILED', updated_at = NOW()
           WHERE id = $1`,
          [activeVideoId]
        );
      } else {
        // Re-queue for retry
        await query(
          `UPDATE video_processing_jobs
           SET status = 'PENDING', error_message = $1, updated_at = NOW()
           WHERE id = $2`,
          [`Attempt ${attempts} failed: ${errMessage}`, activeJobId]
        );
      }
    }
    return false;
  }
}

export function startWorker(intervalMs: number = 2000) {
  if (isRunning) return;
  isRunning = true;
  logger.info('[FFmpeg Worker] 🚀 Started video transcoding worker daemon');

  // Initial orphan media cleanup
  cleanupOrphanMedia().catch(() => {});

  const runLoop = async () => {
    if (!isRunning) return;
    try {
      const processed = await processNextJob();
      // If a job was processed, immediately check for the next job with short pause
      const nextDelay = processed ? 300 : intervalMs;
      workerTimeout = setTimeout(runLoop, nextDelay);
    } catch (err) {
      logger.error('[FFmpeg Worker] Error in worker execution loop', err);
      workerTimeout = setTimeout(runLoop, intervalMs);
    }
  };

  workerTimeout = setTimeout(runLoop, 500);
}

export function stopWorker() {
  if (workerTimeout) {
    clearTimeout(workerTimeout);
    workerTimeout = null;
  }
  isRunning = false;
  logger.info('[FFmpeg Worker] Stopped video transcoding worker');
}

// Standalone execution entry point
if (process.argv[1] && (process.argv[1].includes('ffmpegWorker') || process.env.RUN_STANDALONE_WORKER === 'true')) {
  logger.info('[FFmpeg Worker] 🌟 Running as standalone OS process');
  startWorker(1500);

  const shutdown = () => {
    logger.info('[FFmpeg Worker] Received shutdown signal');
    stopWorker();
    process.exit(0);
  };
  process.on('SIGTERM', shutdown);
  process.on('SIGINT', shutdown);
}
