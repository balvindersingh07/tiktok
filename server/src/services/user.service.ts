import { userRepository } from '../repositories/user.repository.js';
import { notificationRepository } from '../repositories/notification.repository.js';
import { eventBus } from '../utils/eventBus.js';
import { NotFoundError, BadRequestError } from '../utils/errors.js';

export class UserService {
  async getProfile(handleOrId: string, viewerId: string = 'anonymous') {
    const profile = await userRepository.getProfile(handleOrId, viewerId);
    if (!profile) {
      throw new NotFoundError('Creator profile not found');
    }
    return profile;
  }

  async updateProfile(userId: string, data: Record<string, any>) {
    return userRepository.updateProfile(userId, data);
  }

  async listCreators(limit: number = 30, viewerId: string = 'anonymous') {
    return userRepository.listCreators(limit, viewerId);
  }

  async toggleFollow(followerId: string, targetHandleOrId: string) {
    const target = await userRepository.getProfile(targetHandleOrId);
    if (!target) {
      throw new NotFoundError('Target creator not found');
    }

    if (target.userId === followerId) {
      throw new BadRequestError('You cannot follow yourself');
    }

    const isCurrentlyFollowing = await userRepository.isFollowing(followerId, target.userId);

    if (isCurrentlyFollowing) {
      await userRepository.unfollow(followerId, target.userId);
      const updated = await userRepository.getProfile(target.userId);
      return { isFollowing: false, followersCount: updated?.followersCount || 0 };
    } else {
      await userRepository.follow(followerId, target.userId);

      // Create notification
      const notif = await notificationRepository.create({
        recipientId: target.userId,
        actorId: followerId,
        type: 'follow',
        actionText: 'started following you',
      });

      if (notif) {
        eventBus.emitNotification(target.userId, notif);
      }

      const updated = await userRepository.getProfile(target.userId);
      return { isFollowing: true, followersCount: updated?.followersCount || 0 };
    }
  }

  async toggleBlock(userId: string, targetHandleOrId: string) {
    const target = await userRepository.getProfile(targetHandleOrId);
    if (!target) {
      throw new NotFoundError('Target creator not found');
    }

    if (target.userId === userId) {
      throw new BadRequestError('You cannot block yourself');
    }

    const isBlocked = await userRepository.isBlocked(userId, target.userId);
    if (isBlocked) {
      await userRepository.unblock(userId, target.userId);
      return { isBlocked: false };
    } else {
      await userRepository.block(userId, target.userId);
      return { isBlocked: true };
    }
  }

  async toggleMute(userId: string, targetHandleOrId: string) {
    const target = await userRepository.getProfile(targetHandleOrId);
    if (!target) {
      throw new NotFoundError('Target creator not found');
    }

    const isMuted = await userRepository.isMuted(userId, target.userId);
    if (isMuted) {
      await userRepository.unmute(userId, target.userId);
      return { isMuted: false };
    } else {
      await userRepository.mute(userId, target.userId);
      return { isMuted: true };
    }
  }

  async getFollowers(userId: string, limit?: number) {
    return userRepository.getFollowers(userId, limit);
  }

  async getFollowing(userId: string, limit?: number) {
    return userRepository.getFollowing(userId, limit);
  }

  async getBlockedUsers(userId: string) {
    return userRepository.getBlockedUsers(userId);
  }

  async getMutedUsers(userId: string) {
    return userRepository.getMutedUsers(userId);
  }

  async registerDeviceToken(userId: string, token: string, platform?: string) {
    return userRepository.registerDeviceToken(userId, token, platform);
  }

  async removeDeviceToken(userId: string, token: string) {
    return userRepository.removeDeviceToken(userId, token);
  }
}

export const userService = new UserService();
