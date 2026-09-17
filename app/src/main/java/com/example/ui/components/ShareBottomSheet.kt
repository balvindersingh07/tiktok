package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DynamicForm
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VideoEntity
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TikTokDarkCard
import com.example.ui.theme.TikTokDarkSurface
import com.example.ui.theme.TikTokPink
import com.example.ui.theme.TikTokWhite
import com.example.ui.theme.TikTokWhite40
import com.example.ui.theme.TikTokWhite60
import com.example.ui.theme.TikTokWhite80

data class QuickFriend(val name: String, val handle: String, val color: Color)
data class ShareAction(val label: String, val icon: ImageVector, val color: Color, val tag: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    video: VideoEntity,
    onDismiss: () -> Unit,
    onCopyLink: () -> Unit,
    onRepost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    val friends = listOf(
        QuickFriend("Sarah D.", "@sarah_d", Color(0xFF6200EA)),
        QuickFriend("Marco C.", "@chef_marco", Color(0xFFE65100)),
        QuickFriend("Liam W.", "@liam_w", Color(0xFF00897B)),
        QuickFriend("Emma K.", "@emma_k", Color(0xFFC2185B)),
        QuickFriend("Dave R.", "@dave_r", Color(0xFF1E88E5))
    )

    val actions = listOf(
        ShareAction("Repost", Icons.Filled.Repeat, TikTokPink, "share_repost"),
        ShareAction("Copy Link", Icons.Filled.ContentCopy, TikTokCyan, "share_copy_link"),
        ShareAction("Save Video", Icons.Filled.Download, Color(0xFF4CAF50), "share_save_video"),
        ShareAction("Duet", Icons.Filled.DynamicForm, Color(0xFF9C27B0), "share_duet"),
        ShareAction("Stitch", Icons.Filled.ViewCarousel, Color(0xFFFF9800), "share_stitch"),
        ShareAction("QR Code", Icons.Filled.QrCode, Color(0xFF607D8B), "share_qr_code")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = TikTokDarkSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TikTokWhite40)
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    text = "Send to",
                    color = TikTokWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = TikTokWhite80,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Quick Friends row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(friends) { friend ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onDismiss() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(friend.color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = friend.name.take(1),
                                color = TikTokWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = friend.name,
                            color = TikTokWhite80,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color(0xFF222222))
                    .padding(vertical = 4.dp)
            )

            Text(
                text = "Share to",
                color = TikTokWhite60,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )

            // Share actions row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(actions) { action ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                when (action.tag) {
                                    "share_copy_link" -> onCopyLink()
                                    "share_repost" -> onRepost()
                                    else -> onDismiss()
                                }
                            }
                            .testTag(action.tag)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(TikTokDarkCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.label,
                                tint = action.color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = action.label,
                            color = TikTokWhite80,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
