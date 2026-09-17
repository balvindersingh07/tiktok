import { query } from '../db/index.js';

export class CommentRepository {
  async create(data: {
    id: string;
    videoId: string;
    authorId: string;
    content: string;
    parentCommentId?: string | null;
  }): Promise<any> {
    const res = await query(
      `INSERT INTO comments (id, video_id, author_id, content, parent_comment_id)
       VALUES ($1, $2, $3, $4, $5)
       RETURNING *`,
      [data.id, data.videoId, data.authorId, data.content, data.parentCommentId || null]
    );

    // Increment video comments_count
    await query('UPDATE videos SET comments_count = comments_count + 1 WHERE id = $1', [
      data.videoId,
    ]);

    // If this is a reply to another comment, increment parent replies_count
    if (data.parentCommentId) {
      await query(
        'UPDATE comments SET replies_count = replies_count + 1 WHERE id = $1',
        [data.parentCommentId]
      );
    }

    return res.rows[0];
  }

  async findByVideoId(videoId: string, viewerId: string = 'anonymous'): Promise<any[]> {
    const res = await query(
      `SELECT
        c.id,
        c.video_id as "videoId",
        c.author_id as "authorId",
        p.display_name as "authorName",
        p.handle as "authorHandle",
        p.avatar_url as "authorAvatarUrl",
        p.is_verified as "authorVerified",
        c.content,
        c.likes_count as "likesCount",
        c.replies_count as "repliesCount",
        c.parent_comment_id as "parentCommentId",
        c.created_at as "createdAt",
        CASE WHEN cl.user_id IS NOT NULL THEN TRUE ELSE FALSE END as "isLiked"
      FROM comments c
      LEFT JOIN profiles p ON c.author_id = p.user_id
      LEFT JOIN comment_likes cl ON c.id = cl.comment_id AND cl.user_id = $2
      WHERE c.video_id = $1 AND c.status = 'VISIBLE'
      ORDER BY c.created_at ASC`,
      [videoId, viewerId]
    );

    return res.rows.map((row) => ({
      id: isNaN(Number(row.id)) ? row.id : Number(row.id),
      videoId: row.videoId,
      authorId: row.authorId,
      authorName: row.authorName || 'Tashan User',
      authorHandle: row.authorHandle || '@user',
      authorAvatarUrl: row.authorAvatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80',
      authorVerified: Boolean(row.authorVerified),
      content: row.content,
      likesCount: Number(row.likesCount || 0),
      repliesCount: Number(row.repliesCount || 0),
      parentCommentId: row.parentCommentId,
      isLiked: Boolean(row.isLiked),
      timestamp: new Date(row.createdAt).getTime(),
    }));
  }

  async findById(id: string): Promise<any | null> {
    const res = await query('SELECT * FROM comments WHERE id = $1', [id]);
    return res.rows[0] || null;
  }

  async like(userId: string, commentId: string): Promise<{ isLiked: boolean; likesCount: number }> {
    const existing = await query(
      'SELECT user_id FROM comment_likes WHERE user_id = $1 AND comment_id = $2',
      [userId, commentId]
    );

    let isLiked = false;
    if (existing.rows.length > 0) {
      await query('DELETE FROM comment_likes WHERE user_id = $1 AND comment_id = $2', [
        userId,
        commentId,
      ]);
      await query(
        'UPDATE comments SET likes_count = GREATEST(0, likes_count - 1) WHERE id = $1',
        [commentId]
      );
      isLiked = false;
    } else {
      await query('INSERT INTO comment_likes (user_id, comment_id) VALUES ($1, $2)', [
        userId,
        commentId,
      ]);
      await query('UPDATE comments SET likes_count = likes_count + 1 WHERE id = $1', [commentId]);
      isLiked = true;
    }

    const countRes = await query('SELECT likes_count FROM comments WHERE id = $1', [commentId]);
    return {
      isLiked,
      likesCount: Number(countRes.rows[0]?.likes_count || 0),
    };
  }

  async delete(commentId: string, authorId: string): Promise<boolean> {
    // Check if author matches comment author or video author
    const res = await query(
      `SELECT c.id, c.video_id, c.parent_comment_id, c.author_id, v.author_id as video_author_id
       FROM comments c
       JOIN videos v ON c.video_id = v.id
       WHERE c.id = $1`,
      [commentId]
    );

    if (res.rows.length === 0) return false;

    const { video_id, parent_comment_id, author_id: commentAuthor, video_author_id } = res.rows[0];

    if (authorId !== commentAuthor && authorId !== video_author_id) {
      return false;
    }

    await query('DELETE FROM comments WHERE id = $1', [commentId]);
    await query(
      'UPDATE videos SET comments_count = GREATEST(0, comments_count - 1) WHERE id = $1',
      [video_id]
    );

    if (parent_comment_id) {
      await query(
        'UPDATE comments SET replies_count = GREATEST(0, replies_count - 1) WHERE id = $1',
        [parent_comment_id]
      );
    }

    return true;
  }
}

export const commentRepository = new CommentRepository();
