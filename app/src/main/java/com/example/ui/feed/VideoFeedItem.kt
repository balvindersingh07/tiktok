package com.example.ui.feed

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.VideoEntity
import com.example.ui.theme.TikTokBlack
import com.example.ui.theme.TikTokPink
import com.example.ui.theme.TikTokWhite
import com.example.ui.theme.TikTokWhite40
import kotlinx.coroutines.delay

data class HeartPosition(val id: Long, val x: Float, val y: Float)

@Composable
fun VideoFeedItem(
    video: VideoEntity,
    isActivePage: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit,
    onFollowClick: () -> Unit,
    onSoundClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }
    val floatingHearts = remember { mutableStateListOf<HeartPosition>() }

    // Playback loop simulation: video progresses smoothly from 0 to 1 when playing and active
    LaunchedEffect(isActivePage, isPlaying) {
        if (isActivePage && isPlaying) {
            while (true) {
                delay(50)
                progress += 0.004f
                if (progress >= 1.0f) {
                    progress = 0f
                }
            }
        }
    }

    // Reset progress when video page switches
    LaunchedEffect(isActivePage) {
        if (isActivePage) {
            isPlaying = true
            progress = 0f
        } else {
            isPlaying = false
        }
    }

    // Animated gentle camera breathe/zoom effect for video feel
    val infiniteTransition = rememberInfiniteTransition(label = "video_zoom")
    val videoScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val context = LocalContext.current
    val imageDrawableRes = remember(video.coverResName) {
        when (video.coverResName) {
            "video_cover_dance" -> R.drawable.video_cover_dance
            "video_cover_food" -> R.drawable.video_cover_food
            "video_cover_travel" -> R.drawable.video_cover_travel
            else -> R.drawable.video_cover_dance
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TikTokBlack)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        isPlaying = !isPlaying
                    },
                    onDoubleTap = { offset ->
                        if (!video.isLiked) {
                            onLikeClick()
                        }
                        floatingHearts.add(
                            HeartPosition(
                                id = System.currentTimeMillis(),
                                x = (offset.x / context.resources.displayMetrics.density) - 45f,
                                y = (offset.y / context.resources.displayMetrics.density) - 45f
                            )
                        )
                    }
                )
            }
    ) {
        // Video Fullscreen Image Poster with gentle cinematic motion
        Image(
            painter = painterResource(id = imageDrawableRes),
            contentDescription = video.caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .scale(if (isPlaying) videoScale else 1.0f)
        )

        // Top Gradient Vignette for status bar & top tab contrast
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Bottom Gradient Vignette for caption and action readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Center Play / Pause Indicator Icon
        PlayPauseCenterIndicator(
            isPlaying = isPlaying,
            modifier = Modifier.align(Alignment.Center)
        )

        // Double-Tap Floating Hearts Animation
        floatingHearts.forEach { heartPos ->
            FloatingHeart(
                x = heartPos.x,
                y = heartPos.y,
                onAnimationEnd = {
                    floatingHearts.removeAll { it.id == heartPos.id }
                }
            )
        }

        // Right Action Bar (Avatar, Like, Comment, Bookmark, Share, Vinyl)
        RightActionBar(
            video = video,
            onLikeClick = onLikeClick,
            onCommentClick = onCommentClick,
            onBookmarkClick = onBookmarkClick,
            onShareClick = onShareClick,
            onFollowClick = onFollowClick,
            onSoundClick = onSoundClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 54.dp)
        )

        // Bottom Video Info Overlay (Username, verified badge, caption, sound ticker)
        VideoBottomOverlay(
            video = video,
            onSoundClick = onSoundClick,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 54.dp)
        )

        // Video Playback Progress Bar (Scrubber)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 52.dp),
            color = TikTokWhite,
            trackColor = TikTokWhite40
        )
    }
}
