package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FeedCategory
import com.example.ui.MainTab
import com.example.ui.TikTokViewModel
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.LocalBackendConsoleSheet
import com.example.ui.components.ShareBottomSheet
import com.example.ui.components.SoundDetailSheet
import com.example.ui.components.TikTokBottomNav
import com.example.ui.components.VideoQrDialog
import com.example.ui.create.CreateVideoScreen
import com.example.ui.discover.DiscoverScreen
import com.example.ui.feed.FeedScreen
import com.example.ui.inbox.InboxScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.settings.TashanSettingsSheet
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TikTokBlack
import com.example.ui.theme.TikTokWhite
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: TikTokViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TikTokApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TikTokApp(viewModel: TikTokViewModel) {
    val context = LocalContext.current

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val feedCategory by viewModel.feedCategory.collectAsStateWithLifecycle()
    val allVideos by viewModel.allVideos.collectAsStateWithLifecycle()
    val fypRankedVideos by viewModel.fypRankedVideos.collectAsStateWithLifecycle()
    val likedVideos by viewModel.likedVideos.collectAsStateWithLifecycle()
    val bookmarkedVideos by viewModel.bookmarkedVideos.collectAsStateWithLifecycle()
    val userProfile by viewModel.activeUserProfile.collectAsStateWithLifecycle()
    val allUserProfiles by viewModel.allUserProfiles.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val allSounds by viewModel.sounds.collectAsStateWithLifecycle()
    val userDrafts by viewModel.userDrafts.collectAsStateWithLifecycle()

    // Active Bottom Sheets
    val activeCommentVideo by viewModel.activeCommentVideo.collectAsStateWithLifecycle()
    val videoComments by viewModel.videoComments.collectAsStateWithLifecycle()
    val replyingToComment by viewModel.replyingToComment.collectAsStateWithLifecycle()

    val activeShareVideo by viewModel.activeShareVideo.collectAsStateWithLifecycle()
    val activeQrVideo by viewModel.qrCodeVideo.collectAsStateWithLifecycle()
    val activeSoundVideo by viewModel.activeSoundVideo.collectAsStateWithLifecycle()
    val preSelectedSound by viewModel.preSelectedSound.collectAsStateWithLifecycle()
    val duetSourceVideo by viewModel.duetSourceVideo.collectAsStateWithLifecycle()
    val stitchSourceVideo by viewModel.stitchSourceVideo.collectAsStateWithLifecycle()

    // Creator Profile & Direct Messaging
    val viewingCreator by viewModel.viewingCreator.collectAsStateWithLifecycle()
    val activeChatUser by viewModel.activeChatUser.collectAsStateWithLifecycle()
    val directMessages by viewModel.directMessages.collectAsStateWithLifecycle()

    // Local Backend Inspector state
    val showBackendConsole by viewModel.showBackendConsole.collectAsStateWithLifecycle()
    val backendStats by viewModel.backendStats.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2200)
            viewModel.clearToast()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TikTokBlack)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = TikTokBlack,
            bottomBar = {
                // Hide bottom nav when in fullscreen camera studio mode
                if (currentTab != MainTab.CREATE) {
                    TikTokBottomNav(
                        selectedTab = currentTab,
                        onTabSelected = { tab ->
                            if (tab == MainTab.PROFILE && viewingCreator != null) {
                                viewModel.closeCreatorProfile()
                            }
                            viewModel.selectTab(tab)
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (currentTab != MainTab.CREATE) innerPadding.calculateBottomPadding() else 0.dp)
            ) {
                when (currentTab) {
                    MainTab.HOME -> {
                        FeedScreen(
                            viewModel = viewModel,
                            videos = if (feedCategory == FeedCategory.FOR_YOU && fypRankedVideos.isNotEmpty()) fypRankedVideos else allVideos,
                            feedCategory = feedCategory,
                            onNavigateToDiscover = { viewModel.selectTab(MainTab.DISCOVER) }
                        )
                    }

                    MainTab.DISCOVER -> {
                        DiscoverScreen(
                            videos = allVideos,
                            onVideoClick = { video ->
                                viewModel.playVideoInFeed(video.id)
                            },
                            onScanQrClick = {
                                if (allVideos.isNotEmpty()) {
                                    viewModel.openShare(allVideos.first())
                                }
                            }
                        )
                    }

                    MainTab.CREATE -> {
                        CreateVideoScreen(
                            onClose = { viewModel.selectTab(MainTab.HOME) },
                            onPublishVideo = { caption, soundTitle, soundAuthor, coverRes, videoPath, isPrivate, allowComments ->
                                viewModel.uploadVideo(
                                    caption = caption,
                                    soundTitle = soundTitle,
                                    soundAuthor = soundAuthor,
                                    coverResName = coverRes,
                                    videoFilePath = videoPath,
                                    isPrivate = isPrivate,
                                    allowComments = allowComments
                                )
                            },
                            onSaveDraft = { caption, soundTitle, soundAuthor, coverRes, videoPath ->
                                viewModel.saveDraft(
                                    caption = caption,
                                    soundTitle = soundTitle,
                                    soundAuthor = soundAuthor,
                                    coverResName = coverRes,
                                    videoPath = videoPath
                                )
                            },
                            onImportUri = { uri ->
                                viewModel.importVideoFromUri(uri)
                            },
                            availableSounds = allSounds,
                            preSelectedSound = preSelectedSound,
                            duetSourceVideo = duetSourceVideo,
                            stitchSourceVideo = stitchSourceVideo
                        )
                    }

                    MainTab.INBOX -> {
                        InboxScreen(
                            notifications = notifications,
                            onMarkAllRead = { viewModel.markAllNotificationsRead() },
                            activeChatUser = activeChatUser,
                            chatMessages = directMessages,
                            onOpenChat = { handle -> viewModel.openChatWith(handle) },
                            onCloseChat = { viewModel.closeChat() },
                            onSendMessage = { text -> viewModel.sendDirectMessage(text) }
                        )
                    }

                    MainTab.PROFILE -> {
                        ProfileScreen(
                            profile = userProfile,
                            viewingCreator = viewingCreator,
                            allVideos = allVideos,
                            likedVideos = likedVideos,
                            bookmarkedVideos = bookmarkedVideos,
                            drafts = userDrafts,
                            allAccounts = allUserProfiles,
                            onVideoClick = { video ->
                                viewModel.playVideoInFeed(video.id)
                            },
                            onOpenBackendConsole = {
                                viewModel.openBackendConsole()
                            },
                            onOpenSettings = {
                                viewModel.openSettings()
                            },
                            onCloseCreatorProfile = {
                                viewModel.closeCreatorProfile()
                            },
                            onMessageCreator = { handle ->
                                viewModel.openChatWith(handle)
                                viewModel.selectTab(MainTab.INBOX)
                            },
                            onEditProfile = { name, handle, bio, avatar, priv, comments ->
                                viewModel.updateProfile(name, handle, bio, avatar, priv, comments, allowDuet = true)
                            },
                            onSwitchAccount = { userId ->
                                viewModel.switchAccount(userId)
                            },
                            onSignup = { name, handle, pin ->
                                viewModel.signup(name, handle, pin)
                            },
                            onLogin = { handle, pin ->
                                viewModel.login(handle, pin)
                            },
                            onChangePin = { oldPin, newPin ->
                                viewModel.changePin(oldPin, newPin)
                            },
                            onLogout = {
                                viewModel.logout()
                            },
                            onDeleteDraft = { id ->
                                viewModel.deleteDraft(id)
                            },
                            onPublishDraft = { draft ->
                                viewModel.publishDraft(draft)
                            },
                            onToggleFollow = { creator ->
                                viewModel.toggleFollowCreator(creator)
                            }
                        )
                    }
                }
            }
        }

        // Active Comments Bottom Sheet
        activeCommentVideo?.let { video ->
            CommentsBottomSheet(
                video = video,
                comments = videoComments,
                replyingTo = replyingToComment,
                onDismiss = { viewModel.closeComments() },
                onAddComment = { text -> viewModel.addComment(video.id, text) },
                onLikeComment = { comment -> viewModel.toggleCommentLike(comment) },
                onReplyClick = { comment -> viewModel.setReplyTo(comment) },
                onCancelReply = { viewModel.clearReply() },
                onDeleteComment = { comment -> viewModel.deleteComment(comment) },
                currentUserId = userProfile.userId
            )
        }

        // Active Share Bottom Sheet
        activeShareVideo?.let { video ->
            ShareBottomSheet(
                video = video,
                onDismiss = { viewModel.closeShare() },
                onCopyLink = { viewModel.copyVideoLink(video) },
                onRepost = { viewModel.repostVideo(video) },
                onShareViaSystem = { viewModel.shareViaSystem(video) },
                onSaveVideo = { viewModel.exportVideoToGallery(video) },
                onDuet = { viewModel.startDuet(video) },
                onStitch = { viewModel.startStitch(video) },
                onQrCode = { viewModel.openQrCode(video) },
                onSendToFriend = { friend -> viewModel.sendVideoToFriend(friend.handle, video) }
            )
        }

        // QR Code Dialog
        activeQrVideo?.let { video ->
            VideoQrDialog(
                video = video,
                onDismiss = { viewModel.closeQrCode() },
                onCopyLink = { viewModel.copyVideoLink(video) }
            )
        }

        // Active Sound Detail Bottom Sheet
        activeSoundVideo?.let { video ->
            SoundDetailSheet(
                video = video,
                onDismiss = { viewModel.closeSoundDetail() },
                onUseSound = {
                    viewModel.useSound(video.soundTitle, video.soundAuthor)
                }
            )
        }

        // Local Backend Inspector Sheet
        if (showBackendConsole) {
            LocalBackendConsoleSheet(
                stats = backendStats,
                onDismiss = { viewModel.closeBackendConsole() },
                onSeedAdditionalClips = { viewModel.seedAdditionalClips() },
                onResetDatabase = { viewModel.resetLocalDatabase() }
            )
        }

        // Tashan Settings and Privacy Sheet
        val showSettingsSheet by viewModel.showSettingsSheet.collectAsStateWithLifecycle()
        if (showSettingsSheet) {
            TashanSettingsSheet(
                viewModel = viewModel,
                onDismiss = { viewModel.closeSettings() },
                onOpenBackendConsole = {
                    viewModel.closeSettings()
                    viewModel.openBackendConsole()
                }
            )
        }

        // Floating Toast Notification
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
        ) {
            toastMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xEE1E1E24))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = msg,
                        color = TikTokWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
