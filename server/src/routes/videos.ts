import { Hono } from 'hono';
import { v4 as uuidv4 } from 'uuid';
import { videoService } from '../services/video.service.js';
import { commentService } from '../services/comment.service.js';
import { storageService } from '../services/storage.service.js';
import { requireAuth, optionalAuth } from '../middleware/auth.js';
import { uploadLimiter } from '../middleware/rateLimit.js';
import { createVideoSchema, createCommentSchema } from '../schemas/index.js';
import { getClientIp } from '../utils/ip.js';
import { z } from 'zod';

export const videosRouter = new Hono();

const uploadSessionSchema = z.object({
  filename: z.string().min(1).max(255),
  contentType: z.string().min(1).max(128),
  sizeBytes: z.number().int().positive().max(100 * 1024 * 1024), // 100MB
});

const confirmUploadSchema = z.object({
  key: z.string().min(1),
  sessionId: z.string().optional(),
  sessionToken: z.string().optional(),
  caption: z.string().max(2000).optional().default(''),
  soundTitle: z.string().max(255).optional().default('Original Sound'),
  soundAuthor: z.string().max(255).optional(),
  soundId: z.string().optional().nullable(),
  category: z.string().max(64).optional().default('fyp'),
  hashtags: z.string().optional().default(''),
  isPrivate: z.boolean().optional().default(false),
  allowComments: z.boolean().optional().default(true),
  allowDuet: z.boolean().optional().default(true),
  allowStitch: z.boolean().optional().default(true),
  duetWithVideoId: z.string().optional().nullable(),
  stitchWithVideoId: z.string().optional().nullable(),
});

// GET /api/videos - Feed fallback / query
videosRouter.get('/', optionalAuth, async (c) => {
  const currentUser = c.get('user');
  const viewerId = currentUser?.userId || 'anonymous';
  const q = c.req.query();

  const { videos, nextCursor, hasMore } = await (await import('../services/feed.service.js')).feedService.getForYouFeed({
    viewerId,
    category: q.category,
    cursor: q.cursor,
    limit: q.limit ? parseInt(q.limit, 10) : 20,
  });

  return c.json({
    success: true,
    videos,
    nextCursor,
    hasMore,
    count: videos.length,
  });
});

// 1. POST /api/videos/upload-session - Step 1 of direct object upload
// Generates presigned upload URL and session token so Android / client uploads directly to object storage
videosRouter.post('/upload-session', requireAuth, uploadLimiter, async (c) => {
  const user = c.get('user');
  const body = await c.req.json();
  const data = uploadSessionSchema.parse(body);

  const session = await storageService.createUploadSession(
    user.userId,
    data.filename,
    data.contentType,
    data.sizeBytes
  );

  return c.json({
    success: true,
    sessionId: session.sessionId,
    uploadUrl: session.uploadUrl,
    method: session.method,
    headers: session.headers,
    key: session.key,
    sessionToken: session.sessionToken,
    publicUrl: session.publicUrl,
    expiresIn: session.expiresIn,
  });
});

// 2. POST /api/videos/confirm-upload - Step 2 after client finishes direct upload to storage
// Validates uploaded media file, registers video in 'PROCESSING', and enqueues FFmpeg job
videosRouter.post('/confirm-upload', requireAuth, async (c) => {
  const user = c.get('user');
  const body = await c.req.json();
  const data = confirmUploadSchema.parse(body);

  // Verify file exists in storage
  const exists = await storageService.exists(data.key);
  if (!exists) {
    return c.json({
      success: false,
      error: 'Uploaded media file not found in storage. Please complete the direct upload first.',
    }, 400);
  }

  const fileSize = await storageService.getFileSize(data.key);
  if (fileSize <= 0) {
    return c.json({
      success: false,
      error: 'Uploaded file is empty (0 bytes)',
    }, 400);
  }

  if (data.sessionId) {
    await storageService.updateUploadSessionStatus(data.sessionId, 'CONFIRMED', fileSize);
  }

  const video = await videoService.publishVideo(user.userId, {
    caption: data.caption,
    soundTitle: data.soundTitle,
    soundAuthor: data.soundAuthor || user.handle,
    soundId: data.soundId,
    sourceStorageKey: data.key,
    videoPath: storageService.getFileUrl(data.key),
    category: data.category,
    hashtags: data.hashtags,
    isPrivate: data.isPrivate,
    allowComments: data.allowComments,
    allowDuet: data.allowDuet,
    allowStitch: data.allowStitch,
    duetWithVideoId: data.duetWithVideoId,
    stitchWithVideoId: data.stitchWithVideoId,
    status: 'PROCESSING',
  });

  return c.json({
    success: true,
    videoId: video.id,
    jobId: video.jobId,
    status: 'PROCESSING',
    message: 'Video upload confirmed. Enqueued for FFmpeg processing and transcoding.',
    video,
  }, 201);
});

// 3. GET /api/videos/processing-status/:jobId - Poll video transcoding status
videosRouter.get('/processing-status/:jobId', optionalAuth, async (c) => {
  const jobId = c.req.param('jobId');
  const status = await videoService.getProcessingJobStatus(jobId);
  return c.json({
    success: true,
    ...status,
  });
});

// GET /api/videos/:id - Single video
videosRouter.get('/:id', optionalAuth, async (c) => {
  const id = c.req.param('id');
  const currentUser = c.get('user');
  const viewerId = currentUser?.userId || 'anonymous';

  const video = await videoService.getVideoById(id, viewerId);
  return c.json({
    success: true,
    video,
  });
});

// POST /api/videos - Publish metadata only
videosRouter.post('/', requireAuth, async (c) => {
  const user = c.get('user');
  const body = await c.req.json();
  const data = createVideoSchema.parse(body);

  const video = await videoService.publishVideo(user.userId, {
    caption: data.caption,
    soundTitle: data.soundTitle,
    soundAuthor: data.soundAuthor,
    soundId: data.soundId,
    coverResName: data.coverResName,
    videoPath: data.videoPath,
    category: data.category,
    hashtags: data.hashtags,
    durationSeconds: data.durationSeconds,
    isPrivate: data.isPrivate,
    allowComments: data.allowComments,
    allowDuet: data.allowDuet,
    allowStitch: data.allowStitch,
    duetWithVideoId: data.duetWithVideoId,
    stitchWithVideoId: data.stitchWithVideoId,
  });

  return c.json(
    {
      success: true,
      videoId: isNaN(Number(video.id)) ? video.id : Number(video.id),
      video,
      message: 'Video published successfully',
    },
    201
  );
});

// POST /api/videos/upload - Multipart fallback upload with integrated FFmpeg queue
videosRouter.post('/upload', requireAuth, uploadLimiter, async (c) => {
  const user = c.get('user');
  const body = await c.req.parseBody();

  const caption = (body['caption'] as string) || '';
  const soundTitle = (body['soundTitle'] as string) || 'Original Audio';
  const soundAuthor = (body['soundAuthor'] as string) || user.handle;
  const category = (body['category'] as string) || 'fyp';
  const hashtags = (body['hashtags'] as string) || '';
  const soundId = (body['soundId'] as string) || null;
  const file = body['file'];

  let storageKey = '';
  let videoUrl = '';

  if (file && typeof file === 'object' && 'arrayBuffer' in file) {
    const origName = (file as any).name || 'video.mp4';
    const ext = origName.split('.').pop() || 'mp4';
    const mimeType = (file as any).type || 'video/mp4';

    const validation = storageService.validateVideoUpload(mimeType, (file as any).size || 1000);
    if (!validation.valid) {
      return c.json({ success: false, error: validation.error }, 400);
    }

    const filename = `raw_videos/usr_${user.userId.slice(0, 16)}_${Date.now()}_${uuidv4().slice(0, 8)}.${ext}`;
    const buffer = Buffer.from(await (file as any).arrayBuffer());

    const uploadRes = await storageService.uploadFile(filename, buffer, mimeType);
    storageKey = uploadRes.key;
    videoUrl = uploadRes.url;
  } else {
    // Default sample if no file passed
    storageKey = 'videos/vid_001.mp4';
    videoUrl = storageService.getFileUrl(storageKey);
  }

  const video = await videoService.publishVideo(user.userId, {
    caption,
    soundTitle,
    soundAuthor,
    soundId,
    videoPath: videoUrl,
    sourceStorageKey: storageKey,
    coverResName: 'video_cover_dance',
    category,
    hashtags,
    status: 'PROCESSING',
  });

  return c.json({
    success: true,
    videoId: video.id,
    jobId: video.jobId,
    videoUrl,
    status: 'PROCESSING',
    video,
  });
});

// POST /api/videos/:id/like
videosRouter.post('/:id/like', requireAuth, async (c) => {
  const user = c.get('user');
  const videoId = c.req.param('id');
  const result = await videoService.toggleLike(user.userId, videoId);
  return c.json({
    success: true,
    liked: result.isLiked,
    isLiked: result.isLiked,
    likesCount: result.likesCount,
  });
});

// POST /api/videos/:id/save (Bookmark)
videosRouter.post('/:id/save', requireAuth, async (c) => {
  const user = c.get('user');
  const videoId = c.req.param('id');
  const result = await videoService.toggleSave(user.userId, videoId);
  return c.json({
    success: true,
    saved: result.isSaved,
    isSaved: result.isSaved,
    savesCount: result.savesCount,
  });
});

// POST /api/videos/:id/repost
videosRouter.post('/:id/repost', requireAuth, async (c) => {
  const user = c.get('user');
  const videoId = c.req.param('id');
  const result = await videoService.toggleRepost(user.userId, videoId);
  return c.json({
    success: true,
    reposted: result.isReposted,
    isReposted: result.isReposted,
    repostsCount: result.repostsCount,
  });
});

// POST /api/videos/:id/view - Real engagement recording with sanitized IP
videosRouter.post('/:id/view', optionalAuth, async (c) => {
  const videoId = c.req.param('id');
  const currentUser = c.get('user');
  const body = await c.req.json().catch(() => ({}));
  const durationMs = Number(body.durationMs || 15000);
  const completionPercent = Number(body.completionPercent || 1.0);
  const ip = getClientIp(c);

  await videoService.recordView({
    videoId,
    viewerId: currentUser?.userId || null,
    durationMs,
    completionPercent,
    ipAddress: ip,
  });

  return c.json({ success: true });
});

// POST /api/videos/:id/share
videosRouter.post('/:id/share', async (c) => {
  const videoId = c.req.param('id');
  const sharesCount = await videoService.shareVideo(videoId);
  return c.json({ success: true, sharesCount });
});

// GET /api/videos/:id/comments
videosRouter.get('/:id/comments', optionalAuth, async (c) => {
  const videoId = c.req.param('id');
  const currentUser = c.get('user');
  const viewerId = currentUser?.userId || 'anonymous';

  const comments = await commentService.getComments(videoId, viewerId);
  return c.json({
    success: true,
    comments,
    count: comments.length,
  });
});

// POST /api/videos/:id/comments
videosRouter.post('/:id/comments', requireAuth, async (c) => {
  const user = c.get('user');
  const videoId = c.req.param('id');
  const body = await c.req.json();
  const data = createCommentSchema.parse(body);

  const comment = await commentService.addComment(
    user.userId,
    videoId,
    data.content,
    data.parentCommentId
  );

  return c.json({
    success: true,
    commentId: comment.id,
    comment,
    message: 'Comment added successfully',
  });
});

// POST /api/videos/comments/:id/like
videosRouter.post('/comments/:id/like', requireAuth, async (c) => {
  const user = c.get('user');
  const commentId = c.req.param('id');
  const result = await commentService.toggleLike(user.userId, commentId);

  return c.json({
    success: true,
    liked: result.isLiked,
    isLiked: result.isLiked,
    likesCount: result.likesCount,
  });
});

// DELETE /api/videos/comments/:id
videosRouter.delete('/comments/:id', requireAuth, async (c) => {
  const user = c.get('user');
  const commentId = c.req.param('id');
  await commentService.deleteComment(user.userId, commentId);

  return c.json({
    success: true,
    message: 'Comment deleted',
  });
});

// DELETE /api/videos/:id - Deletes video and cleans up remote storage media
videosRouter.delete('/:id', requireAuth, async (c) => {
  const user = c.get('user');
  const videoId = c.req.param('id');
  await videoService.deleteVideo(videoId, user.userId);

  return c.json({
    success: true,
    message: 'Video and remote media deleted successfully',
  });
});
