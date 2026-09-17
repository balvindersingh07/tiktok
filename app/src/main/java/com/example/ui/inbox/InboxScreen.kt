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
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.window.Dialog
import com.example.data.DirectMessageEntity
import com.example.data.NotificationEntity
import com.example.ui.components.formatTimeAgo
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

data class ActiveFriend(
    val name: String,
    val handle: String,
    val isLive: Boolean = false,
    val color: Color
)

@Composable
fun InboxScreen(
    notifications: List<NotificationEntity> = emptyList(),
    onMarkAllRead: () -> Unit = {},
    activeChatUser: String? = null,
    chatMessages: List<DirectMessageEntity> = emptyList(),
    onOpenChat: (handle: String) -> Unit = {},
    onCloseChat: () -> Unit = {},
    onSendMessage: (text: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All activity") }
    val filters = listOf("All activity", "Likes", "Comments", "Followers")

    val activeFriends = listOf(
        ActiveFriend("Marcus", "@marcus_moves", isLive = true, Color(0xFF6200EA)),
        ActiveFriend("Kenji", "@chef_kenji", isLive = false, Color(0xFFE65100)),
        ActiveFriend("Elena", "@elena_travels", isLive = false, Color(0xFF00897B)),
        ActiveFriend("Sarah", "@sarah_d", isLive = false, Color(0xFFC2185B)),
        ActiveFriend("David", "@dave_r", isLive = false, Color(0xFF1E88E5))
    )

    val filteredNotifications = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "Likes" -> notifications.filter { it.type == "like" }
            "Comments" -> notifications.filter { it.type == "comment" }
            "Followers" -> notifications.filter { it.type == "follow" }
            else -> notifications
        }
    }

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

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMarkAllRead, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.DoneAll,
                        contentDescription = "Mark all as read",
                        tint = TikTokCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { onOpenChat("@marcus_moves") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Chat,
                        contentDescription = "Direct Messages",
                        tint = TikTokWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 70.dp)
        ) {
            // Active Friends / Direct Message Row
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
                            modifier = Modifier.clickable { onOpenChat(friend.handle) }
                        ) {
                            Box(contentAlignment = Alignment.BottomCenter) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .border(
                                            2.dp,
                                            if (friend.isLive) TikTokPink else TikTokCyan,
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
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(TikTokOnlineGreen)
                                            .border(1.5.dp, TikTokBlack, CircleShape)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = friend.name,
                                color = TikTokWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Filter Pills Row
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) TikTokWhite else TikTokDarkSurface)
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) TikTokBlack else TikTokWhite80,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Activity Section Header
            item {
                Text(
                    text = "Recent Activity",
                    color = TikTokWhite60,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
                )
            }

            // Notifications List
            if (filteredNotifications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No notifications yet",
                            color = TikTokWhite60,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(filteredNotifications, key = { it.id }) { notif ->
                    NotificationRowItem(
                        notification = notif,
                        onClick = {
                            if (notif.actorHandle.isNotBlank()) {
                                onOpenChat(notif.actorHandle)
                            }
                        }
                    )
                }
            }
        }

        // Direct Message Chat Sheet Dialog
        if (activeChatUser != null) {
            DirectMessageDialog(
                partnerHandle = activeChatUser,
                messages = chatMessages,
                onDismiss = onCloseChat,
                onSendMessage = onSendMessage
            )
        }
    }
}

@Composable
fun NotificationRowItem(
    notification: NotificationEntity,
    onClick: () -> Unit
) {
    val (icon, iconColor) = when (notification.type) {
        "like" -> Icons.Filled.Favorite to TikTokPink
        "comment" -> Icons.Filled.ModeComment to TikTokCyan
        "follow" -> Icons.Filled.PersonAdd to Color(0xFF4CAF50)
        else -> Icons.Filled.Notifications to TikTokPink
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (!notification.isRead) Color(0x15FFFFFF) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with badge
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF333344)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = notification.actorName.take(1).uppercase(),
                    color = TikTokWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TikTokWhite,
                    modifier = Modifier.size(11.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Text Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = notification.actorName,
                    color = TikTokWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formatTimeAgo(notification.timestamp),
                    color = TikTokWhite40,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = notification.actionText,
                color = TikTokWhite80,
                fontSize = 13.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
fun DirectMessageDialog(
    partnerHandle: String,
    messages: List<DirectMessageEntity>,
    onDismiss: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = TikTokDarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1F1F2C))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = TikTokWhite)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Chat with $partnerHandle", color = TikTokWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Local offline DM", color = TikTokCyan, fontSize = 10.sp)
                        }
                    }
                }

                // Messages list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                Text(text = "Say hi to $partnerHandle! 👋", color = TikTokWhite60, fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(messages) { msg ->
                            val isMe = msg.senderHandle != partnerHandle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isMe) TikTokPink else Color(0xFF2E2E3E))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(text = msg.messageText, color = TikTokWhite, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // Bottom Input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TikTokBlack)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Send a message...", color = TikTokWhite40, fontSize = 13.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = TikTokDarkCard,
                            unfocusedContainerColor = TikTokDarkCard,
                            focusedTextColor = TikTokWhite,
                            unfocusedTextColor = TikTokWhite
                        ),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSendMessage(messageText)
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(TikTokCyan)
                    ) {
                        Icon(imageVector = Icons.Filled.Send, contentDescription = "Send", tint = TikTokBlack, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
