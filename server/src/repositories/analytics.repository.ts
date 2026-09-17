import { query } from '../db/index.js';
import { v4 as uuidv4 } from 'uuid';

export class AnalyticsRepository {
  async recordEvent(data: {
    userId?: string | null;
    eventType: string;
    targetId?: string | null;
    targetType?: string;
    categoryTag?: string;
    watchDurationMs?: number;
    completionPercent?: number;
    metadata?: Record<string, any>;
    ipAddress?: string | null;
    userAgent?: string | null;
  }): Promise<void> {
    const eventId = `ev_${uuidv4().replace(/-/g, '').slice(0, 16)}`;
    const sanitizedIp = data.ipAddress
      ? data.ipAddress.split(',')[0].trim().slice(0, 100)
      : null;

    await query(
      `INSERT INTO analytics_events (
        id, user_id, event_type, target_id, target_type,
        category_tag, watch_duration_ms, completion_percent,
        metadata, ip_address, user_agent
      ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)`,
      [
        eventId,
        data.userId || null,
        data.eventType,
        data.targetId ? String(data.targetId).slice(0, 64) : null,
        data.targetType || 'video',
        data.categoryTag || '',
        data.watchDurationMs || 0,
        data.completionPercent || 0,
        JSON.stringify(data.metadata || {}),
        sanitizedIp,
        data.userAgent ? data.userAgent.slice(0, 500) : null,
      ]
    ).catch(() => {});
  }

  async getCreatorMetrics(userId: string): Promise<any> {
    const videoStats = await query(
      `SELECT
        COUNT(*) as "totalVideos",
        COALESCE(SUM(views_count), 0) as "totalViews",
        COALESCE(SUM(likes_count), 0) as "totalLikes",
        COALESCE(SUM(comments_count), 0) as "totalComments",
        COALESCE(SUM(shares_count), 0) as "totalShares"
       FROM videos
       WHERE author_id = $1`,
      [userId]
    );

    const profileStats = await query(
      'SELECT followers_count as "followersCount", following_count as "followingCount" FROM profiles WHERE user_id = $1',
      [userId]
    );

    const recentEvents = await query(
      `SELECT event_type, COUNT(*) as count
       FROM analytics_events
       WHERE target_id IN (SELECT id FROM videos WHERE author_id = $1)
       GROUP BY event_type`,
      [userId]
    );

    return {
      overview: {
        totalVideos: parseInt(videoStats.rows[0]?.totalVideos || '0', 10),
        totalViews: parseInt(videoStats.rows[0]?.totalViews || '0', 10),
        totalLikes: parseInt(videoStats.rows[0]?.totalLikes || '0', 10),
        totalComments: parseInt(videoStats.rows[0]?.totalComments || '0', 10),
        totalShares: parseInt(videoStats.rows[0]?.totalShares || '0', 10),
        followersCount: parseInt(profileStats.rows[0]?.followersCount || '0', 10),
        followingCount: parseInt(profileStats.rows[0]?.followingCount || '0', 10),
      },
      eventsSummary: recentEvents.rows,
    };
  }
}

export const analyticsRepository = new AnalyticsRepository();
