import { Hono } from 'hono';
import { soundService } from '../services/sound.service.js';
import { optionalAuth, requireAuth } from '../middleware/auth.js';
import { createSoundSchema } from '../schemas/index.js';

export const soundsRouter = new Hono();

// GET /api/sounds
soundsRouter.get('/', optionalAuth, async (c) => {
  const category = c.req.query('category');
  const limit = Math.min(parseInt(c.req.query('limit') || '30', 10), 50);
  const currentUser = c.get('user');

  const sounds = await soundService.getSounds(category, limit, currentUser?.userId);
  return c.json({
    success: true,
    sounds,
  });
});

// GET /api/sounds/:id
soundsRouter.get('/:id', optionalAuth, async (c) => {
  const id = c.req.param('id');
  const currentUser = c.get('user');

  const sound = await soundService.getSoundById(id, currentUser?.userId);
  return c.json({
    success: true,
    sound,
  });
});

// POST /api/sounds/:id/favorite
soundsRouter.post('/:id/favorite', requireAuth, async (c) => {
  const user = c.get('user');
  const soundId = c.req.param('id');

  const favorited = await soundService.toggleFavorite(user.userId, soundId);
  return c.json({ success: true, favorited });
});

// POST /api/sounds - Upload/Create sound
soundsRouter.post('/', requireAuth, async (c) => {
  const user = c.get('user');
  const body = await c.req.json();
  const data = createSoundSchema.parse(body);

  const sound = await soundService.createSound(user.userId, data);
  return c.json({ success: true, sound }, 201);
});
