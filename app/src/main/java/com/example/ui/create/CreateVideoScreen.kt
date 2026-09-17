package com.example.ui.create

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.TikTokBlack
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TikTokDarkCard
import com.example.ui.theme.TikTokDarkSurface
import com.example.ui.theme.TikTokPink
import com.example.ui.theme.TikTokWhite
import com.example.ui.theme.TikTokWhite40
import com.example.ui.theme.TikTokWhite60
import com.example.ui.theme.TikTokWhite80
import kotlinx.coroutines.delay

@Composable
fun CreateVideoScreen(
    onClose: () -> Unit,
    onPublishVideo: (caption: String, soundTitle: String, soundAuthor: String, coverRes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isRecording by remember { mutableStateOf(false) }
    var recordingProgress by remember { mutableFloatStateOf(0f) }
    var isCameraFront by remember { mutableStateOf(false) }
    var beautyEnabled by remember { mutableStateOf(true) }
    var flashEnabled by remember { mutableStateOf(false) }
    var selectedSpeed by remember { mutableStateOf("1x") }
    var selectedDuration by remember { mutableStateOf("15s") }
    var selectedSound by remember { mutableStateOf("Original Audio - Trending Beat") }

    // Upload / Publish Sheet state
    var showPublishSheet by remember { mutableStateOf(false) }
    var videoCaption by remember { mutableStateOf("Just created this new vibe! 🔥 What do you think? #creator #fyp #viral") }
    var selectedCoverRes by remember { mutableStateOf("video_cover_dance") }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingProgress = 0f
            while (recordingProgress < 1.0f) {
                delay(100)
                recordingProgress += 0.02f
            }
            isRecording = false
            showPublishSheet = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TikTokBlack)
    ) {
        // Viewfinder Preview Canvas
        Image(
            painter = painterResource(
                id = when (selectedCoverRes) {
                    "video_cover_food" -> R.drawable.video_cover_food
                    "video_cover_travel" -> R.drawable.video_cover_travel
                    else -> R.drawable.video_cover_dance
                }
            ),
            contentDescription = "Studio Camera Preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Viewfinder vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        // Top Controls: Close button & Sound Selector Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x55000000))
                    .testTag("create_close_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close Studio",
                    tint = TikTokWhite
                )
            }

            // Sound Selector Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x77000000))
                    .clickable { }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = TikTokCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selectedSound.take(24) + if (selectedSound.length > 24) "..." else "",
                    color = TikTokWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(36.dp))
        }

        // Right Studio Tools Sidebar (Flip, Speed, Beauty, Flash, Timer)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(end = 12.dp, top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            StudioToolItem(
                icon = Icons.Filled.FlipCameraAndroid,
                label = "Flip",
                isActive = isCameraFront,
                onClick = { isCameraFront = !isCameraFront }
            )
            StudioToolItem(
                icon = Icons.Filled.Speed,
                label = selectedSpeed,
                isActive = selectedSpeed != "1x",
                onClick = {
                    selectedSpeed = when (selectedSpeed) {
                        "0.5x" -> "1x"
                        "1x" -> "2x"
                        "2x" -> "3x"
                        else -> "0.5x"
                    }
                }
            )
            StudioToolItem(
                icon = Icons.Filled.AutoFixHigh,
                label = "Beauty",
                isActive = beautyEnabled,
                onClick = { beautyEnabled = !beautyEnabled }
            )
            StudioToolItem(
                icon = Icons.Filled.FlashOn,
                label = "Flash",
                isActive = flashEnabled,
                onClick = { flashEnabled = !flashEnabled }
            )
            StudioToolItem(
                icon = Icons.Filled.Timer,
                label = "Timer",
                isActive = false,
                onClick = { }
            )
        }

        // Bottom Controls: Recording Ring, Duration, Upload Gallery
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Duration selector tabs (15s | 60s | 10m)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                listOf("15s", "60s", "10m").forEach { dur ->
                    Text(
                        text = dur,
                        color = if (selectedDuration == dur) TikTokWhite else TikTokWhite60,
                        fontSize = 13.sp,
                        fontWeight = if (selectedDuration == dur) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedDuration = dur }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Record Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Effects / Filters button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { }
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x55000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✨", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Effects", color = TikTokWhite, fontSize = 11.sp)
                }

                // Big Red Record Button with Animated Progress Ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(88.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { isRecording = !isRecording }
                        )
                        .testTag("record_video_button")
                ) {
                    // Outer progress ring
                    if (isRecording) {
                        CircularProgressIndicator(
                            progress = { recordingProgress },
                            color = TikTokPink,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(88.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .border(4.dp, TikTokWhite, CircleShape)
                        )
                    }

                    // Inner circle (red button)
                    val innerScale by animateFloatAsState(
                        targetValue = if (isRecording) 0.6f else 1.0f,
                        animationSpec = tween(200),
                        label = "record_inner_scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .scale(innerScale)
                            .clip(if (isRecording) RoundedCornerShape(12.dp) else CircleShape)
                            .background(TikTokPink)
                    )
                }

                // Upload Gallery Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { showPublishSheet = true }
                        .testTag("upload_gallery_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x55000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = "Upload",
                            tint = TikTokWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Upload", color = TikTokWhite, fontSize = 11.sp)
                }
            }
        }

        // Publish / Metadata Sheet
        if (showPublishSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(TikTokDarkSurface)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Ready to Post",
                        color = TikTokWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Video Caption & Hashtags",
                        color = TikTokWhite80,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = videoCaption,
                        onValueChange = { videoCaption = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .testTag("publish_caption_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = TikTokDarkCard,
                            unfocusedContainerColor = TikTokDarkCard,
                            focusedTextColor = TikTokWhite,
                            unfocusedTextColor = TikTokWhite,
                            focusedIndicatorColor = TikTokPink,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Select Cover Style",
                        color = TikTokWhite80,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(
                            "video_cover_dance" to "Dance",
                            "video_cover_food" to "Food",
                            "video_cover_travel" to "Travel"
                        ).forEach { (res, label) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        2.dp,
                                        if (selectedCoverRes == res) TikTokPink else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(TikTokDarkCard)
                                    .clickable { selectedCoverRes = res }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (selectedCoverRes == res) TikTokPink else TikTokWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showPublishSheet = false },
                            colors = ButtonDefaults.buttonColors(containerColor = TikTokDarkCard),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Cancel", color = TikTokWhite)
                        }

                        Button(
                            onClick = {
                                onPublishVideo(
                                    videoCaption,
                                    selectedSound,
                                    "Original Sound",
                                    selectedCoverRes
                                )
                                showPublishSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TikTokPink),
                            modifier = Modifier
                                .weight(1.4f)
                                .testTag("confirm_post_button")
                        ) {
                            Text(text = "Post Now 🚀", color = TikTokWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudioToolItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isActive) TikTokPink else Color(0x55000000)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = TikTokWhite,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = TikTokWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
