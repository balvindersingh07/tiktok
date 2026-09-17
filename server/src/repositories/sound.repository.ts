import { query } from '../db/index.js';

export class SoundRepository {
  async findAll(options: { category?: string; limit?: number; viewerId?: string }): Promise<any[]> {
    const limit = Math.min(options.limit || 30, 50);
    const viewerId = options.viewerId || 'anonymous';

    let sql = `
      SELECT
        s.id,
        s.title,
        s.author,
        s.duration_seconds as "durationSeconds",
        s.audio_url as "audioUrl",
        s.cover_url as "coverUrl",
        s.category,
        s.usage_count as "usageCount",
        s.is_original as "isOriginal",
        CASE WHEN sf.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isFavorited"
      FROM sounds s
      LEFT JOIN sound_favorites sf ON s.id = sf.sound_id AND sf.user_id = $1
    `;
    const params: any[] = [viewerId];

    if (options.category && options.category !== 'trending') {
      params.push(options.category);
      sql += ` WHERE s.category = $${params.length}`;
    }

    sql += ` ORDER BY s.usage_count DESC LIMIT $${params.length + 1}`;
    params.push(limit);

    const res = await query(sql, params);
    return res.rows.map((row) => ({
      ...row,
      durationSeconds: Number(row.durationSeconds || 15),
      usageCount: Number(row.usageCount || 0),
      isOriginal: Boolean(row.isOriginal),
      isFavorited: Boolean(row.isFavorited),
    }));
  }

  async findById(id: string, viewerId: string = 'anonymous'): Promise<any | null> {
    const res = await query(
      `SELECT
        s.id,
        s.title,
        s.author,
        s.duration_seconds as "durationSeconds",
        s.audio_url as "audioUrl",
        s.cover_url as "coverUrl",
        s.category,
        s.usage_count as "usageCount",
        s.is_original as "isOriginal",
        CASE WHEN sf.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isFavorited"
      FROM sounds s
      LEFT JOIN sound_favorites sf ON s.id = sf.sound_id AND sf.user_id = $2
      WHERE s.id = $1`,
      [id, viewerId]
    );

    if (res.rows.length === 0) return null;

    const row = res.rows[0];
    return {
      ...row,
      durationSeconds: Number(row.durationSeconds || 15),
      usageCount: Number(row.usageCount || 0),
      isOriginal: Boolean(row.isOriginal),
      isFavorited: Boolean(row.isFavorited),
    };
  }

  async toggleFavorite(userId: string, soundId: string): Promise<boolean> {
    const existing = await query(
      'SELECT sound_id FROM sound_favorites WHERE user_id = $1 AND sound_id = $2',
      [userId, soundId]
    );

    if (existing.rows.length > 0) {
      await query('DELETE FROM sound_favorites WHERE user_id = $1 AND sound_id = $2', [
        userId,
        soundId,
      ]);
      return false;
    } else {
      await query('INSERT INTO sound_favorites (user_id, sound_id) VALUES ($1, $2)', [
        userId,
        soundId,
      ]);
      return true;
    }
  }

  async incrementUsage(soundId: string, userId: string, videoId: string): Promise<void> {
    await query('UPDATE sounds SET usage_count = usage_count + 1 WHERE id = $1', [soundId]);
    const usageId = `su_${Date.now()}`;
    await query(
      'INSERT INTO sound_usage (id, sound_id, user_id, video_id) VALUES ($1, $2, $3, $4)',
      [usageId, soundId, userId, videoId]
    );
  }
}

export const soundRepository = new SoundRepository();
