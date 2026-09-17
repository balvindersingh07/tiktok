package com.example.ui.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TikTokBlack
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TikTokDarkCard
import com.example.ui.theme.TikTokDarkSurface
import com.example.ui.theme.TikTokOnlineGreen
import com.example.ui.theme.TikTokPink
import com.example.ui.theme.TikTokWhite
import com.example.ui.theme.TikTokWhite40
import com.example.ui.theme.TikTokWhite60
import com.example.ui.theme.TikTokWhite80

data class ActivityNotification(
    val id: String,
    val title: String,
    val subtitle: String,
    val timeAgo: String,
    val icon: ImageVector,
    val iconColor: Color,
    val avatarBg: Color,
    val isFollowRequest: Boolean = false
)

data class ActiveFriend(
    val name: String,
    val isLive: Boolean = false,
    val color: Color
)

@Composable
fun InboxScreen(
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All activity") }
    val filters = listOf("All activity", "Likes", "Comments", "Mentions", "Followers")

    val activeFriends = listOf(
        ActiveFriend("Marcus", isLive = true, Color(0xFF6200EA)),
        ActiveFriend("Kenji", isLive = false, Color(0xFFE65100)),
        ActiveFriend("Elena", isLive = false, Color(0xFF00897B)),
        ActiveFriend("Sarah", isLive = false, Color(0xFFC2185B)),
        ActiveFriend("David", isLive = false, Color(0xFF1E88E5))
    )

    val notifications = listOf(
        ActivityNotification(
            id = "1",
            title = "marcus_moves",
            subtitle = "liked your video",
            timeAgo = "12m",
            icon = Icons.Filled.Favorite,
            iconColor = TikTokPink,
            avatarBg = Color(0xFF6200EA)
        ),
        ActivityNotification(
            id = "2",
            title = "chef_ramen",
            subtitle = "commented: 'Need that recipe marinade!'",
            timeAgo = "45m",
            icon = Icons.Filled.ModeComment,
            iconColor = TikTokCyan,
            avatarBg = Color(0xFFE65100)
        ),
        ActivityNotification(
            id = "3",
            title = "maya_traveler",
            subtitle = "started following you",
            timeAgo = "2h",
            icon = Icons.Filled.PersonAdd,
            iconColor = Color(0xFF4CAF50),
            avatarBg = Color(0xFF00897B),
            isFollowRequest = true
        ),
        ActivityNotification(
            id = "4",
            title = "TikTok System",
            subtitle = "Your video reached 10,000 views! Keep up the momentum 🚀",
            timeAgo = "5h",
            icon = Icons.Filled.Notifications,
            iconColor = TikTokPink,
            avatarBg = Color(0xFF222222)
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TikTokBlack)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Inbox Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Inbox",
                color = TikTokWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { }, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Filled.Chat,
                    contentDescription = "Direct Messages",
                    tint = TikTokWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 70.dp)
        ) {
            // Stories / Active Now row
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(activeFriends) { friend ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { }
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .border(
                                            2.dp,
                                            if (friend.isLive) TikTokPink else TikTokOnlineGreen,
                                            CircleShape
                                        )
                                        .padding(3.dp)
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

                                if (friend.isLive) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(TikTokPink)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "LIVE",
                                            color = TikTokWhite,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(TikTokOnlineGreen)
                                            .border(2.dp, TikTokBlack, CircleShape)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = friend.name,
                                color = TikTokWhite80,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Activity filter chips
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { filter ->
                        val isSelected = filter == selectedFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) TikTokWhite else TikTokDarkSurface)
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) TikTokBlack else TikTokWhite,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Notifications List
            items(notifications, key = { it.id }) { notif ->
                NotificationRowItem(notification = notif)
            }
        }
    }
}

@Composable
fun NotificationRowItem(
    notification: ActivityNotification
) {
    var isFollowedBack by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with icon badge
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(notification.avatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = notification.title.take(1).uppercase(),
                    color = TikTokWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(notification.iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notification.icon,
                    contentDescription = null,
                    tint = TikTokWhite,
                    modifier = Modifier.size(11.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = notification.title,
                    color = TikTokWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = notification.timeAgo,
                    color = TikTokWhite40,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = notification.subtitle,
                color = TikTokWhite80,
                fontSize = 13.sp,
                maxLines = 2
            )
        }

        if (notification.isFollowRequest) {
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { isFollowedBack = !isFollowedBack },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowedBack) TikTokDarkCard else TikTokPink
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = if (isFollowedBack) "Friends" else "Follow",
                    color = TikTokWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
