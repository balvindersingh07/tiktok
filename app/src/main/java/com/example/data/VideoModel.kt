package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val authorId: String = "me",
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String = "",
    val caption: String,
    val soundTitle: String,
    val soundAuthor: String,
    val coverResName: String = "video_cover_dance",
    val videoUrl: String = "",
    val likesCount: Long = 0,
    val commentsCount: Long = 0,
    val sharesCount: Long = 0,
    val bookmarksCount: Long = 0,
    val viewsCount: Long = 0,
    val repostCount: Long = 0,
    val durationSeconds: Int = 15,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val isFollowing: Boolean = false,
    val isReposted: Boolean = false,
    val isPrivate: Boolean = false,
    val allowComments: Boolean = true,
    val allowDuet: Boolean = true,
    val allowStitch: Boolean = true,
    val hashtags: String = "",
    val category: String = "fyp", // "fyp", "following", "trending", "dance", "tech", "food"
    val duetWithVideoId: Long? = null,
    val stitchWithVideoId: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val videoId: Long,
    val parentCommentId: Long? = null,
    val authorId: String = "me",
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String = "",
    val content: String,
    val likesCount: Long = 0,
    val isLiked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val userId: String = "me",
    val displayName: String = "Alex Rivera",
    val handle: String = "@alex_creative",
    val passwordPin: String = "1234",
    val bio: String = "✨ Visual Storyteller & Creator | Daily Reels 🚀\n📍 Los Angeles, CA | Collabs: alex@creativelab.io",
    val followingCount: Int = 184,
    val followersCount: Int = 42900,
    val likesCount: String = "1.2M",
    val avatarUrl: String = "",
    val isVerified: Boolean = true,
    val isPrivate: Boolean = false,
    val allowComments: Boolean = true,
    val allowDuet: Boolean = true,
    val allowStitch: Boolean = true,
    val allowDownloads: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "active_session")
data class ActiveSessionEntity(
    @PrimaryKey
    val sessionKey: String = "current",
    val activeUserId: String = "me",
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "drafts")
data class DraftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val authorId: String = "me",
    val caption: String = "",
    val soundTitle: String = "Original Sound",
    val soundAuthor: String = "Local Audio",
    val coverResName: String = "video_cover_dance",
    val localVideoPath: String = "",
    val durationSeconds: Int = 15,
    val isPrivate: Boolean = false,
    val allowComments: Boolean = true,
    val allowDuet: Boolean = true,
    val allowStitch: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "follows")
data class FollowEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val followerHandle: String,
    val followingHandle: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "direct_messages")
data class DirectMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: String,
    val senderHandle: String,
    val senderName: String,
    val receiverHandle: String,
    val messageText: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "blocked_users")
data class BlockedUserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userHandle: String,
    val blockedHandle: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "muted_users")
data class MutedUserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userHandle: String,
    val mutedHandle: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actorName: String,
    val actorHandle: String,
    val actionText: String,
    val type: String = "like", // "like", "comment", "follow", "repost", "mention", "reply", "system"
    val targetVideoId: Long? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sounds")
data class SoundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val durationSeconds: Int = 15,
    val usageCount: Long = 0,
    val isFavorite: Boolean = false,
    val category: String = "trending",
    val audioUrl: String = ""
)

@Entity(tableName = "analytics_events")
data class AnalyticsEventEntity(
    @PrimaryKey(autoGenerate = true)
    val eventId: Long = 0,
    val eventType: String, // "video_view", "video_like", "video_unlike", "video_share", "video_save", "video_skip", "video_complete", "comment_add", "user_follow", "user_unfollow", "search", "profile_visit"
    val targetId: Long,
    val categoryTag: String = "",
    val watchDurationMs: Long = 0,
    val completionPercent: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

data class LocalBackendStats(
    val totalVideos: Int = 0,
    val totalComments: Int = 0,
    val totalLikes: Long = 0,
    val totalNotifications: Int = 0,
    val totalSounds: Int = 0,
    val totalUsers: Int = 1,
    val totalDrafts: Int = 0,
    val totalMessages: Int = 0,
    val totalAnalyticsEvents: Int = 0,
    val storageUsageKb: Long = 0,
    val dbEngine: String = "Room SQLite (Local Host)",
    val status: String = "ONLINE (Healthy)"
)

data class VideoUploadRequest(
    val caption: String,
    val soundTitle: String,
    val soundAuthor: String,
    val coverResName: String,
    val videoFilePath: String = "",
    val category: String = "fyp",
    val isPrivate: Boolean = false,
    val allowComments: Boolean = true,
    val allowDuet: Boolean = true,
    val allowStitch: Boolean = true,
    val duetWithVideoId: Long? = null,
    val stitchWithVideoId: Long? = null
)

sealed class LocalApiResponse<out T> {
    data class Success<out T>(val data: T, val statusCode: Int = 200, val message: String = "OK") : LocalApiResponse<T>()
    data class Error(val message: String, val statusCode: Int = 500) : LocalApiResponse<Nothing>()
}
