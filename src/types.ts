export type MainTab = 'HOME' | 'DISCOVER' | 'CREATE' | 'INBOX' | 'PROFILE';

export type FeedCategory = 'FOLLOWING' | 'FOR_YOU';

export type ProfileSubTab = 'MY_VIDEOS' | 'LIKED_VIDEOS' | 'BOOKMARKED' | 'PRIVATE' | 'DRAFTS';

export interface VideoEntity {
  id: string | number;
  authorId: string;
  authorName: string;
  authorHandle: string;
  authorAvatarUrl?: string;
  caption: string;
  soundTitle: string;
  soundAuthor: string;
  coverResName: string;
  videoUrl?: string;
  likesCount: number;
  commentsCount: number;
  sharesCount: number;
  bookmarksCount: number;
  viewsCount: number;
  repostCount?: number;
  durationSeconds: number;
  isLiked: boolean;
  isBookmarked: boolean;
  isFollowing: boolean;
  isReposted?: boolean;
  isPrivate: boolean;
  allowComments: boolean;
  allowDuet: boolean;
  allowStitch: boolean;
  hashtags: string;
  category: string; // 'fyp', 'following', 'trending', 'dance', 'tech', 'food'
  duetWithVideoId?: string | number | null;
  stitchWithVideoId?: string | number | null;
  timestamp: number;
}

export interface CommentEntity {
  id: string | number;
  videoId: string | number;
  parentCommentId?: string | number | null;
  authorId: string;
  authorName: string;
  authorHandle: string;
  authorAvatarUrl?: string;
  content: string;
  likesCount: number;
  isLiked: boolean;
  timestamp: number;
}

export interface UserProfileEntity {
  userId: string;
  displayName: string;
  handle: string;
  passwordPin: string;
  bio: string;
  followingCount: number;
  followersCount: number;
  likesCount: string;
  avatarUrl: string;
  isVerified: boolean;
  isPrivate: boolean;
  allowComments: boolean;
  allowDuet: boolean;
  allowStitch: boolean;
  allowDownloads: boolean;
  createdAt: number;
}

export interface ActiveSessionEntity {
  sessionKey: string;
  activeUserId: string;
  lastActiveTimestamp: number;
}

export interface DraftEntity {
  id: string | number;
  authorId: string;
  caption: string;
  soundTitle: string;
  soundAuthor: string;
  coverResName: string;
  localVideoPath: string;
  durationSeconds: number;
  isPrivate: boolean;
  allowComments: boolean;
  allowDuet: boolean;
  allowStitch: boolean;
  updatedAt: number;
}

export interface FollowEntity {
  id: number;
  followerHandle: string;
  followingHandle: string;
  timestamp: number;
}

export interface DirectMessageEntity {
  id: number;
  conversationId: string;
  senderHandle: string;
  senderName: string;
  receiverHandle: string;
  messageText: string;
  isRead: boolean;
  timestamp: number;
}

export interface SearchHistoryEntity {
  id: number;
  query: string;
  timestamp: number;
}

export interface BlockedUserEntity {
  id: number;
  userHandle: string;
  blockedHandle: string;
  timestamp: number;
}

export interface MutedUserEntity {
  id: number;
  userHandle: string;
  mutedHandle: string;
  timestamp: number;
}

export interface NotificationEntity {
  id: string | number;
  actorName: string;
  actorHandle: string;
  actionText: string;
  type: 'like' | 'comment' | 'follow' | 'repost' | 'mention' | 'reply' | 'system';
  targetVideoId?: string | number | null;
  isRead: boolean;
  timestamp: number;
}

export interface SoundEntity {
  id: number;
  title: string;
  author: string;
  durationSeconds: number;
  usageCount: number;
  isFavorite: boolean;
  category: string;
  audioUrl?: string;
}

export interface StorageUsageBreakdown {
  videoCacheBytes: number;
  thumbnailCacheBytes: number;
  temporaryBytes: number;
  totalStorageBytes: number;
}

export interface LocalBackendStats {
  totalVideos: number;
  totalComments: number;
  totalLikes: number;
  totalNotifications: number;
  totalSounds: number;
  totalUsers: number;
  totalDrafts: number;
  totalMessages: number;
  totalAnalyticsEvents: number;
  storageUsageKb: number;
  dbEngine: string;
  status: string;
}

export interface TashanPreferencesState {
  isPrivateAccount: boolean;
  allowComments: boolean;
  allowDirectMessages: string;
  allowDownloads: boolean;
  allowDuet: boolean;
  allowStitch: boolean;
  activityStatus: boolean;
  followingListVisibility: string;
  likedVideosVisibility: string;

  masterNotifications: boolean;
  notifyLikes: boolean;
  notifyComments: boolean;
  notifyReplies: boolean;
  notifyNewFollowers: boolean;
  notifyReposts: boolean;
  notifyMentions: boolean;
  notifyDirectMessages: boolean;

  autoplay: boolean;
  loopVideos: boolean;
  muteByDefault: boolean;
  dataSaver: boolean;

  themeMode: string;
  reduceMotion: boolean;
  animationScale: string;
  textSizePreference: string;

  appLockEnabled: boolean;
  biometricLockEnabled: boolean;
}
