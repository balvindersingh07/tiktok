package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tashan Central Preferences Manager
 * Persists user settings in local SharedPreferences and exposes reactive StateFlows.
 * Survives process death and restarts.
 */
class TashanPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ------------------------------------------------------------------------
    // 1. Privacy Controls
    // ------------------------------------------------------------------------
    private val _isPrivateAccount = MutableStateFlow(prefs.getBoolean(KEY_IS_PRIVATE, false))
    val isPrivateAccount: StateFlow<Boolean> = _isPrivateAccount.asStateFlow()

    private val _allowComments = MutableStateFlow(prefs.getBoolean(KEY_ALLOW_COMMENTS, true))
    val allowComments: StateFlow<Boolean> = _allowComments.asStateFlow()

    private val _allowDirectMessages = MutableStateFlow(prefs.getString(KEY_ALLOW_DM, "EVERYONE") ?: "EVERYONE")
    val allowDirectMessages: StateFlow<String> = _allowDirectMessages.asStateFlow()

    private val _allowDownloads = MutableStateFlow(prefs.getBoolean(KEY_ALLOW_DOWNLOADS, true))
    val allowDownloads: StateFlow<Boolean> = _allowDownloads.asStateFlow()

    private val _allowDuet = MutableStateFlow(prefs.getBoolean(KEY_ALLOW_DUET, true))
    val allowDuet: StateFlow<Boolean> = _allowDuet.asStateFlow()

    private val _allowStitch = MutableStateFlow(prefs.getBoolean(KEY_ALLOW_STITCH, true))
    val allowStitch: StateFlow<Boolean> = _allowStitch.asStateFlow()

    private val _activityStatus = MutableStateFlow(prefs.getBoolean(KEY_ACTIVITY_STATUS, true))
    val activityStatus: StateFlow<Boolean> = _activityStatus.asStateFlow()

    private val _followingListVisibility = MutableStateFlow(prefs.getString(KEY_FOLLOWING_VISIBILITY, "PUBLIC") ?: "PUBLIC")
    val followingListVisibility: StateFlow<String> = _followingListVisibility.asStateFlow()

    private val _likedVideosVisibility = MutableStateFlow(prefs.getString(KEY_LIKED_VISIBILITY, "PUBLIC") ?: "PUBLIC")
    val likedVideosVisibility: StateFlow<String> = _likedVideosVisibility.asStateFlow()

    // ------------------------------------------------------------------------
    // 2. Notification Preferences
    // ------------------------------------------------------------------------
    private val _masterNotifications = MutableStateFlow(prefs.getBoolean(KEY_NOTIFY_MASTER, true))
    val masterNotifications: StateFlow<Boolean> = _masterNotifications.asStateFlow()

    private val _notifyLikes = MutableStateFlow(prefs.getBoolean(KEY_NOTIFY_LIKES, true))
    val notifyLikes: StateFlow<Boolean> = _notifyLikes.asStateFlow()

    private val _notifyComments = MutableStateFlow(prefs.getBoolean(KEY_NOTIFY_COMMENTS, true))
    val notifyComments: StateFlow<Boolean> = _notifyComments.asStateFlow()

    private val _notifyReplies = MutableStateFlow(prefs.getBoolean(KEY_NOTIFY_REPLIES, true))
    val notifyReplies: StateFlow<Boolean> = _notifyReplies.asStateFlow()

    private val _notifyNewFollowers = MutableStateFlow(prefs.getBoolean(KEY_NOTIFY_FOLLOWERS, true))
    val notifyNewFollowers: StateFlow<Boolean> = _notifyNewFollowers.asStateFlow()

    private val _notifyReposts = MutableStateFlow(prefs.getBoolean(KEY_NOTIFY_REPOSTS, true))
    val notifyReposts: StateFlow<Boolean> = _notifyReposts.asStateFlow()

    private val _notifyMentions = MutableStateFlow(prefs.getBoolean(KEY_NOTIFY_MENTIONS, true))
    val notifyMentions: StateFlow<Boolean> = _notifyMentions.asStateFlow()

    private val _notifyDirectMessages = MutableStateFlow(prefs.getBoolean(KEY_NOTIFY_DM, true))
    val notifyDirectMessages: StateFlow<Boolean> = _notifyDirectMessages.asStateFlow()

    // ------------------------------------------------------------------------
    // 3. Feed & Recommendation Controls
    // ------------------------------------------------------------------------
    private val _autoplay = MutableStateFlow(prefs.getBoolean(KEY_AUTOPLAY, true))
    val autoplay: StateFlow<Boolean> = _autoplay.asStateFlow()

    private val _loopVideos = MutableStateFlow(prefs.getBoolean(KEY_LOOP_VIDEOS, true))
    val loopVideos: StateFlow<Boolean> = _loopVideos.asStateFlow()

    private val _muteByDefault = MutableStateFlow(prefs.getBoolean(KEY_MUTE_BY_DEFAULT, false))
    val muteByDefault: StateFlow<Boolean> = _muteByDefault.asStateFlow()

    private val _dataSaver = MutableStateFlow(prefs.getBoolean(KEY_DATA_SAVER, false))
    val dataSaver: StateFlow<Boolean> = _dataSaver.asStateFlow()

    // ------------------------------------------------------------------------
    // 5. Appearance
    // ------------------------------------------------------------------------
    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "DARK") ?: "DARK")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _reduceMotion = MutableStateFlow(prefs.getBoolean(KEY_REDUCE_MOTION, false))
    val reduceMotion: StateFlow<Boolean> = _reduceMotion.asStateFlow()

    private val _animationScale = MutableStateFlow(prefs.getString(KEY_ANIMATION_SCALE, "STANDARD") ?: "STANDARD")
    val animationScale: StateFlow<String> = _animationScale.asStateFlow()

    private val _textSizePreference = MutableStateFlow(prefs.getString(KEY_TEXT_SIZE, "STANDARD") ?: "STANDARD")
    val textSizePreference: StateFlow<String> = _textSizePreference.asStateFlow()

    // ------------------------------------------------------------------------
    // 8. Security
    // ------------------------------------------------------------------------
    private val _appLockEnabled = MutableStateFlow(prefs.getBoolean(KEY_APP_LOCK, false))
    val appLockEnabled: StateFlow<Boolean> = _appLockEnabled.asStateFlow()

    private val _biometricLockEnabled = MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_LOCK, false))
    val biometricLockEnabled: StateFlow<Boolean> = _biometricLockEnabled.asStateFlow()

    // ------------------------------------------------------------------------
    // Mutators
    // ------------------------------------------------------------------------
    fun setPrivateAccount(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IS_PRIVATE, enabled).apply()
        _isPrivateAccount.value = enabled
    }

    fun setAllowComments(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALLOW_COMMENTS, enabled).apply()
        _allowComments.value = enabled
    }

    fun setAllowDirectMessages(option: String) {
        prefs.edit().putString(KEY_ALLOW_DM, option).apply()
        _allowDirectMessages.value = option
    }

    fun setAllowDownloads(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALLOW_DOWNLOADS, enabled).apply()
        _allowDownloads.value = enabled
    }

    fun setAllowDuet(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALLOW_DUET, enabled).apply()
        _allowDuet.value = enabled
    }

    fun setAllowStitch(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ALLOW_STITCH, enabled).apply()
        _allowStitch.value = enabled
    }

    fun setActivityStatus(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ACTIVITY_STATUS, enabled).apply()
        _activityStatus.value = enabled
    }

    fun setFollowingListVisibility(option: String) {
        prefs.edit().putString(KEY_FOLLOWING_VISIBILITY, option).apply()
        _followingListVisibility.value = option
    }

    fun setLikedVideosVisibility(option: String) {
        prefs.edit().putString(KEY_LIKED_VISIBILITY, option).apply()
        _likedVideosVisibility.value = option
    }

    // Notification Mutators
    fun setMasterNotifications(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY_MASTER, enabled).apply()
        _masterNotifications.value = enabled
    }

    fun setNotifyLikes(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY_LIKES, enabled).apply()
        _notifyLikes.value = enabled
    }

    fun setNotifyComments(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY_COMMENTS, enabled).apply()
        _notifyComments.value = enabled
    }

    fun setNotifyReplies(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY_REPLIES, enabled).apply()
        _notifyReplies.value = enabled
    }

    fun setNotifyNewFollowers(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY_FOLLOWERS, enabled).apply()
        _notifyNewFollowers.value = enabled
    }

    fun setNotifyReposts(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY_REPOSTS, enabled).apply()
        _notifyReposts.value = enabled
    }

    fun setNotifyMentions(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY_MENTIONS, enabled).apply()
        _notifyMentions.value = enabled
    }

    fun setNotifyDirectMessages(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY_DM, enabled).apply()
        _notifyDirectMessages.value = enabled
    }

    // Feed Mutators
    fun setAutoplay(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTOPLAY, enabled).apply()
        _autoplay.value = enabled
    }

    fun setLoopVideos(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOOP_VIDEOS, enabled).apply()
        _loopVideos.value = enabled
    }

    fun setMuteByDefault(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MUTE_BY_DEFAULT, enabled).apply()
        _muteByDefault.value = enabled
    }

    fun setDataSaver(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DATA_SAVER, enabled).apply()
        _dataSaver.value = enabled
    }

    // Appearance Mutators
    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setReduceMotion(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCE_MOTION, enabled).apply()
        _reduceMotion.value = enabled
    }

    fun setAnimationScale(scale: String) {
        prefs.edit().putString(KEY_ANIMATION_SCALE, scale).apply()
        _animationScale.value = scale
    }

    fun setTextSizePreference(pref: String) {
        prefs.edit().putString(KEY_TEXT_SIZE, pref).apply()
        _textSizePreference.value = pref
    }

    // Security Mutators
    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCK, enabled).apply()
        _appLockEnabled.value = enabled
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK, enabled).apply()
        _biometricLockEnabled.value = enabled
    }

    companion object {
        private const val PREFS_NAME = "tashan_settings_prefs"

        private const val KEY_IS_PRIVATE = "pref_is_private"
        private const val KEY_ALLOW_COMMENTS = "pref_allow_comments"
        private const val KEY_ALLOW_DM = "pref_allow_dm"
        private const val KEY_ALLOW_DOWNLOADS = "pref_allow_downloads"
        private const val KEY_ALLOW_DUET = "pref_allow_duet"
        private const val KEY_ALLOW_STITCH = "pref_allow_stitch"
        private const val KEY_ACTIVITY_STATUS = "pref_activity_status"
        private const val KEY_FOLLOWING_VISIBILITY = "pref_following_visibility"
        private const val KEY_LIKED_VISIBILITY = "pref_liked_visibility"

        private const val KEY_NOTIFY_MASTER = "pref_notify_master"
        private const val KEY_NOTIFY_LIKES = "pref_notify_likes"
        private const val KEY_NOTIFY_COMMENTS = "pref_notify_comments"
        private const val KEY_NOTIFY_REPLIES = "pref_notify_replies"
        private const val KEY_NOTIFY_FOLLOWERS = "pref_notify_followers"
        private const val KEY_NOTIFY_REPOSTS = "pref_notify_reposts"
        private const val KEY_NOTIFY_MENTIONS = "pref_notify_mentions"
        private const val KEY_NOTIFY_DM = "pref_notify_dm"

        private const val KEY_AUTOPLAY = "pref_autoplay"
        private const val KEY_LOOP_VIDEOS = "pref_loop_videos"
        private const val KEY_MUTE_BY_DEFAULT = "pref_mute_by_default"
        private const val KEY_DATA_SAVER = "pref_data_saver"

        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_REDUCE_MOTION = "pref_reduce_motion"
        private const val KEY_ANIMATION_SCALE = "pref_animation_scale"
        private const val KEY_TEXT_SIZE = "pref_text_size"

        private const val KEY_APP_LOCK = "pref_app_lock"
        private const val KEY_BIOMETRIC_LOCK = "pref_biometric_lock"
    }
}
