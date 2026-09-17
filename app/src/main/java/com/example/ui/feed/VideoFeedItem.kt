package com.example.ui.feed

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.R
import com.example.data.VideoEntity
import com.example.ui.theme.TikTokBlack
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TikTokWhite
import com.example.ui.theme.TikTokWhite40
import kotlinx.coroutines.delay
import java.io.File

data class HeartPosition(val id: Long, val x: Float, val y: Float)

@OptIn(UnstableApi::class)
@Composable
fun VideoFeedItem(
    video: VideoEntity,
    isActivePage: Boolean,
    isMuted: Boolean = false,
    autoplay: Boolean = true,
    loopVideos: Boolean = true,
    dataSaver: Boolean = false,
    reduceMotion: Boolean = false,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit,
    onFollowClick: () -> Unit,
    onSoundClick: () -> Unit,
    onCreatorClick: () -> Unit = {},
    onToggleMute: () -> Unit = {},
    onRecordView: (watchDurationMs: Long, completed: Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember(autoplay) { mutableStateOf(autoplay) }
    var progress by remember { mutableFloatStateOf(0f) }
    val floatingHearts = remember { mutableStateListOf<HeartPosition>() }

    val hasLocalVideoFile = remember(video.videoUrl) {
        if (video.videoUrl.isNotBlank()) {
            val file = File(video.videoUrl)
            file.exists() && file.length() > 0
        } else false
    }

    // Real Media3 ExoPlayer instance when a local video file exists
    val exoPlayer = remember(video.videoUrl, hasLocalVideoFile, loopVideos) {
        if (hasLocalVideoFile) {
            ExoPlayer.Builder(context).build().apply {
                repeatMode = if (loopVideos) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                val mediaItem = MediaItem.fromUri(Uri.fromFile(File(video.videoUrl)))
                setMediaItem(mediaItem)
                prepare()
            }
        } else null
    }

    // Manage ExoPlayer Lifecycle & playback
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer?.release()
        }
    }

    LaunchedEffect(exoPlayer, isMuted) {
        exoPlayer?.volume = if (isMuted) 0f else 1f
    }

    LaunchedEffect(isActivePage, isPlaying, exoPlayer) {
        if (exoPlayer != null) {
            if (isActivePage && isPlaying) {
                exoPlayer.play()
            } else {
                exoPlayer.pause()
            }
        }
    }

    // Playback loop and view reporting
    LaunchedEffect(isActivePage, isPlaying, exoPlayer) {
        if (isActivePage && isPlaying) {
            var elapsedMs = 0L
            while (true) {
                delay(100)
                elapsedMs += 100

                if (exoPlayer != null) {
                    val duration = exoPlayer.duration.coerceAtLeast(1L)
                    val currentPos = exoPlayer.currentPosition
                    progress = (currentPos.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    if (currentPos >= duration - 200) {
                        onRecordView(duration, true)
                    }
                } else {
                    progress += 0.007f
                    if (progress >= 1.0f) {
                        onRecordView(15000L, true)
                        if (loopVideos) {
                            progress = 0f
                        } else {
                            progress = 1.0f
                            isPlaying = false
                        }
                    }
                }

                // Record initial view impression after 1.5 seconds of watching
                if (elapsedMs == 1500L) {
                    onRecordView(1500L, false)
                }
            }
        }
    }

    // Reset progress when video page switches
    LaunchedEffect(isActivePage, autoplay) {
        if (isActivePage) {
            isPlaying = autoplay
            progress = 0f
            exoPlayer?.seekTo(0)
        } else {
            isPlaying = false
        }
    }

    // Animated gentle camera breathe/zoom effect for video feel
    val infiniteTransition = rememberInfiniteTransition(label = "video_zoom")
    val videoScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (reduceMotion) 1.0f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

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
        // Video Fullscreen Layer: Real ExoPlayer when local file exists, or poster image canvas
        if (exoPlayer != null && hasLocalVideoFile) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(id = imageDrawableRes),
                contentDescription = video.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(if (isPlaying) videoScale else 1.0f)
            )
        }

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

        if (dataSaver) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 56.dp, end = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x99111116))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Data Saver",
                    color = TikTokCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

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

        // Mute / Unmute Quick Toggle Button (Top-Right under status bar)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 60.dp, end = 16.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onToggleMute() })
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Filled.VolumeMute else Icons.Filled.VolumeUp,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = TikTokWhite,
                modifier = Modifier.size(20.dp)
            )
        }

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
            onAvatarClick = onCreatorClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 12.dp)
        )

        // Bottom Video Info Overlay (Username, verified badge, caption, sound ticker)
        VideoBottomOverlay(
            video = video,
            onSoundClick = onSoundClick,
            onCreatorClick = onCreatorClick,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 12.dp)
        )

        // Video Playback Progress Bar (Scrubber)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.BottomCenter),
            color = TikTokWhite,
            trackColor = TikTokWhite40
        )
    }
}
