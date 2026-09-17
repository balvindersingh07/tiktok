package com.example.data

import android.content.Context
import android.net.Uri
import com.example.backend.LocalTikTokBackendServer
import kotlinx.coroutines.flow.Flow

class TikTokRepository(private val context: Context) {

    val backendServer = LocalTikTokBackendServer(context)

    val allVideos: Flow<List<VideoEntity>> = backendServer.getAllVideos()
    val likedVideos: Flow<List<VideoEntity>> = backendServer.getLikedVideos()
    val bookmarkedVideos: Flow<List<VideoEntity>> = backendServer.getBookmarkedVideos()
    val allUserProfiles: Flow<List<UserProfileEntity>> = backendServer.getAllUserProfiles()
    val activeSession: Flow<ActiveSessionEntity?> = backendServer.getActiveSession()
    val notifications: Flow<List<NotificationEntity>> = backendServer.getNotifications()
    val sounds: Flow<List<SoundEntity>> = backendServer.getAllSounds()
    val searchHistory: Flow<List<SearchHistoryEntity>> = backendServer.getSearchHistory()

    fun getUserProfile(userId: String): Flow<UserProfileEntity?> = backendServer.getUserProfile(userId)

    suspend fun getActiveUser(): UserProfileEntity = backendServer.getActiveUser()

    fun getRankedFeed(category: String): Flow<List<VideoEntity>> {
        return backendServer.getRankedFeed(category)
    }

    fun getCommentsForVideo(videoId: Long): Flow<List<CommentEntity>> {
        return backendServer.getComments(videoId)
    }

    fun getRepliesForComment(parentId: Long): Flow<List<CommentEntity>> {
        return backendServer.getRepliesForComment(parentId)
    }

    fun getUserVideos(handle: String): Flow<List<VideoEntity>> {
        return backendServer.getUserVideos(handle)
    }

    fun getUserPrivateVideos(handle: String): Flow<List<VideoEntity>> {
        return backendServer.getUserPrivateVideos(handle)
    }

    fun getDrafts(authorId: String): Flow<List<DraftEntity>> {
        return backendServer.getDrafts(authorId)
    }

    fun getDirectMessages(handle: String): Flow<List<DirectMessageEntity>> {
        return backendServer.getDirectMessages(handle)
    }

    fun getConversationMessages(convId: String): Flow<List<DirectMessageEntity>> {
        return backendServer.getConversationMessages(convId)
    }

    suspend fun initializePreloadedData() {
        backendServer.initializeBackend()
    }

    // ------------------------------------------------------------------------
    // Auth & Accounts
    // ------------------------------------------------------------------------
    suspend fun signup(displayName: String, handle: String, pin: String): LocalApiResponse<UserProfileEntity> {
        return backendServer.signup(displayName, handle, pin)
    }

    suspend fun login(handle: String, pin: String): LocalApiResponse<UserProfileEntity> {
        return backendServer.login(handle, pin)
    }

    suspend fun logout(): LocalApiResponse<Unit> {
        return backendServer.logout()
    }

    suspend fun switchAccount(userId: String): LocalApiResponse<UserProfileEntity> {
        return backendServer.switchAccount(userId)
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
    ): LocalApiResponse<UserProfileEntity> {
        return backendServer.updateProfile(userId, displayName, handle, bio, avatarUrl, isPrivate, allowComments, allowDuet)
    }

    suspend fun changePin(userId: String, oldPin: String, newPin: String): LocalApiResponse<Unit> {
        return backendServer.changePin(userId, oldPin, newPin)
    }

    suspend fun deleteAccount(userId: String): LocalApiResponse<Unit> {
        return backendServer.deleteAccount(userId)
    }

    // ------------------------------------------------------------------------
    // Video Interactions
    // ------------------------------------------------------------------------
    suspend fun toggleLike(video: VideoEntity): LocalApiResponse<Boolean> {
        return backendServer.toggleVideoLike(video)
    }

    suspend fun toggleBookmark(video: VideoEntity): LocalApiResponse<Boolean> {
        return backendServer.toggleVideoBookmark(video)
    }

    suspend fun toggleRepost(video: VideoEntity): LocalApiResponse<Boolean> {
        return backendServer.toggleVideoRepost(video)
    }

    suspend fun toggleFollow(authorHandle: String, currentlyFollowing: Boolean): LocalApiResponse<Boolean> {
        return backendServer.toggleUserFollow(authorHandle, currentlyFollowing)
    }

    suspend fun recordView(videoId: Long, watchDurationMs: Long, completed: Boolean) {
        backendServer.recordVideoView(videoId, watchDurationMs, completed)
    }

    suspend fun recordShare(videoId: Long): LocalApiResponse<Unit> {
        return backendServer.recordVideoShare(videoId)
    }

    suspend fun deleteVideo(videoId: Long): LocalApiResponse<Unit> {
        return backendServer.deleteVideo(videoId)
    }

    // ------------------------------------------------------------------------
    // Comments
    // ------------------------------------------------------------------------
    suspend fun addComment(videoId: Long, text: String, parentCommentId: Long? = null): LocalApiResponse<CommentEntity> {
        return backendServer.postComment(videoId, text, parentCommentId)
    }

    suspend fun editComment(commentId: Long, newText: String): LocalApiResponse<Unit> {
        return backendServer.editComment(commentId, newText)
    }

    suspend fun deleteComment(commentId: Long, videoId: Long): LocalApiResponse<Unit> {
        return backendServer.deleteComment(commentId, videoId)
    }

    suspend fun toggleCommentLike(comment: CommentEntity): LocalApiResponse<Boolean> {
        return backendServer.toggleCommentLike(comment)
    }

    // ------------------------------------------------------------------------
    // Publishing & Drafts
    // ------------------------------------------------------------------------
    suspend fun postNewVideo(
        caption: String,
        soundTitle: String,
        soundAuthor: String,
        coverResName: String,
        videoFilePath: String = "",
        category: String = "fyp",
        isPrivate: Boolean = false,
        allowComments: Boolean = true,
        allowDuet: Boolean = true,
        allowStitch: Boolean = true,
        duetWithVideoId: Long? = null,
        stitchWithVideoId: Long? = null
    ): LocalApiResponse<VideoEntity> {
        return backendServer.createVideo(
            VideoUploadRequest(
                caption = caption,
                soundTitle = soundTitle,
                soundAuthor = soundAuthor,
                coverResName = coverResName,
                videoFilePath = videoFilePath,
                category = category,
                isPrivate = isPrivate,
                allowComments = allowComments,
                allowDuet = allowDuet,
                allowStitch = allowStitch,
                duetWithVideoId = duetWithVideoId,
                stitchWithVideoId = stitchWithVideoId
            )
        )
    }

    suspend fun saveDraft(draft: DraftEntity): LocalApiResponse<Long> {
        return backendServer.saveDraft(draft)
    }

    suspend fun deleteDraft(id: Long): LocalApiResponse<Unit> {
        return backendServer.deleteDraft(id)
    }

    // ------------------------------------------------------------------------
    // Direct Messaging
    // ------------------------------------------------------------------------
    suspend fun sendDirectMessage(receiverHandle: String, text: String): LocalApiResponse<DirectMessageEntity> {
        return backendServer.sendDirectMessage(receiverHandle, text)
    }

    suspend fun deleteConversation(convId: String) {
        backendServer.deleteConversation(convId)
    }

    // ------------------------------------------------------------------------
    // Search
    // ------------------------------------------------------------------------
    suspend fun recordSearch(query: String) {
        backendServer.recordSearch(query)
    }

    suspend fun clearSearchHistory() {
        backendServer.clearSearchHistory()
    }

    suspend fun searchVideos(query: String): List<VideoEntity> {
        return backendServer.searchVideos(query)
    }

    suspend fun searchUsers(query: String): List<UserProfileEntity> {
        return backendServer.searchUsers(query)
    }

    suspend fun searchSounds(query: String): List<SoundEntity> {
        return backendServer.searchSounds(query)
    }

    // ------------------------------------------------------------------------
    // Media Storage & Exports
    // ------------------------------------------------------------------------
    fun saveVideoFromUri(uri: Uri): String? {
        return backendServer.mediaStorage.saveVideoFromUri(uri)
    }

    val preferences: TashanPreferences = backendServer.preferences
    val watchHistory: Flow<List<VideoEntity>> = backendServer.getWatchHistory()
    val recentlyUsedSounds: Flow<List<SoundEntity>> = backendServer.getRecentlyUsedSounds()

    fun exportVideoToGallery(videoPath: String, title: String): Boolean {
        return backendServer.mediaStorage.exportVideoToGallery(videoPath, title)
    }

    suspend fun clearCache(): Boolean {
        return backendServer.clearCache()
    }

    suspend fun repairOrphanMedia(): Int {
        return backendServer.cleanupOrphanMedia()
    }

    fun clearVideoCache(): Long = backendServer.clearVideoCache()
    fun clearThumbnailCache(): Long = backendServer.clearThumbnailCache()
    fun clearTemporaryFiles(): Long = backendServer.clearTemporaryFiles()
    suspend fun repairMediaIndex(): Int = backendServer.repairMediaIndex()
    fun getStorageUsageBreakdown(): com.example.backend.StorageUsageBreakdown = backendServer.getStorageUsageBreakdown()

    // ------------------------------------------------------------------------
    // Blocked & Muted Users
    // ------------------------------------------------------------------------
    fun getBlockedUsers(handle: String): Flow<List<BlockedUserEntity>> = backendServer.getBlockedUsers(handle)
    suspend fun blockUser(userHandle: String, blockedHandle: String): LocalApiResponse<Unit> = backendServer.blockUser(userHandle, blockedHandle)
    suspend fun unblockUser(userHandle: String, blockedHandle: String): LocalApiResponse<Unit> = backendServer.unblockUser(userHandle, blockedHandle)

    fun getMutedUsers(handle: String): Flow<List<MutedUserEntity>> = backendServer.getMutedUsers(handle)
    suspend fun muteUser(userHandle: String, mutedHandle: String): LocalApiResponse<Unit> = backendServer.muteUser(userHandle, mutedHandle)
    suspend fun unmuteUser(userHandle: String, mutedHandle: String): LocalApiResponse<Unit> = backendServer.unmuteUser(userHandle, mutedHandle)

    // ------------------------------------------------------------------------
    // History & Recommendation Management
    // ------------------------------------------------------------------------
    suspend fun deleteWatchHistoryItem(videoId: Long) = backendServer.deleteWatchHistoryItem(videoId)
    suspend fun clearWatchHistory() = backendServer.clearWatchHistory()
    suspend fun clearRecentlyUsedSound(id: Long) = backendServer.clearRecentlyUsedSound(id)
    suspend fun clearAllRecentlyUsedSounds() = backendServer.clearAllRecentlyUsedSounds()
    suspend fun deleteSearchQuery(id: Long) = backendServer.dao.deleteSearchQuery(id)
    suspend fun resetRecommendationPreferences() = backendServer.resetRecommendationAffinity()

    // ------------------------------------------------------------------------
    // Notifications & Sounds
    // ------------------------------------------------------------------------
    suspend fun markAllNotificationsRead() {
        backendServer.markAllNotificationsRead()
    }

    suspend fun toggleFavoriteSound(id: Long) {
        backendServer.toggleFavoriteSound(id)
    }

    // ------------------------------------------------------------------------
    // Admin & Diagnostics
    // ------------------------------------------------------------------------
    suspend fun getBackendStats(): LocalBackendStats {
        return backendServer.getBackendStats()
    }

    suspend fun seedAdditionalClips() {
        backendServer.seedAdditionalClips()
    }

    suspend fun resetDatabase() {
        backendServer.resetDatabase()
    }
}
