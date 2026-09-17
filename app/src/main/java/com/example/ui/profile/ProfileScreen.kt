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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DraftEntity
import com.example.data.UserProfileEntity
import com.example.data.VideoEntity
import com.example.ui.ProfileSubTab
import com.example.ui.discover.VideoThumbnailGridItem
import com.example.ui.theme.RainbowHorizontalBrush
import com.example.ui.theme.RainbowLinearBrush
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
    viewingCreator: UserProfileEntity? = null,
    allVideos: List<VideoEntity>,
    likedVideos: List<VideoEntity>,
    bookmarkedVideos: List<VideoEntity>,
    drafts: List<DraftEntity> = emptyList(),
    allAccounts: List<UserProfileEntity> = emptyList(),
    onVideoClick: (VideoEntity) -> Unit,
    onOpenBackendConsole: () -> Unit,
    onOpenSettings: () -> Unit = onOpenBackendConsole,
    onCloseCreatorProfile: () -> Unit = {},
    onEditProfile: (displayName: String, handle: String, bio: String, avatarUrl: String, isPrivate: Boolean, allowComments: Boolean) -> Unit = { _, _, _, _, _, _ -> },
    onSwitchAccount: (userId: String) -> Unit = {},
    onSignup: (displayName: String, handle: String, pin: String) -> Unit = { _, _, _ -> },
    onLogin: (handle: String, pin: String) -> Unit = { _, _ -> },
    onChangePin: (oldPin: String, newPin: String) -> Unit = { _, _ -> },
    onLogout: () -> Unit = {},
    onDeleteDraft: (Long) -> Unit = {},
    onPublishDraft: (DraftEntity) -> Unit = {},
    onToggleFollow: (UserProfileEntity) -> Unit = {},
    onMessageCreator: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ProfileSubTab.MY_VIDEOS) }

    // Dialog state
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAccountMenuDialog by remember { mutableStateOf(false) }
    var showSignupDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    val activeProfile = viewingCreator ?: profile ?: UserProfileEntity()
    val isViewingSelf = viewingCreator == null || viewingCreator.userId == profile?.userId

    val userVideos = remember(allVideos, activeProfile.handle, activeProfile.displayName) {
        allVideos.filter {
            it.authorHandle.equals(activeProfile.handle, ignoreCase = true) ||
            it.authorName.equals(activeProfile.displayName, ignoreCase = true)
        }
    }

    val privateVideos = remember(allVideos, activeProfile.handle) {
        allVideos.filter {
            it.isPrivate && (it.authorHandle.equals(activeProfile.handle, ignoreCase = true))
        }
    }

    val displayVideos = when (selectedTab) {
        ProfileSubTab.MY_VIDEOS -> if (userVideos.isNotEmpty()) userVideos else if (isViewingSelf) allVideos.take(2) else emptyList()
        ProfileSubTab.LIKED_VIDEOS -> likedVideos
        ProfileSubTab.BOOKMARKED -> bookmarkedVideos
        ProfileSubTab.PRIVATE -> privateVideos
        ProfileSubTab.DRAFTS -> emptyList()
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
            if (!isViewingSelf) {
                IconButton(onClick = onCloseCreatorProfile, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TikTokWhite
                    )
                }
            } else {
                IconButton(onClick = { showAccountMenuDialog = true }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.SwitchAccount,
                        contentDescription = "Switch Accounts",
                        tint = TikTokWhite
                    )
                }
            }

            Text(
                text = activeProfile.displayName,
                color = TikTokWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = onOpenBackendConsole,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("profile_backend_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Settings & Backend Menu",
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
                            .border(2.5.dp, RainbowHorizontalBrush, CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(RainbowLinearBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeProfile.displayName.take(1).uppercase(),
                            color = TikTokWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 38.sp
                        )
                    }

                    if (isViewingSelf) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(TikTokCyan)
                                .border(2.dp, TikTokBlack, CircleShape)
                                .clickable { showEditProfileDialog = true },
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
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = activeProfile.handle,
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
                        count = "${activeProfile.followingCount}",
                        label = "Following"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(TikTokWhite40)
                    )
                    StatColumn(
                        count = "${activeProfile.followersCount}",
                        label = "Followers"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(TikTokWhite40)
                    )
                    StatColumn(
                        count = "${activeProfile.likesCount}",
                        label = "Likes"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Profile Action Buttons
            item {
                if (isViewingSelf) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showEditProfileDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TikTokDarkSurface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Edit profile", color = TikTokWhite, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { showAccountMenuDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TikTokDarkSurface),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Accounts", color = TikTokWhite, fontWeight = FontWeight.SemiBold)
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TikTokDarkSurface)
                                .clickable { onOpenSettings() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = TikTokWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onToggleFollow(activeProfile) },
                            colors = ButtonDefaults.buttonColors(containerColor = TikTokPink),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Follow", color = TikTokWhite, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onMessageCreator(activeProfile.handle) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Message", color = TikTokWhite)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Bio
            item {
                Text(
                    text = activeProfile.bio,
                    color = TikTokWhite,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Local Backend Inspector Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(TikTokDarkCard)
                        .clickable(onClick = onOpenBackendConsole)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("open_backend_console_chip")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Dns,
                        contentDescription = null,
                        tint = TikTokCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Local Backend Engine (SQLite Active)",
                        color = TikTokCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Sub-Tabs Header
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
                    if (isViewingSelf) {
                        ProfileTabItem(
                            icon = Icons.Filled.Drafts,
                            label = "Drafts",
                            isSelected = selectedTab == ProfileSubTab.DRAFTS,
                            onClick = { selectedTab = ProfileSubTab.DRAFTS },
                            testTag = "profile_tab_drafts"
                        )
                    }
                }
            }

            // Tab Content: Drafts or Video Grid
            if (selectedTab == ProfileSubTab.DRAFTS) {
                if (drafts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No local drafts saved yet 📝",
                                color = TikTokWhite60,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(drafts) { draft ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(TikTokDarkCard)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = draft.caption.ifBlank { "Untitled Draft" },
                                    color = TikTokWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Sound: ${draft.soundTitle}",
                                    color = TikTokWhite60,
                                    fontSize = 12.sp
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { onPublishDraft(draft) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Publish,
                                        contentDescription = "Post Draft",
                                        tint = TikTokCyan
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteDraft(draft.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete Draft",
                                        tint = TikTokPink
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
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
                    items(displayVideos.chunked(3)) { rowVideos ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp, vertical = 1.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            rowVideos.forEach { video ->
                                Box(modifier = Modifier.weight(1f)) {
                                    VideoThumbnailGridItem(
                                        video = video,
                                        onClick = { onVideoClick(video) }
                                    )
                                }
                            }
                            repeat(3 - rowVideos.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Edit Profile Dialog
        if (showEditProfileDialog) {
            EditProfileDialog(
                currentProfile = activeProfile,
                onDismiss = { showEditProfileDialog = false },
                onSave = { name, handle, bio, avatar, priv, comments ->
                    onEditProfile(name, handle, bio, avatar, priv, comments)
                    showEditProfileDialog = false
                }
            )
        }

        // Account Switcher & Auth Menu Dialog
        if (showAccountMenuDialog) {
            AccountsDialog(
                currentUserId = activeProfile.userId,
                accounts = allAccounts,
                onDismiss = { showAccountMenuDialog = false },
                onSwitch = { id ->
                    onSwitchAccount(id)
                    showAccountMenuDialog = false
                },
                onAddNewAccount = {
                    showAccountMenuDialog = false
                    showSignupDialog = true
                },
                onLoginExisting = {
                    showAccountMenuDialog = false
                    showLoginDialog = true
                },
                onChangePin = {
                    showAccountMenuDialog = false
                    showChangePinDialog = true
                },
                onLogout = {
                    onLogout()
                    showAccountMenuDialog = false
                }
            )
        }

        // Local Sign Up Dialog
        if (showSignupDialog) {
            SignupDialog(
                onDismiss = { showSignupDialog = false },
                onSignup = { name, handle, pin ->
                    onSignup(name, handle, pin)
                    showSignupDialog = false
                }
            )
        }

        // Local Log In Dialog
        if (showLoginDialog) {
            LoginDialog(
                onDismiss = { showLoginDialog = false },
                onLogin = { handle, pin ->
                    onLogin(handle, pin)
                    showLoginDialog = false
                }
            )
        }

        // Change PIN Dialog
        if (showChangePinDialog) {
            ChangePinDialog(
                onDismiss = { showChangePinDialog = false },
                onChangePin = { oldPin, newPin ->
                    onChangePin(oldPin, newPin)
                    showChangePinDialog = false
                }
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    currentProfile: UserProfileEntity,
    onDismiss: () -> Unit,
    onSave: (displayName: String, handle: String, bio: String, avatarUrl: String, isPrivate: Boolean, allowComments: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(currentProfile.displayName) }
    var handle by remember { mutableStateOf(currentProfile.handle) }
    var bio by remember { mutableStateOf(currentProfile.bio) }
    var isPrivate by remember { mutableStateOf(currentProfile.isPrivate) }
    var allowComments by remember { mutableStateOf(currentProfile.allowComments) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(TikTokDarkSurface)
                .padding(20.dp)
        ) {
            Text(
                text = "Edit Profile",
                color = TikTokWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = handle,
                onValueChange = { handle = it },
                label = { Text("Username Handle") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Private Account", color = TikTokWhite, fontSize = 13.sp)
                Switch(
                    checked = isPrivate,
                    onCheckedChange = { isPrivate = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = TikTokPink)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Allow Comments", color = TikTokWhite, fontSize = 13.sp)
                Switch(
                    checked = allowComments,
                    onCheckedChange = { allowComments = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = TikTokCyan)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = TikTokDarkCard),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Cancel", color = TikTokWhite)
                }
                Button(
                    onClick = {
                        onSave(name.trim(), handle.trim(), bio.trim(), currentProfile.avatarUrl, isPrivate, allowComments)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TikTokPink),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Save", color = TikTokWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AccountsDialog(
    currentUserId: String,
    accounts: List<UserProfileEntity>,
    onDismiss: () -> Unit,
    onSwitch: (userId: String) -> Unit,
    onAddNewAccount: () -> Unit,
    onLoginExisting: () -> Unit,
    onChangePin: () -> Unit,
    onLogout: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(TikTokDarkSurface)
                .padding(20.dp)
        ) {
            Text(
                text = "Local Accounts & Profiles",
                color = TikTokWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(modifier = Modifier.height(180.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts) { acc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (acc.userId == currentUserId) Color(0xFF2A2A38) else TikTokDarkCard)
                            .clickable { onSwitch(acc.userId) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(TikTokPink),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = acc.displayName.take(1).uppercase(),
                                    color = TikTokWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = acc.displayName, color = TikTokWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = acc.handle, color = TikTokWhite60, fontSize = 11.sp)
                            }
                        }
                        if (acc.userId == currentUserId) {
                            Text(text = "Active", color = TikTokCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAddNewAccount,
                colors = ButtonDefaults.buttonColors(containerColor = TikTokCyan),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "+ Create New Profile", color = TikTokBlack, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onLoginExisting,
                colors = ButtonDefaults.buttonColors(containerColor = TikTokDarkCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Log In with PIN 🔑", color = TikTokWhite)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onChangePin,
                    colors = ButtonDefaults.buttonColors(containerColor = TikTokDarkCard),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Change PIN", color = TikTokWhite, fontSize = 12.sp)
                }
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = TikTokPink),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Log Out", color = TikTokWhite, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SignupDialog(
    onDismiss: () -> Unit,
    onSignup: (displayName: String, handle: String, pin: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(TikTokDarkSurface)
                .padding(20.dp)
        ) {
            Text(text = "Create Local Account", color = TikTokWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = handle,
                onValueChange = { handle = it },
                label = { Text("Username Handle (e.g. @dancer)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text("Security PIN (4 digits)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = TikTokDarkCard), modifier = Modifier.weight(1f)) {
                    Text("Cancel", color = TikTokWhite)
                }
                Button(
                    onClick = {
                        if (name.isNotBlank() && handle.isNotBlank()) {
                            onSignup(name.trim(), handle.trim(), pin.trim().ifBlank { "1234" })
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TikTokPink),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Sign Up", color = TikTokWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onLogin: (handle: String, pin: String) -> Unit
) {
    var handle by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(TikTokDarkSurface)
                .padding(20.dp)
        ) {
            Text(text = "Local Log In", color = TikTokWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = handle,
                onValueChange = { handle = it },
                label = { Text("Username Handle") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text("PIN") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = TikTokDarkCard), modifier = Modifier.weight(1f)) {
                    Text("Cancel", color = TikTokWhite)
                }
                Button(
                    onClick = {
                        if (handle.isNotBlank()) {
                            onLogin(handle.trim(), pin.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TikTokCyan),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Log In", color = TikTokBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ChangePinDialog(
    onDismiss: () -> Unit,
    onChangePin: (oldPin: String, newPin: String) -> Unit
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(TikTokDarkSurface)
                .padding(20.dp)
        ) {
            Text(text = "Change Security PIN", color = TikTokWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = oldPin,
                onValueChange = { oldPin = it },
                label = { Text("Current PIN") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = newPin,
                onValueChange = { newPin = it },
                label = { Text("New PIN (4 digits)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = TikTokDarkCard), modifier = Modifier.weight(1f)) {
                    Text("Cancel", color = TikTokWhite)
                }
                Button(
                    onClick = {
                        if (oldPin.isNotBlank() && newPin.isNotBlank()) {
                            onChangePin(oldPin.trim(), newPin.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TikTokPink),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Update PIN", color = TikTokWhite, fontWeight = FontWeight.Bold)
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
                .height(2.5.dp)
                .background(if (isSelected) RainbowHorizontalBrush else androidx.compose.ui.graphics.SolidColor(Color.Transparent))
        )
    }
}
