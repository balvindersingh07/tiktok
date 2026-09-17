package com.example.ui.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.SoundEntity
import com.example.data.VideoEntity
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
    onPublishVideo: (
        caption: String,
        soundTitle: String,
        soundAuthor: String,
        coverRes: String,
        videoPath: String,
        isPrivate: Boolean,
        allowComments: Boolean
    ) -> Unit,
    onSaveDraft: (
        caption: String,
        soundTitle: String,
        soundAuthor: String,
        coverRes: String,
        videoPath: String
    ) -> Unit = { _, _, _, _, _ -> },
    onImportUri: (Uri) -> String? = { null },
    availableSounds: List<SoundEntity> = emptyList(),
    preSelectedSound: SoundEntity? = null,
    duetSourceVideo: VideoEntity? = null,
    stitchSourceVideo: VideoEntity? = null,
    modifier: Modifier = Modifier
) {
    var isRecording by remember { mutableStateOf(false) }
    var recordingProgress by remember { mutableFloatStateOf(0f) }
    var isCameraFront by remember { mutableStateOf(false) }
    var beautyEnabled by remember { mutableStateOf(true) }
    var flashEnabled by remember { mutableStateOf(false) }
    var selectedSpeed by remember { mutableStateOf("1x") }
    var selectedDuration by remember { mutableStateOf("15s") }
    var selectedSoundTitle by remember {
        mutableStateOf(preSelectedSound?.title ?: "Original Audio - Trending Beat")
    }
    var selectedSoundAuthor by remember {
        mutableStateOf(preSelectedSound?.author ?: "TikTok Music")
    }
    var showSoundPicker by remember { mutableStateOf(false) }

    // Upload / Publish Sheet state
    var showPublishSheet by remember { mutableStateOf(false) }
    var videoCaption by remember {
        mutableStateOf(
            if (duetSourceVideo != null) "#duet with @${duetSourceVideo.authorHandle} 🔥"
            else if (stitchSourceVideo != null) "#stitch with @${stitchSourceVideo.authorHandle} 🎬"
            else "Just created this new vibe! 🔥 What do you think? #creator #fyp #viral"
        )
    }
    var selectedCoverRes by remember { mutableStateOf("video_cover_dance") }
    var localVideoFilePath by remember { mutableStateOf("") }
    var isPrivateVideo by remember { mutableStateOf(false) }
    var allowComments by remember { mutableStateOf(true) }

    // Android 13+ zero-permission Photo & Video Picker launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = onImportUri(uri)
            if (savedPath != null) {
                localVideoFilePath = savedPath
            }
            showPublishSheet = true
        }
    }

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

        // Duet / Stitch Banner Indicator if active
        if (duetSourceVideo != null || stitchSourceVideo != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .border(1.dp, TikTokPink, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (duetSourceVideo != null) "👯 Duet with ${duetSourceVideo.authorHandle}"
                    else "✂️ Stitch with ${stitchSourceVideo?.authorHandle}",
                    color = TikTokWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Top Studio Bar (Close, Sound Selector)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x55000000))
                    .testTag("close_camera_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close Camera",
                    tint = TikTokWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Sound Selector Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x66000000))
                    .clickable { showSoundPicker = true }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = "Sound Selector",
                    tint = TikTokWhite,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selectedSoundTitle.take(18) + if (selectedSoundTitle.length > 18) "..." else "",
                    color = TikTokWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.size(36.dp))
        }

        // Right Studio Toolset
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                icon = Icons.Filled.Timer,
                label = "Timer",
                isActive = false,
                onClick = { isRecording = true }
            )

            StudioToolItem(
                icon = Icons.Filled.FlashOn,
                label = "Flash",
                isActive = flashEnabled,
                onClick = { flashEnabled = !flashEnabled }
            )
        }

        // Bottom Controls Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 24.dp),
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

                // Upload Gallery Button (Launches zero-permission Android Photo/Video Picker)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        }
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
                            contentDescription = "Upload from gallery",
                            tint = TikTokWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Upload", color = TikTokWhite, fontSize = 11.sp)
                }
            }
        }

        // Publish & Draft Confirmation Dialog
        if (showPublishSheet) {
            Dialog(onDismissRequest = { showPublishSheet = false }) {
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
                            .height(84.dp)
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

                    Spacer(modifier = Modifier.height(12.dp))

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
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Privacy & Comments switches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPrivateVideo) "Private video 🔒" else "Public video 🌍",
                            color = TikTokWhite,
                            fontSize = 13.sp
                        )
                        Switch(
                            checked = isPrivateVideo,
                            onCheckedChange = { isPrivateVideo = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = TikTokPink)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Allow comments 💬",
                            color = TikTokWhite,
                            fontSize = 13.sp
                        )
                        Switch(
                            checked = allowComments,
                            onCheckedChange = { allowComments = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = TikTokCyan)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons: Cancel, Drafts, Post
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveDraft(
                                    videoCaption,
                                    selectedSoundTitle,
                                    selectedSoundAuthor,
                                    selectedCoverRes,
                                    localVideoFilePath
                                )
                                showPublishSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TikTokDarkCard),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Draft 📝", color = TikTokWhite, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                onPublishVideo(
                                    videoCaption,
                                    selectedSoundTitle,
                                    selectedSoundAuthor,
                                    selectedCoverRes,
                                    localVideoFilePath,
                                    isPrivateVideo,
                                    allowComments
                                )
                                showPublishSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TikTokPink),
                            modifier = Modifier
                                .weight(1.4f)
                                .testTag("confirm_post_button")
                        ) {
                            Text(
                                text = "Post Now 🚀",
                                color = TikTokWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Sound Selection Modal Dialog
        if (showSoundPicker) {
            Dialog(onDismissRequest = { showSoundPicker = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(TikTokDarkSurface)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Select Soundtrack",
                        color = TikTokWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableSounds) { sound ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TikTokDarkCard)
                                    .clickable {
                                        selectedSoundTitle = sound.title
                                        selectedSoundAuthor = sound.author
                                        showSoundPicker = false
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = sound.title,
                                        color = TikTokWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = sound.author,
                                        color = TikTokWhite60,
                                        fontSize = 11.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Select",
                                    tint = if (selectedSoundTitle == sound.title) TikTokCyan else Color.Transparent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
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
