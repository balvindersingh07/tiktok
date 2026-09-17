package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
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
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val isFollowing: Boolean = false,
    val category: String = "fyp", // "fyp", "following", "trending"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val videoId: Long,
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
    val bio: String = "✨ Visual Storyteller & Creator | Daily Reels 🚀\n📍 Los Angeles, CA | Collabs: alex@creativelab.io",
    val followingCount: Int = 184,
    val followersCount: Int = 42900,
    val likesCount: String = "1.2M",
    val avatarUrl: String = ""
)
