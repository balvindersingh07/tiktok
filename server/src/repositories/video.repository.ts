import { query } from '../db/index.js';
import { v4 as uuidv4 } from 'uuid';

export interface VideoRecord {
  id: string;
  authorId: string;
  caption: string;
  soundId?: string | null;
  soundTitle: string;
  soundAuthor: string;
  sourceStorageKey?: string | null;
  videoUrl: string;
  thumbnailUrl?: string | null;
  coverResName: string;
  width: number;
  height: number;
  durationSeconds: number;
  aspectRatio: string;
  status: string;
  visibility: string;
  isPrivate: boolean;
  allowComments: boolean;
  allowDuet: boolean;
  allowStitch: boolean;
  duetWithVideoId?: string | null;
  stitchWithVideoId?: string | null;
  category: string;
  hashtags: string;
  viewsCount: number;
  likesCount: number;
  commentsCount: number;
  sharesCount: number;
  savesCount: number;
  repostsCount: number;
  moderationStatus: string;
  createdAt: Date;
  updatedAt: Date;
}

export class VideoRepository {
  async create(data: {
    id: string;
    authorId: string;
    caption?: string;
    soundId?: string | null;
    soundTitle?: string;
    soundAuthor?: string;
    sourceStorageKey?: string | null;
    videoUrl?: string;
    thumbnailUrl?: string | null;
    coverResName?: string;
    durationSeconds?: number;
    category?: string;
    hashtags?: string;
    isPrivate?: boolean;
    allowComments?: boolean;
    allowDuet?: boolean;
    allowStitch?: boolean;
    duetWithVideoId?: string | null;
    stitchWithVideoId?: string | null;
    status?: string;
  }): Promise<any> {
    const res = await query(
      `INSERT INTO videos (
        id, author_id, caption, sound_id, sound_title, sound_author,
        source_storage_key, video_url, thumbnail_url, cover_res_name,
        duration_seconds, category, hashtags, is_private, allow_comments,
        allow_duet, allow_stitch, duet_with_video_id, stitch_with_video_id,
        status, visibility
      ) VALUES (
        $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17, $18, $19, $20, $21
      ) RETURNING *`,
      [
        data.id,
        data.authorId,
        data.caption || '',
        data.soundId || null,
        data.soundTitle || 'Original Audio',
        data.soundAuthor || 'Tashan Creator',
        data.sourceStorageKey || null,
        data.videoUrl || '',
        data.thumbnailUrl || '',
        data.coverResName || 'video_cover_dance',
        data.durationSeconds || 15,
        data.category || 'fyp',
        data.hashtags || '',
        data.isPrivate || false,
        data.allowComments ?? true,
        data.allowDuet ?? true,
        data.allowStitch ?? true,
        data.duetWithVideoId || null,
        data.stitchWithVideoId || null,
        data.status || 'READY',
        data.isPrivate ? 'PRIVATE' : 'PUBLIC',
      ]
    );

    // Update creator videos_count
    await query('UPDATE profiles SET videos_count = videos_count + 1 WHERE user_id = $1', [
      data.authorId,
    ]);

    // Process hashtags
    if (data.hashtags) {
      await this.linkHashtags(data.id, data.hashtags);
    }

    return res.rows[0];
  }

  async findById(id: string | number, viewerId: string = 'anonymous'): Promise<any | null> {
    const stringId = String(id);
    const res = await query(
      `SELECT
        v.id,
        v.author_id as "authorId",
        p.display_name as "authorName",
        p.handle as "authorHandle",
        p.avatar_url as "authorAvatarUrl",
        p.is_verified as "authorVerified",
        v.caption,
        v.sound_id as "soundId",
        v.sound_title as "soundTitle",
        v.sound_author as "soundAuthor",
        v.cover_res_name as "coverResName",
        v.video_url as "videoUrl",
        v.thumbnail_url as "thumbnailUrl",
        v.likes_count as "likesCount",
        v.comments_count as "commentsCount",
        v.shares_count as "sharesCount",
        v.saves_count as "bookmarksCount",
        v.views_count as "viewsCount",
        v.duration_seconds as "durationSeconds",
        v.category,
        v.hashtags,
        v.is_private as "isPrivate",
        v.allow_comments as "allowComments",
        v.allow_duet as "allowDuet",
        v.allow_stitch as "allowStitch",
        v.duet_with_video_id as "duetWithVideoId",
        v.stitch_with_video_id as "stitchWithVideoId",
        v.status,
        v.created_at as "createdAt",
        CASE WHEN vl.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isLiked",
        CASE WHEN vs.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isBookmarked",
        CASE WHEN vr.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isReposted",
        CASE WHEN f.follower_id IS NOT NULL THEN TRUE ELSE FALSE END as "isFollowing"
      FROM videos v
      LEFT JOIN profiles p ON v.author_id = p.user_id
      LEFT JOIN video_likes vl ON v.id = vl.video_id AND vl.user_id = $2
      LEFT JOIN video_saves vs ON v.id = vs.video_id AND vs.user_id = $2
      LEFT JOIN video_reposts vr ON v.id = vr.video_id AND vr.user_id = $2
      LEFT JOIN follows f ON v.author_id = f.following_id AND f.follower_id = $2
      WHERE v.id = $1`,
      [stringId, viewerId]
    );

    if (res.rows.length === 0) return null;

    const row = res.rows[0];
    return this.formatVideoRow(row);
  }

  async findFeedVideos(options: {
    viewerId?: string;
    category?: string;
    cursor?: string;
    limit?: number;
  }): Promise<{ videos: any[]; nextCursor: string | null; hasMore: boolean }> {
    const viewerId = options.viewerId || 'anonymous';
    const limit = Math.min(options.limit || 10, 50);

    let sql = `
      SELECT
        v.id,
        v.author_id as "authorId",
        p.display_name as "authorName",
        p.handle as "authorHandle",
        p.avatar_url as "authorAvatarUrl",
        p.is_verified as "authorVerified",
        v.caption,
        v.sound_id as "soundId",
        v.sound_title as "soundTitle",
        v.sound_author as "soundAuthor",
        v.cover_res_name as "coverResName",
        v.video_url as "videoUrl",
        v.thumbnail_url as "thumbnailUrl",
        v.likes_count as "likesCount",
        v.comments_count as "commentsCount",
        v.shares_count as "sharesCount",
        v.saves_count as "bookmarksCount",
        v.views_count as "viewsCount",
        v.duration_seconds as "durationSeconds",
        v.category,
        v.hashtags,
        v.is_private as "isPrivate",
        v.allow_comments as "allowComments",
        v.allow_duet as "allowDuet",
        v.allow_stitch as "allowStitch",
        v.created_at as "createdAt",
        CASE WHEN vl.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isLiked",
        CASE WHEN vs.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isBookmarked",
        CASE WHEN vr.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isReposted",
        CASE WHEN f.follower_id IS NOT NULL THEN TRUE ELSE FALSE END as "isFollowing"
      FROM videos v
      LEFT JOIN profiles p ON v.author_id = p.user_id
      LEFT JOIN video_likes vl ON v.id = vl.video_id AND vl.user_id = $1
      LEFT JOIN video_saves vs ON v.id = vs.video_id AND vs.user_id = $1
      LEFT JOIN video_reposts vr ON v.id = vr.video_id AND vr.user_id = $1
      LEFT JOIN follows f ON v.author_id = f.following_id AND f.follower_id = $1
      WHERE v.status = 'READY'
        AND (v.is_private = FALSE OR v.author_id = $1)
    `;

    const params: any[] = [viewerId];

    if (options.category && options.category !== 'fyp') {
      params.push(options.category);
      sql += ` AND v.category = $${params.length}`;
    }

    if (options.cursor) {
      params.push(options.cursor);
      sql += ` AND v.created_at < $${params.length}`;
    }

    // TikTok-style multi-signal recommendation scoring algorithm:
    // Follower boost + engagement weights + views + recency
    sql += `
      ORDER BY
        (CASE WHEN f.follower_id IS NOT NULL THEN 100000 ELSE 0 END
         + (v.likes_count * 3)
         + (v.comments_count * 5)
         + (v.shares_count * 4)
         + (v.saves_count * 3)
         + (v.views_count * 0.1)
        ) DESC,
        v.created_at DESC
      LIMIT $${params.length + 1}
    `;
    params.push(limit);

    const res = await query(sql, params);
    const videos = res.rows.map((r) => this.formatVideoRow(r));
    const nextCursor = videos.length === limit ? videos[videos.length - 1].createdAt : null;

    return {
      videos,
      nextCursor,
      hasMore: Boolean(nextCursor),
    };
  }

  async findFollowingVideos(options: {
    viewerId: string;
    cursor?: string;
    limit?: number;
  }): Promise<{ videos: any[]; nextCursor: string | null; hasMore: boolean; isSuggested: boolean }> {
    const viewerId = options.viewerId;
    const limit = Math.min(options.limit || 10, 50);

    let sql = `
      SELECT
        v.id,
        v.author_id as "authorId",
        p.display_name as "authorName",
        p.handle as "authorHandle",
        p.avatar_url as "authorAvatarUrl",
        p.is_verified as "authorVerified",
        v.caption,
        v.sound_id as "soundId",
        v.sound_title as "soundTitle",
        v.sound_author as "soundAuthor",
        v.cover_res_name as "coverResName",
        v.video_url as "videoUrl",
        v.thumbnail_url as "thumbnailUrl",
        v.likes_count as "likesCount",
        v.comments_count as "commentsCount",
        v.shares_count as "sharesCount",
        v.saves_count as "bookmarksCount",
        v.views_count as "viewsCount",
        v.duration_seconds as "durationSeconds",
        v.category,
        v.hashtags,
        v.created_at as "createdAt",
        CASE WHEN vl.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isLiked",
        CASE WHEN vs.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isBookmarked",
        CASE WHEN vr.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isReposted",
        TRUE as "isFollowing"
      FROM videos v
      INNER JOIN follows f ON v.author_id = f.following_id AND f.follower_id = $1
      LEFT JOIN profiles p ON v.author_id = p.user_id
      LEFT JOIN video_likes vl ON v.id = vl.video_id AND vl.user_id = $1
      LEFT JOIN video_saves vs ON v.id = vs.video_id AND vs.user_id = $1
      LEFT JOIN video_reposts vr ON v.id = vr.video_id AND vr.user_id = $1
      WHERE v.status = 'READY'
    `;

    const params: any[] = [viewerId];

    if (options.cursor) {
      params.push(options.cursor);
      sql += ` AND v.created_at < $${params.length}`;
    }

    sql += ` ORDER BY v.created_at DESC LIMIT $${params.length + 1}`;
    params.push(limit);

    const res = await query(sql, params);

    if (res.rows.length === 0) {
      // Fallback to top creators if user follows no one or has no fresh following videos
      const fallback = await query(
        `SELECT
          v.id,
          v.author_id as "authorId",
          p.display_name as "authorName",
          p.handle as "authorHandle",
          p.avatar_url as "authorAvatarUrl",
          p.is_verified as "authorVerified",
          v.caption,
          v.sound_id as "soundId",
          v.sound_title as "soundTitle",
          v.sound_author as "soundAuthor",
          v.cover_res_name as "coverResName",
          v.video_url as "videoUrl",
          v.thumbnail_url as "thumbnailUrl",
          v.likes_count as "likesCount",
          v.comments_count as "commentsCount",
          v.shares_count as "sharesCount",
          v.saves_count as "bookmarksCount",
          v.views_count as "viewsCount",
          v.duration_seconds as "durationSeconds",
          v.category,
          v.hashtags,
          v.created_at as "createdAt",
          CASE WHEN vl.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isLiked",
          CASE WHEN vs.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isBookmarked",
          CASE WHEN vr.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isReposted",
          FALSE as "isFollowing"
        FROM videos v
        LEFT JOIN profiles p ON v.author_id = p.user_id
        LEFT JOIN video_likes vl ON v.id = vl.video_id AND vl.user_id = $1
        LEFT JOIN video_saves vs ON v.id = vs.video_id AND vs.user_id = $1
        LEFT JOIN video_reposts vr ON v.id = vr.video_id AND vr.user_id = $1
        WHERE v.status = 'READY'
        ORDER BY v.likes_count DESC
        LIMIT $2`,
        [viewerId, limit]
      );

      return {
        videos: fallback.rows.map((r) => this.formatVideoRow(r)),
        nextCursor: null,
        hasMore: false,
        isSuggested: true,
      };
    }

    const videos = res.rows.map((r) => this.formatVideoRow(r));
    const nextCursor = videos.length === limit ? videos[videos.length - 1].createdAt : null;

    return {
      videos,
      nextCursor,
      hasMore: Boolean(nextCursor),
      isSuggested: false,
    };
  }

  async findByAuthor(authorIdentifier: string, viewerId: string = 'anonymous'): Promise<any[]> {
    const cleanHandle = authorIdentifier.startsWith('@') ? authorIdentifier : `@${authorIdentifier}`;
    const res = await query(
      `SELECT
        v.id,
        v.author_id as "authorId",
        p.display_name as "authorName",
        p.handle as "authorHandle",
        p.avatar_url as "authorAvatarUrl",
        p.is_verified as "authorVerified",
        v.caption,
        v.sound_id as "soundId",
        v.sound_title as "soundTitle",
        v.sound_author as "soundAuthor",
        v.cover_res_name as "coverResName",
        v.video_url as "videoUrl",
        v.thumbnail_url as "thumbnailUrl",
        v.likes_count as "likesCount",
        v.comments_count as "commentsCount",
        v.shares_count as "sharesCount",
        v.saves_count as "bookmarksCount",
        v.views_count as "viewsCount",
        v.duration_seconds as "durationSeconds",
        v.category,
        v.hashtags,
        v.created_at as "createdAt",
        CASE WHEN vl.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isLiked",
        CASE WHEN vs.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isBookmarked",
        CASE WHEN vr.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isReposted",
        CASE WHEN f.follower_id IS NOT NULL THEN TRUE ELSE FALSE END as "isFollowing"
      FROM videos v
      INNER JOIN profiles p ON v.author_id = p.user_id
      LEFT JOIN video_likes vl ON v.id = vl.video_id AND vl.user_id = $2
      LEFT JOIN video_saves vs ON v.id = vs.video_id AND vs.user_id = $2
      LEFT JOIN video_reposts vr ON v.id = vr.video_id AND vr.user_id = $2
      LEFT JOIN follows f ON v.author_id = f.following_id AND f.follower_id = $2
      WHERE (p.handle = $1 OR p.user_id = $1)
        AND v.status = 'READY'
        AND (v.is_private = FALSE OR v.author_id = $2)
      ORDER BY v.created_at DESC`,
      [cleanHandle, viewerId]
    );

    return res.rows.map((r) => this.formatVideoRow(r));
  }

  async findLikedByUser(userId: string, viewerId: string = 'anonymous'): Promise<any[]> {
    const res = await query(
      `SELECT
        v.id,
        v.author_id as "authorId",
        p.display_name as "authorName",
        p.handle as "authorHandle",
        p.avatar_url as "authorAvatarUrl",
        p.is_verified as "authorVerified",
        v.caption,
        v.sound_title as "soundTitle",
        v.sound_author as "soundAuthor",
        v.cover_res_name as "coverResName",
        v.video_url as "videoUrl",
        v.likes_count as "likesCount",
        v.comments_count as "commentsCount",
        v.shares_count as "sharesCount",
        v.saves_count as "bookmarksCount",
        v.views_count as "viewsCount",
        v.duration_seconds as "durationSeconds",
        v.category,
        v.hashtags,
        v.created_at as "createdAt",
        TRUE as "isLiked",
        CASE WHEN vs.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isBookmarked",
        CASE WHEN vr.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isReposted",
        CASE WHEN f.follower_id IS NOT NULL THEN TRUE ELSE FALSE END as "isFollowing"
      FROM video_likes vl
      INNER JOIN videos v ON vl.video_id = v.id
      LEFT JOIN profiles p ON v.author_id = p.user_id
      LEFT JOIN video_saves vs ON v.id = vs.video_id AND vs.user_id = $2
      LEFT JOIN video_reposts vr ON v.id = vr.video_id AND vr.user_id = $2
      LEFT JOIN follows f ON v.author_id = f.following_id AND f.follower_id = $2
      WHERE vl.user_id = $1 AND v.status = 'READY'
      ORDER BY vl.created_at DESC
      LIMIT 50`,
      [userId, viewerId]
    );

    return res.rows.map((r) => this.formatVideoRow(r));
  }

  async findSavedByUser(userId: string): Promise<any[]> {
    const res = await query(
      `SELECT
        v.id,
        v.author_id as "authorId",
        p.display_name as "authorName",
        p.handle as "authorHandle",
        p.avatar_url as "authorAvatarUrl",
        p.is_verified as "authorVerified",
        v.caption,
        v.sound_title as "soundTitle",
        v.sound_author as "soundAuthor",
        v.cover_res_name as "coverResName",
        v.video_url as "videoUrl",
        v.likes_count as "likesCount",
        v.comments_count as "commentsCount",
        v.shares_count as "sharesCount",
        v.saves_count as "bookmarksCount",
        v.views_count as "viewsCount",
        v.duration_seconds as "durationSeconds",
        v.category,
        v.hashtags,
        v.created_at as "createdAt",
        CASE WHEN vl.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isLiked",
        TRUE as "isBookmarked",
        CASE WHEN vr.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isReposted",
        CASE WHEN f.follower_id IS NOT NULL THEN TRUE ELSE FALSE END as "isFollowing"
      FROM video_saves vs
      INNER JOIN videos v ON vs.video_id = v.id
      LEFT JOIN profiles p ON v.author_id = p.user_id
      LEFT JOIN video_likes vl ON v.id = vl.video_id AND vl.user_id = $1
      LEFT JOIN video_reposts vr ON v.id = vr.video_id AND vr.user_id = $1
      LEFT JOIN follows f ON v.author_id = f.following_id AND f.follower_id = $1
      WHERE vs.user_id = $1 AND v.status = 'READY'
      ORDER BY vs.created_at DESC
      LIMIT 50`,
      [userId]
    );

    return res.rows.map((r) => this.formatVideoRow(r));
  }

  async like(userId: string, videoId: string): Promise<{ isLiked: boolean; likesCount: number }> {
    const videoCheck = await query('SELECT id FROM videos WHERE id = $1', [videoId]);
    if (videoCheck.rows.length === 0) {
      return { isLiked: false, likesCount: 0 };
    }

    try {
      const existing = await query(
        'SELECT user_id FROM video_likes WHERE user_id = $1 AND video_id = $2',
        [userId, videoId]
      );

      let isLiked = false;
      if (existing.rows.length > 0) {
        await query('DELETE FROM video_likes WHERE user_id = $1 AND video_id = $2', [userId, videoId]);
        await query(
          'UPDATE videos SET likes_count = GREATEST(0, likes_count - 1) WHERE id = $1',
          [videoId]
        );
        // Decrement creator total likes
        await query(
          `UPDATE profiles SET likes_count = GREATEST(0, likes_count - 1)
           WHERE user_id = (SELECT author_id FROM videos WHERE id = $1)`,
          [videoId]
        );
        isLiked = false;
      } else {
        await query('INSERT INTO video_likes (user_id, video_id) VALUES ($1, $2)', [userId, videoId]);
        await query('UPDATE videos SET likes_count = likes_count + 1 WHERE id = $1', [videoId]);
        // Increment creator total likes
        await query(
          `UPDATE profiles SET likes_count = likes_count + 1
           WHERE user_id = (SELECT author_id FROM videos WHERE id = $1)`,
          [videoId]
        );
        isLiked = true;
      }

      const countRes = await query('SELECT likes_count FROM videos WHERE id = $1', [videoId]);
      const likesCount = Number(countRes.rows[0]?.likes_count || 0);

      return { isLiked, likesCount };
    } catch (err: any) {
      if (err?.code === '23503') {
        return { isLiked: false, likesCount: 0 };
      }
      throw err;
    }
  }

  async save(userId: string, videoId: string): Promise<{ isSaved: boolean; savesCount: number }> {
    const videoCheck = await query('SELECT id FROM videos WHERE id = $1', [videoId]);
    if (videoCheck.rows.length === 0) {
      return { isSaved: false, savesCount: 0 };
    }

    try {
      const existing = await query(
        'SELECT user_id FROM video_saves WHERE user_id = $1 AND video_id = $2',
        [userId, videoId]
      );

      let isSaved = false;
      if (existing.rows.length > 0) {
        await query('DELETE FROM video_saves WHERE user_id = $1 AND video_id = $2', [userId, videoId]);
        await query(
          'UPDATE videos SET saves_count = GREATEST(0, saves_count - 1) WHERE id = $1',
          [videoId]
        );
        isSaved = false;
      } else {
        await query('INSERT INTO video_saves (user_id, video_id) VALUES ($1, $2)', [userId, videoId]);
        await query('UPDATE videos SET saves_count = saves_count + 1 WHERE id = $1', [videoId]);
        isSaved = true;
      }

      const countRes = await query('SELECT saves_count FROM videos WHERE id = $1', [videoId]);
      const savesCount = Number(countRes.rows[0]?.saves_count || 0);

      return { isSaved, savesCount };
    } catch (err: any) {
      if (err?.code === '23503') {
        return { isSaved: false, savesCount: 0 };
      }
      throw err;
    }
  }

  async repost(userId: string, videoId: string): Promise<{ isReposted: boolean; repostsCount: number }> {
    const videoCheck = await query('SELECT id FROM videos WHERE id = $1', [videoId]);
    if (videoCheck.rows.length === 0) {
      return { isReposted: false, repostsCount: 0 };
    }

    try {
      const existing = await query(
        'SELECT user_id FROM video_reposts WHERE user_id = $1 AND video_id = $2',
        [userId, videoId]
      );

      let isReposted = false;
      if (existing.rows.length > 0) {
        await query('DELETE FROM video_reposts WHERE user_id = $1 AND video_id = $2', [userId, videoId]);
        await query(
          'UPDATE videos SET reposts_count = GREATEST(0, reposts_count - 1) WHERE id = $1',
          [videoId]
        );
        isReposted = false;
      } else {
        await query('INSERT INTO video_reposts (user_id, video_id) VALUES ($1, $2)', [userId, videoId]);
        await query('UPDATE videos SET reposts_count = reposts_count + 1 WHERE id = $1', [videoId]);
        isReposted = true;
      }

      const countRes = await query('SELECT reposts_count FROM videos WHERE id = $1', [videoId]);
      const repostsCount = Number(countRes.rows[0]?.reposts_count || 0);

      return { isReposted, repostsCount };
    } catch (err: any) {
      if (err?.code === '23503') {
        return { isReposted: false, repostsCount: 0 };
      }
      throw err;
    }
  }

  async recordView(data: {
    videoId: string;
    viewerId?: string | null;
    durationMs?: number;
    completionPercent?: number;
    ipAddress?: string | null;
  }): Promise<void> {
    // Validate video existence to prevent foreign key violations on non-existent or client-transient IDs
    const videoCheck = await query('SELECT id FROM videos WHERE id = $1', [data.videoId]);
    if (videoCheck.rows.length === 0) {
      return;
    }

    const id = `vw_${uuidv4().replace(/-/g, '').slice(0, 16)}`;
    const duration = data.durationMs || 0;
    const completion = data.completionPercent || 0;

    try {
      await query(
        `INSERT INTO video_views (id, video_id, viewer_id, watch_duration_ms, completion_percent, is_complete, ip_address)
         VALUES ($1, $2, $3, $4, $5, $6, $7)`,
        [
          id,
          data.videoId,
          data.viewerId || null,
          duration,
          completion,
          completion >= 0.9,
          data.ipAddress || null,
        ]
      );

      // Atomically increment video view count
      await query('UPDATE videos SET views_count = views_count + 1 WHERE id = $1', [data.videoId]);
    } catch (err: any) {
      // Gracefully ignore foreign key constraint violations if the video was concurrently deleted
      if (err?.code === '23503') {
        return;
      }
      throw err;
    }
  }

  async incrementShare(videoId: string): Promise<number> {
    const videoCheck = await query('SELECT id FROM videos WHERE id = $1', [videoId]);
    if (videoCheck.rows.length === 0) {
      return 0;
    }
    await query('UPDATE videos SET shares_count = shares_count + 1 WHERE id = $1', [videoId]);
    const countRes = await query('SELECT shares_count FROM videos WHERE id = $1', [videoId]);
    return Number(countRes.rows[0]?.shares_count || 0);
  }

  async deleteVideo(videoId: string, authorId: string): Promise<boolean> {
    const res = await query(
      'DELETE FROM videos WHERE id = $1 AND author_id = $2',
      [videoId, authorId]
    );
    if ((res.rowCount ?? 0) > 0) {
      await query(
        'UPDATE profiles SET videos_count = GREATEST(0, videos_count - 1) WHERE user_id = $1',
        [authorId]
      );
      return true;
    }
    return false;
  }

  async linkHashtags(videoId: string, hashtagsString: string): Promise<void> {
    const tags = hashtagsString
      .split(/[\s,]+/)
      .filter((t) => t.startsWith('#') && t.length > 1)
      .map((t) => t.toLowerCase());

    for (const tag of tags) {
      const tagId = `tag_${tag.replace('#', '')}`;
      await query(
        `INSERT INTO hashtags (id, name, usage_count)
         VALUES ($1, $2, 1)
         ON CONFLICT (name) DO UPDATE SET usage_count = hashtags.usage_count + 1`,
        [tagId, tag]
      );

      const hRes = await query('SELECT id FROM hashtags WHERE name = $1', [tag]);
      if (hRes.rows.length > 0) {
        await query(
          `INSERT INTO video_hashtags (video_id, hashtag_id)
           VALUES ($1, $2)
           ON CONFLICT DO NOTHING`,
          [videoId, hRes.rows[0].id]
        );
      }
    }
  }

  async createProcessingJob(data: {
    id: string;
    videoId: string;
    sourceStorageKey: string;
  }): Promise<void> {
    await query(
      `INSERT INTO video_processing_jobs (id, video_id, source_storage_key, status)
       VALUES ($1, $2, $3, 'PENDING')`,
      [data.id, data.videoId, data.sourceStorageKey]
    );
  }

  private formatVideoRow(row: any): any {
    return {
      ...row,
      id: row.id,
      likesCount: Number(row.likesCount || 0),
      commentsCount: Number(row.commentsCount || 0),
      sharesCount: Number(row.sharesCount || 0),
      bookmarksCount: Number(row.bookmarksCount || 0),
      viewsCount: Number(row.viewsCount || 0),
      durationSeconds: Number(row.durationSeconds || 15),
      timestamp: new Date(row.createdAt).getTime(),
    };
  }
}

export const videoRepository = new VideoRepository();
