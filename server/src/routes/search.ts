import { Hono } from 'hono';
import { searchService } from '../services/search.service.js';
import { optionalAuth } from '../middleware/auth.js';

export const searchRouter = new Hono();

// GET /api/search?q=...
searchRouter.get('/', optionalAuth, async (c) => {
  const q = (c.req.query('q') || '').trim();
  const currentUser = c.get('user');

  const result = await searchService.search(q, currentUser?.userId);
  return c.json({
    success: true,
    videos: result.videos,
    users: result.users,
    hashtags: result.hashtags,
    sounds: result.sounds,
  });
});

// GET /api/search/trending
searchRouter.get('/trending', async (c) => {
  const trending = await searchService.getTrending();
  const curatedHashtags = [
    { tag: '#shuffle', count: '4.8M videos', label: 'Trending in Dance' },
    { tag: '#foodietok', count: '12.4M videos', label: 'Viral Recipes' },
    { tag: '#creativecoding', count: '890K videos', label: 'Tech & Art' },
    { tag: '#balletroutine', count: '1.2M videos', label: 'Lifestyle' },
    { tag: '#tashanvibes', count: '28.1M videos', label: 'Official Tashan Community' },
  ];

  return c.json({
    success: true,
    hashtags: curatedHashtags,
    trendingHashtags: trending.hashtags,
    topSounds: trending.sounds,
  });
});
