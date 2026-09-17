import { query } from '../db/index.js';
import { logger } from '../utils/logger.js';

let isRunning = false;
let workerInterval: NodeJS.Timeout | null = null;

export async function processNextJob(): Promise<boolean> {
  try {
    const res = await query(
      `SELECT id, video_id, source_storage_key, attempts
       FROM video_processing_jobs
       WHERE status = 'PENDING'
       ORDER BY created_at ASC
       LIMIT 1`
    );

    if (res.rows.length === 0) {
      return false;
    }

    const job = res.rows[0];
    logger.info(`[FFmpeg Worker] Processing job ${job.id} for video ${job.video_id}...`);

    await query(
      `UPDATE video_processing_jobs
       SET status = 'PROCESSING', started_at = NOW(), attempts = attempts + 1
       WHERE id = $1`,
      [job.id]
    );

    // Simulate transcoding & thumbnail extraction pipeline
    // (In production with ffmpeg binary available, ffmpeg -i input -ss 00:00:01 -vframes 1 thumb.jpg)
    await new Promise((resolve) => setTimeout(resolve, 500));

    await query(
      `UPDATE video_processing_jobs
       SET status = 'COMPLETED', completed_at = NOW()
       WHERE id = $1`,
      [job.id]
    );

    await query(
      `UPDATE videos
       SET status = 'READY', updated_at = NOW()
       WHERE id = $1`,
      [job.video_id]
    );

    logger.info(`[FFmpeg Worker] Completed job ${job.id} for video ${job.video_id}.`);
    return true;
  } catch (err) {
    logger.error('[FFmpeg Worker] Error processing video job', err);
    return false;
  }
}

export function startWorker(intervalMs: number = 3000) {
  if (isRunning) return;
  isRunning = true;
  logger.info('[FFmpeg Worker] Started background video transcoding worker');

  workerInterval = setInterval(async () => {
    try {
      await processNextJob();
    } catch (err) {
      logger.error('[FFmpeg Worker] Unhandled worker loop error', err);
    }
  }, intervalMs);
}

export function stopWorker() {
  if (workerInterval) {
    clearInterval(workerInterval);
    workerInterval = null;
  }
  isRunning = false;
  logger.info('[FFmpeg Worker] Stopped video transcoding worker');
}

// If executed directly from CLI (e.g. tsx src/workers/ffmpegWorker.ts)
if (process.argv[1] && process.argv[1].includes('ffmpegWorker')) {
  logger.info('[FFmpeg Worker] CLI standalone entry point launched');
  startWorker(2000);
}
