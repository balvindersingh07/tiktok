import { Hono } from 'hono';
import { query } from '../db/index.js';
import { requireAuth } from '../middleware/auth.js';

export const draftsRouter = new Hono();

// GET /api/drafts
draftsRouter.get('/', requireAuth, async (c) => {
  const user = c.get('user');

  const res = await query(
    `SELECT
      id,
      caption,
      sound_title as "soundTitle",
      sound_author as "soundAuthor",
      cover_res_name as "coverResName",
      duration_seconds as "durationSeconds",
      created_at as "createdAt"
    FROM drafts
    WHERE user_id = $1
    ORDER BY created_at DESC`,
    [user.userId]
  );

  return c.json({
    success: true,
    drafts: res.rows.map((row) => ({
      id: row.id,
      caption: row.caption,
      soundTitle: row.soundTitle,
      soundAuthor: row.soundAuthor,
      coverResName: row.coverResName,
      durationSeconds: Number(row.durationSeconds),
      timestamp: new Date(row.createdAt).getTime(),
    })),
  });
});

// POST /api/drafts
draftsRouter.post('/', requireAuth, async (c) => {
  const user = c.get('user');
  const body = await c.req.json();

  const id = `draft_${Date.now()}`;
  await query(
    `INSERT INTO drafts (id, user_id, caption, sound_title, sound_author, cover_res_name, duration_seconds)
     VALUES ($1, $2, $3, $4, $5, $6, $7)`,
    [
      id,
      user.userId,
      body.caption || '',
      body.soundTitle || 'Original Audio',
      body.soundAuthor || user.handle,
      body.coverResName || '/assets/video_cover_dance.jpg',
      body.durationSeconds || 15,
    ]
  );

  return c.json({
    success: true,
    draftId: id,
  });
});

// DELETE /api/drafts/:id
draftsRouter.delete('/:id', requireAuth, async (c) => {
  const user = c.get('user');
  const draftId = c.req.param('id');

  await query('DELETE FROM drafts WHERE id = $1 AND user_id = $2', [draftId, user.userId]);

  return c.json({ success: true });
});
