package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TikTokDao {

    // ------------------------------------------------------------------------
    // User Profiles & Auth Accounts
    // ------------------------------------------------------------------------
    @Query("SELECT * FROM user_profile ORDER BY createdAt ASC")
    fun getAllUserProfiles(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    fun getUserProfile(userId: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfileOnce(userId: String): UserProfileEntity?

    @Query("SELECT * FROM user_profile WHERE handle = :handle LIMIT 1")
    suspend fun getProfileByHandle(handle: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profile WHERE userId = :userId")
    suspend fun deleteUserProfile(userId: String)

    // Active Session
    @Query("SELECT * FROM active_session WHERE sessionKey = 'current' LIMIT 1")
    fun getActiveSession(): Flow<ActiveSessionEntity?>

    @Query("SELECT * FROM active_session WHERE sessionKey = 'current' LIMIT 1")
    suspend fun getActiveSessionOnce(): ActiveSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setActiveSession(session: ActiveSessionEntity)

    @Query("DELETE FROM active_session WHERE sessionKey = 'current'")
    suspend fun clearActiveSession()

    // ------------------------------------------------------------------------
    // Follows & Social Graph
    // ------------------------------------------------------------------------
    @Query("SELECT COUNT(*) FROM follows WHERE followerHandle = :follower AND followingHandle = :following")
    suspend fun isFollowing(follower: String, following: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollow(follow: FollowEntity): Long

    @Query("DELETE FROM follows WHERE followerHandle = :follower AND followingHandle = :following")
    suspend fun deleteFollow(follower: String, following: String)

    @Query("SELECT * FROM follows WHERE followerHandle = :handle")
    fun getFollowingForUser(handle: String): Flow<List<FollowEntity>>

    @Query("SELECT * FROM follows WHERE followingHandle = :handle")
    fun getFollowersForUser(handle: String): Flow<List<FollowEntity>>

    @Query("SELECT COUNT(*) FROM follows WHERE followerHandle = :handle")
    suspend fun getFollowingCount(handle: String): Int

    @Query("SELECT COUNT(*) FROM follows WHERE followingHandle = :handle")
    suspend fun getFollowersCount(handle: String): Int

    // ------------------------------------------------------------------------
    // Videos
    // ------------------------------------------------------------------------
    @Query("SELECT * FROM videos WHERE isPrivate = 0 ORDER BY id ASC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos")
    suspend fun getAllVideosList(): List<VideoEntity>

    @Query("SELECT * FROM videos WHERE category = :category AND isPrivate = 0 ORDER BY id ASC")
    fun getVideosByCategory(category: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isLiked = 1 ORDER BY timestamp DESC")
    fun getLikedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE authorHandle = :handle AND isPrivate = 0 ORDER BY timestamp DESC")
    fun getUserVideos(handle: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE authorHandle = :handle AND isPrivate = 1 ORDER BY timestamp DESC")
    fun getUserPrivateVideos(handle: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: Long): VideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity): Long

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideoById(id: Long)

    @Query("UPDATE videos SET isLiked = :liked, likesCount = MAX(0, likesCount + :delta) WHERE id = :id")
    suspend fun toggleLike(id: Long, liked: Boolean, delta: Int)

    @Query("UPDATE videos SET isBookmarked = :bookmarked, bookmarksCount = MAX(0, bookmarksCount + :delta) WHERE id = :id")
    suspend fun toggleBookmark(id: Long, bookmarked: Boolean, delta: Int)

    @Query("UPDATE videos SET isReposted = :reposted, repostCount = MAX(0, repostCount + :delta) WHERE id = :id")
    suspend fun toggleRepost(id: Long, reposted: Boolean, delta: Int)

    @Query("UPDATE videos SET sharesCount = sharesCount + 1 WHERE id = :id")
    suspend fun incrementShare(id: Long)

    @Query("UPDATE videos SET viewsCount = viewsCount + 1 WHERE id = :id")
    suspend fun incrementView(id: Long)

    @Query("UPDATE videos SET isFollowing = :following WHERE authorHandle = :authorHandle")
    suspend fun toggleFollow(authorHandle: String, following: Boolean)

    @Query("DELETE FROM videos")
    suspend fun clearVideos()

    // ------------------------------------------------------------------------
    // Comments & Replies
    // ------------------------------------------------------------------------
    @Query("SELECT * FROM comments WHERE videoId = :videoId AND parentCommentId IS NULL ORDER BY timestamp DESC")
    fun getCommentsForVideo(videoId: Long): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments WHERE parentCommentId = :parentId ORDER BY timestamp ASC")
    fun getRepliesForComment(parentId: Long): Flow<List<CommentEntity>>

    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY timestamp DESC")
    fun getAllCommentsAndReplies(videoId: Long): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    @Query("UPDATE comments SET content = :newContent WHERE id = :commentId")
    suspend fun updateCommentContent(commentId: Long, newContent: String)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: Long)

    @Query("UPDATE comments SET isLiked = :liked, likesCount = MAX(0, likesCount + :delta) WHERE id = :commentId")
    suspend fun toggleCommentLike(commentId: Long, liked: Boolean, delta: Int)

    @Query("UPDATE videos SET commentsCount = commentsCount + 1 WHERE id = :videoId")
    suspend fun incrementCommentCount(videoId: Long)

    @Query("UPDATE videos SET commentsCount = MAX(0, commentsCount - 1) WHERE id = :videoId")
    suspend fun decrementCommentCount(videoId: Long)

    @Query("DELETE FROM comments")
    suspend fun clearComments()

    // ------------------------------------------------------------------------
    // Drafts
    // ------------------------------------------------------------------------
    @Query("SELECT * FROM drafts WHERE authorId = :authorId ORDER BY updatedAt DESC")
    fun getDrafts(authorId: String): Flow<List<DraftEntity>>

    @Query("SELECT * FROM drafts WHERE id = :id LIMIT 1")
    suspend fun getDraftById(id: Long): DraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: DraftEntity): Long

    @Query("DELETE FROM drafts WHERE id = :id")
    suspend fun deleteDraft(id: Long)

    // ------------------------------------------------------------------------
    // Direct Messaging (Chat)
    // ------------------------------------------------------------------------
    @Query("SELECT * FROM direct_messages WHERE senderHandle = :handle OR receiverHandle = :handle ORDER BY timestamp DESC")
    fun getAllMessagesForUser(handle: String): Flow<List<DirectMessageEntity>>

    @Query("SELECT * FROM direct_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<DirectMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: DirectMessageEntity): Long

    @Query("UPDATE direct_messages SET isRead = 1 WHERE conversationId = :convId AND receiverHandle = :myHandle")
    suspend fun markMessagesAsRead(convId: String, myHandle: String)

    @Query("DELETE FROM direct_messages WHERE conversationId = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    // ------------------------------------------------------------------------
    // Search History & Search Queries
    // ------------------------------------------------------------------------
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 20")
    fun getSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(search: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearchQuery(id: Long)

    @Query("SELECT * FROM videos WHERE caption LIKE '%' || :query || '%' OR authorHandle LIKE '%' || :query || '%' OR authorName LIKE '%' || :query || '%' OR soundTitle LIKE '%' || :query || '%'")
    suspend fun searchVideos(query: String): List<VideoEntity>

    @Query("SELECT * FROM user_profile WHERE handle LIKE '%' || :query || '%' OR displayName LIKE '%' || :query || '%'")
    suspend fun searchUsers(query: String): List<UserProfileEntity>

    // ------------------------------------------------------------------------
    // Blocked & Muted Users
    // ------------------------------------------------------------------------
    @Query("SELECT * FROM blocked_users WHERE userHandle = :userHandle")
    fun getBlockedUsers(userHandle: String): Flow<List<BlockedUserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun blockUser(blocked: BlockedUserEntity): Long

    @Query("DELETE FROM blocked_users WHERE userHandle = :userHandle AND blockedHandle = :blockedHandle")
    suspend fun unblockUser(userHandle: String, blockedHandle: String)

    @Query("SELECT COUNT(*) FROM blocked_users WHERE userHandle = :userHandle AND blockedHandle = :blockedHandle")
    suspend fun isUserBlocked(userHandle: String, blockedHandle: String): Int

    @Query("SELECT * FROM muted_users WHERE userHandle = :userHandle")
    fun getMutedUsers(userHandle: String): Flow<List<MutedUserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun muteUser(muted: MutedUserEntity): Long

    @Query("DELETE FROM muted_users WHERE userHandle = :userHandle AND mutedHandle = :mutedHandle")
    suspend fun unmuteUser(userHandle: String, mutedHandle: String)

    @Query("SELECT COUNT(*) FROM muted_users WHERE userHandle = :userHandle AND mutedHandle = :mutedHandle")
    suspend fun isUserMuted(userHandle: String, mutedHandle: String): Int

    // ------------------------------------------------------------------------
    // Notifications
    // ------------------------------------------------------------------------
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearNotifications()

    // ------------------------------------------------------------------------
    // Sounds
    // ------------------------------------------------------------------------
    @Query("SELECT * FROM sounds ORDER BY usageCount DESC")
    fun getAllSounds(): Flow<List<SoundEntity>>

    @Query("SELECT * FROM sounds WHERE id = :id LIMIT 1")
    suspend fun getSoundById(id: Long): SoundEntity?

    @Query("SELECT * FROM sounds WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'")
    suspend fun searchSounds(query: String): List<SoundEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSounds(sounds: List<SoundEntity>)

    @Query("UPDATE sounds SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavoriteSound(id: Long)

    @Query("UPDATE sounds SET usageCount = usageCount + 1 WHERE title = :title")
    suspend fun incrementSoundUsage(title: String)

    @Query("SELECT * FROM sounds WHERE usageCount > 0 ORDER BY usageCount DESC")
    fun getRecentlyUsedSounds(): Flow<List<SoundEntity>>

    @Query("UPDATE sounds SET usageCount = 0 WHERE id = :id")
    suspend fun clearSoundUsage(id: Long)

    @Query("UPDATE sounds SET usageCount = 0")
    suspend fun clearAllSoundUsage()

    // ------------------------------------------------------------------------
    // Watch History
    // ------------------------------------------------------------------------
    @Query("SELECT v.* FROM videos v INNER JOIN analytics_events a ON v.id = a.targetId WHERE a.eventType IN ('video_view', 'video_complete') GROUP BY v.id ORDER BY MAX(a.timestamp) DESC LIMIT 50")
    fun getWatchHistory(): Flow<List<VideoEntity>>

    @Query("DELETE FROM analytics_events WHERE eventType IN ('video_view', 'video_complete') AND targetId = :videoId")
    suspend fun deleteWatchHistoryItem(videoId: Long)

    @Query("DELETE FROM analytics_events WHERE eventType IN ('video_view', 'video_complete')")
    suspend fun clearWatchHistory()

    // ------------------------------------------------------------------------
    // Analytics & Recommendations
    // ------------------------------------------------------------------------
    @Query("DELETE FROM analytics_events WHERE eventType IN ('video_like', 'video_save', 'video_share', 'video_complete', 'comment_add')")
    suspend fun resetRecommendationEvents()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun logAnalyticsEvent(event: AnalyticsEventEntity)

    @Query("SELECT * FROM analytics_events ORDER BY timestamp DESC LIMIT 200")
    suspend fun getRecentAnalyticsEvents(): List<AnalyticsEventEntity>

    // ------------------------------------------------------------------------
    // Database Metrics
    // ------------------------------------------------------------------------
    @Query("SELECT COUNT(*) FROM videos")
    suspend fun getVideoCount(): Int

    @Query("SELECT COUNT(*) FROM comments")
    suspend fun getCommentCount(): Int

    @Query("SELECT COALESCE(SUM(likesCount), 0) FROM videos")
    suspend fun getTotalVideoLikes(): Long

    @Query("SELECT COUNT(*) FROM notifications")
    suspend fun getNotificationCount(): Int

    @Query("SELECT COUNT(*) FROM sounds")
    suspend fun getSoundCount(): Int

    @Query("SELECT COUNT(*) FROM user_profile")
    suspend fun getUserProfileCount(): Int

    @Query("SELECT COUNT(*) FROM drafts")
    suspend fun getDraftCount(): Int

    @Query("SELECT COUNT(*) FROM direct_messages")
    suspend fun getDirectMessageCount(): Int

    @Query("SELECT COUNT(*) FROM analytics_events")
    suspend fun getAnalyticsEventCount(): Int
}
