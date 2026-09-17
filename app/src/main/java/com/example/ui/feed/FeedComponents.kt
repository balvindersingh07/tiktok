package com.example.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoEntity
import com.example.ui.theme.RainbowSweepBrush
import com.example.ui.theme.TikTokBlack
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TikTokGold
import com.example.ui.theme.TikTokPink
import com.example.ui.theme.TikTokWhite
import com.example.ui.theme.TikTokWhite60
import com.example.ui.theme.TikTokWhite80
import kotlinx.coroutines.delay

fun formatCount(count: Long): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 10_000 -> String.format("%.1fK", count / 1_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}

@Composable
fun RightActionBar(
    video: VideoEntity,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onShareClick: () -> Unit,
    onFollowClick: () -> Unit,
    onSoundClick: () -> Unit,
    onAvatarClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(end = 8.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Creator Avatar with Follow button
        CreatorAvatar(
            authorName = video.authorName,
            isFollowing = video.isFollowing,
            onFollowClick = onFollowClick,
            onAvatarClick = onAvatarClick
        )

        // Like Button
        ActionIconButton(
            icon = if (video.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            label = formatCount(video.likesCount),
            tint = if (video.isLiked) TikTokPink else TikTokWhite,
            onClick = onLikeClick,
            testTag = "feed_like_button_${video.id}",
            animateBounce = video.isLiked
        )

        // Comment Button
        ActionIconButton(
            icon = Icons.Filled.Comment,
            label = formatCount(video.commentsCount),
            tint = TikTokWhite,
            onClick = onCommentClick,
            testTag = "feed_comment_button_${video.id}"
        )

        // Bookmark / Favorite Button
        ActionIconButton(
            icon = if (video.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            label = formatCount(video.bookmarksCount),
            tint = if (video.isBookmarked) TikTokGold else TikTokWhite,
            onClick = onBookmarkClick,
            testTag = "feed_bookmark_button_${video.id}",
            animateBounce = video.isBookmarked
        )

        // Share Button
        ActionIconButton(
            icon = Icons.Filled.Reply,
            label = formatCount(video.sharesCount),
            tint = TikTokWhite,
            onClick = onShareClick,
            testTag = "feed_share_button_${video.id}"
        )

        // Rotating Vinyl Record Disc
        SpinningVinylRecord(
            soundAuthor = video.soundAuthor,
            onClick = onSoundClick,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun CreatorAvatar(
    authorName: String,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onAvatarClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier.size(54.dp)
    ) {
        // Circular profile picture frame
        Box(
            modifier = Modifier
                .size(46.dp)
                .align(Alignment.TopCenter)
                .clip(CircleShape)
                .border(1.5.dp, TikTokWhite, CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF4A0E4E), Color(0xFF161823))
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAvatarClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = authorName.take(1).uppercase(),
                color = TikTokWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        // Plus / Checkmark badge
        AnimatedVisibility(
            visible = !isFollowing,
            enter = scaleIn(spring(dampingRatio = 0.5f)),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(TikTokPink)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onFollowClick
                    )
                    .testTag("follow_creator_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Follow Creator",
                    tint = TikTokWhite,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
fun ActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String,
    animateBounce: Boolean = false
) {
    var bounceTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(animateBounce) {
        if (animateBounce) {
            bounceTrigger = true
            delay(300)
            bounceTrigger = false
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (bounceTrigger) 1.35f else 1.0f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f),
        label = "bounce_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier
                .size(34.dp)
                .scale(scale)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = TikTokWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SpinningVinylRecord(
    soundAuthor: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Floating note animation
    val noteOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "note_float"
    )
    val noteAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "note_alpha"
    )

    Box(
        modifier = modifier
            .size(50.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Drifting music note
        Icon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = null,
            tint = TikTokWhite.copy(alpha = noteAlpha),
            modifier = Modifier
                .size(16.dp)
                .offset(x = (-12).dp, y = noteOffset.dp)
        )

        // Black vinyl disc with record grooves
        Box(
            modifier = Modifier
                .size(46.dp)
                .rotate(rotation)
                .clip(CircleShape)
                .background(Color(0xFF141414))
                .border(2.dp, Color(0xFF2A2A2A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Grooves
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFF333333), CircleShape)
            )

            // Center album label
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(RainbowSweepBrush),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = soundAuthor.take(1).uppercase(),
                    color = TikTokWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun VideoBottomOverlay(
    video: VideoEntity,
    onSoundClick: () -> Unit,
    onCreatorClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth(0.78f)
            .padding(start = 14.dp, bottom = 12.dp)
    ) {
        // Creator handle and verified badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 6.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCreatorClick
                )
        ) {
            Text(
                text = video.authorHandle,
                color = TikTokWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = "Verified Creator",
                tint = TikTokCyan,
                modifier = Modifier.size(16.dp)
            )
        }

        // Caption
        Text(
            text = video.caption,
            color = TikTokWhite,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            maxLines = if (isExpanded) 10 else 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .clickable { isExpanded = !isExpanded }
                .padding(bottom = 8.dp)
        )

        // Sound ticker
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x33000000))
                .clickable(onClick = onSoundClick)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = "Music",
                tint = TikTokWhite,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${video.soundTitle} - ${video.soundAuthor}",
                color = TikTokWhite,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PlayPauseCenterIndicator(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isPlaying,
        enter = fadeIn() + scaleIn(initialScale = 1.3f),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0x77000000)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play Video",
                tint = TikTokWhite.copy(alpha = 0.9f),
                modifier = Modifier.size(46.dp)
            )
        }
    }
}

@Composable
fun FloatingHeart(
    x: Float,
    y: Float,
    onAnimationEnd: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1.2f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "heart_scale"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) -100f else -250f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "heart_offset_y"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700),
        label = "heart_alpha"
    )

    LaunchedEffect(Unit) {
        delay(400)
        visible = false
        delay(300)
        onAnimationEnd()
    }

    Box(
        modifier = Modifier
            .offset(x = x.dp, y = (y + offsetY).dp)
            .scale(scale)
            .size(90.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = null,
            tint = TikTokPink.copy(alpha = alpha),
            modifier = Modifier.size(80.dp)
        )
    }
}
