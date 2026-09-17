package com.example.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BlockedUserEntity
import com.example.data.MutedUserEntity
import com.example.data.SearchHistoryEntity
import com.example.data.SoundEntity
import com.example.data.VideoEntity
import com.example.ui.theme.TikTokBlack
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TikTokDarkSurface
import com.example.ui.theme.TikTokGray
import com.example.ui.theme.TikTokPink
import com.example.ui.theme.TikTokWhite
import com.example.ui.theme.TikTokWhite40
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ----------------------------------------------------------------------------
// Blocked Users Dialog
// ----------------------------------------------------------------------------
@Composable
fun BlockedUsersDialog(
    blockedUsers: List<BlockedUserEntity>,
    onUnblock: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TikTokDarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Blocked Accounts",
                    color = TikTokWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = TikTokWhite40)
                }
            }
        },
        text = {
            if (blockedUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Block,
                            contentDescription = null,
                            tint = TikTokWhite40,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No blocked accounts",
                            color = TikTokWhite40,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    items(blockedUsers, key = { it.id }) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.blockedHandle,
                                    color = TikTokWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Blocked on ${formatTimestamp(user.timestamp)}",
                                    color = TikTokWhite40,
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = { onUnblock(user.blockedHandle) },
                                colors = ButtonDefaults.buttonColors(containerColor = TikTokGray),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Unblock", color = TikTokWhite, fontSize = 12.sp)
                            }
                        }
                        HorizontalDivider(color = Color(0x1AFFFFFF))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TikTokPink),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Done", color = TikTokWhite)
            }
        }
    )
}

// ----------------------------------------------------------------------------
// Muted Users Dialog
// ----------------------------------------------------------------------------
@Composable
fun MutedUsersDialog(
    mutedUsers: List<MutedUserEntity>,
    onUnmute: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TikTokDarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Muted Accounts",
                    color = TikTokWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = TikTokWhite40)
                }
            }
        },
        text = {
            if (mutedUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.VolumeMute,
                            contentDescription = null,
                            tint = TikTokWhite40,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No muted accounts",
                            color = TikTokWhite40,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    items(mutedUsers, key = { it.id }) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.mutedHandle,
                                    color = TikTokWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Muted on ${formatTimestamp(user.timestamp)}",
                                    color = TikTokWhite40,
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = { onUnmute(user.mutedHandle) },
                                colors = ButtonDefaults.buttonColors(containerColor = TikTokGray),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Unmute", color = TikTokWhite, fontSize = 12.sp)
                            }
                        }
                        HorizontalDivider(color = Color(0x1AFFFFFF))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TikTokPink),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Done", color = TikTokWhite)
            }
        }
    )
}

// ----------------------------------------------------------------------------
// Watch History Dialog
// ----------------------------------------------------------------------------
@Composable
fun WatchHistoryDialog(
    videos: List<VideoEntity>,
    onDeleteSingle: (Long) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var showClearConfirmation by remember { mutableStateOf(false) }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            containerColor = TikTokDarkSurface,
            title = { Text("Clear Watch History?", color = TikTokWhite, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove your watch history records. Your personalized feed will readjust.", color = TikTokWhite40) },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmation = false
                        onClearAll()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Clear All", color = TikTokWhite)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel", color = TikTokWhite40)
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TikTokDarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Watch History",
                    color = TikTokWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (videos.isNotEmpty()) {
                    Text(
                        text = "Clear All",
                        color = TikTokPink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { showClearConfirmation = true }
                            .padding(4.dp)
                    )
                }
            }
        },
        text = {
            if (videos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null,
                            tint = TikTokWhite40,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No watched videos yet",
                            color = TikTokWhite40,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    items(videos, key = { it.id }) { video ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF24242E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = video.authorName.take(1).uppercase(),
                                    color = TikTokCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = video.caption.ifBlank { "Untitled Clip" },
                                    color = TikTokWhite,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${video.authorHandle} • ${video.viewsCount} views",
                                    color = TikTokWhite40,
                                    fontSize = 11.sp
                                )
                            }
                            IconButton(
                                onClick = { onDeleteSingle(video.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove item",
                                    tint = TikTokWhite40,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0x1AFFFFFF))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TikTokPink),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Close", color = TikTokWhite)
            }
        }
    )
}

// ----------------------------------------------------------------------------
// Search History Dialog
// ----------------------------------------------------------------------------
@Composable
fun SearchHistoryDialog(
    searchHistory: List<SearchHistoryEntity>,
    onDeleteSingle: (Long) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var showClearConfirmation by remember { mutableStateOf(false) }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            containerColor = TikTokDarkSurface,
            title = { Text("Clear Search History?", color = TikTokWhite, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently clear all recent searches from your local device database.", color = TikTokWhite40) },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmation = false
                        onClearAll()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Clear All", color = TikTokWhite)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel", color = TikTokWhite40)
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TikTokDarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search History",
                    color = TikTokWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (searchHistory.isNotEmpty()) {
                    Text(
                        text = "Clear All",
                        color = TikTokPink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { showClearConfirmation = true }
                            .padding(4.dp)
                    )
                }
            }
        },
        text = {
            if (searchHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null,
                            tint = TikTokWhite40,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No recent searches",
                            color = TikTokWhite40,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    items(searchHistory, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.query,
                                    color = TikTokWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = formatTimestamp(item.timestamp),
                                    color = TikTokWhite40,
                                    fontSize = 11.sp
                                )
                            }
                            IconButton(
                                onClick = { onDeleteSingle(item.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove query",
                                    tint = TikTokWhite40,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0x1AFFFFFF))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TikTokPink),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Done", color = TikTokWhite)
            }
        }
    )
}

// ----------------------------------------------------------------------------
// Recently Used Sounds Dialog
// ----------------------------------------------------------------------------
@Composable
fun SoundsHistoryDialog(
    sounds: List<SoundEntity>,
    onClearSingle: (Long) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TikTokDarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recently Used Sounds",
                    color = TikTokWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (sounds.isNotEmpty()) {
                    Text(
                        text = "Clear All",
                        color = TikTokPink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onClearAll() }
                            .padding(4.dp)
                    )
                }
            }
        },
        text = {
            if (sounds.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = TikTokWhite40,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No sound usage history",
                            color = TikTokWhite40,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    items(sounds, key = { it.id }) { sound ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sound.title,
                                    color = TikTokWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${sound.author} • Used ${sound.usageCount} times",
                                    color = TikTokWhite40,
                                    fontSize = 11.sp
                                )
                            }
                            IconButton(
                                onClick = { onClearSingle(sound.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove sound",
                                    tint = TikTokWhite40,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0x1AFFFFFF))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TikTokPink),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Done", color = TikTokWhite)
            }
        }
    )
}

// ----------------------------------------------------------------------------
// Generic Bundled Document View (FAQ, How It Works, Guidelines, Privacy, etc.)
// ----------------------------------------------------------------------------
@Composable
fun BundledDocumentDialog(
    title: String,
    subtitle: String,
    sections: List<Pair<String, String>>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TikTokDarkSurface,
        title = {
            Column {
                Text(
                    text = title,
                    color = TikTokWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TikTokCyan,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                items(sections) { (heading, content) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E26)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = heading,
                                color = TikTokWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = content,
                                color = TikTokWhite40,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TikTokPink),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Close", color = TikTokWhite)
            }
        }
    )
}

// ----------------------------------------------------------------------------
// Expandable FAQ Dialog
// ----------------------------------------------------------------------------
@Composable
fun FaqDialog(onDismiss: () -> Unit) {
    val faqs = listOf(
        "How is my data stored in Tashan?" to
            "Tashan is built with a local-first privacy architecture. All your videos, watch history, preferences, and drafts are stored strictly on your device inside an embedded Room SQLite database. Nothing is transmitted to external cloud servers.",
        "How does the For You recommendation algorithm work?" to
            "The recommendation engine operates directly on your device CPU. It analyzes real-time engagement events—such as completed watch cycles, shares, saves, and likes—to compute category affinity weights and deliver personalized feeds without tracking cookies.",
        "How do I create a Duet or Stitch?" to
            "Tap the Share icon on any video where the creator has permitted Duets or Stitches. Select 'Duet' to record a side-by-side video or 'Stitch' to clip up to 5 seconds of the source clip into your post.",
        "Can I download videos to my gallery?" to
            "Yes, if the creator has enabled downloads. Open the Share menu and tap 'Save video' to export the MP4 directly to your device Movies/Gallery folder.",
        "What happens when I reset recommendation preferences?" to
            "Resetting wipes your historical category affinity scores from local SQLite and returns the For You feed to the baseline trending order. Your liked videos, bookmarks, and drafts remain untouched."
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TikTokDarkSurface,
        title = {
            Text(
                text = "Frequently Asked Questions",
                color = TikTokWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                items(faqs) { (q, a) ->
                    var expanded by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { expanded = !expanded },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E26)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = q,
                                    color = TikTokWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = null,
                                    tint = TikTokCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            AnimatedVisibility(visible = expanded) {
                                Column {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = a,
                                        color = TikTokWhite40,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TikTokPink),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Close", color = TikTokWhite)
            }
        }
    )
}

// ----------------------------------------------------------------------------
// App Info & Version Dialog
// ----------------------------------------------------------------------------
@Composable
fun AppInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TikTokDarkSurface,
        title = {
            Text(
                text = "Tashan Version & Environment",
                color = TikTokWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                InfoRow("App Name", "Tashan")
                InfoRow("Version", "2.5.0 Production")
                InfoRow("Build", "2026.09.17-LOCAL")
                InfoRow("Database Schema", "Room SQLite v4 (Multi-entity)")
                InfoRow("Media Engine", "AndroidX Media3 ExoPlayer")
                InfoRow("UI Framework", "Jetpack Compose M3 (Material 3)")
                InfoRow("Architecture", "Clean Architecture / On-Device Local Server")
                InfoRow("Telemetry Policy", "Zero Remote Tracking (100% Local)")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TikTokPink),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("OK", color = TikTokWhite)
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TikTokWhite40, fontSize = 12.sp)
        Text(text = value, color = TikTokWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
    HorizontalDivider(color = Color(0x1AFFFFFF))
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
