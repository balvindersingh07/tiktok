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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FeedCategory
import com.example.ui.MainTab
import com.example.ui.TikTokViewModel
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.ShareBottomSheet
import com.example.ui.components.SoundDetailSheet
import com.example.ui.components.TikTokBottomNav
import com.example.ui.create.CreateVideoScreen
import com.example.ui.discover.DiscoverScreen
import com.example.ui.feed.FeedScreen
import com.example.ui.inbox.InboxScreen
import com.example.ui.profile.ProfileScreen
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
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val feedCategory by viewModel.feedCategory.collectAsStateWithLifecycle()
    val allVideos by viewModel.allVideos.collectAsStateWithLifecycle()
    val likedVideos by viewModel.likedVideos.collectAsStateWithLifecycle()
    val bookmarkedVideos by viewModel.bookmarkedVideos.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val activeCommentVideo by viewModel.activeCommentVideo.collectAsStateWithLifecycle()
    val videoComments by viewModel.videoComments.collectAsStateWithLifecycle()

    val activeShareVideo by viewModel.activeShareVideo.collectAsStateWithLifecycle()
    val activeSoundVideo by viewModel.activeSoundVideo.collectAsStateWithLifecycle()
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
                        onTabSelected = { tab -> viewModel.selectTab(tab) }
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
                            videos = allVideos,
                            feedCategory = feedCategory,
                            onNavigateToDiscover = { viewModel.selectTab(MainTab.DISCOVER) }
                        )
                    }

                    MainTab.DISCOVER -> {
                        DiscoverScreen(
                            videos = allVideos,
                            onVideoClick = { video ->
                                viewModel.selectTab(MainTab.HOME)
                            }
                        )
                    }

                    MainTab.CREATE -> {
                        CreateVideoScreen(
                            onClose = { viewModel.selectTab(MainTab.HOME) },
                            onPublishVideo = { caption, soundTitle, soundAuthor, coverRes ->
                                viewModel.uploadVideo(
                                    caption = caption,
                                    soundTitle = soundTitle,
                                    soundAuthor = soundAuthor,
                                    coverResName = coverRes
                                )
                            }
                        )
                    }

                    MainTab.INBOX -> {
                        InboxScreen()
                    }

                    MainTab.PROFILE -> {
                        ProfileScreen(
                            profile = userProfile,
                            allVideos = allVideos,
                            likedVideos = likedVideos,
                            bookmarkedVideos = bookmarkedVideos,
                            onVideoClick = { video ->
                                viewModel.selectTab(MainTab.HOME)
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
                onDismiss = { viewModel.closeComments() },
                onAddComment = { text -> viewModel.addComment(video.id, text) },
                onLikeComment = { comment -> viewModel.toggleCommentLike(comment) }
            )
        }

        // Active Share Bottom Sheet
        activeShareVideo?.let { video ->
            ShareBottomSheet(
                video = video,
                onDismiss = { viewModel.closeShare() },
                onCopyLink = { viewModel.copyVideoLink(video) },
                onRepost = { viewModel.repostVideo(video) }
            )
        }

        // Active Sound Detail Bottom Sheet
        activeSoundVideo?.let { video ->
            SoundDetailSheet(
                video = video,
                onDismiss = { viewModel.closeSoundDetail() },
                onUseSound = {
                    viewModel.closeSoundDetail()
                    viewModel.selectTab(MainTab.CREATE)
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

