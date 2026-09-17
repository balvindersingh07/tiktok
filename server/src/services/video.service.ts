import { v4 as uuidv4 } from 'uuid';
import { videoRepository } from '../repositories/video.repository.js';
import { notificationRepository } from '../repositories/notification.repository.js';
import { soundRepository } from '../repositories/sound.repository.js';
import { eventBus } from '../utils/eventBus.js';
import { NotFoundError, ForbiddenError } from '../utils/errors.js';

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
  }) {
    const videoId = `vid_${uuidv4().replace(/-/g, '').slice(0, 16)}`;

    // Default assets if none provided
    const videoUrl = data.videoPath || '/assets/sample_clip_dance.mp4';
    const coverRes = data.coverResName || 'video_cover_dance';

    const video = await videoRepository.create({
      id: videoId,
      authorId,
      caption: data.caption || '',
      soundId: data.soundId || null,
      soundTitle: data.soundTitle || 'Original Audio',
      soundAuthor: data.soundAuthor || 'Creator',
      sourceStorageKey: data.sourceStorageKey || videoUrl,
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
      status: 'READY',
    });

    // If a sound is attached, increment its usage
    if (data.soundId) {
      await soundRepository.incrementUsage(data.soundId, authorId, videoId).catch(() => {});
    }

    // Trigger asynchronous transcoding/thumbnail queue
    const jobId = `job_${uuidv4().replace(/-/g, '').slice(0, 16)}`;
    await videoRepository.createProcessingJob({
      id: jobId,
      videoId,
      sourceStorageKey: videoUrl,
    });

    return video;
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
    const deleted = await videoRepository.deleteVideo(String(videoId), authorId);
    if (!deleted) {
      throw new ForbiddenError('Not authorized to delete this video');
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
