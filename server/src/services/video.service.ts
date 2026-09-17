import { v4 as uuidv4 } from 'uuid';
import { videoRepository } from '../repositories/video.repository.js';
import { notificationRepository } from '../repositories/notification.repository.js';
import { soundRepository } from '../repositories/sound.repository.js';
import { eventBus } from '../utils/eventBus.js';
import { NotFoundError, ForbiddenError } from '../utils/errors.js';
import { storageService } from './storage.service.js';
import { query } from '../db/index.js';

export class VideoService {
  async publishVideo(authorId: string, data: {
    caption?: string;
    soundTitle?: string;
    soundAuthor?: string;
    soundId?: string | null;
    videoPath?: string;
    sourceStorageKey?: string | null;
    coverResName?: string;
    category?: string;
    hashtags?: string;
    durationSeconds?: number;
    isPrivate?: boolean;
    allowComments?: boolean;
    allowDuet?: boolean;
    allowStitch?: boolean;
    duetWithVideoId?: string | null;
    stitchWithVideoId?: string | null;
    status?: 'UPLOAD_PENDING' | 'PROCESSING' | 'READY' | 'FAILED';
  }) {
    const videoId = `vid_${uuidv4().replace(/-/g, '').slice(0, 16)}`;
    const sourceStorageKey = data.sourceStorageKey || data.videoPath || '';
    const initialStatus = data.status || (sourceStorageKey ? 'PROCESSING' : 'READY');
    const videoUrl = data.videoPath || (sourceStorageKey ? storageService.getFileUrl(sourceStorageKey) : '/storage/videos/vid_001.mp4');
    const coverRes = data.coverResName || 'video_cover_dance';

    const video = await videoRepository.create({
      id: videoId,
      authorId,
      caption: data.caption || '',
      soundId: data.soundId || null,
      soundTitle: data.soundTitle || 'Original Audio',
      soundAuthor: data.soundAuthor || 'Creator',
      sourceStorageKey,
      videoUrl,
      coverResName: coverRes,
      durationSeconds: data.durationSeconds || 15,
      category: data.category || 'fyp',
      hashtags: data.hashtags || '',
      isPrivate: data.isPrivate || false,
      allowComments: data.allowComments ?? true,
      allowDuet: data.allowDuet ?? true,
      allowStitch: data.allowStitch ?? true,
      duetWithVideoId: data.duetWithVideoId || null,
      stitchWithVideoId: data.stitchWithVideoId || null,
      status: initialStatus,
    });

    // If a sound is attached, increment its usage
    if (data.soundId) {
      await soundRepository.incrementUsage(data.soundId, authorId, videoId).catch(() => {});
    }

    // Trigger asynchronous transcoding/thumbnail queue
    const jobId = `job_${uuidv4().replace(/-/g, '').slice(0, 16)}`;
    if (sourceStorageKey) {
      await videoRepository.createProcessingJob({
        id: jobId,
        videoId,
        sourceStorageKey,
      });
    }

    return {
      ...video,
      jobId,
      status: initialStatus,
    };
  }

  async getProcessingJobStatus(jobId: string) {
    const res = await query(
      `SELECT j.id, j.video_id, j.source_storage_key, j.status, j.attempts, j.max_attempts, j.error_message, j.created_at, j.updated_at,
              v.video_url, v.thumbnail_url, v.status as video_status, v.duration_seconds, v.width, v.height
       FROM video_processing_jobs j
       LEFT JOIN videos v ON j.video_id = v.id
       WHERE j.id = $1 OR j.video_id = $1`,
      [jobId]
    );
    if (res.rows.length === 0) {
      throw new NotFoundError('Processing job not found');
    }
    const row = res.rows[0];
    return {
      jobId: row.id,
      videoId: row.video_id,
      status: row.status,
      attempts: row.attempts,
      maxAttempts: row.max_attempts,
      errorMessage: row.error_message,
      video: {
        id: row.video_id,
        status: row.video_status,
        videoUrl: row.video_url,
        thumbnailUrl: row.thumbnail_url,
        durationSeconds: row.duration_seconds,
        width: row.width,
        height: row.height,
      },
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }

  async getVideoById(id: string | number, viewerId: string = 'anonymous') {
    const video = await videoRepository.findById(id, viewerId);
    if (!video) {
      throw new NotFoundError('Video not found');
    }
    return video;
  }

  async toggleLike(userId: string, videoId: string | number) {
    const stringId = String(videoId);
    const video = await videoRepository.findById(stringId);
    if (!video) {
      throw new NotFoundError('Video not found');
    }

    const res = await videoRepository.like(userId, stringId);

    // If liked, notify creator
    if (res.isLiked && video.authorId !== userId) {
      const notif = await notificationRepository.create({
        recipientId: video.authorId,
        actorId: userId,
        type: 'like',
        actionText: 'liked your video',
        targetVideoId: stringId,
      });

      if (notif) {
        eventBus.emitNotification(video.authorId, notif);
      }
    }

    return res;
  }

  async toggleSave(userId: string, videoId: string | number) {
    const stringId = String(videoId);
    const video = await videoRepository.findById(stringId);
    if (!video) {
      throw new NotFoundError('Video not found');
    }
    return videoRepository.save(userId, stringId);
  }

  async toggleRepost(userId: string, videoId: string | number) {
    const stringId = String(videoId);
    const video = await videoRepository.findById(stringId);
    if (!video) {
      throw new NotFoundError('Video not found');
    }

    const res = await videoRepository.repost(userId, stringId);

    if (res.isReposted && video.authorId !== userId) {
      const notif = await notificationRepository.create({
        recipientId: video.authorId,
        actorId: userId,
        type: 'repost',
        actionText: 'reposted your video',
        targetVideoId: stringId,
      });

      if (notif) {
        eventBus.emitNotification(video.authorId, notif);
      }
    }

    return res;
  }

  async recordView(data: {
    videoId: string | number;
    viewerId?: string | null;
    durationMs?: number;
    completionPercent?: number;
    ipAddress?: string | null;
  }) {
    return videoRepository.recordView({
      videoId: String(data.videoId),
      viewerId: data.viewerId,
      durationMs: data.durationMs,
      completionPercent: data.completionPercent,
      ipAddress: data.ipAddress,
    });
  }

  async shareVideo(videoId: string | number) {
    return videoRepository.incrementShare(String(videoId));
  }

  async deleteVideo(videoId: string | number, authorId: string) {
    const stringId = String(videoId);
    const existing = await videoRepository.findById(stringId, authorId);
    if (!existing) {
      throw new NotFoundError('Video not found');
    }
    if (existing.authorId !== authorId) {
      throw new ForbiddenError('Not authorized to delete this video');
    }
    const deleted = await videoRepository.deleteVideo(stringId, authorId);
    if (!deleted) {
      throw new ForbiddenError('Not authorized to delete this video');
    }

    // Clean up media files
    if (existing.sourceStorageKey) {
      await storageService.deleteFile(existing.sourceStorageKey).catch(() => {});
    }
    if (existing.videoUrl && existing.videoUrl.startsWith('/storage/')) {
      await storageService.deleteFile(existing.videoUrl).catch(() => {});
    }
    if (existing.thumbnailUrl && existing.thumbnailUrl.startsWith('/storage/')) {
      await storageService.deleteFile(existing.thumbnailUrl).catch(() => {});
    }

    return { success: true };
  }

  async getAuthorVideos(authorHandle: string, viewerId: string = 'anonymous') {
    return videoRepository.findByAuthor(authorHandle, viewerId);
  }

  async getLikedVideos(userId: string, viewerId: string = 'anonymous') {
    return videoRepository.findLikedByUser(userId, viewerId);
  }

  async getSavedVideos(userId: string) {
    return videoRepository.findSavedByUser(userId);
  }
}

export const videoService = new VideoService();
