package com.example.ui.feed

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoEntity
import com.example.ui.FeedCategory
import com.example.ui.MainTab
import com.example.ui.TikTokViewModel
import com.example.ui.theme.RainbowHorizontalBrush
import com.example.ui.theme.TikTokBlack
import com.example.ui.theme.TikTokLivePulse
import com.example.ui.theme.TikTokWhite
import com.example.ui.theme.TikTokWhite60

@Composable
fun FeedScreen(
    viewModel: TikTokViewModel,
    videos: List<VideoEntity>,
    feedCategory: FeedCategory,
    onNavigateToDiscover: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredVideos = remember(videos, feedCategory) {
        when (feedCategory) {
            FeedCategory.FOR_YOU -> videos.filter { it.category == "fyp" || it.category == "trending" }
            FeedCategory.FOLLOWING -> {
                val followList = videos.filter { it.isFollowing || it.category == "following" }
                if (followList.isNotEmpty()) followList else videos
            }
        }
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { filteredVideos.size.coerceAtLeast(1) }
    )

    val targetVideoId by viewModel.targetFeedVideoId.collectAsStateWithLifecycle()
    val autoplay by viewModel.preferences.autoplay.collectAsStateWithLifecycle()
    val loopVideos by viewModel.preferences.loopVideos.collectAsStateWithLifecycle()
    val dataSaver by viewModel.preferences.dataSaver.collectAsStateWithLifecycle()
    val reduceMotion by viewModel.preferences.reduceMotion.collectAsStateWithLifecycle()

    LaunchedEffect(targetVideoId, filteredVideos) {
        val targetId = targetVideoId
        if (targetId != null && filteredVideos.isNotEmpty()) {
            val targetIndex = filteredVideos.indexOfFirst { it.id == targetId }
            if (targetIndex >= 0) {
                pagerState.scrollToPage(targetIndex)
            }
            viewModel.clearTargetFeedVideo()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(TikTokBlack)) {
        if (filteredVideos.isNotEmpty()) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().testTag("vertical_feed_pager")
            ) { page ->
                val video = filteredVideos[page % filteredVideos.size]
                val isActive = remember(pagerState.currentPage) {
                    derivedStateOf { pagerState.currentPage == page }
                }

                val isMuted by viewModel.isGlobalMuted.collectAsStateWithLifecycle()

                VideoFeedItem(
                    video = video,
                    isActivePage = isActive.value,
                    isMuted = isMuted,
                    autoplay = autoplay,
                    loopVideos = loopVideos,
                    dataSaver = dataSaver,
                    reduceMotion = reduceMotion,
                    onLikeClick = { viewModel.toggleLike(video) },
                    onCommentClick = { viewModel.openComments(video) },
                    onBookmarkClick = { viewModel.toggleBookmark(video) },
                    onShareClick = { viewModel.openShare(video) },
                    onFollowClick = { viewModel.toggleFollow(video) },
                    onSoundClick = { viewModel.openSoundDetail(video) },
                    onCreatorClick = { viewModel.openCreatorProfile(video.authorHandle) },
                    onToggleMute = { viewModel.toggleMute() },
                    onRecordView = { durationMs, completed ->
                        viewModel.recordVideoView(video, durationMs, completed)
                    }
                )
            }
        }

        // Top Navigation Bar (LIVE badge, Following | For You tabs, Search icon)
        TopFeedBar(
            feedCategory = feedCategory,
            onCategorySelected = { viewModel.setFeedCategory(it) },
            onSearchClick = onNavigateToDiscover,
            onLiveClick = { viewModel.handleLiveClick() },
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .align(Alignment.TopCenter)
        )
    }
}

@Composable
fun TopFeedBar(
    feedCategory: FeedCategory,
    onCategorySelected: (FeedCategory) -> Unit,
    onSearchClick: () -> Unit,
    onLiveClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Pulse animation for LIVE badge
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // LIVE button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x33000000))
                .clickable { onLiveClick() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("feed_live_button")
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(TikTokLivePulse)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "LIVE",
                color = TikTokWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Center Feed Tabs: Following | For You
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            FeedTabTitle(
                title = "Following",
                isSelected = feedCategory == FeedCategory.FOLLOWING,
                onClick = { onCategorySelected(FeedCategory.FOLLOWING) },
                testTag = "tab_following"
            )

            Spacer(modifier = Modifier.width(18.dp))

            FeedTabTitle(
                title = "For You",
                isSelected = feedCategory == FeedCategory.FOR_YOU,
                onClick = { onCategorySelected(FeedCategory.FOR_YOU) },
                testTag = "tab_foryou"
            )
        }

        // Search icon
        IconButton(
            onClick = onSearchClick,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0x33000000))
                .testTag("feed_search_button")
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = TikTokWhite,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun FeedTabTitle(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected) TikTokWhite else TikTokWhite60,
            fontSize = if (isSelected) 17.sp else 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Active indicator line
        Box(
            modifier = Modifier
                .width(30.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isSelected) RainbowHorizontalBrush else androidx.compose.ui.graphics.SolidColor(Color.Transparent))
        )
    }
}
