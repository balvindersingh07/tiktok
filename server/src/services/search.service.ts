import { query } from '../db/index.js';

export class SearchService {
  async search(searchTerm: string, viewerId: string = 'anonymous') {
    const term = searchTerm.trim();
    if (!term) {
      return { videos: [], users: [], hashtags: [], sounds: [] };
    }

    const pattern = `%${term.toLowerCase()}%`;

    // 1. Search Videos
    const videosRes = await query(
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
        v.category,
        v.hashtags,
        v.created_at as "createdAt",
        CASE WHEN vl.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isLiked"
      FROM videos v
      LEFT JOIN profiles p ON v.author_id = p.user_id
      LEFT JOIN video_likes vl ON v.id = vl.video_id AND vl.user_id = $2
      WHERE v.status = 'READY'
        AND v.is_private = FALSE
        AND (LOWER(v.caption) LIKE $1 OR LOWER(v.hashtags) LIKE $1)
      ORDER BY v.likes_count DESC
      LIMIT 20`,
      [pattern, viewerId]
    );

    // 2. Search Users
    const cleanHandlePattern = term.startsWith('@') ? pattern : `%${term.toLowerCase()}%`;
    const usersRes = await query(
      `SELECT
        p.user_id as "userId",
        p.display_name as "displayName",
        p.handle,
        p.bio,
        p.avatar_url as "avatarUrl",
        p.followers_count as "followersCount",
        p.likes_count as "likesCount",
        p.is_verified as "isVerified",
        CASE WHEN f.follower_id IS NOT NULL THEN TRUE ELSE FALSE END as "isFollowing"
      FROM profiles p
      LEFT JOIN follows f ON p.user_id = f.following_id AND f.follower_id = $2
      WHERE LOWER(p.handle) LIKE $1 OR LOWER(p.display_name) LIKE $1
      ORDER BY p.followers_count DESC
      LIMIT 15`,
      [cleanHandlePattern, viewerId]
    );

    // 3. Search Hashtags
    const hashPattern = term.startsWith('#') ? `%${term.toLowerCase()}%` : `%#${term.toLowerCase()}%`;
    const hashtagsRes = await query(
      `SELECT id, name, usage_count as "usageCount"
       FROM hashtags
       WHERE LOWER(name) LIKE $1 OR LOWER(name) LIKE $2
       ORDER BY usage_count DESC
       LIMIT 10`,
      [pattern, hashPattern]
    );

    // 4. Search Sounds
    const soundsRes = await query(
      `SELECT id, title, author, duration_seconds as "durationSeconds", audio_url as "audioUrl", usage_count as "usageCount"
       FROM sounds
       WHERE LOWER(title) LIKE $1 OR LOWER(author) LIKE $1
       ORDER BY usage_count DESC
       LIMIT 10`,
      [pattern]
    );

    return {
      videos: videosRes.rows.map((row) => ({
        ...row,
        id: isNaN(Number(row.id)) ? row.id : Number(row.id),
        likesCount: Number(row.likesCount || 0),
        commentsCount: Number(row.commentsCount || 0),
        timestamp: new Date(row.createdAt).getTime(),
      })),
      users: usersRes.rows.map((row) => ({
        ...row,
        followersCount: Number(row.followersCount || 0),
        likesCount: Number(row.likesCount || 0),
        isFollowing: Boolean(row.isFollowing),
      })),
      hashtags: hashtagsRes.rows.map((row) => ({
        ...row,
        usageCount: Number(row.usageCount || 0),
      })),
      sounds: soundsRes.rows.map((row) => ({
        ...row,
        durationSeconds: Number(row.durationSeconds || 15),
        usageCount: Number(row.usageCount || 0),
      })),
    };
  }

  async getTrending() {
    const hashtags = await query('SELECT id, name, usage_count as "usageCount" FROM hashtags ORDER BY usage_count DESC LIMIT 10');
    const sounds = await query('SELECT id, title, author, usage_count as "usageCount" FROM sounds ORDER BY usage_count DESC LIMIT 10');

    return {
      hashtags: hashtags.rows.map((r) => ({ ...r, usageCount: Number(r.usageCount || 0) })),
      sounds: sounds.rows.map((r) => ({ ...r, usageCount: Number(r.usageCount || 0) })),
    };
  }
}

export const searchService = new SearchService();
