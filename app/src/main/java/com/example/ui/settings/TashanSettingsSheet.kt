package com.example.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.TikTokViewModel
import com.example.ui.theme.TikTokBlack
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TikTokDarkSurface
import com.example.ui.theme.TikTokGray
import com.example.ui.theme.TikTokPink
import com.example.ui.theme.TikTokWhite
import com.example.ui.theme.TikTokWhite40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TashanSettingsSheet(
    viewModel: TikTokViewModel,
    onDismiss: () -> Unit,
    onOpenBackendConsole: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Observe all preferences
    val privateAccount by viewModel.preferences.isPrivateAccount.collectAsStateWithLifecycle()
    val allowComments by viewModel.preferences.allowComments.collectAsStateWithLifecycle()
    val allowDirectMessages by viewModel.preferences.allowDirectMessages.collectAsStateWithLifecycle()
    val allowDownloads by viewModel.preferences.allowDownloads.collectAsStateWithLifecycle()
    val allowDuet by viewModel.preferences.allowDuet.collectAsStateWithLifecycle()
    val allowStitch by viewModel.preferences.allowStitch.collectAsStateWithLifecycle()
    val activityStatus by viewModel.preferences.activityStatus.collectAsStateWithLifecycle()
    val followingListVisibility by viewModel.preferences.followingListVisibility.collectAsStateWithLifecycle()
    val likedVideosVisibility by viewModel.preferences.likedVideosVisibility.collectAsStateWithLifecycle()

    val masterNotifications by viewModel.preferences.masterNotifications.collectAsStateWithLifecycle()
    val notifyLikes by viewModel.preferences.notifyLikes.collectAsStateWithLifecycle()
    val notifyComments by viewModel.preferences.notifyComments.collectAsStateWithLifecycle()
    val notifyReplies by viewModel.preferences.notifyReplies.collectAsStateWithLifecycle()
    val notifyNewFollowers by viewModel.preferences.notifyNewFollowers.collectAsStateWithLifecycle()
    val notifyReposts by viewModel.preferences.notifyReposts.collectAsStateWithLifecycle()
    val notifyMentions by viewModel.preferences.notifyMentions.collectAsStateWithLifecycle()
    val notifyDirectMessages by viewModel.preferences.notifyDirectMessages.collectAsStateWithLifecycle()

    val autoplay by viewModel.preferences.autoplay.collectAsStateWithLifecycle()
    val loopVideos by viewModel.preferences.loopVideos.collectAsStateWithLifecycle()
    val muteByDefault by viewModel.preferences.muteByDefault.collectAsStateWithLifecycle()
    val dataSaver by viewModel.preferences.dataSaver.collectAsStateWithLifecycle()

    val themeMode by viewModel.preferences.themeMode.collectAsStateWithLifecycle()
    val reduceMotion by viewModel.preferences.reduceMotion.collectAsStateWithLifecycle()
    val animationScale by viewModel.preferences.animationScale.collectAsStateWithLifecycle()
    val textSizePref by viewModel.preferences.textSizePreference.collectAsStateWithLifecycle()

    val appLockEnabled by viewModel.preferences.appLockEnabled.collectAsStateWithLifecycle()
    val biometricLockEnabled by viewModel.preferences.biometricLockEnabled.collectAsStateWithLifecycle()

    val storageBreakdown by viewModel.storageBreakdown.collectAsStateWithLifecycle()
    val blockedUsers by viewModel.blockedUsers.collectAsStateWithLifecycle()
    val mutedUsers by viewModel.mutedUsers.collectAsStateWithLifecycle()
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val recentlyUsedSounds by viewModel.recentlyUsedSounds.collectAsStateWithLifecycle()

    // Dialog navigation state
    var showBlockedDialog by remember { mutableStateOf(false) }
    var showMutedDialog by remember { mutableStateOf(false) }
    var showWatchHistoryDialog by remember { mutableStateOf(false) }
    var showSearchHistoryDialog by remember { mutableStateOf(false) }
    var showSoundsHistoryDialog by remember { mutableStateOf(false) }

    var showResetRecsConfirmation by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var showHowItWorksDialog by remember { mutableStateOf(false) }
    var showGuidelinesDialog by remember { mutableStateOf(false) }
    var showSafetyDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showAppInfoDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = TikTokBlack,
        dragHandle = null,
        modifier = modifier
            .fillMaxSize()
            .testTag("tashan_settings_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header with rainbow gradient title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close Settings",
                        tint = TikTokWhite
                    )
                }

                Text(
                    text = "Settings and Privacy",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TikTokWhite
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(TikTokPink, TikTokCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        tint = TikTokWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0x1FFFFFFF))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // -------------------------------------------------------------
                // 1. PRIVACY CONTROLS
                // -------------------------------------------------------------
                item {
                    SettingsSectionHeader("Privacy Controls", Icons.Filled.Lock)
                    SettingsCard {
                        SettingsSwitchRow(
                            title = "Private Account",
                            subtitle = "Only approved followers can view your videos and details",
                            checked = privateAccount,
                            onCheckedChange = { viewModel.setPrivateAccount(it) },
                            testTag = "switch_private_account"
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsSwitchRow(
                            title = "Allow Comments",
                            subtitle = "Choose if other users can comment on your clips",
                            checked = allowComments,
                            onCheckedChange = { viewModel.setAllowComments(it) },
                            testTag = "switch_allow_comments"
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsChoiceRow(
                            title = "Direct Messages",
                            subtitle = "Who can send you private chat messages",
                            currentValue = allowDirectMessages,
                            options = listOf("EVERYONE", "FOLLOWING", "NO_ONE"),
                            onSelect = { viewModel.setAllowDirectMessages(it) }
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsSwitchRow(
                            title = "Allow Downloads",
                            subtitle = "Let viewers save your videos to device gallery",
                            checked = allowDownloads,
                            onCheckedChange = { viewModel.setAllowDownloads(it) },
                            testTag = "switch_allow_downloads"
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsSwitchRow(
                            title = "Allow Duet",
                            subtitle = "Allow other creators to post side-by-side reactions",
                            checked = allowDuet,
                            onCheckedChange = { viewModel.setAllowDuet(it) },
                            testTag = "switch_allow_duet"
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsSwitchRow(
                            title = "Allow Stitch",
                            subtitle = "Allow others to clip up to 5s of your videos",
                            checked = allowStitch,
                            onCheckedChange = { viewModel.setAllowStitch(it) },
                            testTag = "switch_allow_stitch"
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsSwitchRow(
                            title = "Activity Status",
                            subtitle = "Show when you are active to mutual followers",
                            checked = activityStatus,
                            onCheckedChange = { viewModel.setActivityStatus(it) },
                            testTag = "switch_activity_status"
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsChoiceRow(
                            title = "Following List Visibility",
                            subtitle = "Who can view accounts you follow",
                            currentValue = followingListVisibility,
                            options = listOf("EVERYONE", "ONLY_ME"),
                            onSelect = { viewModel.setFollowingListVisibility(it) }
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsChoiceRow(
                            title = "Liked Videos Visibility",
                            subtitle = "Who can view clips you've liked",
                            currentValue = likedVideosVisibility,
                            options = listOf("EVERYONE", "ONLY_ME"),
                            onSelect = { viewModel.setLikedVideosVisibility(it) }
                        )
                    }
                }

                // -------------------------------------------------------------
                // 2. NOTIFICATIONS
                // -------------------------------------------------------------
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    SettingsSectionHeader("Notification Preferences", Icons.Filled.Notifications)
                    SettingsCard {
                        SettingsSwitchRow(
                            title = "Master Notification Switch",
                            subtitle = "Enable or pause all in-app notification alerts",
                            checked = masterNotifications,
                            onCheckedChange = { viewModel.setMasterNotifications(it) },
                            testTag = "switch_master_notifications"
                        )
                        AnimatedVisibility(visible = masterNotifications) {
                            Column {
                                HorizontalDivider(color = Color(0x14FFFFFF))
                                SettingsSwitchRow(
                                    title = "Likes",
                                    subtitle = "Notify when someone likes your content",
                                    checked = notifyLikes,
                                    onCheckedChange = { viewModel.setNotifyLikes(it) },
                                    testTag = "switch_notify_likes"
                                )
                                HorizontalDivider(color = Color(0x14FFFFFF))
                                SettingsSwitchRow(
                                    title = "Comments",
                                    subtitle = "Notify when someone comments on your clips",
                                    checked = notifyComments,
                                    onCheckedChange = { viewModel.setNotifyComments(it) },
                                    testTag = "switch_notify_comments"
                                )
                                HorizontalDivider(color = Color(0x14FFFFFF))
                                SettingsSwitchRow(
                                    title = "Replies",
                                    subtitle = "Notify when someone replies to your comment",
                                    checked = notifyReplies,
                                    onCheckedChange = { viewModel.setNotifyReplies(it) },
                                    testTag = "switch_notify_replies"
                                )
                                HorizontalDivider(color = Color(0x14FFFFFF))
                                SettingsSwitchRow(
                                    title = "New Followers",
                                    subtitle = "Notify when a user starts following your profile",
                                    checked = notifyNewFollowers,
                                    onCheckedChange = { viewModel.setNotifyNewFollowers(it) },
                                    testTag = "switch_notify_followers"
                                )
                                HorizontalDivider(color = Color(0x14FFFFFF))
                                SettingsSwitchRow(
                                    title = "Reposts",
                                    subtitle = "Notify when someone reposts your videos",
                                    checked = notifyReposts,
                                    onCheckedChange = { viewModel.setNotifyReposts(it) },
                                    testTag = "switch_notify_reposts"
                                )
                                HorizontalDivider(color = Color(0x14FFFFFF))
                                SettingsSwitchRow(
                                    title = "Mentions & Tags",
                                    subtitle = "Notify when you are tagged in captions",
                                    checked = notifyMentions,
                                    onCheckedChange = { viewModel.setNotifyMentions(it) },
                                    testTag = "switch_notify_mentions"
                                )
                                HorizontalDivider(color = Color(0x14FFFFFF))
                                SettingsSwitchRow(
                                    title = "Direct Messages",
                                    subtitle = "Notify when new chat messages arrive",
                                    checked = notifyDirectMessages,
                                    onCheckedChange = { viewModel.setNotifyDirectMessages(it) },
                                    testTag = "switch_notify_dms"
                                )
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // 3. FEED & RECOMMENDATION
                // -------------------------------------------------------------
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    SettingsSectionHeader("Feed & Recommendation", Icons.Filled.Tune)
                    SettingsCard {
                        SettingsSwitchRow(
                            title = "Autoplay Videos",
                            subtitle = "Automatically start playback when scrolling feed",
                            checked = autoplay,
                            onCheckedChange = { viewModel.setAutoplay(it) },
                            testTag = "switch_autoplay"
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsSwitchRow(
                            title = "Loop Videos",
                            subtitle = "Seamlessly loop current video until scrolled",
                            checked = loopVideos,
                            onCheckedChange = { viewModel.setLoopVideos(it) },
                            testTag = "switch_loop_videos"
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsSwitchRow(
                            title = "Mute by Default",
                            subtitle = "Always enter feed with volume muted",
                            checked = muteByDefault,
                            onCheckedChange = { viewModel.setMuteByDefault(it) },
                            testTag = "switch_mute_default"
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsSwitchRow(
                            title = "Data Saver Mode",
                            subtitle = "Adjust playback buffer and network caching",
                            checked = dataSaver,
                            onCheckedChange = { viewModel.setDataSaver(it) },
                            testTag = "switch_data_saver"
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsActionRow(
                            title = "Reset Recommendation Preferences",
                            subtitle = "Reverts on-device category weights back to baseline",
                            actionLabel = "Reset",
                            actionColor = TikTokPink,
                            onClick = { showResetRecsConfirmation = true }
                        )
                    }
                }

                // -------------------------------------------------------------
                // 4. MEDIA & STORAGE
                // -------------------------------------------------------------
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    SettingsSectionHeader("Media & Storage", Icons.Filled.Storage)
                    SettingsCard {
                        // Storage breakdown summary
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E1E26))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "Device Storage Footprint",
                                color = TikTokWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StorageStatItem("Total App", storageBreakdown.totalFormattedMb)
                                StorageStatItem("Video Media", storageBreakdown.videosFormattedMb)
                                StorageStatItem("Thumbnails", storageBreakdown.thumbnailsFormattedMb)
                                StorageStatItem("Temp Cache", storageBreakdown.cacheFormattedMb)
                            }
                        }
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsActionRow(
                            title = "Clear Video Cache",
                            subtitle = "Deletes cached downloaded streaming clips",
                            actionLabel = "Clear",
                            actionColor = TikTokCyan,
                            onClick = { viewModel.clearVideoCache() }
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsActionRow(
                            title = "Clear Thumbnail Cache",
                            subtitle = "Deletes cached cover previews and avatars",
                            actionLabel = "Clear",
                            actionColor = TikTokCyan,
                            onClick = { viewModel.clearThumbnailCache() }
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsActionRow(
                            title = "Clear Temporary Files",
                            subtitle = "Purges scratch files from app temporary folder",
                            actionLabel = "Clear",
                            actionColor = TikTokCyan,
                            onClick = { viewModel.clearTemporaryFiles() }
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsActionRow(
                            title = "Repair Media Index & Cleanup",
                            subtitle = "Verifies active SQLite video files and purges orphans",
                            actionLabel = "Verify",
                            actionColor = TikTokWhite,
                            onClick = { viewModel.repairMediaIndex() }
                        )
                    }
                }

                // -------------------------------------------------------------
                // 5. APPEARANCE
                // -------------------------------------------------------------
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    SettingsSectionHeader("Appearance & Display", Icons.Filled.Palette)
                    SettingsCard {
                        SettingsChoiceRow(
                            title = "App Theme",
                            subtitle = "Color system and contrast mode",
                            currentValue = themeMode,
                            options = listOf("DARK", "LIGHT", "SYSTEM"),
                            onSelect = { viewModel.setThemeMode(it) }
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsSwitchRow(
                            title = "Reduce Motion",
                            subtitle = "Minimize camera zooms, feed pulses, and background motion",
                            checked = reduceMotion,
                            onCheckedChange = { viewModel.setReduceMotion(it) },
                            testTag = "switch_reduce_motion"
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsChoiceRow(
                            title = "Animation Scale",
                            subtitle = "Transition velocity across sheets and feeds",
                            currentValue = animationScale,
                            options = listOf("STANDARD", "REDUCED", "OFF"),
                            onSelect = { viewModel.setAnimationScale(it) }
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsChoiceRow(
                            title = "Text Size",
                            subtitle = "Scaling factor for captions and comments",
                            currentValue = textSizePref,
                            options = listOf("STANDARD", "LARGE", "XLARGE"),
                            onSelect = { viewModel.setTextSizePreference(it) }
                        )
                    }
                }

                // -------------------------------------------------------------
                // 6. BLOCKED & MUTED USERS
                // -------------------------------------------------------------
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    SettingsSectionHeader("Blocked & Muted Users", Icons.Filled.Block)
                    SettingsCard {
                        SettingsNavRow(
                            title = "Blocked Accounts",
                            subtitle = "${blockedUsers.size} user(s) blocked from interacting with you",
                            onClick = { showBlockedDialog = true },
                            testTag = "row_blocked_users"
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsNavRow(
                            title = "Muted Accounts",
                            subtitle = "${mutedUsers.size} user(s) muted in conversations and notifications",
                            onClick = { showMutedDialog = true },
                            testTag = "row_muted_users"
                        )
                    }
                }

                // -------------------------------------------------------------
                // 7. HISTORY
                // -------------------------------------------------------------
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    SettingsSectionHeader("History Management", Icons.Filled.History)
                    SettingsCard {
                        SettingsNavRow(
                            title = "Watch History",
                            subtitle = "${watchHistory.size} video(s) watched on this device",
                            onClick = { showWatchHistoryDialog = true },
                            testTag = "row_watch_history"
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsNavRow(
                            title = "Search History",
                            subtitle = "${searchHistory.size} query term(s) recorded",
                            onClick = { showSearchHistoryDialog = true },
                            testTag = "row_search_history"
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsNavRow(
                            title = "Sound History",
                            subtitle = "${recentlyUsedSounds.size} audio track(s) recently selected",
                            onClick = { showSoundsHistoryDialog = true },
                            testTag = "row_sound_history"
                        )
                    }
                }

                // -------------------------------------------------------------
                // 8. SECURITY
                // -------------------------------------------------------------
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    SettingsSectionHeader("Security & Sessions", Icons.Filled.Security)
                    SettingsCard {
                        SettingsSwitchRow(
                            title = "App Lock PIN",
                            subtitle = "Require authentication when opening Tashan",
                            checked = appLockEnabled,
                            onCheckedChange = { viewModel.setAppLockEnabled(it) },
                            testTag = "switch_app_lock"
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsSwitchRow(
                            title = "Biometric Authentication",
                            subtitle = "Use device fingerprint / face unlock",
                            checked = biometricLockEnabled,
                            onCheckedChange = { viewModel.setBiometricLockEnabled(it) },
                            testTag = "switch_biometric_lock"
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsActionRow(
                            title = "Log Out & Clear Session",
                            subtitle = "Terminates active session and resets credentials",
                            actionLabel = "Log Out",
                            actionColor = Color(0xFFE53935),
                            onClick = { showLogoutConfirmation = true }
                        )
                    }
                }

                // -------------------------------------------------------------
                // 9. HELP & ABOUT
                // -------------------------------------------------------------
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    SettingsSectionHeader("Help & About", Icons.AutoMirrored.Filled.HelpOutline)
                    SettingsCard {
                        SettingsNavRow(
                            title = "Frequently Asked Questions",
                            subtitle = "Common questions regarding storage, feed algorithm, and features",
                            onClick = { showFaqDialog = true },
                            testTag = "row_faq"
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsNavRow(
                            title = "How Tashan Works",
                            subtitle = "Explore our on-device architecture and zero-cloud privacy model",
                            onClick = { showHowItWorksDialog = true }
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsNavRow(
                            title = "Community Guidelines",
                            subtitle = "Standards for creative expression, safety, and mutual respect",
                            onClick = { showGuidelinesDialog = true }
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsNavRow(
                            title = "Safety Center",
                            subtitle = "Tools and controls for managing safety, blocks, and reports",
                            onClick = { showSafetyDialog = true }
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsNavRow(
                            title = "Privacy Policy",
                            subtitle = "Local data sovereignty guarantee and storage disclosures",
                            onClick = { showPrivacyPolicyDialog = true }
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsNavRow(
                            title = "Terms of Service",
                            subtitle = "Local device application license terms",
                            onClick = { showTermsDialog = true }
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsNavRow(
                            title = "Open Source Licenses",
                            subtitle = "Third-party libraries, software acknowledgements, and credits",
                            onClick = { showLicensesDialog = true }
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsNavRow(
                            title = "App Version & Environment",
                            subtitle = "Tashan v2.5.0 Production (Build 2026.09.17)",
                            onClick = { showAppInfoDialog = true }
                        )
                        HorizontalDivider(color = Color(0x14FFFFFF))
                        SettingsNavRow(
                            title = "Developer & Backend Console",
                            subtitle = "Inspect SQLite database, seed viral clips, and view real-time stats",
                            onClick = onOpenBackendConsole,
                            icon = Icons.Filled.DeveloperMode,
                            accentColor = TikTokCyan
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Tashan Local Edition • Made with Jetpack Compose & Room",
                        color = TikTokWhite40,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // Sub-Dialogs & Dialog Flows
    // ------------------------------------------------------------------------

    if (showBlockedDialog) {
        BlockedUsersDialog(
            blockedUsers = blockedUsers,
            onUnblock = { viewModel.unblockUser(it) },
            onDismiss = { showBlockedDialog = false }
        )
    }

    if (showMutedDialog) {
        MutedUsersDialog(
            mutedUsers = mutedUsers,
            onUnmute = { viewModel.unmuteUser(it) },
            onDismiss = { showMutedDialog = false }
        )
    }

    if (showWatchHistoryDialog) {
        WatchHistoryDialog(
            videos = watchHistory,
            onDeleteSingle = { viewModel.deleteWatchHistoryItem(it) },
            onClearAll = { viewModel.clearWatchHistory() },
            onDismiss = { showWatchHistoryDialog = false }
        )
    }

    if (showSearchHistoryDialog) {
        SearchHistoryDialog(
            searchHistory = searchHistory,
            onDeleteSingle = { viewModel.deleteSearchQuery(it) },
            onClearAll = { viewModel.clearSearchHistory() },
            onDismiss = { showSearchHistoryDialog = false }
        )
    }

    if (showSoundsHistoryDialog) {
        SoundsHistoryDialog(
            sounds = recentlyUsedSounds,
            onClearSingle = { viewModel.clearRecentlyUsedSound(it) },
            onClearAll = { viewModel.clearAllRecentlyUsedSounds() },
            onDismiss = { showSoundsHistoryDialog = false }
        )
    }

    if (showResetRecsConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetRecsConfirmation = false },
            containerColor = TikTokDarkSurface,
            title = { Text("Reset Recommendations?", color = TikTokWhite, fontWeight = FontWeight.Bold) },
            text = { Text("This will clear on-device engagement history (views, completion rates, category affinities) and reset the For You feed to baseline order.", color = TikTokWhite40) },
            confirmButton = {
                Button(
                    onClick = {
                        showResetRecsConfirmation = false
                        viewModel.resetRecommendationPreferences()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TikTokPink)
                ) {
                    Text("Reset Feed", color = TikTokWhite)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetRecsConfirmation = false }) {
                    Text("Cancel", color = TikTokWhite40)
                }
            }
        )
    }

    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            containerColor = TikTokDarkSurface,
            title = { Text("Log Out?", color = TikTokWhite, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out? Stored session credentials will be cleared.", color = TikTokWhite40) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmation = false
                        viewModel.clearSavedSessionCredentials()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Log Out", color = TikTokWhite)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutConfirmation = false }) {
                    Text("Cancel", color = TikTokWhite40)
                }
            }
        )
    }

    if (showFaqDialog) {
        FaqDialog(onDismiss = { showFaqDialog = false })
    }

    if (showHowItWorksDialog) {
        BundledDocumentDialog(
            title = "How Tashan Works",
            subtitle = "On-device architecture & zero-cloud privacy",
            sections = listOf(
                "Local-First SQLite Engine" to "Tashan operates a complete embedded server on your device using Android Room. All user profiles, video metadata, comments, direct messages, and notification counters live in local SQLite tables.",
                "Client-Side Recommendation AI" to "Unlike traditional social networks that track your habits across the web, Tashan's recommendation algorithm processes interactions strictly on your CPU. It computes category vectors and ranks videos in real-time.",
                "Private Media Storage" to "Videos recorded, imported, or downloaded are organized in your private application sandbox directory (`/files/videos/` and `/files/thumbnails/`)."
            ),
            onDismiss = { showHowItWorksDialog = false }
        )
    }

    if (showGuidelinesDialog) {
        BundledDocumentDialog(
            title = "Community Guidelines",
            subtitle = "Standards for creative expression",
            sections = listOf(
                "Respect & Civility" to "Tashan is an expressive platform for creators. We encourage uplifting interactions and prohibit harassment, bullying, and hate speech.",
                "Originality & Attribution" to "Credit original creators when using sounds, Duets, and Stitches. Respect intellectual property and creative ownership.",
                "Safety & Protection" to "Content depicting dangerous activities, violence, or unlawful conduct is strictly disallowed in local and exported media."
            ),
            onDismiss = { showGuidelinesDialog = false }
        )
    }

    if (showSafetyDialog) {
        BundledDocumentDialog(
            title = "Safety Center",
            subtitle = "Empowering creator safety & peace of mind",
            sections = listOf(
                "Muting & Blocking" to "You can block any user to completely eliminate their presence from your feeds and direct messages. Muting silences notifications while keeping profile views open.",
                "Private Profile Controls" to "Switching your account to 'Private' prevents unapproved users from viewing your clips, liked videos, and follower lists.",
                "Comment & DM Filters" to "Restrict comment access to mutual followers or turn comments off entirely per video or globally."
            ),
            onDismiss = { showSafetyDialog = false }
        )
    }

    if (showPrivacyPolicyDialog) {
        BundledDocumentDialog(
            title = "Privacy Policy",
            subtitle = "Local Data Sovereignty Guarantee",
            sections = listOf(
                "Zero Remote Tracking" to "Tashan does not transmit your personal data, video files, comments, or analytics to external servers. Your phone is your data center.",
                "Device Permissions" to "Camera, microphone, and storage permissions are accessed solely for live recording, video import, and exporting to gallery upon user request.",
                "Data Deletion" to "You retain complete control. You can clear watch history, search queries, media caches, or reset the entire database at any time from these settings."
            ),
            onDismiss = { showPrivacyPolicyDialog = false }
        )
    }

    if (showTermsDialog) {
        BundledDocumentDialog(
            title = "Terms of Service",
            subtitle = "Application license & usage agreement",
            sections = listOf(
                "Personal License" to "Tashan is provided as an open-architecture, local-first short video application for personal, non-commercial entertainment and creation.",
                "User Generated Content" to "You own all content you create, record, or import into the application sandbox.",
                "Disclaimer" to "The software is provided 'as is' without warranties of any kind regarding third-party external media codecs or custom ROM compatibility."
            ),
            onDismiss = { showTermsDialog = false }
        )
    }

    if (showLicensesDialog) {
        BundledDocumentDialog(
            title = "Open Source Acknowledgements",
            subtitle = "Libraries and components powering Tashan",
            sections = listOf(
                "AndroidX Jetpack Compose & M3" to "Apache License 2.0 • Modern reactive UI toolkit for Android.",
                "AndroidX Room SQLite" to "Apache License 2.0 • Robust object-relational mapping database engine.",
                "AndroidX Media3 ExoPlayer" to "Apache License 2.0 • High-performance media playback engine.",
                "Coil Image Loader" to "Apache License 2.0 • Fast, lightweight image loading for Kotlin Coroutines.",
                "KotlinX Coroutines & Serialization" to "Apache License 2.0 • Asynchronous reactive streams and state flow."
            ),
            onDismiss = { showLicensesDialog = false }
        )
    }

    if (showAppInfoDialog) {
        AppInfoDialog(onDismiss = { showAppInfoDialog = false })
    }
}

// ----------------------------------------------------------------------------
// Reusable Setting UI Components
// ----------------------------------------------------------------------------

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TikTokCyan,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = TikTokCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TikTokDarkSurface)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TikTokWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TikTokWhite40,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TikTokWhite,
                checkedTrackColor = TikTokPink,
                uncheckedThumbColor = TikTokWhite40,
                uncheckedTrackColor = TikTokGray
            )
        )
    }
}

@Composable
private fun SettingsChoiceRow(
    title: String,
    subtitle: String,
    currentValue: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            color = TikTokWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            color = TikTokWhite40,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { opt ->
                val isSelected = opt.equals(currentValue, ignoreCase = true)
                val label = opt.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) TikTokPink else Color(0xFF22222C))
                        .clickable { onSelect(opt) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) TikTokWhite else TikTokWhite40,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    actionLabel: String,
    actionColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TikTokWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TikTokWhite40,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = actionColor.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = actionLabel,
                color = actionColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SettingsNavRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    accentColor: Color = TikTokWhite,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TikTokWhite40,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = TikTokWhite40,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun StorageStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = TikTokCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, color = TikTokWhite40, fontSize = 10.sp)
    }
}
