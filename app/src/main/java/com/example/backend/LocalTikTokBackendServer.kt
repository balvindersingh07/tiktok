package com.example.backend

import android.content.Context
import com.example.data.ActiveSessionEntity
import com.example.data.AnalyticsEventEntity
import com.example.data.BlockedUserEntity
import com.example.data.CommentEntity
import com.example.data.DirectMessageEntity
import com.example.data.DraftEntity
import com.example.data.FollowEntity
import com.example.data.LocalApiResponse
import com.example.data.LocalBackendStats
import com.example.data.MutedUserEntity
import com.example.data.NotificationEntity
import com.example.data.SearchHistoryEntity
import com.example.data.SoundEntity
import com.example.data.TashanPreferences
import com.example.data.TikTokDao
import com.example.data.TikTokDatabase
import com.example.data.UserProfileEntity
import com.example.data.VideoEntity
import com.example.data.VideoUploadRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Local TikTok Backend Server
 * Complete embedded server & RESTful API controller running on device.
 * Powered by SQLite/Room, local media storage, and an on-device recommendation algorithm.
 */
class LocalTikTokBackendServer(private val context: Context) {

    private val db = TikTokDatabase.getDatabase(context)
    val dao: TikTokDao = db.tikTokDao()
    val mediaStorage = LocalMediaStorage(context)
    val preferences = TashanPreferences(context)

    suspend fun initializeBackend() = withContext(Dispatchers.IO) {
        val count = dao.getVideoCount()
        if (count == 0) {
            seedInitialDatabase()
        }
        // Ensure active session exists
        val currentSession = dao.getActiveSessionOnce()
        if (currentSession == null) {
            dao.setActiveSession(ActiveSessionEntity(activeUserId = "me"))
        }
    }

    // ------------------------------------------------------------------------
    // Authentication & Local Accounts
    // ------------------------------------------------------------------------

    fun getAllUserProfiles(): Flow<List<UserProfileEntity>> = dao.getAllUserProfiles()

    fun getActiveSession(): Flow<ActiveSessionEntity?> = dao.getActiveSession()

    fun getUserProfile(userId: String): Flow<UserProfileEntity?> = dao.getUserProfile(userId)

    suspend fun getActiveUser(): UserProfileEntity = withContext(Dispatchers.IO) {
        val session = dao.getActiveSessionOnce()
        val userId = session?.activeUserId ?: "me"
        dao.getUserProfileOnce(userId) ?: UserProfileEntity(userId = userId)
    }

    suspend fun signup(displayName: String, handle: String, pin: String): LocalApiResponse<UserProfileEntity> = withContext(Dispatchers.IO) {
        val cleanHandle = if (handle.startsWith("@")) handle else "@$handle"
        val existing = dao.getProfileByHandle(cleanHandle)
        if (existing != null) {
            return@withContext LocalApiResponse.Error("Username already taken", statusCode = 409)
        }

        val newId = "user_${System.currentTimeMillis()}"
        val newProfile = UserProfileEntity(
            userId = newId,
            displayName = displayName.ifBlank { "New Creator" },
            handle = cleanHandle,
            passwordPin = pin.ifBlank { "1234" },
            bio = "👋 Hey! I'm on TikTok Local.",
            followingCount = 0,
            followersCount = 0,
            likesCount = "0",
            isVerified = false,
            createdAt = System.currentTimeMillis()
        )
        dao.insertUserProfile(newProfile)
        dao.setActiveSession(ActiveSessionEntity(activeUserId = newId))

        // Create welcome notification
        dao.insertNotification(
            NotificationEntity(
                actorName = "TikTok Team",
                actorHandle = "@tiktok",
                actionText = "Welcome to your local TikTok experience! 🚀",
                type = "system"
            )
        )

        LocalApiResponse.Success(newProfile, statusCode = 201, message = "Account created")
    }

    suspend fun login(handle: String, pin: String): LocalApiResponse<UserProfileEntity> = withContext(Dispatchers.IO) {
        val cleanHandle = if (handle.startsWith("@")) handle else "@$handle"
        val user = dao.getProfileByHandle(cleanHandle)
            ?: return@withContext LocalApiResponse.Error("User not found", statusCode = 404)

        if (user.passwordPin != pin) {
            return@withContext LocalApiResponse.Error("Incorrect PIN", statusCode = 401)
        }

        dao.setActiveSession(ActiveSessionEntity(activeUserId = user.userId))
        LocalApiResponse.Success(user, message = "Login successful")
    }

    suspend fun switchAccount(userId: String): LocalApiResponse<UserProfileEntity> = withContext(Dispatchers.IO) {
        val target = dao.getUserProfileOnce(userId)
            ?: return@withContext LocalApiResponse.Error("Account not found", statusCode = 404)

        dao.setActiveSession(ActiveSessionEntity(activeUserId = userId))
        LocalApiResponse.Success(target, message = "Switched to ${target.displayName}")
    }

    suspend fun logout(): LocalApiResponse<Unit> = withContext(Dispatchers.IO) {
        val allProfiles = dao.getAllUserProfiles().first()
        val fallback = allProfiles.firstOrNull { it.userId != "me" } ?: allProfiles.firstOrNull()
        if (fallback != null) {
            dao.setActiveSession(ActiveSessionEntity(activeUserId = fallback.userId))
        }
        LocalApiResponse.Success(Unit, message = "Logged out")
    }

    suspend fun updateProfile(
        userId: String,
        displayName: String,
        handle: String,
        bio: String,
        avatarUrl: String,
        isPrivate: Boolean,
        allowComments: Boolean,
        allowDuet: Boolean
    ): LocalApiResponse<UserProfileEntity> = withContext(Dispatchers.IO) {
        val current = dao.getUserProfileOnce(userId) ?: return@withContext LocalApiResponse.Error("User not found")
        val cleanHandle = if (handle.startsWith("@")) handle else "@$handle"
        val updated = current.copy(
            displayName = displayName,
            handle = cleanHandle,
            bio = bio,
            avatarUrl = avatarUrl,
            isPrivate = isPrivate,
            allowComments = allowComments,
            allowDuet = allowDuet
        )
        dao.updateUserProfile(updated)
        LocalApiResponse.Success(updated, message = "Profile updated")
    }

    suspend fun changePin(userId: String, oldPin: String, newPin: String): LocalApiResponse<Unit> = withContext(Dispatchers.IO) {
        val user = dao.getUserProfileOnce(userId) ?: return@withContext LocalApiResponse.Error("User not found")
        if (user.passwordPin != oldPin) {
            return@withContext LocalApiResponse.Error("Current PIN is incorrect", statusCode = 401)
        }
        dao.updateUserProfile(user.copy(passwordPin = newPin))
        LocalApiResponse.Success(Unit, message = "PIN updated successfully")
    }

    suspend fun deleteAccount(userId: String): LocalApiResponse<Unit> = withContext(Dispatchers.IO) {
        dao.deleteUserProfile(userId)
        val remaining = dao.getAllUserProfiles().first()
        val next = remaining.firstOrNull() ?: UserProfileEntity(userId = "me")
        dao.insertUserProfile(next)
        dao.setActiveSession(ActiveSessionEntity(activeUserId = next.userId))
        LocalApiResponse.Success(Unit, message = "Account deleted")
    }

    // ------------------------------------------------------------------------
    // Feed & Videos API Endpoints
    // ------------------------------------------------------------------------

    fun getRankedFeed(category: String): Flow<List<VideoEntity>> {
        return dao.getAllVideos().map { videos ->
            val user = try { getActiveUser() } catch (e: Exception) { null }
            val blockedHandles = if (user != null) {
                try {
                    dao.getBlockedUsers(user.handle).first().map { it.blockedHandle }.toSet()
                } catch (e: Exception) {
                    emptySet()
                }
            } else emptySet()

            val filteredVideos = if (blockedHandles.isNotEmpty()) {
                videos.filterNot { blockedHandles.contains(it.authorHandle) }
            } else videos

            when (category) {
                "following" -> {
                    val followingList = filteredVideos.filter { it.isFollowing || it.category == "following" }
                    if (followingList.isNotEmpty()) followingList else filteredVideos
                }
                else -> {
                    val events = dao.getRecentAnalyticsEvents()
                    val profile = RecommendationEngine.buildAffinityProfile(events)
                    RecommendationEngine.rankVideos(filteredVideos, profile)
                }
            }
        }
    }

    fun getAllVideos(): Flow<List<VideoEntity>> = dao.getAllVideos()
    fun getLikedVideos(): Flow<List<VideoEntity>> = dao.getLikedVideos()
    fun getBookmarkedVideos(): Flow<List<VideoEntity>> = dao.getBookmarkedVideos()
    fun getUserVideos(handle: String): Flow<List<VideoEntity>> = dao.getUserVideos(handle)
    fun getUserPrivateVideos(handle: String): Flow<List<VideoEntity>> = dao.getUserPrivateVideos(handle)

    suspend fun createVideo(request: VideoUploadRequest): LocalApiResponse<VideoEntity> = withContext(Dispatchers.IO) {
        try {
            val user = getActiveUser()
            // Extract hashtags automatically
            val hashtagRegex = Regex("#[a-zA-Z0-9_]+")
            val foundHashtags = hashtagRegex.findAll(request.caption).map { it.value }.toList().joinToString(" ")

            val videoPath = if (request.videoFilePath.isNotBlank()) {
                request.videoFilePath
            } else {
                mediaStorage.saveUploadedClip("${System.currentTimeMillis()}.mp4")
            }

            val newVideo = VideoEntity(
                authorId = user.userId,
                authorName = user.displayName,
                authorHandle = user.handle,
                authorAvatarUrl = user.avatarUrl,
                caption = request.caption,
                soundTitle = request.soundTitle,
                soundAuthor = request.soundAuthor,
                coverResName = request.coverResName,
                videoUrl = videoPath,
                likesCount = 1,
                commentsCount = 0,
                sharesCount = 0,
                bookmarksCount = 0,
                viewsCount = 1,
                repostCount = 0,
                durationSeconds = 15,
                isLiked = true,
                isBookmarked = false,
                isFollowing = false,
                isReposted = false,
                isPrivate = request.isPrivate,
                allowComments = request.allowComments,
                allowDuet = request.allowDuet,
                allowStitch = request.allowStitch,
                hashtags = foundHashtags,
                category = request.category,
                duetWithVideoId = request.duetWithVideoId,
                stitchWithVideoId = request.stitchWithVideoId,
                timestamp = System.currentTimeMillis()
            )

            val id = dao.insertVideo(newVideo)
            val created = newVideo.copy(id = id)

            // Increment sound usage
            dao.incrementSoundUsage(request.soundTitle)

            // Log event
            dao.logAnalyticsEvent(
                AnalyticsEventEntity(
                    eventType = "video_publish",
                    targetId = id,
                    categoryTag = request.category
                )
            )

            LocalApiResponse.Success(created, statusCode = 201, message = "Video published to local feed")
        } catch (e: Exception) {
            LocalApiResponse.Error("Failed to publish video: ${e.message}", statusCode = 500)
        }
    }

    suspend fun deleteVideo(videoId: Long): LocalApiResponse<Unit> = withContext(Dispatchers.IO) {
        dao.deleteVideoById(videoId)
        LocalApiResponse.Success(Unit, message = "Video deleted")
    }

    suspend fun toggleVideoLike(video: VideoEntity): LocalApiResponse<Boolean> = withContext(Dispatchers.IO) {
        val newLiked = !video.isLiked
        val delta = if (newLiked) 1 else -1
        dao.toggleLike(video.id, newLiked, delta)

        dao.logAnalyticsEvent(
            AnalyticsEventEntity(
                eventType = if (newLiked) "video_like" else "video_unlike",
                targetId = video.id,
                categoryTag = video.category
            )
        )

        // Notify author if liked
        val canNotify = preferences.masterNotifications.value && preferences.notifyLikes.value
        if (newLiked && video.authorHandle != "@alex_creative" && canNotify) {
            val user = getActiveUser()
            dao.insertNotification(
                NotificationEntity(
                    actorName = user.displayName,
                    actorHandle = user.handle,
                    actionText = "liked your video: '${video.caption.take(24)}...'",
                    type = "like",
                    targetVideoId = video.id
                )
            )
        }

        LocalApiResponse.Success(newLiked, message = if (newLiked) "Liked" else "Unliked")
    }

    suspend fun toggleVideoBookmark(video: VideoEntity): LocalApiResponse<Boolean> = withContext(Dispatchers.IO) {
        val newBookmarked = !video.isBookmarked
        val delta = if (newBookmarked) 1 else -1
        dao.toggleBookmark(video.id, newBookmarked, delta)

        dao.logAnalyticsEvent(
            AnalyticsEventEntity(
                eventType = if (newBookmarked) "video_save" else "video_unsave",
                targetId = video.id,
                categoryTag = video.category
            )
        )

        LocalApiResponse.Success(newBookmarked, message = if (newBookmarked) "Saved to favorites" else "Removed from favorites")
    }

    suspend fun toggleVideoRepost(video: VideoEntity): LocalApiResponse<Boolean> = withContext(Dispatchers.IO) {
        val newRepost = !video.isReposted
        val delta = if (newRepost) 1 else -1
        dao.toggleRepost(video.id, newRepost, delta)

        dao.logAnalyticsEvent(
            AnalyticsEventEntity(
                eventType = if (newRepost) "video_repost" else "video_unrepost",
                targetId = video.id,
                categoryTag = video.category
            )
        )

        val canNotify = preferences.masterNotifications.value && preferences.notifyReposts.value
        if (newRepost && video.authorHandle != "@alex_creative" && canNotify) {
            val user = getActiveUser()
            dao.insertNotification(
                NotificationEntity(
                    actorName = user.displayName,
                    actorHandle = user.handle,
                    actionText = "reposted your video 🔄",
                    type = "repost",
                    targetVideoId = video.id
                )
            )
        }

        LocalApiResponse.Success(newRepost, message = if (newRepost) "Reposted to your feed!" else "Removed repost")
    }

    suspend fun recordVideoView(videoId: Long, watchDurationMs: Long, completed: Boolean) = withContext(Dispatchers.IO) {
        dao.incrementView(videoId)
        dao.logAnalyticsEvent(
            AnalyticsEventEntity(
                eventType = if (completed) "video_complete" else if (watchDurationMs < 2000) "video_skip" else "video_view",
                targetId = videoId,
                watchDurationMs = watchDurationMs,
                completionPercent = if (completed) 1.0f else (watchDurationMs / 15000f).coerceIn(0f, 1f)
            )
        )
    }

    suspend fun recordVideoShare(videoId: Long): LocalApiResponse<Unit> = withContext(Dispatchers.IO) {
        dao.incrementShare(videoId)
        dao.logAnalyticsEvent(
            AnalyticsEventEntity(
                eventType = "video_share",
                targetId = videoId
            )
        )
        LocalApiResponse.Success(Unit, message = "Share link generated")
    }

    suspend fun toggleUserFollow(authorHandle: String, currentFollowing: Boolean): LocalApiResponse<Boolean> = withContext(Dispatchers.IO) {
        val newStatus = !currentFollowing
        val user = getActiveUser()

        if (newStatus) {
            dao.insertFollow(FollowEntity(followerHandle = user.handle, followingHandle = authorHandle))
            val canNotify = preferences.masterNotifications.value && preferences.notifyNewFollowers.value
            if (canNotify) {
                dao.insertNotification(
                    NotificationEntity(
                        actorName = user.displayName,
                        actorHandle = user.handle,
                        actionText = "started following you",
                        type = "follow"
                    )
                )
            }
        } else {
            dao.deleteFollow(user.handle, authorHandle)
        }

        dao.toggleFollow(authorHandle, newStatus)

        dao.logAnalyticsEvent(
            AnalyticsEventEntity(
                eventType = if (newStatus) "user_follow" else "user_unfollow",
                targetId = 0,
                categoryTag = authorHandle
            )
        )

        LocalApiResponse.Success(newStatus, message = if (newStatus) "Following creator" else "Unfollowed")
    }

    // ------------------------------------------------------------------------
    // Comments API Endpoints
    // ------------------------------------------------------------------------

    fun getComments(videoId: Long): Flow<List<CommentEntity>> = dao.getCommentsForVideo(videoId)

    fun getRepliesForComment(parentId: Long): Flow<List<CommentEntity>> = dao.getRepliesForComment(parentId)

    suspend fun postComment(videoId: Long, text: String, parentCommentId: Long? = null): LocalApiResponse<CommentEntity> = withContext(Dispatchers.IO) {
        val user = getActiveUser()
        val newComment = CommentEntity(
            videoId = videoId,
            parentCommentId = parentCommentId,
            authorId = user.userId,
            authorName = user.displayName,
            authorHandle = user.handle,
            authorAvatarUrl = user.avatarUrl,
            content = text,
            likesCount = 0,
            isLiked = false,
            timestamp = System.currentTimeMillis()
        )
        val id = dao.insertComment(newComment)
        dao.incrementCommentCount(videoId)

        dao.logAnalyticsEvent(
            AnalyticsEventEntity(
                eventType = if (parentCommentId != null) "comment_reply" else "comment_add",
                targetId = videoId
            )
        )

        val targetVideo = dao.getVideoById(videoId)
        val isReply = parentCommentId != null
        val canNotify = preferences.masterNotifications.value && (if (isReply) preferences.notifyReplies.value else preferences.notifyComments.value)
        if (targetVideo != null && targetVideo.authorHandle != user.handle && canNotify) {
            dao.insertNotification(
                NotificationEntity(
                    actorName = user.displayName,
                    actorHandle = user.handle,
                    actionText = if (isReply) "replied to your comment: '$text'" else "commented: '$text'",
                    type = "comment",
                    targetVideoId = videoId
                )
            )
        }

        LocalApiResponse.Success(newComment.copy(id = id), statusCode = 201, message = "Comment posted")
    }

    suspend fun editComment(commentId: Long, newText: String): LocalApiResponse<Unit> = withContext(Dispatchers.IO) {
        dao.updateCommentContent(commentId, newText)
        LocalApiResponse.Success(Unit, message = "Comment edited")
    }

    suspend fun deleteComment(commentId: Long, videoId: Long): LocalApiResponse<Unit> = withContext(Dispatchers.IO) {
        dao.deleteComment(commentId)
        dao.decrementCommentCount(videoId)
        LocalApiResponse.Success(Unit, message = "Comment deleted")
    }

    suspend fun toggleCommentLike(comment: CommentEntity): LocalApiResponse<Boolean> = withContext(Dispatchers.IO) {
        val newLiked = !comment.isLiked
        val delta = if (newLiked) 1 else -1
        dao.toggleCommentLike(comment.id, newLiked, delta)
        LocalApiResponse.Success(newLiked)
    }

    // ------------------------------------------------------------------------
    // Drafts API
    // ------------------------------------------------------------------------

    fun getDrafts(authorId: String): Flow<List<DraftEntity>> = dao.getDrafts(authorId)

    suspend fun saveDraft(draft: DraftEntity): LocalApiResponse<Long> = withContext(Dispatchers.IO) {
        val id = dao.insertDraft(draft)
        LocalApiResponse.Success(id, message = "Draft saved locally")
    }

    suspend fun deleteDraft(id: Long): LocalApiResponse<Unit> = withContext(Dispatchers.IO) {
        dao.deleteDraft(id)
        LocalApiResponse.Success(Unit, message = "Draft deleted")
    }

    // ------------------------------------------------------------------------
    // Direct Messages API
    // ------------------------------------------------------------------------

    fun getDirectMessages(handle: String): Flow<List<DirectMessageEntity>> = dao.getAllMessagesForUser(handle)

    fun getConversationMessages(conversationId: String): Flow<List<DirectMessageEntity>> = dao.getMessagesForConversation(conversationId)

    suspend fun sendDirectMessage(receiverHandle: String, text: String): LocalApiResponse<DirectMessageEntity> = withContext(Dispatchers.IO) {
        val user = getActiveUser()
        if (dao.isUserBlocked(receiverHandle, user.handle) > 0) {
            return@withContext LocalApiResponse.Error("You cannot send messages to this user.")
        }
        val convId = listOf(user.handle, receiverHandle).sorted().joinToString("_")
        val msg = DirectMessageEntity(
            conversationId = convId,
            senderHandle = user.handle,
            senderName = user.displayName,
            receiverHandle = receiverHandle,
            messageText = text,
            isRead = false,
            timestamp = System.currentTimeMillis()
        )
        val id = dao.insertMessage(msg)
        if (preferences.masterNotifications.value && preferences.notifyDirectMessages.value) {
            dao.insertNotification(
                NotificationEntity(
                    actorName = user.displayName,
                    actorHandle = user.handle,
                    actionText = "sent you a message: '${text.take(24)}...'",
                    type = "message"
                )
            )
        }
        LocalApiResponse.Success(msg.copy(id = id), message = "Message sent")
    }

    suspend fun deleteConversation(conversationId: String) = withContext(Dispatchers.IO) {
        dao.deleteConversation(conversationId)
    }

    // ------------------------------------------------------------------------
    // Search History & Search Queries
    // ------------------------------------------------------------------------

    fun getSearchHistory(): Flow<List<SearchHistoryEntity>> = dao.getSearchHistory()

    suspend fun recordSearch(query: String) = withContext(Dispatchers.IO) {
        if (query.isNotBlank()) {
            dao.insertSearchQuery(SearchHistoryEntity(query = query.trim()))
            dao.logAnalyticsEvent(
                AnalyticsEventEntity(
                    eventType = "search",
                    targetId = 0,
                    categoryTag = query.trim()
                )
            )
        }
    }

    suspend fun clearSearchHistory() = withContext(Dispatchers.IO) {
        dao.clearSearchHistory()
    }

    suspend fun deleteSearchQuery(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteSearchQuery(id)
    }

    suspend fun searchVideos(query: String): List<VideoEntity> = withContext(Dispatchers.IO) {
        dao.searchVideos(query)
    }

    suspend fun searchUsers(query: String): List<UserProfileEntity> = withContext(Dispatchers.IO) {
        dao.searchUsers(query)
    }

    suspend fun searchSounds(query: String): List<SoundEntity> = withContext(Dispatchers.IO) {
        dao.searchSounds(query)
    }

    // ------------------------------------------------------------------------
    // Block / Unblock & Mute / Unmute Users
    // ------------------------------------------------------------------------

    fun getBlockedUsers(userHandle: String): Flow<List<BlockedUserEntity>> = dao.getBlockedUsers(userHandle)

    suspend fun blockUser(userHandle: String, blockedHandle: String): LocalApiResponse<Unit> = withContext(Dispatchers.IO) {
        dao.blockUser(BlockedUserEntity(userHandle = userHandle, blockedHandle = blockedHandle))
        LocalApiResponse.Success(Unit, message = "User blocked")
    }

    suspend fun unblockUser(userHandle: String, blockedHandle: String): LocalApiResponse<Unit> = withContext(Dispatchers.IO) {
        dao.unblockUser(userHandle, blockedHandle)
        LocalApiResponse.Success(Unit, message = "User unblocked")
    }

    fun getMutedUsers(userHandle: String): Flow<List<MutedUserEntity>> = dao.getMutedUsers(userHandle)

    suspend fun muteUser(userHandle: String, mutedHandle: String): LocalApiResponse<Unit> = withContext(Dispatchers.IO) {
        dao.muteUser(MutedUserEntity(userHandle = userHandle, mutedHandle = mutedHandle))
        LocalApiResponse.Success(Unit, message = "User muted")
    }

    suspend fun unmuteUser(userHandle: String, mutedHandle: String): LocalApiResponse<Unit> = withContext(Dispatchers.IO) {
        dao.unmuteUser(userHandle, mutedHandle)
        LocalApiResponse.Success(Unit, message = "User unmuted")
    }

    // ------------------------------------------------------------------------
    // Watch History & Sound History
    // ------------------------------------------------------------------------

    fun getWatchHistory(): Flow<List<VideoEntity>> = dao.getWatchHistory()

    suspend fun deleteWatchHistoryItem(videoId: Long) = withContext(Dispatchers.IO) {
        dao.deleteWatchHistoryItem(videoId)
    }

    suspend fun clearWatchHistory() = withContext(Dispatchers.IO) {
        dao.clearWatchHistory()
    }

    fun getRecentlyUsedSounds(): Flow<List<SoundEntity>> = dao.getRecentlyUsedSounds()

    suspend fun clearRecentlyUsedSound(id: Long) = withContext(Dispatchers.IO) {
        dao.clearSoundUsage(id)
    }

    suspend fun clearAllRecentlyUsedSounds() = withContext(Dispatchers.IO) {
        dao.clearAllSoundUsage()
    }

    suspend fun resetRecommendationAffinity() = withContext(Dispatchers.IO) {
        dao.resetRecommendationEvents()
    }

    // ------------------------------------------------------------------------
    // Media & Storage Operations
    // ------------------------------------------------------------------------

    fun clearVideoCache(): Long = mediaStorage.clearVideoCache()
    fun clearThumbnailCache(): Long = mediaStorage.clearThumbnailCache()
    fun clearTemporaryFiles(): Long = mediaStorage.clearTemporaryFiles()

    suspend fun cleanupOrphanMedia(): Int = withContext(Dispatchers.IO) {
        val activeVideos = dao.getAllVideosList().map { it.videoUrl }.toSet()
        mediaStorage.cleanupOrphanMedia(activeVideos)
    }

    suspend fun repairMediaIndex(): Int = withContext(Dispatchers.IO) {
        val activeVideos = dao.getAllVideosList().map { it.videoUrl }
        mediaStorage.repairMediaIndex(activeVideos)
    }

    fun getStorageUsageBreakdown(): StorageUsageBreakdown = mediaStorage.getStorageUsageBreakdown()

    // ------------------------------------------------------------------------
    // Notifications & Sounds API
    // ------------------------------------------------------------------------

    fun getNotifications(): Flow<List<NotificationEntity>> = dao.getNotifications()

    suspend fun markNotificationRead(id: Long) = withContext(Dispatchers.IO) {
        dao.markNotificationAsRead(id)
    }

    suspend fun markAllNotificationsRead() = withContext(Dispatchers.IO) {
        dao.markAllNotificationsAsRead()
    }

    fun getAllSounds(): Flow<List<SoundEntity>> = dao.getAllSounds()

    suspend fun toggleFavoriteSound(id: Long) = withContext(Dispatchers.IO) {
        dao.toggleFavoriteSound(id)
    }

    // ------------------------------------------------------------------------
    // Backend Admin & Health Diagnostics
    // ------------------------------------------------------------------------

    suspend fun getBackendStats(): LocalBackendStats = withContext(Dispatchers.IO) {
        LocalBackendStats(
            totalVideos = dao.getVideoCount(),
            totalComments = dao.getCommentCount(),
            totalLikes = dao.getTotalVideoLikes(),
            totalNotifications = dao.getNotificationCount(),
            totalSounds = dao.getSoundCount(),
            totalUsers = dao.getUserProfileCount(),
            totalDrafts = dao.getDraftCount(),
            totalMessages = dao.getDirectMessageCount(),
            totalAnalyticsEvents = dao.getAnalyticsEventCount(),
            storageUsageKb = mediaStorage.getMediaStorageUsageKb(),
            dbEngine = "Room SQLite / Local Embedded Engine v3.0",
            status = "ONLINE (Healthy)"
        )
    }

    suspend fun seedAdditionalClips() = withContext(Dispatchers.IO) {
        val additionalClips = listOf(
            VideoEntity(
                authorName = "Jordan Skate",
                authorHandle = "@jordan_skates",
                caption = "Sunset kickflip line at Venice Beach boardwalk! 🛹🌅 #skate #venice #cali #fyp",
                soundTitle = "Golden Hour Skate - Instrumental",
                soundAuthor = "Sunset Collective",
                coverResName = "video_cover_dance",
                likesCount = 384000,
                commentsCount = 2940,
                sharesCount = 12000,
                bookmarksCount = 8900,
                viewsCount = 1290000,
                hashtags = "#skate #venice #cali #fyp",
                category = "fyp"
            ),
            VideoEntity(
                authorName = "Matcha Master",
                authorHandle = "@matcha_art",
                caption = "Ceremonial grade iced strawberry matcha latte step-by-step 🍵🍓 #matcha #recipe #aesthetic",
                soundTitle = "Morning Coffee Chillhop",
                soundAuthor = "Tokyo Lo-Fi Beats",
                coverResName = "video_cover_food",
                likesCount = 612000,
                commentsCount = 4800,
                sharesCount = 31000,
                bookmarksCount = 54000,
                viewsCount = 1840000,
                hashtags = "#matcha #recipe #aesthetic",
                category = "food"
            ),
            VideoEntity(
                authorName = "Nordic Hikes",
                authorHandle = "@nordic_explorer",
                caption = "Camping under the aurora borealis in Lofoten, Norway 🌌⛺️ #norway #aurora #wilderness",
                soundTitle = "Ambient Aurora Skies",
                soundAuthor = "Nordic Soundscape",
                coverResName = "video_cover_travel",
                likesCount = 892000,
                commentsCount = 7300,
                sharesCount = 49000,
                bookmarksCount = 76000,
                viewsCount = 2700000,
                hashtags = "#norway #aurora #wilderness",
                category = "fyp"
            )
        )
        dao.insertVideos(additionalClips)
    }

    suspend fun clearCache(): Boolean = withContext(Dispatchers.IO) {
        mediaStorage.clearCache()
    }

    suspend fun repairOrphanMedia(): Int = withContext(Dispatchers.IO) {
        val videos = dao.getAllVideos().first()
        val paths = videos.map { it.videoUrl }.filter { it.isNotBlank() }.toSet()
        mediaStorage.cleanupOrphanMedia(paths)
    }

    suspend fun resetDatabase() = withContext(Dispatchers.IO) {
        dao.clearVideos()
        dao.clearComments()
        dao.clearNotifications()
        dao.clearSearchHistory()
        mediaStorage.clearCache()
        seedInitialDatabase()
    }

    private suspend fun seedInitialDatabase() {
        val initialProfiles = listOf(
            UserProfileEntity(
                userId = "me",
                displayName = "Alex Rivera",
                handle = "@alex_creative",
                passwordPin = "1234",
                bio = "✨ Visual Storyteller & Creator | Daily Reels 🚀\n📍 Los Angeles, CA | Collabs: alex@creativelab.io",
                followingCount = 184,
                followersCount = 42900,
                likesCount = "1.2M",
                avatarUrl = ""
            ),
            UserProfileEntity(
                userId = "user_elena",
                displayName = "Elena Rostova",
                handle = "@elena_dance",
                passwordPin = "1234",
                bio = "🩰 Principal Ballerina & Choreographer | NYC 🗽",
                followingCount = 210,
                followersCount = 189000,
                likesCount = "4.5M",
                avatarUrl = ""
            ),
            UserProfileEntity(
                userId = "user_marcus",
                displayName = "Marcus Chen",
                handle = "@marcus_tech",
                passwordPin = "1234",
                bio = "💻 Creative technologist, AI engineer & tech reviewer",
                followingCount = 95,
                followersCount = 67000,
                likesCount = "890K",
                avatarUrl = ""
            )
        )
        initialProfiles.forEach { dao.insertUserProfile(it) }

        val initialVideos = listOf(
            VideoEntity(
                authorId = "user_elena",
                authorName = "Marcus Moves",
                authorHandle = "@marcus_moves",
                caption = "Learning the new shuffle choreo in the warehouse studio! 🕺🔥 Tag a friend who needs to try this sequence! #shuffle #dancechallenge #streetstyle #fyp",
                soundTitle = "Electro Funk Shuffle Remix 2026",
                soundAuthor = "DJ Neon Grooves",
                coverResName = "video_cover_dance",
                likesCount = 842500,
                commentsCount = 14200,
                sharesCount = 67300,
                bookmarksCount = 38400,
                viewsCount = 3120000,
                hashtags = "#shuffle #dancechallenge #streetstyle #fyp",
                isLiked = false,
                isBookmarked = false,
                isFollowing = false,
                category = "fyp"
            ),
            VideoEntity(
                authorId = "creator_ramen",
                authorName = "Chef Ramen King",
                authorHandle = "@chef_ramen",
                caption = "Secret 18-hour Tonkotsu broth recipe revealed! 🍜✨ The secret ingredient at 0:08 will blow your mind! #ramen #foodietok #cookingtips #chefsecret",
                soundTitle = "Lofi Kitchen Vibes - Cooking Chill",
                soundAuthor = "Ramen Lo-Fi Project",
                coverResName = "video_cover_food",
                likesCount = 1250000,
                commentsCount = 28900,
                sharesCount = 112000,
                bookmarksCount = 94000,
                viewsCount = 4800000,
                hashtags = "#ramen #foodietok #cookingtips #chefsecret",
                isLiked = false,
                isBookmarked = false,
                isFollowing = false,
                category = "fyp"
            ),
            VideoEntity(
                authorId = "user_maya",
                authorName = "Maya Wanderlust",
                authorHandle = "@maya_traveler",
                caption = "Hidden infinity pool overlooking the Swiss Alps 🏔️💧 You must save this destination for your 2026 bucket list! #traveltok #swissalps #paradise #wanderlust",
                soundTitle = "Epic Cinematic Adventure Theme",
                soundAuthor = "Mountain Soundscapes",
                coverResName = "video_cover_travel",
                likesCount = 2100000,
                commentsCount = 34500,
                sharesCount = 189000,
                bookmarksCount = 152000,
                viewsCount = 7600000,
                hashtags = "#traveltok #swissalps #paradise #wanderlust",
                isLiked = false,
                isBookmarked = false,
                isFollowing = true,
                category = "following"
            )
        )
        dao.insertVideos(initialVideos)

        val initialComments = listOf(
            CommentEntity(
                videoId = 1,
                authorName = "Elena Theo",
                authorHandle = "@elena_theo",
                content = "That reverse spin transition at 0:05 is unreal! Tutorial please!! 😍👏",
                likesCount = 3200,
                isLiked = false
            ),
            CommentEntity(
                videoId = 1,
                authorName = "Kai Rhythm",
                authorHandle = "@kairhythm",
                content = "Adding this to my practice routine immediately. Certified banger track too 🔥",
                likesCount = 1840,
                isLiked = false
            ),
            CommentEntity(
                videoId = 2,
                authorName = "Gourmet Guy",
                authorHandle = "@gourmet_guy",
                content = "I’ve tried making tonkotsu 4 times and this is the first recipe that worked! Thank you 🙏🍜",
                likesCount = 5410,
                isLiked = false
            )
        )
        initialComments.forEach { dao.insertComment(it) }

        val initialNotifications = listOf(
            NotificationEntity(
                actorName = "Marcus Moves",
                actorHandle = "@marcus_moves",
                actionText = "liked your video",
                type = "like",
                timestamp = System.currentTimeMillis() - 720000
            ),
            NotificationEntity(
                actorName = "Chef Ramen King",
                actorHandle = "@chef_ramen",
                actionText = "commented: 'Need that recipe marinade!'",
                type = "comment",
                timestamp = System.currentTimeMillis() - 2700000
            ),
            NotificationEntity(
                actorName = "Maya Wanderlust",
                actorHandle = "@maya_traveler",
                actionText = "started following you",
                type = "follow",
                timestamp = System.currentTimeMillis() - 7200000
            ),
            NotificationEntity(
                actorName = "TikTok System",
                actorHandle = "@tiktok",
                actionText = "Your video reached 10,000 views! Keep up the momentum 🚀",
                type = "system",
                timestamp = System.currentTimeMillis() - 18000000
            )
        )
        dao.insertNotifications(initialNotifications)

        val initialSounds = listOf(
            SoundEntity(
                title = "Electro Funk Shuffle Remix 2026",
                author = "DJ Neon Grooves",
                durationSeconds = 30,
                usageCount = 1420000,
                category = "trending"
            ),
            SoundEntity(
                title = "Lofi Kitchen Vibes - Cooking Chill",
                author = "Ramen Lo-Fi Project",
                durationSeconds = 60,
                usageCount = 890000,
                category = "chill"
            ),
            SoundEntity(
                title = "Epic Cinematic Adventure Theme",
                author = "Mountain Soundscapes",
                durationSeconds = 45,
                usageCount = 2100000,
                category = "cinematic"
            )
        )
        dao.insertSounds(initialSounds)

        // Seed initial direct message
        dao.insertMessage(
            DirectMessageEntity(
                conversationId = "@alex_creative_@marcus_moves",
                senderHandle = "@marcus_moves",
                senderName = "Marcus Moves",
                receiverHandle = "@alex_creative",
                messageText = "Loved your latest edit! Want to collab next week?",
                timestamp = System.currentTimeMillis() - 3600000
            )
        )
    }
}
