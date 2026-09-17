import { query } from '../db/index.js';
import { v4 as uuidv4 } from 'uuid';

export interface UserRow {
  id: string;
  handle: string;
  email: string | null;
  phone: string | null;
  password_hash: string;
  role: string;
  is_verified: boolean;
  is_active: boolean;
  created_at: Date;
  updated_at: Date;
}

export interface ProfileRow {
  user_id: string;
  display_name: string;
  handle: string;
  bio: string;
  avatar_url: string;
  cover_url: string;
  is_verified: boolean;
  is_private: boolean;
  allow_comments: string;
  allow_duet: string;
  allow_stitch: string;
  allow_downloads: boolean;
  allow_direct_messages: string;
  show_liked_videos: boolean;
  show_following_list: boolean;
  following_count: number;
  followers_count: number;
  likes_count: number;
  videos_count: number;
  created_at: Date;
  updated_at: Date;
}

export class UserRepository {
  async findById(id: string): Promise<UserRow | null> {
    const res = await query('SELECT * FROM users WHERE id = $1', [id]);
    return res.rows[0] || null;
  }

  async findByHandle(handle: string): Promise<UserRow | null> {
    const cleanHandle = handle.startsWith('@') ? handle : `@${handle}`;
    const res = await query('SELECT * FROM users WHERE handle = $1', [cleanHandle]);
    return res.rows[0] || null;
  }

  async findByEmail(email: string): Promise<UserRow | null> {
    const res = await query('SELECT * FROM users WHERE LOWER(email) = LOWER($1)', [email]);
    return res.rows[0] || null;
  }

  async findByHandleOrEmail(identifier: string): Promise<UserRow | null> {
    const isEmail = identifier.includes('@') && !identifier.startsWith('@');
    if (isEmail) {
      return this.findByEmail(identifier);
    }
    return this.findByHandle(identifier);
  }

  async createUser(data: {
    id: string;
    handle: string;
    email?: string | null;
    passwordHash: string;
    role?: string;
    isVerified?: boolean;
  }): Promise<UserRow> {
    const cleanHandle = data.handle.startsWith('@') ? data.handle : `@${data.handle}`;
    const res = await query(
      `INSERT INTO users (id, handle, email, password_hash, role, is_verified, is_active)
       VALUES ($1, $2, $3, $4, $5, $6, TRUE)
       RETURNING *`,
      [
        data.id,
        cleanHandle,
        data.email || null,
        data.passwordHash,
        data.role || 'USER',
        data.isVerified || false,
      ]
    );
    return res.rows[0];
  }

  async createProfile(data: {
    userId: string;
    displayName: string;
    handle: string;
    bio?: string;
    avatarUrl?: string;
    coverUrl?: string;
  }): Promise<ProfileRow> {
    const cleanHandle = data.handle.startsWith('@') ? data.handle : `@${data.handle}`;
    const res = await query(
      `INSERT INTO profiles (
        user_id, display_name, handle, bio, avatar_url, cover_url,
        is_verified, is_private, following_count, followers_count, likes_count, videos_count
      ) VALUES ($1, $2, $3, $4, $5, $6, FALSE, FALSE, 0, 0, 0, 0)
      RETURNING *`,
      [
        data.userId,
        data.displayName,
        cleanHandle,
        data.bio || '',
        data.avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80',
        data.coverUrl || '',
      ]
    );

    // Also initialize default notification preferences
    await query(
      `INSERT INTO notification_preferences (user_id)
       VALUES ($1) ON CONFLICT (user_id) DO NOTHING`,
      [data.userId]
    );

    return res.rows[0];
  }

  async getProfile(identifier: string, viewerId: string = 'anonymous'): Promise<any | null> {
    const cleanHandle = identifier.startsWith('@') ? identifier : `@${identifier}`;
    const res = await query(
      `SELECT
        p.user_id as "userId",
        p.display_name as "displayName",
        p.handle,
        p.bio,
        p.avatar_url as "avatarUrl",
        p.cover_url as "coverUrl",
        p.followers_count as "followersCount",
        p.following_count as "followingCount",
        p.likes_count as "likesCount",
        p.videos_count as "videosCount",
        p.is_verified as "isVerified",
        p.is_private as "isPrivate",
        p.allow_comments as "allowComments",
        p.allow_duet as "allowDuet",
        p.allow_stitch as "allowStitch",
        p.allow_downloads as "allowDownloads",
        p.allow_direct_messages as "allowDirectMessages",
        p.show_liked_videos as "showLikedVideos",
        p.show_following_list as "showFollowingList",
        CASE WHEN f.follower_id IS NOT NULL THEN TRUE ELSE FALSE END as "isFollowing",
        CASE WHEN b.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isBlocked",
        CASE WHEN m.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isMuted"
      FROM profiles p
      LEFT JOIN follows f ON p.user_id = f.following_id AND f.follower_id = $2
      LEFT JOIN blocked_users b ON p.user_id = b.blocked_user_id AND b.user_id = $2
      LEFT JOIN muted_users m ON p.user_id = m.muted_user_id AND m.user_id = $2
      WHERE p.handle = $1 OR p.user_id = $1`,
      [cleanHandle, viewerId]
    );
    return res.rows[0] || null;
  }

  async updateProfile(userId: string, data: Record<string, any>): Promise<any> {
    const updates: string[] = [];
    const params: any[] = [userId];

    const mapping: Record<string, string> = {
      displayName: 'display_name',
      bio: 'bio',
      avatarUrl: 'avatar_url',
      coverUrl: 'cover_url',
      isPrivate: 'is_private',
      allowComments: 'allow_comments',
      allowDirectMessages: 'allow_direct_messages',
      allowDuet: 'allow_duet',
      allowStitch: 'allow_stitch',
      allowDownloads: 'allow_downloads',
      showLikedVideos: 'show_liked_videos',
      showFollowingList: 'show_following_list',
    };

    for (const [key, col] of Object.entries(mapping)) {
      if (data[key] !== undefined) {
        let val = data[key];
        if (typeof val === 'boolean' && (col === 'allow_comments' || col === 'allow_duet' || col === 'allow_stitch')) {
          val = val ? 'EVERYONE' : 'NO_ONE';
        }
        params.push(val);
        updates.push(`${col} = $${params.length}`);
      }
    }

    if (updates.length > 0) {
      updates.push('updated_at = NOW()');
      await query(`UPDATE profiles SET ${updates.join(', ')} WHERE user_id = $1`, params);
    }

    return this.getProfile(userId, userId);
  }

  async updatePassword(userId: string, newHash: string): Promise<void> {
    await query('UPDATE users SET password_hash = $1, updated_at = NOW() WHERE id = $2', [
      newHash,
      userId,
    ]);
  }

  async listCreators(limit: number = 30, viewerId: string = 'anonymous'): Promise<any[]> {
    const res = await query(
      `SELECT
        p.user_id as "userId",
        p.display_name as "displayName",
        p.handle,
        p.bio,
        p.avatar_url as "avatarUrl",
        p.followers_count as "followersCount",
        p.following_count as "followingCount",
        p.likes_count as "likesCount",
        p.is_verified as "isVerified",
        p.is_private as "isPrivate",
        CASE WHEN f.follower_id IS NOT NULL THEN TRUE ELSE FALSE END as "isFollowing"
      FROM profiles p
      LEFT JOIN follows f ON p.user_id = f.following_id AND f.follower_id = $1
      ORDER BY p.followers_count DESC
      LIMIT $2`,
      [viewerId, limit]
    );
    return res.rows;
  }

  async follow(followerId: string, followingId: string): Promise<void> {
    await query(
      `INSERT INTO follows (follower_id, following_id)
       VALUES ($1, $2)
       ON CONFLICT (follower_id, following_id) DO NOTHING`,
      [followerId, followingId]
    );
    await query(
      'UPDATE profiles SET followers_count = followers_count + 1 WHERE user_id = $1',
      [followingId]
    );
    await query(
      'UPDATE profiles SET following_count = following_count + 1 WHERE user_id = $1',
      [followerId]
    );
  }

  async unfollow(followerId: string, followingId: string): Promise<void> {
    const res = await query(
      'DELETE FROM follows WHERE follower_id = $1 AND following_id = $2',
      [followerId, followingId]
    );
    if ((res.rowCount ?? 0) > 0) {
      await query(
        'UPDATE profiles SET followers_count = GREATEST(0, followers_count - 1) WHERE user_id = $1',
        [followingId]
      );
      await query(
        'UPDATE profiles SET following_count = GREATEST(0, following_count - 1) WHERE user_id = $1',
        [followerId]
      );
    }
  }

  async isFollowing(followerId: string, followingId: string): Promise<boolean> {
    const res = await query(
      'SELECT 1 FROM follows WHERE follower_id = $1 AND following_id = $2',
      [followerId, followingId]
    );
    return res.rows.length > 0;
  }

  async getFollowers(userId: string, limit: number = 50): Promise<any[]> {
    const res = await query(
      `SELECT
        p.user_id as "userId",
        p.display_name as "displayName",
        p.handle,
        p.avatar_url as "avatarUrl",
        p.bio,
        p.is_verified as "isVerified",
        f.created_at as "followedAt"
      FROM follows f
      INNER JOIN profiles p ON f.follower_id = p.user_id
      WHERE f.following_id = $1
      ORDER BY f.created_at DESC
      LIMIT $2`,
      [userId, limit]
    );
    return res.rows;
  }

  async getFollowing(userId: string, limit: number = 50): Promise<any[]> {
    const res = await query(
      `SELECT
        p.user_id as "userId",
        p.display_name as "displayName",
        p.handle,
        p.avatar_url as "avatarUrl",
        p.bio,
        p.is_verified as "isVerified",
        f.created_at as "followedAt"
      FROM follows f
      INNER JOIN profiles p ON f.following_id = p.user_id
      WHERE f.follower_id = $1
      ORDER BY f.created_at DESC
      LIMIT $2`,
      [userId, limit]
    );
    return res.rows;
  }

  async block(userId: string, targetUserId: string): Promise<void> {
    await query(
      `INSERT INTO blocked_users (user_id, blocked_user_id)
       VALUES ($1, $2)
       ON CONFLICT DO NOTHING`,
      [userId, targetUserId]
    );
    // Unfollow both ways if blocked
    await this.unfollow(userId, targetUserId);
    await this.unfollow(targetUserId, userId);
  }

  async unblock(userId: string, targetUserId: string): Promise<void> {
    await query('DELETE FROM blocked_users WHERE user_id = $1 AND blocked_user_id = $2', [
      userId,
      targetUserId,
    ]);
  }

  async isBlocked(userA: string, userB: string): Promise<boolean> {
    const res = await query(
      `SELECT 1 FROM blocked_users
       WHERE (user_id = $1 AND blocked_user_id = $2)
          OR (user_id = $2 AND blocked_user_id = $1)`,
      [userA, userB]
    );
    return res.rows.length > 0;
  }

  async getBlockedUsers(userId: string): Promise<any[]> {
    const res = await query(
      `SELECT p.user_id as "userId", p.display_name as "displayName", p.handle, p.avatar_url as "avatarUrl"
       FROM blocked_users b
       JOIN profiles p ON b.blocked_user_id = p.user_id
       WHERE b.user_id = $1`,
      [userId]
    );
    return res.rows;
  }

  async mute(userId: string, targetUserId: string): Promise<void> {
    await query(
      `INSERT INTO muted_users (user_id, muted_user_id)
       VALUES ($1, $2)
       ON CONFLICT DO NOTHING`,
      [userId, targetUserId]
    );
  }

  async unmute(userId: string, targetUserId: string): Promise<void> {
    await query('DELETE FROM muted_users WHERE user_id = $1 AND muted_user_id = $2', [
      userId,
      targetUserId,
    ]);
  }

  async isMuted(userId: string, targetUserId: string): Promise<boolean> {
    const res = await query(
      'SELECT 1 FROM muted_users WHERE user_id = $1 AND muted_user_id = $2',
      [userId, targetUserId]
    );
    return res.rows.length > 0;
  }

  async getMutedUsers(userId: string): Promise<any[]> {
    const res = await query(
      `SELECT p.user_id as "userId", p.display_name as "displayName", p.handle, p.avatar_url as "avatarUrl"
       FROM muted_users m
       JOIN profiles p ON m.muted_user_id = p.user_id
       WHERE m.user_id = $1`,
      [userId]
    );
    return res.rows;
  }

  async registerDeviceToken(userId: string, token: string, platform: string = 'ANDROID'): Promise<void> {
    const id = `dt_${uuidv4().replace(/-/g, '').slice(0, 16)}`;
    await query(
      `INSERT INTO device_tokens (id, user_id, token, platform, last_seen_at)
       VALUES ($1, $2, $3, $4, NOW())
       ON CONFLICT (user_id, token) DO UPDATE SET last_seen_at = NOW(), platform = $4`,
      [id, userId, token, platform]
    );
  }

  async removeDeviceToken(userId: string, token: string): Promise<void> {
    await query('DELETE FROM device_tokens WHERE user_id = $1 AND token = $2', [userId, token]);
  }

  async createSession(data: {
    id: string;
    userId: string;
    refreshTokenHash: string;
    deviceInfo?: string;
    ipAddress?: string;
    expiresAt: Date;
  }): Promise<void> {
    const sanitizedIp = data.ipAddress
      ? data.ipAddress.split(',')[0].trim().slice(0, 100)
      : null;

    await query(
      `INSERT INTO sessions (id, user_id, refresh_token_hash, device_info, ip_address, expires_at)
       VALUES ($1, $2, $3, $4, $5, $6)`,
      [
        data.id,
        data.userId,
        data.refreshTokenHash,
        data.deviceInfo || null,
        sanitizedIp,
        data.expiresAt,
      ]
    );
  }

  async revokeSession(id: string): Promise<void> {
    await query('UPDATE sessions SET revoked_at = NOW() WHERE id = $1', [id]);
  }

  async revokeAllUserSessions(userId: string): Promise<void> {
    await query('UPDATE sessions SET revoked_at = NOW() WHERE user_id = $1', [userId]);
  }
}

export const userRepository = new UserRepository();
