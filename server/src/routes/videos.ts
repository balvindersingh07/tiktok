import { Hono } from 'hono';
import { v4 as uuidv4 } from 'uuid';
import { createVideoSchema, videoQuerySchema, createCommentSchema } from '../schemas/index.js';
import { videoService } from '../services/video.service.js';
import { commentService } from '../services/comment.service.js';
import { storageService } from '../services/storage.service.js';
import { requireAuth, optionalAuth } from '../middleware/auth.js';
import { rateLimit } from '../middleware/rateLimit.js';
import { getClientIp } from '../utils/ip.js';

export const videosRouter = new Hono();

const uploadLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 10,
  message: 'Upload limit reached, please wait a minute.',
});

// GET /api/videos - Query videos
videosRouter.get('/', optionalAuth, async (c) => {
  const q = videoQuerySchema.parse(c.req.query());
  const currentUser = c.get('user');
  const viewerId = currentUser?.userId || 'anonymous';

  if (q.authorId) {
    const videos = await videoService.getAuthorVideos(q.authorId, viewerId);
    return c.json({ success: true, videos, count: videos.length });
  }

  const result = await videoService.getAuthorVideos(viewerId, viewerId).catch(() => []);
  const feed = await videoService.getVideoById(1, viewerId).catch(() => null);

  // Default query to feed
  const { videos, nextCursor, hasMore } = await (await import('../services/feed.service.js')).feedService.getForYouFeed({
    viewerId,
    category: q.category,
    cursor: q.cursor,
    limit: q.limit,
  });

  return c.json({
    success: true,
    videos,
    nextCursor,
    hasMore,
    count: videos.length,
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

// POST /api/videos - Publish metadata
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

// POST /api/videos/upload - Video media file upload & storage integration
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

  let videoUrl = '/assets/sample_clip_dance.mp4';
  let storageKey = '';

  if (file && typeof file === 'object' && 'arrayBuffer' in file) {
    const origName = (file as any).name || 'video.mp4';
    const ext = origName.split('.').pop() || 'mp4';
    const filename = `videos/${Date.now()}_${uuidv4().slice(0, 8)}.${ext}`;
    const buffer = Buffer.from(await (file as any).arrayBuffer());
    const mimeType = (file as any).type || 'video/mp4';

    const uploadRes = await storageService.uploadFile(filename, buffer, mimeType);
    videoUrl = uploadRes.url;
    storageKey = uploadRes.key;
  }

  const video = await videoService.publishVideo(user.userId, {
    caption,
    soundTitle,
    soundAuthor,
    soundId,
    videoPath: videoUrl,
    sourceStorageKey: storageKey || videoUrl,
    coverResName: 'video_cover_dance',
    category,
    hashtags,
    durationSeconds: 15,
  });

  return c.json({
    success: true,
    videoId: isNaN(Number(video.id)) ? video.id : Number(video.id),
    videoUrl,
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

// POST /api/videos/:id/view - Real engagement recording
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

// DELETE /api/videos/:id
videosRouter.delete('/:id', requireAuth, async (c) => {
  const user = c.get('user');
  const videoId = c.req.param('id');
  await videoService.deleteVideo(videoId, user.userId);

  return c.json({
    success: true,
    message: 'Video deleted',
  });
});
