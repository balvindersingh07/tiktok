package com.example.ui.profile

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfileEntity
import com.example.data.VideoEntity
import com.example.ui.ProfileSubTab
import com.example.ui.discover.VideoThumbnailGridItem
import com.example.ui.theme.TikTokBlack
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TikTokDarkCard
import com.example.ui.theme.TikTokDarkSurface
import com.example.ui.theme.TikTokPink
import com.example.ui.theme.TikTokWhite
import com.example.ui.theme.TikTokWhite40
import com.example.ui.theme.TikTokWhite60
import com.example.ui.theme.TikTokWhite80

@Composable
fun ProfileScreen(
    profile: UserProfileEntity?,
    allVideos: List<VideoEntity>,
    likedVideos: List<VideoEntity>,
    bookmarkedVideos: List<VideoEntity>,
    onVideoClick: (VideoEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ProfileSubTab.MY_VIDEOS) }

    val userVideos = remember(allVideos) {
        allVideos.filter { it.authorHandle == "@alex_creative" || it.authorName == "Alex Rivera" }
    }

    val displayVideos = when (selectedTab) {
        ProfileSubTab.MY_VIDEOS -> if (userVideos.isNotEmpty()) userVideos else allVideos.take(2)
        ProfileSubTab.LIKED_VIDEOS -> likedVideos
        ProfileSubTab.BOOKMARKED -> bookmarkedVideos
        ProfileSubTab.PRIVATE -> emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TikTokBlack)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { }, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Filled.PersonAdd,
                    contentDescription = "Add Friends",
                    tint = TikTokWhite
                )
            }

            Text(
                text = profile?.displayName ?: "Alex Rivera",
                color = TikTokWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { }, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Settings Menu",
                    tint = TikTokWhite
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Avatar & Handle
            item {
                Spacer(modifier = Modifier.height(12.dp))

                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .border(2.dp, TikTokPink, CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(TikTokCyan, TikTokPink)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A",
                            color = TikTokWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 38.sp
                        )
                    }

                    // Edit badge
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(TikTokCyan)
                            .border(2.dp, TikTokBlack, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Avatar",
                            tint = TikTokBlack,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile?.handle ?: "@alex_creative",
                        color = TikTokWhite80,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verified",
                        tint = TikTokCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Stats Counters (Following | Followers | Likes)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatColumn(
                        count = "${profile?.followingCount ?: 184}",
                        label = "Following"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(TikTokWhite40)
                    )
                    StatColumn(
                        count = "42.9K",
                        label = "Followers"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(TikTokWhite40)
                    )
                    StatColumn(
                        count = profile?.likesCount ?: "1.2M",
                        label = "Likes"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Profile Buttons: Edit Profile, Share Profile, Bookmark
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = TikTokDarkSurface),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Edit profile", color = TikTokWhite, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = TikTokDarkSurface),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Share profile", color = TikTokWhite, fontWeight = FontWeight.SemiBold)
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TikTokDarkSurface)
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BookmarkBorder,
                            contentDescription = "Favorites",
                            tint = TikTokWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Bio
            item {
                Text(
                    text = profile?.bio ?: "✨ Visual Storyteller & Creator | Daily Reels 🚀\n📍 Los Angeles, CA | Collabs: alex@creativelab.io",
                    color = TikTokWhite,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Sub-Tabs Header (My Videos, Liked, Bookmarked, Private)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.5.dp,
                            color = Color(0xFF222222)
                        ),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ProfileTabItem(
                        icon = Icons.Filled.GridOn,
                        label = "Videos",
                        isSelected = selectedTab == ProfileSubTab.MY_VIDEOS,
                        onClick = { selectedTab = ProfileSubTab.MY_VIDEOS },
                        testTag = "profile_tab_videos"
                    )
                    ProfileTabItem(
                        icon = Icons.Filled.Lock,
                        label = "Private",
                        isSelected = selectedTab == ProfileSubTab.PRIVATE,
                        onClick = { selectedTab = ProfileSubTab.PRIVATE },
                        testTag = "profile_tab_private"
                    )
                    ProfileTabItem(
                        icon = Icons.Filled.Bookmark,
                        label = "Saved",
                        isSelected = selectedTab == ProfileSubTab.BOOKMARKED,
                        onClick = { selectedTab = ProfileSubTab.BOOKMARKED },
                        testTag = "profile_tab_saved"
                    )
                    ProfileTabItem(
                        icon = Icons.Filled.Favorite,
                        label = "Liked",
                        isSelected = selectedTab == ProfileSubTab.LIKED_VIDEOS,
                        onClick = { selectedTab = ProfileSubTab.LIKED_VIDEOS },
                        testTag = "profile_tab_liked"
                    )
                }
            }

            // Videos Grid for Tab
            if (displayVideos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (selectedTab) {
                                ProfileSubTab.PRIVATE -> "Your private videos are only visible to you 🔒"
                                ProfileSubTab.BOOKMARKED -> "No saved videos yet"
                                ProfileSubTab.LIKED_VIDEOS -> "Videos you like will appear here"
                                else -> "Upload your first video!"
                            },
                            color = TikTokWhite60,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        userScrollEnabled = false
                    ) {
                        items(displayVideos) { video ->
                            VideoThumbnailGridItem(
                                video = video,
                                onClick = { onVideoClick(video) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatColumn(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            color = TikTokWhite,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = TikTokWhite60,
            fontSize = 12.sp
        )
    }
}

@Composable
fun ProfileTabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) TikTokWhite else TikTokWhite60,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(2.dp)
                .background(if (isSelected) TikTokWhite else Color.Transparent)
        )
    }
}
