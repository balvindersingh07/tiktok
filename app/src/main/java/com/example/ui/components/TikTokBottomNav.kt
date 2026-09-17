package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainTab
import com.example.ui.theme.RainbowHorizontalBrush
import com.example.ui.theme.TikTokBlack
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TikTokPink
import com.example.ui.theme.TikTokWhite
import com.example.ui.theme.TikTokWhite60

@Composable
fun TikTokBottomNav(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isHome = selectedTab == MainTab.HOME
    val backgroundColor = if (isHome) TikTokBlack.copy(alpha = 0.95f) else TikTokBlack

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Rainbow top border divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(RainbowHorizontalBrush)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Home
            NavTabItem(
                label = "Home",
                isSelected = selectedTab == MainTab.HOME,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                onClick = { onTabSelected(MainTab.HOME) },
                testTag = "nav_home"
            )

            // Discover / Friends
            NavTabItem(
                label = "Discover",
                isSelected = selectedTab == MainTab.DISCOVER,
                selectedIcon = Icons.Filled.Explore,
                unselectedIcon = Icons.Outlined.Explore,
                onClick = { onTabSelected(MainTab.DISCOVER) },
                testTag = "nav_discover"
            )

            // Special Center Create (+) Button
            TikTokCreateButton(
                onClick = { onTabSelected(MainTab.CREATE) },
                testTag = "nav_create"
            )

            // Inbox with unread badge
            NavTabItem(
                label = "Inbox",
                isSelected = selectedTab == MainTab.INBOX,
                selectedIcon = Icons.Filled.ChatBubble,
                unselectedIcon = Icons.Filled.ChatBubbleOutline,
                badgeCount = 3,
                onClick = { onTabSelected(MainTab.INBOX) },
                testTag = "nav_inbox"
            )

            // Profile
            NavTabItem(
                label = "Profile",
                isSelected = selectedTab == MainTab.PROFILE,
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.PersonOutline,
                onClick = { onTabSelected(MainTab.PROFILE) },
                testTag = "nav_profile"
            )
        }
    }
}

@Composable
fun NavTabItem(
    label: String,
    isSelected: Boolean,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeCount: Int = 0,
    onClick: () -> Unit,
    testTag: String
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "tab_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .scale(scale)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = if (isSelected) TikTokWhite else TikTokWhite60,
                modifier = Modifier.size(24.dp)
            )

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 6.dp, y = (-3).dp)
                        .clip(CircleShape)
                        .background(TikTokPink)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = TikTokWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) TikTokWhite else TikTokWhite60,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun TikTokCreateButton(
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = 4.dp)
    ) {
        // Dual color cyan and pink offset backdrop
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(29.dp)
        ) {
            // Cyan tab (left)
            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(29.dp)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TikTokCyan)
            )

            // Pink tab (right)
            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(29.dp)
                    .align(Alignment.CenterEnd)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TikTokPink)
            )

            // Center White button
            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(29.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TikTokWhite),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Create Video",
                    tint = TikTokBlack,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
