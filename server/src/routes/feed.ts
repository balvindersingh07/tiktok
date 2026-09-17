import { Hono } from 'hono';
import { feedService } from '../services/feed.service.js';
import { optionalAuth } from '../middleware/auth.js';

export const feedRouter = new Hono();

// GET /api/feed/for-you
feedRouter.get('/for-you', optionalAuth, async (c) => {
  const currentUser = c.get('user');
  const cursor = c.req.query('cursor');
  const limit = Math.min(parseInt(c.req.query('limit') || '10', 10), 30);
  const category = c.req.query('category');
  const viewerId = currentUser?.userId || 'anonymous';

  const result = await feedService.getForYouFeed({
    viewerId,
    category,
    cursor,
    limit,
  });

  return c.json({
    success: true,
    videos: result.videos,
    nextCursor: result.nextCursor,
    hasMore: result.hasMore,
  });
});

// GET /api/feed/following
feedRouter.get('/following', optionalAuth, async (c) => {
  const currentUser = c.get('user');
  const cursor = c.req.query('cursor');
  const limit = Math.min(parseInt(c.req.query('limit') || '10', 10), 30);
  const viewerId = currentUser?.userId || 'anonymous';

  const result = await feedService.getFollowingFeed({
    viewerId,
    cursor,
    limit,
  });

  return c.json({
    success: true,
    videos: result.videos,
    nextCursor: result.nextCursor,
    hasMore: result.hasMore,
    isSuggested: result.isSuggested,
  });
});
