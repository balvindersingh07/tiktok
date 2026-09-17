import { query } from '../db/index.js';
import { v4 as uuidv4 } from 'uuid';

export class MessageRepository {
  async getConversations(userId: string): Promise<any[]> {
    const res = await query(
      `SELECT
        dc.id,
        dc.last_message_text as "lastMessage",
        dc.last_message_at as "lastMessageAt",
        CASE WHEN dc.user_one_id = $1 THEN dc.user_two_id ELSE dc.user_one_id END as "peerId",
        p.display_name as "peerDisplayName",
        p.handle as "peerHandle",
        p.avatar_url as "peerAvatarUrl",
        p.is_verified as "peerVerified",
        (
          SELECT COUNT(*) FROM direct_messages dm
          WHERE dm.conversation_id = dc.id AND dm.receiver_id = $1 AND dm.is_read = FALSE
        ) as "unreadCount"
      FROM direct_conversations dc
      LEFT JOIN profiles p ON (CASE WHEN dc.user_one_id = $1 THEN dc.user_two_id ELSE dc.user_one_id END) = p.user_id
      WHERE dc.user_one_id = $1 OR dc.user_two_id = $1
      ORDER BY dc.last_message_at DESC`,
      [userId]
    );

    return res.rows.map((row) => ({
      id: row.id,
      peerId: row.peerId,
      peerDisplayName: row.peerDisplayName || 'Tashan User',
      peerHandle: row.peerHandle || '@user',
      peerAvatarUrl: row.peerAvatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80',
      peerVerified: Boolean(row.peerVerified),
      lastMessage: row.lastMessage || 'Started conversation',
      lastMessageAt: new Date(row.lastMessageAt).getTime(),
      unreadCount: parseInt(row.unreadCount || '0', 10),
    }));
  }

  async getConversation(convId: string): Promise<any | null> {
    const res = await query('SELECT * FROM direct_conversations WHERE id = $1', [convId]);
    return res.rows[0] || null;
  }

  async getOrCreateConversation(userA: string, userB: string): Promise<string> {
    const res = await query(
      `SELECT id FROM direct_conversations
       WHERE (user_one_id = $1 AND user_two_id = $2)
          OR (user_one_id = $2 AND user_two_id = $1)`,
      [userA, userB]
    );

    if (res.rows.length > 0) {
      return res.rows[0].id;
    }

    const newId = `conv_${uuidv4().replace(/-/g, '').slice(0, 16)}`;
    await query(
      `INSERT INTO direct_conversations (id, user_one_id, user_two_id, last_message_text, last_message_at)
       VALUES ($1, $2, $3, '', NOW())`,
      [newId, userA, userB]
    );
    return newId;
  }

  async getMessages(convId: string, limit: number = 100): Promise<any[]> {
    const res = await query(
      `SELECT
        dm.id,
        dm.conversation_id as "conversationId",
        dm.sender_id as "senderId",
        dm.receiver_id as "receiverId",
        dm.content,
        dm.is_read as "isRead",
        dm.created_at as "createdAt"
      FROM direct_messages dm
      WHERE dm.conversation_id = $1
      ORDER BY dm.created_at ASC
      LIMIT $2`,
      [convId, limit]
    );

    return res.rows.map((row) => ({
      id: row.id,
      conversationId: row.conversationId,
      senderId: row.senderId,
      receiverId: row.receiverId,
      text: row.content,
      isRead: Boolean(row.isRead),
      timestamp: new Date(row.createdAt).getTime(),
    }));
  }

  async createMessage(data: {
    conversationId: string;
    senderId: string;
    receiverId: string;
    content: string;
  }): Promise<any> {
    const msgId = `msg_${Date.now()}`;
    await query(
      `INSERT INTO direct_messages (id, conversation_id, sender_id, receiver_id, content, is_read)
       VALUES ($1, $2, $3, $4, $5, FALSE)`,
      [msgId, data.conversationId, data.senderId, data.receiverId, data.content]
    );

    // Update conversation last message
    await query(
      `UPDATE direct_conversations
       SET last_message_text = $1, last_message_at = NOW(), updated_at = NOW()
       WHERE id = $2`,
      [data.content, data.conversationId]
    );

    return {
      id: msgId,
      conversationId: data.conversationId,
      senderId: data.senderId,
      receiverId: data.receiverId,
      text: data.content,
      isRead: false,
      timestamp: Date.now(),
    };
  }

  async markAsRead(convId: string, readerUserId: string): Promise<void> {
    await query(
      'UPDATE direct_messages SET is_read = TRUE WHERE conversation_id = $1 AND receiver_id = $2',
      [convId, readerUserId]
    );
  }
}

export const messageRepository = new MessageRepository();
