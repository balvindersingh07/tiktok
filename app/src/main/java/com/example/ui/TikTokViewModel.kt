package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BlockedUserEntity
import com.example.data.CommentEntity
import com.example.data.DirectMessageEntity
import com.example.data.DraftEntity
import com.example.data.LocalApiResponse
import com.example.data.LocalBackendStats
import com.example.data.MutedUserEntity
import com.example.data.NotificationEntity
import com.example.data.SearchHistoryEntity
import com.example.data.SoundEntity
import com.example.data.TashanPreferences
import com.example.data.TikTokRepository
import com.example.data.UserProfileEntity
import com.example.data.VideoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    HOME, DISCOVER, CREATE, INBOX, PROFILE
}

enum class FeedCategory {
    FOLLOWING, FOR_YOU
}

enum class ProfileSubTab {
    MY_VIDEOS, LIKED_VIDEOS, BOOKMARKED, PRIVATE, DRAFTS
}

class TikTokViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TikTokRepository(application)

    val currentTab = MutableStateFlow(MainTab.HOME)
    val feedCategory = MutableStateFlow(FeedCategory.FOR_YOU)

    // Target video navigation to jump directly to a selected video in Feed
    val targetFeedVideoId = MutableStateFlow<Long?>(null)

    fun playVideoInFeed(videoId: Long) {
        targetFeedVideoId.value = videoId
        currentTab.value = MainTab.HOME
    }

    fun clearTargetFeedVideo() {
        targetFeedVideoId.value = null
    }

    val allVideos: StateFlow<List<VideoEntity>>
    val fypRankedVideos: StateFlow<List<VideoEntity>>
    val likedVideos: StateFlow<List<VideoEntity>>
    val bookmarkedVideos: StateFlow<List<VideoEntity>>
    val allUserProfiles: StateFlow<List<UserProfileEntity>>
    val activeUserProfile = MutableStateFlow(UserProfileEntity())
    val notifications: StateFlow<List<NotificationEntity>>
    val sounds: StateFlow<List<SoundEntity>>
    val searchHistory: StateFlow<List<SearchHistoryEntity>>

    // User Drafts
    val userDrafts = MutableStateFlow<List<DraftEntity>>(emptyList())

    // Selected video comments bottom sheet
    private val _activeCommentVideo = MutableStateFlow<VideoEntity?>(null)
    val activeCommentVideo: StateFlow<VideoEntity?> = _activeCommentVideo.asStateFlow()

    private val _videoComments = MutableStateFlow<List<CommentEntity>>(emptyList())
    val videoComments: StateFlow<List<CommentEntity>> = _videoComments.asStateFlow()

    val replyingToComment = MutableStateFlow<CommentEntity?>(null)

    // Selected video share bottom sheet
    private val _activeShareVideo = MutableStateFlow<VideoEntity?>(null)
    val activeShareVideo: StateFlow<VideoEntity?> = _activeShareVideo.asStateFlow()

    // QR Code Dialog
    val qrCodeVideo = MutableStateFlow<VideoEntity?>(null)

    // Selected sound dialog/screen
    private val _activeSoundVideo = MutableStateFlow<VideoEntity?>(null)
    val activeSoundVideo: StateFlow<VideoEntity?> = _activeSoundVideo.asStateFlow()

    // Pre-selected sound for Create Screen
    val preSelectedSound = MutableStateFlow<SoundEntity?>(null)

    // Duet / Stitch context for Create Screen
    val duetSourceVideo = MutableStateFlow<VideoEntity?>(null)
    val stitchSourceVideo = MutableStateFlow<VideoEntity?>(null)

    // Local Backend Inspector & Console
    private val _showBackendConsole = MutableStateFlow(false)
    val showBackendConsole: StateFlow<Boolean> = _showBackendConsole.asStateFlow()

    private val _backendStats = MutableStateFlow(LocalBackendStats())
    val backendStats: StateFlow<LocalBackendStats> = _backendStats.asStateFlow()

    // Profile tab
    val profileSubTab = MutableStateFlow(ProfileSubTab.MY_VIDEOS)

    // Tashan Settings Sheet & Preferences
    val preferences: TashanPreferences = repository.preferences
    private val _showSettingsSheet = MutableStateFlow(false)
    val showSettingsSheet: StateFlow<Boolean> = _showSettingsSheet.asStateFlow()

    // History and Lists
    lateinit var watchHistory: StateFlow<List<VideoEntity>>
    lateinit var recentlyUsedSounds: StateFlow<List<SoundEntity>>
    val blockedUsers = MutableStateFlow<List<BlockedUserEntity>>(emptyList())
    val mutedUsers = MutableStateFlow<List<MutedUserEntity>>(emptyList())
    val storageBreakdown = MutableStateFlow(com.example.backend.StorageUsageBreakdown())

    // Active creator profile sheet / view
    val viewingCreator = MutableStateFlow<UserProfileEntity?>(null)

    // Direct Messaging state
    val directMessages = MutableStateFlow<List<DirectMessageEntity>>(emptyList())
    val activeChatUser = MutableStateFlow<String?>(null) // handle of user chatting with

    // Discover search query & dynamic search results
    val searchQuery = MutableStateFlow("")
    val searchVideoResults = MutableStateFlow<List<VideoEntity>>(emptyList())
    val searchUserResults = MutableStateFlow<List<UserProfileEntity>>(emptyList())
    val searchSoundResults = MutableStateFlow<List<SoundEntity>>(emptyList())

    // Global Playback & Mute State
    val isGlobalMuted = MutableStateFlow(false)

    // Session-level viewed video tracking to prevent duplicate view increments
    private val viewedVideosThisSession = mutableSetOf<Long>()

    // Snackbar / Toast feedback
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializePreloadedData()
            refreshActiveUser()
            refreshBackendStats()
            refreshDrafts()
        }

        allVideos = repository.allVideos.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        fypRankedVideos = repository.getRankedFeed("fyp").stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        likedVideos = repository.likedVideos.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        bookmarkedVideos = repository.bookmarkedVideos.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allUserProfiles = repository.allUserProfiles.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        notifications = repository.notifications.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        sounds = repository.sounds.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        searchHistory = repository.searchHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        watchHistory = repository.watchHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        recentlyUsedSounds = repository.recentlyUsedSounds.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        refreshBlockedAndMutedUsers()
        refreshStorageBreakdown()

        // Observe active session
        viewModelScope.launch {
            repository.activeSession.collect { session ->
                if (session != null) {
                    val user = repository.backendServer.dao.getUserProfileOnce(session.activeUserId)
                    if (user != null) {
                        activeUserProfile.value = user
                        refreshDrafts()
                    }
                }
            }
        }
    }

    private suspend fun refreshActiveUser() {
        activeUserProfile.value = repository.getActiveUser()
    }

    fun refreshDrafts() {
        viewModelScope.launch {
            repository.getDrafts(activeUserProfile.value.userId).collect {
                userDrafts.value = it
            }
        }
    }

    fun selectTab(tab: MainTab) {
        currentTab.value = tab
    }

    fun setFeedCategory(category: FeedCategory) {
        feedCategory.value = category
    }

    fun toggleMute() {
        isGlobalMuted.value = !isGlobalMuted.value
    }

    // ------------------------------------------------------------------------
    // Video Feed Actions
    // ------------------------------------------------------------------------

    fun toggleLike(video: VideoEntity) {
        viewModelScope.launch {
            repository.toggleLike(video)
            refreshBackendStats()
        }
    }

    fun toggleBookmark(video: VideoEntity) {
        viewModelScope.launch {
            repository.toggleBookmark(video)
            _toastMessage.value = if (!video.isBookmarked) "Saved to favorites" else "Removed from favorites"
            refreshBackendStats()
        }
    }

    fun toggleFollow(video: VideoEntity) {
        viewModelScope.launch {
            repository.toggleFollow(video.authorHandle, video.isFollowing)
            _toastMessage.value = if (!video.isFollowing) "Following ${video.authorName}" else "Unfollowed ${video.authorName}"
        }
    }

    fun recordVideoView(video: VideoEntity, watchDurationMs: Long, completed: Boolean) {
        val alreadyViewed = synchronized(viewedVideosThisSession) {
            !viewedVideosThisSession.add(video.id)
        }
        viewModelScope.launch {
            // Log analytics event always for recommendation engine learning
            repository.recordView(video.id, watchDurationMs, completed)
            if (!alreadyViewed) {
                refreshBackendStats()
            }
        }
    }

    // ------------------------------------------------------------------------
    // Comments
    // ------------------------------------------------------------------------

    fun openComments(video: VideoEntity) {
        _activeCommentVideo.value = video
        replyingToComment.value = null
        viewModelScope.launch {
            repository.getCommentsForVideo(video.id).collect { comments ->
                _videoComments.value = comments
            }
        }
    }

    fun closeComments() {
        _activeCommentVideo.value = null
        replyingToComment.value = null
    }

    fun setReplyTo(comment: CommentEntity) {
        replyingToComment.value = comment
    }

    fun clearReply() {
        replyingToComment.value = null
    }

    fun addComment(videoId: Long, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val parentId = replyingToComment.value?.id
            repository.addComment(videoId, text.trim(), parentId)
            replyingToComment.value = null
            refreshBackendStats()
        }
    }

    fun deleteComment(comment: CommentEntity) {
        viewModelScope.launch {
            repository.deleteComment(comment.id, comment.videoId)
            _toastMessage.value = "Comment deleted"
            refreshBackendStats()
        }
    }

    fun toggleCommentLike(comment: CommentEntity) {
        viewModelScope.launch {
            repository.toggleCommentLike(comment)
        }
    }

    // ------------------------------------------------------------------------
    // Share & System Integrations
    // ------------------------------------------------------------------------

    fun openShare(video: VideoEntity) {
        _activeShareVideo.value = video
    }

    fun closeShare() {
        _activeShareVideo.value = null
    }

    fun copyVideoLink(video: VideoEntity) {
        viewModelScope.launch {
            val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Tashan Video", "tashan://video/${video.id}")
            clipboard.setPrimaryClip(clip)
            repository.recordShare(video.id)
            _toastMessage.value = "Link copied to clipboard!"
            closeShare()
            refreshBackendStats()
        }
    }

    fun shareViaSystem(video: VideoEntity) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out this video on Tashan by ${video.authorHandle}: ${video.caption} tashan://video/${video.id}")
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Video").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<Application>().startActivity(shareIntent)
        viewModelScope.launch {
            repository.recordShare(video.id)
            closeShare()
            refreshBackendStats()
        }
    }

    fun exportVideoToGallery(video: VideoEntity) {
        val success = repository.exportVideoToGallery(video.videoUrl, "Tashan_${video.id}")
        if (success) {
            _toastMessage.value = "Video saved to device Gallery / Movies! 📲"
        } else {
            _toastMessage.value = "Saved to local Tashan storage folder"
        }
        closeShare()
    }

    fun repostVideo(video: VideoEntity) {
        viewModelScope.launch {
            repository.toggleRepost(video)
            _toastMessage.value = if (!video.isReposted) "Reposted to your profile feed! 🔄" else "Removed repost"
            closeShare()
            refreshBackendStats()
        }
    }

    fun startDuet(video: VideoEntity) {
        duetSourceVideo.value = video
        closeShare()
        currentTab.value = MainTab.CREATE
        _toastMessage.value = "Duet mode active: ${video.authorHandle}"
    }

    fun startStitch(video: VideoEntity) {
        stitchSourceVideo.value = video
        closeShare()
        currentTab.value = MainTab.CREATE
        _toastMessage.value = "Stitch mode active: ${video.authorHandle}"
    }

    fun openQrCode(video: VideoEntity) {
        qrCodeVideo.value = video
        closeShare()
    }

    fun closeQrCode() {
        qrCodeVideo.value = null
    }

    // ------------------------------------------------------------------------
    // Sounds
    // ------------------------------------------------------------------------

    fun openSoundDetail(video: VideoEntity) {
        _activeSoundVideo.value = video
    }

    fun closeSoundDetail() {
        _activeSoundVideo.value = null
    }

    fun toggleFavoriteSound(soundId: Long) {
        viewModelScope.launch {
            repository.toggleFavoriteSound(soundId)
        }
    }

    fun useSound(soundTitle: String, soundAuthor: String) {
        preSelectedSound.value = SoundEntity(
            title = soundTitle,
            author = soundAuthor
        )
        closeSoundDetail()
        currentTab.value = MainTab.CREATE
        _toastMessage.value = "Using sound: $soundTitle 🎵"
    }

    // ------------------------------------------------------------------------
    // Creator Profiles & Navigation
    // ------------------------------------------------------------------------

    fun openCreatorProfile(handle: String) {
        viewModelScope.launch {
            val user = repository.backendServer.dao.getProfileByHandle(handle)
            if (user != null) {
                viewingCreator.value = user
            } else {
                _toastMessage.value = "Creator: $handle"
            }
        }
    }

    fun closeCreatorProfile() {
        viewingCreator.value = null
    }

    // ------------------------------------------------------------------------
    // Auth & Accounts
    // ------------------------------------------------------------------------

    fun signup(displayName: String, handle: String, pin: String) {
        viewModelScope.launch {
            val res = repository.signup(displayName, handle, pin)
            when (res) {
                is LocalApiResponse.Success -> {
                    activeUserProfile.value = res.data
                    _toastMessage.value = "Account @${res.data.handle} created!"
                    refreshDrafts()
                    refreshBackendStats()
                }
                is LocalApiResponse.Error -> {
                    _toastMessage.value = res.message
                }
            }
        }
    }

    fun login(handle: String, pin: String) {
        viewModelScope.launch {
            val res = repository.login(handle, pin)
            when (res) {
                is LocalApiResponse.Success -> {
                    activeUserProfile.value = res.data
                    _toastMessage.value = "Welcome back, ${res.data.displayName}!"
                    refreshDrafts()
                    refreshBackendStats()
                }
                is LocalApiResponse.Error -> {
                    _toastMessage.value = res.message
                }
            }
        }
    }

    fun switchAccount(userId: String) {
        viewModelScope.launch {
            val res = repository.switchAccount(userId)
            if (res is LocalApiResponse.Success) {
                activeUserProfile.value = res.data
                _toastMessage.value = "Switched to ${res.data.displayName}"
                refreshDrafts()
                refreshBackendStats()
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            refreshActiveUser()
            _toastMessage.value = "Logged out"
            refreshDrafts()
        }
    }

    fun updateProfile(
        displayName: String,
        handle: String,
        bio: String,
        avatarUrl: String,
        isPrivate: Boolean,
        allowComments: Boolean,
        allowDuet: Boolean
    ) {
        viewModelScope.launch {
            val res = repository.updateProfile(
                userId = activeUserProfile.value.userId,
                displayName = displayName,
                handle = handle,
                bio = bio,
                avatarUrl = avatarUrl,
                isPrivate = isPrivate,
                allowComments = allowComments,
                allowDuet = allowDuet
            )
            if (res is LocalApiResponse.Success) {
                activeUserProfile.value = res.data
                _toastMessage.value = "Profile updated! ✨"
                refreshBackendStats()
            }
        }
    }

    fun changePin(oldPin: String, newPin: String) {
        viewModelScope.launch {
            val res = repository.changePin(activeUserProfile.value.userId, oldPin, newPin)
            when (res) {
                is LocalApiResponse.Success -> _toastMessage.value = "PIN changed successfully! 🔒"
                is LocalApiResponse.Error -> _toastMessage.value = res.message
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            repository.deleteAccount(activeUserProfile.value.userId)
            refreshActiveUser()
            _toastMessage.value = "Account deleted"
            refreshDrafts()
            refreshBackendStats()
        }
    }

    // ------------------------------------------------------------------------
    // Publishing & Drafts
    // ------------------------------------------------------------------------

    fun saveDraft(
        caption: String,
        soundTitle: String,
        soundAuthor: String,
        coverResName: String,
        videoPath: String = ""
    ) {
        viewModelScope.launch {
            val draft = DraftEntity(
                authorId = activeUserProfile.value.userId,
                caption = caption,
                soundTitle = soundTitle,
                soundAuthor = soundAuthor,
                coverResName = coverResName,
                localVideoPath = videoPath
            )
            repository.saveDraft(draft)
            _toastMessage.value = "Draft saved locally! 📝"
            currentTab.value = MainTab.PROFILE
            profileSubTab.value = ProfileSubTab.DRAFTS
            refreshDrafts()
            refreshBackendStats()
        }
    }

    fun publishDraft(draft: DraftEntity) {
        viewModelScope.launch {
            repository.postNewVideo(
                caption = draft.caption,
                soundTitle = draft.soundTitle,
                soundAuthor = draft.soundAuthor,
                coverResName = draft.coverResName,
                videoFilePath = draft.localVideoPath,
                category = "fyp"
            )
            repository.deleteDraft(draft.id)
            refreshDrafts()
            _toastMessage.value = "Draft published to feed! 🚀"
            currentTab.value = MainTab.HOME
            refreshBackendStats()
        }
    }

    fun sendVideoToFriend(friendHandle: String, video: VideoEntity) {
        viewModelScope.launch {
            repository.sendDirectMessage(friendHandle, "Check out this clip: ${video.caption} (tiktok://video/${video.id})")
            _toastMessage.value = "Sent video to $friendHandle 🚀"
            closeShare()
            refreshBackendStats()
        }
    }

    fun toggleFollowCreator(creator: UserProfileEntity) {
        viewModelScope.launch {
            val user = activeUserProfile.value
            val isFollowing = repository.backendServer.dao.isFollowing(user.handle, creator.handle) > 0
            val res = repository.toggleFollow(creator.handle, isFollowing)
            val updatedCreator = repository.backendServer.dao.getProfileByHandle(creator.handle)
            if (updatedCreator != null) {
                viewingCreator.value = updatedCreator
            }
            val followed = (res as? LocalApiResponse.Success)?.data ?: !isFollowing
            _toastMessage.value = if (followed) "Following ${creator.displayName}" else "Unfollowed ${creator.displayName}"
            refreshBackendStats()
        }
    }

    fun deleteDraft(id: Long) {
        viewModelScope.launch {
            repository.deleteDraft(id)
            _toastMessage.value = "Draft deleted"
            refreshDrafts()
            refreshBackendStats()
        }
    }

    fun uploadVideo(
        caption: String,
        soundTitle: String,
        soundAuthor: String,
        coverResName: String,
        videoFilePath: String = "",
        isPrivate: Boolean = false,
        allowComments: Boolean = true,
        allowDuet: Boolean = true,
        allowStitch: Boolean = true
    ) {
        viewModelScope.launch {
            val duetId = duetSourceVideo.value?.id
            val stitchId = stitchSourceVideo.value?.id

            repository.postNewVideo(
                caption = caption,
                soundTitle = soundTitle,
                soundAuthor = soundAuthor,
                coverResName = coverResName,
                videoFilePath = videoFilePath,
                category = "fyp",
                isPrivate = isPrivate,
                allowComments = allowComments,
                allowDuet = allowDuet,
                allowStitch = allowStitch,
                duetWithVideoId = duetId,
                stitchWithVideoId = stitchId
            )
            duetSourceVideo.value = null
            stitchSourceVideo.value = null
            _toastMessage.value = "Video published to local feed! 🚀"
            currentTab.value = MainTab.HOME
            refreshBackendStats()
        }
    }

    fun importVideoFromUri(uri: Uri): String? {
        val savedPath = repository.saveVideoFromUri(uri)
        if (savedPath != null) {
            _toastMessage.value = "Video imported from device!"
        }
        return savedPath
    }

    // ------------------------------------------------------------------------
    // Search & Discover
    // ------------------------------------------------------------------------

    fun performSearch(query: String) {
        searchQuery.value = query
        viewModelScope.launch {
            if (query.isNotBlank()) {
                repository.recordSearch(query)
                searchVideoResults.value = repository.searchVideos(query)
                searchUserResults.value = repository.searchUsers(query)
                searchSoundResults.value = repository.searchSounds(query)
            } else {
                searchVideoResults.value = emptyList()
                searchUserResults.value = emptyList()
                searchSoundResults.value = emptyList()
            }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
            _toastMessage.value = "Search history cleared"
        }
    }

    // ------------------------------------------------------------------------
    // Direct Messaging (Chat)
    // ------------------------------------------------------------------------

    fun openChatWith(partnerHandle: String) {
        activeChatUser.value = partnerHandle
        viewModelScope.launch {
            val user = activeUserProfile.value
            val convId = listOf(user.handle, partnerHandle).sorted().joinToString("_")
            repository.getConversationMessages(convId).collect {
                directMessages.value = it
            }
        }
    }

    fun closeChat() {
        activeChatUser.value = null
    }

    fun sendDirectMessage(text: String) {
        val partner = activeChatUser.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendDirectMessage(partner, text.trim())
            refreshBackendStats()
        }
    }

    // ------------------------------------------------------------------------
    // Notifications
    // ------------------------------------------------------------------------

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
            _toastMessage.value = "All notifications marked as read"
        }
    }

    // ------------------------------------------------------------------------
    // Backend Console / Diagnostics
    // ------------------------------------------------------------------------

    fun openBackendConsole() {
        _showBackendConsole.value = true
        refreshBackendStats()
    }

    fun closeBackendConsole() {
        _showBackendConsole.value = false
    }

    fun refreshBackendStats() {
        viewModelScope.launch {
            _backendStats.value = repository.getBackendStats()
        }
    }

    fun clearMediaCache() {
        viewModelScope.launch {
            repository.clearCache()
            _toastMessage.value = "Local media cache cleared! 🧹"
            refreshBackendStats()
        }
    }

    fun repairOrphanMedia() {
        viewModelScope.launch {
            val count = repository.repairOrphanMedia()
            _toastMessage.value = "Cleaned $count unreferenced media files"
            refreshBackendStats()
        }
    }

    fun seedAdditionalClips() {
        viewModelScope.launch {
            repository.seedAdditionalClips()
            _toastMessage.value = "Added 3 trending viral clips to local backend! 🎬"
            refreshBackendStats()
        }
    }

    fun resetLocalDatabase() {
        viewModelScope.launch {
            repository.resetDatabase()
            refreshActiveUser()
            refreshDrafts()
            _toastMessage.value = "Local database reset to fresh default seed!"
            refreshBackendStats()
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // ------------------------------------------------------------------------
    // Settings Navigation & Operations
    // ------------------------------------------------------------------------

    fun openSettings() {
        _showSettingsSheet.value = true
        refreshStorageBreakdown()
        refreshBlockedAndMutedUsers()
    }

    fun closeSettings() {
        _showSettingsSheet.value = false
    }

    // 1. Privacy Controls
    fun setPrivateAccount(enabled: Boolean) {
        preferences.setPrivateAccount(enabled)
        val current = activeUserProfile.value
        viewModelScope.launch {
            repository.updateProfile(
                userId = current.userId,
                displayName = current.displayName,
                handle = current.handle,
                bio = current.bio,
                avatarUrl = current.avatarUrl,
                isPrivate = enabled,
                allowComments = current.allowComments,
                allowDuet = current.allowDuet
            )
            refreshActiveUser()
        }
    }

    fun setAllowComments(enabled: Boolean) {
        preferences.setAllowComments(enabled)
        val current = activeUserProfile.value
        viewModelScope.launch {
            repository.updateProfile(
                userId = current.userId,
                displayName = current.displayName,
                handle = current.handle,
                bio = current.bio,
                avatarUrl = current.avatarUrl,
                isPrivate = current.isPrivate,
                allowComments = enabled,
                allowDuet = current.allowDuet
            )
            refreshActiveUser()
        }
    }

    fun setAllowDirectMessages(option: String) = preferences.setAllowDirectMessages(option)
    fun setAllowDownloads(enabled: Boolean) = preferences.setAllowDownloads(enabled)
    fun setAllowDuet(enabled: Boolean) = preferences.setAllowDuet(enabled)
    fun setAllowStitch(enabled: Boolean) = preferences.setAllowStitch(enabled)
    fun setActivityStatus(enabled: Boolean) = preferences.setActivityStatus(enabled)
    fun setFollowingListVisibility(option: String) = preferences.setFollowingListVisibility(option)
    fun setLikedVideosVisibility(option: String) = preferences.setLikedVideosVisibility(option)

    // 2. Notification Preferences
    fun setMasterNotifications(enabled: Boolean) = preferences.setMasterNotifications(enabled)
    fun setNotifyLikes(enabled: Boolean) = preferences.setNotifyLikes(enabled)
    fun setNotifyComments(enabled: Boolean) = preferences.setNotifyComments(enabled)
    fun setNotifyReplies(enabled: Boolean) = preferences.setNotifyReplies(enabled)
    fun setNotifyNewFollowers(enabled: Boolean) = preferences.setNotifyNewFollowers(enabled)
    fun setNotifyReposts(enabled: Boolean) = preferences.setNotifyReposts(enabled)
    fun setNotifyMentions(enabled: Boolean) = preferences.setNotifyMentions(enabled)
    fun setNotifyDirectMessages(enabled: Boolean) = preferences.setNotifyDirectMessages(enabled)

    // 3. Feed & Recommendation Controls
    fun setAutoplay(enabled: Boolean) = preferences.setAutoplay(enabled)
    fun setLoopVideos(enabled: Boolean) = preferences.setLoopVideos(enabled)
    fun setMuteByDefault(enabled: Boolean) {
        preferences.setMuteByDefault(enabled)
        isGlobalMuted.value = enabled
    }
    fun setDataSaver(enabled: Boolean) = preferences.setDataSaver(enabled)

    fun resetRecommendationPreferences() {
        viewModelScope.launch {
            repository.resetRecommendationPreferences()
            _toastMessage.value = "Recommendation preferences reset to default"
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearWatchHistory()
            _toastMessage.value = "Watch history cleared"
        }
    }

    fun deleteWatchHistoryItem(videoId: Long) {
        viewModelScope.launch {
            repository.deleteWatchHistoryItem(videoId)
        }
    }

    // 4. Media & Storage Operations
    fun refreshStorageBreakdown() {
        viewModelScope.launch {
            storageBreakdown.value = repository.getStorageUsageBreakdown()
        }
    }

    fun clearVideoCache() {
        viewModelScope.launch {
            val freed = repository.clearVideoCache()
            val mb = freed / (1024f * 1024f)
            _toastMessage.value = String.format("Video cache cleared (%.2f MB freed)", mb)
            refreshStorageBreakdown()
        }
    }

    fun clearThumbnailCache() {
        viewModelScope.launch {
            val freed = repository.clearThumbnailCache()
            val mb = freed / (1024f * 1024f)
            _toastMessage.value = String.format("Thumbnail cache cleared (%.2f MB freed)", mb)
            refreshStorageBreakdown()
        }
    }

    fun clearTemporaryFiles() {
        viewModelScope.launch {
            val freed = repository.clearTemporaryFiles()
            val mb = freed / (1024f * 1024f)
            _toastMessage.value = String.format("Temporary files cleared (%.2f MB freed)", mb)
            refreshStorageBreakdown()
        }
    }

    fun repairMediaIndex() {
        viewModelScope.launch {
            val count = repository.repairMediaIndex()
            _toastMessage.value = "Media index verified & synchronized ($count active files)"
            refreshStorageBreakdown()
        }
    }

    // 5. Appearance
    fun setThemeMode(mode: String) = preferences.setThemeMode(mode)
    fun setReduceMotion(enabled: Boolean) = preferences.setReduceMotion(enabled)
    fun setAnimationScale(scale: String) = preferences.setAnimationScale(scale)
    fun setTextSizePreference(pref: String) = preferences.setTextSizePreference(pref)

    // 6. Blocked & Muted Users
    fun refreshBlockedAndMutedUsers() {
        viewModelScope.launch {
            val user = activeUserProfile.value
            repository.getBlockedUsers(user.handle).collect {
                blockedUsers.value = it
            }
        }
        viewModelScope.launch {
            val user = activeUserProfile.value
            repository.getMutedUsers(user.handle).collect {
                mutedUsers.value = it
            }
        }
    }

    fun blockUser(targetHandle: String) {
        viewModelScope.launch {
            val user = activeUserProfile.value
            repository.blockUser(user.handle, targetHandle)
            _toastMessage.value = "Blocked $targetHandle"
            refreshBlockedAndMutedUsers()
        }
    }

    fun unblockUser(targetHandle: String) {
        viewModelScope.launch {
            val user = activeUserProfile.value
            repository.unblockUser(user.handle, targetHandle)
            _toastMessage.value = "Unblocked $targetHandle"
            refreshBlockedAndMutedUsers()
        }
    }

    fun muteUser(targetHandle: String) {
        viewModelScope.launch {
            val user = activeUserProfile.value
            repository.muteUser(user.handle, targetHandle)
            _toastMessage.value = "Muted $targetHandle"
            refreshBlockedAndMutedUsers()
        }
    }

    fun unmuteUser(targetHandle: String) {
        viewModelScope.launch {
            val user = activeUserProfile.value
            repository.unmuteUser(user.handle, targetHandle)
            _toastMessage.value = "Unmuted $targetHandle"
            refreshBlockedAndMutedUsers()
        }
    }

    // 7. History Operations
    fun deleteSearchQuery(id: Long) {
        viewModelScope.launch {
            repository.deleteSearchQuery(id)
        }
    }

    fun clearRecentlyUsedSound(id: Long) {
        viewModelScope.launch {
            repository.clearRecentlyUsedSound(id)
        }
    }

    fun clearAllRecentlyUsedSounds() {
        viewModelScope.launch {
            repository.clearAllRecentlyUsedSounds()
            _toastMessage.value = "Sound history cleared"
        }
    }

    // 8. Security
    fun setAppLockEnabled(enabled: Boolean) = preferences.setAppLockEnabled(enabled)
    fun setBiometricLockEnabled(enabled: Boolean) = preferences.setBiometricLockEnabled(enabled)

    fun clearSavedSessionCredentials() {
        viewModelScope.launch {
            repository.logout()
            refreshActiveUser()
            _showSettingsSheet.value = false
            _toastMessage.value = "Session cleared. Logged out."
        }
    }
}
