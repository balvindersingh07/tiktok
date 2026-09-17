import { v4 as uuidv4 } from 'uuid';
import { query } from '../db/index.js';
import { soundRepository } from '../repositories/sound.repository.js';
import { NotFoundError } from '../utils/errors.js';

export class SoundService {
  async getSounds(category?: string, limit?: number, viewerId?: string) {
    return soundRepository.findAll({ category, limit, viewerId });
  }

  async getSoundById(id: string, viewerId?: string) {
    const sound = await soundRepository.findById(id, viewerId);
    if (!sound) {
      throw new NotFoundError('Sound not found');
    }
    return sound;
  }

  async toggleFavorite(userId: string, soundId: string) {
    return soundRepository.toggleFavorite(userId, soundId);
  }

  async createSound(creatorId: string, data: {
    title: string;
    author: string;
    durationSeconds?: number;
    category?: string;
    audioUrl?: string;
    coverUrl?: string;
  }) {
    const id = `snd_${uuidv4().replace(/-/g, '').slice(0, 12)}`;
    const res = await query(
      `INSERT INTO sounds (id, title, author, duration_seconds, audio_url, cover_url, category, is_original, creator_id)
       VALUES ($1, $2, $3, $4, $5, $6, $7, TRUE, $8)
       RETURNING *`,
      [
        id,
        data.title,
        data.author,
        data.durationSeconds || 15,
        data.audioUrl || `/cdn/sounds/${id}.mp3`,
        data.coverUrl || '',
        data.category || 'trending',
        creatorId,
      ]
    );
    return res.rows[0];
  }
}

export const soundService = new SoundService();
