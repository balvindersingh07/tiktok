import { v4 as uuidv4 } from 'uuid';
import { commentRepository } from '../repositories/comment.repository.js';
import { videoRepository } from '../repositories/video.repository.js';
import { userRepository } from '../repositories/user.repository.js';
import { notificationRepository } from '../repositories/notification.repository.js';
import { eventBus } from '../utils/eventBus.js';
import { NotFoundError, ForbiddenError, BadRequestError } from '../utils/errors.js';

export class CommentService {
  async addComment(
    userId: string,
    videoId: string | number,
    content: string,
    parentCommentId?: string
  ) {
    const stringVideoId = String(videoId);
    const video = await videoRepository.findById(stringVideoId);
    if (!video) {
      throw new NotFoundError('Video not found');
    }

    // Check if video allows comments
    if (video.allowComments === false) {
      throw new ForbiddenError('Comments are disabled for this video');
    }

    // Check author's profile settings
    const authorProfile = await userRepository.getProfile(video.authorId);
    if (authorProfile && authorProfile.allowComments === 'NO_ONE') {
      throw new ForbiddenError('Creator has turned off comments');
    }

    // Check if blocked
    const isBlocked = await userRepository.isBlocked(userId, video.authorId);
    if (isBlocked) {
      throw new ForbiddenError('Unable to comment on this video');
    }

    const commentId = `cmt_${uuidv4().replace(/-/g, '').slice(0, 16)}`;
    const comment = await commentRepository.create({
      id: commentId,
      videoId: stringVideoId,
      authorId: userId,
      content: content.trim(),
      parentCommentId: parentCommentId || null,
    });

    // Notify video author or parent comment author
    let notifyTargetUserId = video.authorId;
    let notifType: 'comment' | 'reply' = 'comment';
    let actionText = `commented: "${content.slice(0, 30)}"`;

    if (parentCommentId) {
      const parent = await commentRepository.findById(parentCommentId);
      if (parent && parent.author_id !== userId) {
        notifyTargetUserId = parent.author_id;
        notifType = 'reply';
        actionText = `replied to your comment: "${content.slice(0, 30)}"`;
      }
    }

    if (notifyTargetUserId !== userId) {
      const notif = await notificationRepository.create({
        recipientId: notifyTargetUserId,
        actorId: userId,
        type: notifType,
        actionText,
        targetVideoId: stringVideoId,
        targetCommentId: commentId,
      });

      if (notif) {
        eventBus.emitNotification(notifyTargetUserId, notif);
      }
    }

    const userProfile = await userRepository.getProfile(userId);

    return {
      id: isNaN(Number(comment.id)) ? comment.id : Number(comment.id),
      videoId: stringVideoId,
      authorId: userId,
      authorName: userProfile?.displayName || 'Tashan User',
      authorHandle: userProfile?.handle || '@user',
      authorAvatarUrl: userProfile?.avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80',
      authorVerified: Boolean(userProfile?.isVerified),
      content: comment.content,
      likesCount: 0,
      repliesCount: 0,
      parentCommentId: comment.parent_comment_id,
      isLiked: false,
      timestamp: Date.now(),
    };
  }

  async getComments(videoId: string | number, viewerId: string = 'anonymous') {
    return commentRepository.findByVideoId(String(videoId), viewerId);
  }

  async toggleLike(userId: string, commentId: string | number) {
    const stringId = String(commentId);
    return commentRepository.like(userId, stringId);
  }

  async deleteComment(userId: string, commentId: string | number) {
    const stringId = String(commentId);
    const deleted = await commentRepository.delete(stringId, userId);
    if (!deleted) {
      throw new ForbiddenError('Not authorized to delete this comment');
    }
    return { success: true };
  }
}

export const commentService = new CommentService();
