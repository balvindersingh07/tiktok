import { z } from 'zod';

// Authentication Schemas
export const signupSchema = z.object({
  displayName: z.string().min(1).max(50).optional(),
  handle: z.string().min(2).max(30).regex(/^@?[a-zA-Z0-9_]+$/, 'Invalid handle format'),
  email: z.string().email().optional(),
  password: z.string().min(4).max(100).optional(),
  passwordPin: z.string().min(4).max(100).optional(),
  bio: z.string().max(250).optional(),
}).refine((data) => Boolean(data.password || data.passwordPin), {
  message: 'Password or PIN is required',
  path: ['password'],
});

export const loginSchema = z.object({
  handle: z.string().min(2).optional(),
  handleOrEmail: z.string().min(2).optional(),
  password: z.string().min(4).optional(),
  passwordPin: z.string().min(4).optional(),
}).refine((data) => Boolean(data.handle || data.handleOrEmail), {
  message: 'Handle or Email is required',
  path: ['handle'],
}).refine((data) => Boolean(data.password || data.passwordPin), {
  message: 'Password or PIN is required',
  path: ['password'],
});

export const changePinSchema = z.object({
  oldPin: z.string().min(4),
  newPin: z.string().min(4),
});

// Profile Schemas
export const updateProfileSchema = z.object({
  displayName: z.string().min(1).max(50).optional(),
  bio: z.string().max(300).optional(),
  avatarUrl: z.string().optional(),
  coverUrl: z.string().optional(),
  isPrivate: z.boolean().optional(),
  allowComments: z.union([z.boolean(), z.enum(['EVERYONE', 'FRIENDS', 'NO_ONE'])]).optional(),
  allowDirectMessages: z.enum(['EVERYONE', 'FRIENDS', 'NO_ONE']).optional(),
  allowDuet: z.union([z.boolean(), z.enum(['EVERYONE', 'FRIENDS', 'NO_ONE'])]).optional(),
  allowStitch: z.union([z.boolean(), z.enum(['EVERYONE', 'FRIENDS', 'NO_ONE'])]).optional(),
  allowDownloads: z.boolean().optional(),
  showLikedVideos: z.boolean().optional(),
  showFollowingList: z.boolean().optional(),
});

// Video Schemas
export const createVideoSchema = z.object({
  caption: z.string().max(1000).default(''),
  soundTitle: z.string().max(100).default('Original Audio'),
  soundAuthor: z.string().max(100).default('Unknown Artist'),
  soundId: z.string().optional(),
  coverResName: z.string().optional(),
  category: z.string().default('fyp'),
  hashtags: z.string().optional(),
  videoPath: z.string().optional(),
  durationSeconds: z.number().int().positive().default(15),
  isPrivate: z.boolean().default(false),
  allowComments: z.boolean().default(true),
  allowDuet: z.boolean().default(true),
  allowStitch: z.boolean().default(true),
  duetWithVideoId: z.string().optional(),
  stitchWithVideoId: z.string().optional(),
});

export const videoQuerySchema = z.object({
  category: z.string().optional(),
  cursor: z.string().optional(),
  limit: z.coerce.number().int().min(1).max(50).default(10),
  authorId: z.string().optional(),
});

// Comment Schemas
export const createCommentSchema = z.object({
  content: z.string().min(1).max(500),
  parentCommentId: z.string().optional(),
});

// Sound Schemas
export const createSoundSchema = z.object({
  title: z.string().min(1).max(100),
  author: z.string().min(1).max(100),
  durationSeconds: z.number().int().positive().default(15),
  category: z.string().default('trending'),
  audioUrl: z.string().optional(),
});

// Direct Message Schemas
export const sendMessageSchema = z.object({
  receiverId: z.string().min(1),
  content: z.string().min(1).max(2000),
});

// Notification Preferences Schema
export const notificationPreferencesSchema = z.object({
  likesEnabled: z.boolean().optional(),
  commentsEnabled: z.boolean().optional(),
  followsEnabled: z.boolean().optional(),
  mentionsEnabled: z.boolean().optional(),
  dmsEnabled: z.boolean().optional(),
  repostsEnabled: z.boolean().optional(),
  systemEnabled: z.boolean().optional(),
});

// Device Token Schema
export const registerDeviceTokenSchema = z.object({
  token: z.string().min(10),
  platform: z.enum(['ANDROID', 'IOS', 'WEB']).default('ANDROID'),
});

// Reports Schema
export const createReportSchema = z.object({
  targetType: z.enum(['VIDEO', 'COMMENT', 'USER']),
  targetId: z.string().min(1),
  reason: z.string().min(2).max(128),
  details: z.string().max(1000).optional(),
});

// Analytics Event Schema
export const analyticsEventSchema = z.object({
  eventType: z.string().min(1),
  targetId: z.string().optional().nullable(),
  targetType: z.enum(['video', 'profile', 'sound', 'search', 'system', 'comment']).default('video'),
  categoryTag: z.string().optional(),
  watchDurationMs: z.number().nonnegative().optional(),
  completionPercent: z.number().min(0).max(100).optional(),
  metadata: z.record(z.any()).optional(),
});

