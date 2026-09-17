import { query } from '../db/index.js';
import { v4 as uuidv4 } from 'uuid';

export class NotificationRepository {
  async create(data: {
    recipientId: string;
    actorId: string;
    type: 'like' | 'comment' | 'reply' | 'follow' | 'repost' | 'mention' | 'dm' | 'system';
    actionText: string;
    targetVideoId?: string | null;
    targetCommentId?: string | null;
  }): Promise<any | null> {
    // Check recipient notification preferences first
    const prefRes = await query('SELECT * FROM notification_preferences WHERE user_id = $1', [
      data.recipientId,
    ]);

    if (prefRes.rows.length > 0) {
      const prefs = prefRes.rows[0];
      if (data.type === 'like' && !prefs.likes_enabled) return null;
      if ((data.type === 'comment' || data.type === 'reply') && !prefs.comments_enabled) return null;
      if (data.type === 'follow' && !prefs.follows_enabled) return null;
      if (data.type === 'repost' && !prefs.reposts_enabled) return null;
      if (data.type === 'mention' && !prefs.mentions_enabled) return null;
      if (data.type === 'dm' && !prefs.dms_enabled) return null;
    }

    const notifId = `notif_${uuidv4().replace(/-/g, '').slice(0, 16)}`;
    const res = await query(
      `INSERT INTO notifications (
        id, recipient_id, actor_id, type, action_text, target_video_id, target_comment_id
      ) VALUES ($1, $2, $3, $4, $5, $6, $7)
      RETURNING *`,
      [
        notifId,
        data.recipientId,
        data.actorId,
        data.type,
        data.actionText,
        data.targetVideoId || null,
        data.targetCommentId || null,
      ]
    );

    return res.rows[0];
  }

  async findByRecipient(recipientId: string, limit: number = 50): Promise<any[]> {
    const res = await query(
      `SELECT
        n.id,
        n.type,
        n.action_text as "actionText",
        n.is_read as "isRead",
        n.created_at as "createdAt",
        p.display_name as "actorName",
        p.handle as "actorHandle",
        p.avatar_url as "actorAvatarUrl",
        v.cover_res_name as "videoCover",
        v.caption as "videoCaption",
        n.target_video_id as "targetVideoId"
      FROM notifications n
      LEFT JOIN profiles p ON n.actor_id = p.user_id
      LEFT JOIN videos v ON n.target_video_id = v.id
      WHERE n.recipient_id = $1
      ORDER BY n.created_at DESC
      LIMIT $2`,
      [recipientId, limit]
    );

    return res.rows.map((row) => ({
      id: row.id,
      type: row.type,
      actionText: row.actionText,
      isRead: Boolean(row.isRead),
      timestamp: new Date(row.createdAt).getTime(),
      actorName: row.actorName || 'Tashan User',
      actorHandle: row.actorHandle || '@user',
      actorAvatarUrl: row.actorAvatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80',
      videoCover: row.videoCover,
      videoCaption: row.videoCaption,
      targetVideoId: row.targetVideoId,
    }));
  }

  async getUnreadCount(userId: string): Promise<number> {
    const res = await query(
      'SELECT COUNT(*) as count FROM notifications WHERE recipient_id = $1 AND is_read = FALSE',
      [userId]
    );
    return parseInt(res.rows[0]?.count || '0', 10);
  }

  async markAsRead(notifId: string, recipientId: string): Promise<void> {
    await query('UPDATE notifications SET is_read = TRUE WHERE id = $1 AND recipient_id = $2', [
      notifId,
      recipientId,
    ]);
  }

  async markAllAsRead(recipientId: string): Promise<void> {
    await query('UPDATE notifications SET is_read = TRUE WHERE recipient_id = $1', [recipientId]);
  }

  async getPreferences(userId: string): Promise<any> {
    const res = await query('SELECT * FROM notification_preferences WHERE user_id = $1', [userId]);
    if (res.rows.length === 0) {
      return {
        likesEnabled: true,
        commentsEnabled: true,
        followsEnabled: true,
        mentionsEnabled: true,
        dmsEnabled: true,
        repostsEnabled: true,
        systemEnabled: true,
      };
    }
    const r = res.rows[0];
    return {
      likesEnabled: r.likes_enabled,
      commentsEnabled: r.comments_enabled,
      followsEnabled: r.follows_enabled,
      mentionsEnabled: r.mentions_enabled,
      dmsEnabled: r.dms_enabled,
      repostsEnabled: r.reposts_enabled,
      systemEnabled: r.system_enabled,
    };
  }

  async updatePreferences(userId: string, prefs: Record<string, boolean>): Promise<any> {
    const updates: string[] = [];
    const params: any[] = [userId];

    const mapping: Record<string, string> = {
      likesEnabled: 'likes_enabled',
      commentsEnabled: 'comments_enabled',
      followsEnabled: 'follows_enabled',
      mentionsEnabled: 'mentions_enabled',
      dmsEnabled: 'dms_enabled',
      repostsEnabled: 'reposts_enabled',
      systemEnabled: 'system_enabled',
    };

    for (const [key, col] of Object.entries(mapping)) {
      if (prefs[key] !== undefined) {
        params.push(Boolean(prefs[key]));
        updates.push(`${col} = $${params.length}`);
      }
    }

    if (updates.length > 0) {
      updates.push('updated_at = NOW()');
      await query(
        `UPDATE notification_preferences SET ${updates.join(', ')} WHERE user_id = $1`,
        params
      );
    }

    return this.getPreferences(userId);
  }
}

export const notificationRepository = new NotificationRepository();
