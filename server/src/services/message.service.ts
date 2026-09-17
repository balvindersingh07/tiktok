import { messageRepository } from '../repositories/message.repository.js';
import { userRepository } from '../repositories/user.repository.js';
import { notificationRepository } from '../repositories/notification.repository.js';
import { eventBus } from '../utils/eventBus.js';
import { NotFoundError, ForbiddenError, BadRequestError } from '../utils/errors.js';

export class MessageService {
  async getConversations(userId: string) {
    return messageRepository.getConversations(userId);
  }

  async getMessages(convId: string, userId: string, limit?: number) {
    const conv = await messageRepository.getConversation(convId);
    if (!conv) {
      throw new NotFoundError('Conversation not found');
    }

    if (conv.user_one_id !== userId && conv.user_two_id !== userId) {
      throw new ForbiddenError('Not a participant in this conversation');
    }

    // Mark incoming messages as read
    await messageRepository.markAsRead(convId, userId);

    const messages = await messageRepository.getMessages(convId, limit);
    return messages.map((m) => ({
      ...m,
      isMine: m.senderId === userId,
    }));
  }

  async sendMessage(senderId: string, receiverId: string, content: string) {
    const trimmed = content.trim();
    if (!trimmed) {
      throw new BadRequestError('Message content cannot be empty');
    }

    if (senderId === receiverId) {
      throw new BadRequestError('Cannot send message to yourself');
    }

    // Check blocked status
    const isBlocked = await userRepository.isBlocked(senderId, receiverId);
    if (isBlocked) {
      throw new ForbiddenError('Unable to send message to this creator');
    }

    // Check recipient DM permissions
    const recipientProfile = await userRepository.getProfile(receiverId);
    if (recipientProfile && recipientProfile.allowDirectMessages === 'NO_ONE') {
      throw new ForbiddenError('User does not accept direct messages');
    }

    const convId = await messageRepository.getOrCreateConversation(senderId, receiverId);

    const message = await messageRepository.createMessage({
      conversationId: convId,
      senderId,
      receiverId,
      content: trimmed,
    });

    // Real-time dispatch via event bus
    eventBus.emitDirectMessage(receiverId, message);

    // Create notification
    const notif = await notificationRepository.create({
      recipientId,
      actorId: senderId,
      type: 'dm',
      actionText: `sent you a message: "${trimmed.slice(0, 30)}"`,
    });

    if (notif) {
      eventBus.emitNotification(receiverId, notif);
    }

    return {
      ...message,
      isMine: true,
    };
  }
}

export const messageService = new MessageService();
