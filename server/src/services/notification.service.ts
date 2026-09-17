import { notificationRepository } from '../repositories/notification.repository.js';

export class NotificationService {
  async getNotifications(userId: string, limit?: number) {
    return notificationRepository.findByRecipient(userId, limit);
  }

  async getUnreadCount(userId: string) {
    return notificationRepository.getUnreadCount(userId);
  }

  async markAsRead(notifId: string, userId: string) {
    return notificationRepository.markAsRead(notifId, userId);
  }

  async markAllAsRead(userId: string) {
    return notificationRepository.markAllAsRead(userId);
  }

  async getPreferences(userId: string) {
    return notificationRepository.getPreferences(userId);
  }

  async updatePreferences(userId: string, prefs: Record<string, boolean>) {
    return notificationRepository.updatePreferences(userId, prefs);
  }
}

export const notificationService = new NotificationService();
