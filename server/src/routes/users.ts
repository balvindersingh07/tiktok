import { Hono } from 'hono';
import { userService } from '../services/user.service.js';
import { videoService } from '../services/video.service.js';
import { requireAuth, optionalAuth } from '../middleware/auth.js';
import { updateProfileSchema, registerDeviceTokenSchema } from '../schemas/index.js';

export const usersRouter = new Hono();

// GET /api/users - List creators
usersRouter.get('/', optionalAuth, async (c) => {
  const currentUser = c.get('user');
  const viewerId = currentUser?.userId || 'anonymous';
  const limit = Math.min(parseInt(c.req.query('limit') || '30', 10), 50);

  const users = await userService.listCreators(limit, viewerId);
  return c.json({
    success: true,
    users,
  });
});

// GET /api/users/blocked - Get blocked users
usersRouter.get('/blocked', requireAuth, async (c) => {
  const user = c.get('user');
  const blocked = await userService.getBlockedUsers(user.userId);
  return c.json({ success: true, blocked });
});

// GET /api/users/muted - Get muted users
usersRouter.get('/muted', requireAuth, async (c) => {
  const user = c.get('user');
  const muted = await userService.getMutedUsers(user.userId);
  return c.json({ success: true, muted });
});

// POST /api/users/device-token - Register FCM/device token
usersRouter.post('/device-token', requireAuth, async (c) => {
  const user = c.get('user');
  const body = await c.req.json();
  const data = registerDeviceTokenSchema.parse(body);

  await userService.registerDeviceToken(user.userId, data.token, data.platform);
  return c.json({ success: true, message: 'Device token registered' });
});

// DELETE /api/users/device-token
usersRouter.delete('/device-token', requireAuth, async (c) => {
  const user = c.get('user');
  const body = await c.req.json();
  if (body.token) {
    await userService.removeDeviceToken(user.userId, body.token);
  }
  return c.json({ success: true });
});

// GET /api/users/:handle - Profile details
usersRouter.get('/:handle', optionalAuth, async (c) => {
  const handle = c.req.param('handle');
  const currentUser = c.get('user');
  const viewerId = currentUser?.userId || 'anonymous';

  const user = await userService.getProfile(handle, viewerId);
  return c.json({
    success: true,
    user,
  });
});

// PUT /api/users/profile - Update own profile
usersRouter.put('/profile', requireAuth, async (c) => {
  const user = c.get('user');
  const body = await c.req.json();
  const data = updateProfileSchema.parse(body);

  const profile = await userService.updateProfile(user.userId, data);
  return c.json({
    success: true,
    profile,
  });
});

// POST /api/users/:handle/follow - Toggle follow
usersRouter.post('/:handle/follow', requireAuth, async (c) => {
  const currentUser = c.get('user');
  const handle = c.req.param('handle');

  const result = await userService.toggleFollow(currentUser.userId, handle);
  return c.json({
    success: true,
    isFollowing: result.isFollowing,
    followersCount: result.followersCount,
  });
});

// POST /api/users/:handle/block - Toggle block
usersRouter.post('/:handle/block', requireAuth, async (c) => {
  const currentUser = c.get('user');
  const handle = c.req.param('handle');

  const result = await userService.toggleBlock(currentUser.userId, handle);
  return c.json({ success: true, isBlocked: result.isBlocked });
});

// POST /api/users/:handle/mute - Toggle mute
usersRouter.post('/:handle/mute', requireAuth, async (c) => {
  const currentUser = c.get('user');
  const handle = c.req.param('handle');

  const result = await userService.toggleMute(currentUser.userId, handle);
  return c.json({ success: true, isMuted: result.isMuted });
});

// GET /api/users/:handle/videos - Get videos uploaded by user
usersRouter.get('/:handle/videos', optionalAuth, async (c) => {
  const handle = c.req.param('handle');
  const currentUser = c.get('user');
  const viewerId = currentUser?.userId || 'anonymous';

  const videos = await videoService.getAuthorVideos(handle, viewerId);
  return c.json({
    success: true,
    videos,
  });
});

// GET /api/users/:handle/followers
usersRouter.get('/:handle/followers', optionalAuth, async (c) => {
  const handle = c.req.param('handle');
  const target = await userService.getProfile(handle);
  const followers = await userService.getFollowers(target.userId);
  return c.json({ success: true, followers });
});

// GET /api/users/:handle/following
usersRouter.get('/:handle/following', optionalAuth, async (c) => {
  const handle = c.req.param('handle');
  const target = await userService.getProfile(handle);
  const following = await userService.getFollowing(target.userId);
  return c.json({ success: true, following });
});
