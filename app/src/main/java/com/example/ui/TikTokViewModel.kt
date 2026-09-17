package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CommentEntity
import com.example.data.TikTokDatabase
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
    MY_VIDEOS, LIKED_VIDEOS, BOOKMARKED, PRIVATE
}

class TikTokViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TikTokRepository

    val currentTab = MutableStateFlow(MainTab.HOME)
    val feedCategory = MutableStateFlow(FeedCategory.FOR_YOU)

    val allVideos: StateFlow<List<VideoEntity>>
    val likedVideos: StateFlow<List<VideoEntity>>
    val bookmarkedVideos: StateFlow<List<VideoEntity>>
    val userProfile: StateFlow<UserProfileEntity?>

    // Selected video comments bottom sheet
    private val _activeCommentVideo = MutableStateFlow<VideoEntity?>(null)
    val activeCommentVideo: StateFlow<VideoEntity?> = _activeCommentVideo.asStateFlow()

    private val _videoComments = MutableStateFlow<List<CommentEntity>>(emptyList())
    val videoComments: StateFlow<List<CommentEntity>> = _videoComments.asStateFlow()

    // Selected video share bottom sheet
    private val _activeShareVideo = MutableStateFlow<VideoEntity?>(null)
    val activeShareVideo: StateFlow<VideoEntity?> = _activeShareVideo.asStateFlow()

    // Selected sound dialog/screen
    private val _activeSoundVideo = MutableStateFlow<VideoEntity?>(null)
    val activeSoundVideo: StateFlow<VideoEntity?> = _activeSoundVideo.asStateFlow()

    // Profile tab
    val profileSubTab = MutableStateFlow(ProfileSubTab.MY_VIDEOS)

    // Discover search query
    val searchQuery = MutableStateFlow("")

    // Snackbar / Toast feedback
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        val database = TikTokDatabase.getDatabase(application)
        repository = TikTokRepository(database.tikTokDao())

        viewModelScope.launch {
            repository.initializePreloadedData()
        }

        allVideos = repository.allVideos.stateIn(
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

        userProfile = repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfileEntity()
        )
    }

    fun selectTab(tab: MainTab) {
        currentTab.value = tab
    }

    fun setFeedCategory(category: FeedCategory) {
        feedCategory.value = category
    }

    fun toggleLike(video: VideoEntity) {
        viewModelScope.launch {
            repository.toggleLike(video.id, video.isLiked)
        }
    }

    fun toggleBookmark(video: VideoEntity) {
        viewModelScope.launch {
            repository.toggleBookmark(video.id, video.isBookmarked)
            _toastMessage.value = if (!video.isBookmarked) "Added to Favorites" else "Removed from Favorites"
        }
    }

    fun toggleFollow(video: VideoEntity) {
        viewModelScope.launch {
            repository.toggleFollow(video.authorHandle, video.isFollowing)
            _toastMessage.value = if (!video.isFollowing) "Following ${video.authorName}" else "Unfollowed ${video.authorName}"
        }
    }

    fun openComments(video: VideoEntity) {
        _activeCommentVideo.value = video
        viewModelScope.launch {
            repository.getCommentsForVideo(video.id).collect { comments ->
                _videoComments.value = comments
            }
        }
    }

    fun closeComments() {
        _activeCommentVideo.value = null
    }

    fun addComment(videoId: Long, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addComment(
                videoId = videoId,
                authorName = "Alex Rivera",
                authorHandle = "@alex_creative",
                text = text.trim()
            )
        }
    }

    fun toggleCommentLike(comment: CommentEntity) {
        viewModelScope.launch {
            repository.toggleCommentLike(comment.id, comment.isLiked)
        }
    }

    fun openShare(video: VideoEntity) {
        _activeShareVideo.value = video
    }

    fun closeShare() {
        _activeShareVideo.value = null
    }

    fun copyVideoLink(video: VideoEntity) {
        _toastMessage.value = "Link copied to clipboard!"
        closeShare()
    }

    fun repostVideo(video: VideoEntity) {
        _toastMessage.value = "Reposted to your feed!"
        closeShare()
    }

    fun openSoundDetail(video: VideoEntity) {
        _activeSoundVideo.value = video
    }

    fun closeSoundDetail() {
        _activeSoundVideo.value = null
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun uploadVideo(
        caption: String,
        soundTitle: String,
        soundAuthor: String,
        coverResName: String
    ) {
        viewModelScope.launch {
            repository.postNewVideo(
                caption = caption,
                soundTitle = soundTitle,
                soundAuthor = soundAuthor,
                coverResName = coverResName,
                category = "fyp"
            )
            _toastMessage.value = "Video published successfully! 🎉"
            currentTab.value = MainTab.HOME
        }
    }
}
