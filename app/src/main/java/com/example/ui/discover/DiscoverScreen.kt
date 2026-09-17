package com.example.ui.discover

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.VideoEntity
import com.example.ui.feed.formatCount
import com.example.ui.theme.TikTokBlack
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TikTokDarkCard
import com.example.ui.theme.TikTokDarkSurface
import com.example.ui.theme.TikTokPink
import com.example.ui.theme.TikTokWhite
import com.example.ui.theme.TikTokWhite40
import com.example.ui.theme.TikTokWhite60
import com.example.ui.theme.TikTokWhite80

data class TrendingTag(val name: String, val views: String, val description: String)

@Composable
fun DiscoverScreen(
    videos: List<VideoEntity>,
    onVideoClick: (VideoEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val trendingTags = listOf(
        TrendingTag("StreetDance", "8.4B", "Trending breakbeats & street styles"),
        TrendingTag("FoodieTok", "14.2B", "Viral culinary recipes & reviews"),
        TrendingTag("TravelVibes", "5.1B", "Hidden paradises & wanderlust"),
        TrendingTag("DailyComedy", "12.8B", "Hilarious sketches & pets"),
        TrendingTag("TechRevealed", "3.2B", "Futuristic hardware unboxings")
    )

    val filteredVideos = remember(videos, searchQuery) {
        if (searchQuery.isBlank()) videos
        else videos.filter {
            it.caption.contains(searchQuery, ignoreCase = true) ||
            it.authorName.contains(searchQuery, ignoreCase = true) ||
            it.soundTitle.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TikTokBlack)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Search Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search creators, sounds, #tags", color = TikTokWhite60, fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = TikTokWhite60
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear",
                                tint = TikTokWhite60
                            )
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("discover_search_input"),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TikTokDarkSurface,
                    unfocusedContainerColor = TikTokDarkSurface,
                    focusedTextColor = TikTokWhite,
                    unfocusedTextColor = TikTokWhite,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(TikTokDarkSurface)
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = "Scan QR",
                    tint = TikTokWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            // Trending Banner Carousel
            item {
                TrendingHeroBanner()
            }

            // Trending Hashtags Row
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = TikTokPink,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Trending Now",
                            color = TikTokWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(trendingTags) { tag ->
                            TrendingTagCard(
                                tag = tag,
                                onClick = { searchQuery = tag.name }
                            )
                        }
                    }
                }
            }

            // Explore Videos Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "Discover Popular Videos" else "Search Results",
                        color = TikTokWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Video Grid Items
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    userScrollEnabled = false
                ) {
                    items(filteredVideos) { video ->
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

@Composable
fun TrendingHeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF833AB4),
                        TikTokPink,
                        Color(0xFFFCB045)
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(TikTokBlack.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "#FEATURED CREATOR SPOTLIGHT",
                    color = TikTokCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Join The Summer Remix Challenge",
                color = TikTokWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Showcase your best 15s transition • 2.8M submissions",
                color = TikTokWhite80,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun TrendingTagCard(
    tag: TrendingTag,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(TikTokDarkSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(TikTokDarkCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Tag,
                contentDescription = null,
                tint = TikTokCyan,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "#${tag.name}",
                color = TikTokWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${tag.views} views",
                color = TikTokWhite60,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun VideoThumbnailGridItem(
    video: VideoEntity,
    onClick: () -> Unit
) {
    val imageDrawableRes = remember(video.coverResName) {
        when (video.coverResName) {
            "video_cover_dance" -> R.drawable.video_cover_dance
            "video_cover_food" -> R.drawable.video_cover_food
            "video_cover_travel" -> R.drawable.video_cover_travel
            else -> R.drawable.video_cover_dance
        }
    }

    Box(
        modifier = Modifier
            .height(160.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .testTag("discover_video_item_${video.id}")
    ) {
        Image(
            painter = painterResource(id = imageDrawableRes),
            contentDescription = video.caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
        )

        // View count
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = TikTokWhite,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = formatCount(video.likesCount),
                color = TikTokWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
