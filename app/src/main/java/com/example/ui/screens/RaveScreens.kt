package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import android.content.Context
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.RaveApiFactory
import com.example.player.SyncVideoPlayer
import com.example.viewmodel.RaveViewModel
import com.example.viewmodel.Screen
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RaveLogoCircle(size: Int = 32) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(Color.White, CircleShape)
            .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "RC",
            color = Color.Black,
            fontWeight = FontWeight.Black,
            fontSize = (size * 0.38f).sp
        )
    }
}

@Composable
fun RaveHeader(
    title: String,
    roomText: String? = null,
    onBackClick: (() -> Unit)? = null,
    rightContent: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF090909))
            .statusBarsPadding()
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri Git", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            } else {
                RaveLogoCircle(32)
            }
            
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 16.sp
                )
                if (roomText != null) {
                    Text(
                        text = roomText,
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        lineHeight = 12.sp
                    )
                }
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rightContent?.invoke(this)
        }
    }
}

@Composable
fun RaveSyncStatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF020202))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "API ENDPOINT: bosforlab.online/v1/sync",
            color = Color(0xFF888888),
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 8.sp,
            letterSpacing = (-0.2).sp
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(Color(0xFF00FF66), CircleShape)
            )
            Text(
                text = "14ms Ping",
                color = Color(0xFFBBBBBB),
                fontWeight = FontWeight.Bold,
                fontSize = 8.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun RaveModToolsBar(
    onMuteRoomClick: () -> Unit = {},
    onModerateAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Row(
            modifier = Modifier
                .background(Color(0xFF1E1E1E), RoundedCornerShape(4.dp))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Shield",
                tint = Color.LightGray,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = "MOD TOOLS",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(12.dp)
                .background(Color.White.copy(alpha = 0.1f))
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFF121212), CircleShape)
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), CircleShape)
                    .clickable { onMuteRoomClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MicOff,
                    contentDescription = "Mute Room",
                    tint = Color.LightGray,
                    modifier = Modifier.size(12.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFF121212), CircleShape)
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), CircleShape)
                    .clickable { onModerateAllClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Moderate Room",
                    tint = Color.LightGray,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

// DEFINED AVATARS (Emojis paired with styling colors)
val AVATARS = listOf(
    "avatar1" to Triple("🐼", "Neon Panda", Color(0xFFE0E0E0)),
    "avatar2" to Triple("🚀", "Space Cadet", Color(0xFF6E6E6E)),
    "avatar3" to Triple("⚔️", "Cyber Samurai", Color(0xFF262626)),
    "avatar4" to Triple("🦇", "Goth Wave", Color(0xFF121212)),
    "avatar5" to Triple("🌊", "Abstract Wave", Color(0xFFF2F2F2)),
    "avatar6" to Triple("⚡", "Retro Laser", Color(0xFF8C8C8C)),
    "avatar7" to Triple("👾", "Mono Pixel", Color(0xFF424242)),
    "avatar8" to Triple("🌙", "Luna Star", Color(0xFFD9D9D9))
)

fun getAvatarEmoji(key: String): String {
    return AVATARS.firstOrNull { it.first == key }?.second?.first ?: "🐼"
}

fun getAvatarColor(key: String): Color {
    return AVATARS.firstOrNull { it.first == key }?.second?.third ?: Color.White
}

@Composable
fun AvatarBadge(
    avatarKey: String,
    modifier: Modifier = Modifier,
    size: Int = 40,
    selected: Boolean = false
) {
    val emoji = getAvatarEmoji(avatarKey)
    val color = getAvatarColor(avatarKey)
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Color.White else Color.Black.copy(alpha = 0.2f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = (size * 0.55f).sp)
    }
}

// 1. LOGIN SCREEN
@Composable
fun LoginScreen(viewModel: RaveViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loggedIn by viewModel.isLoggedIn.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            RaveLogoCircle(64)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Rave Co",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 4.sp
            )
            Text(
                text = "Eş Zamanlı Hayat",
                fontSize = 12.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Kullanıcı Adı", color = Color.White) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_username_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Şifre", color = Color.White) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.login(username, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_submit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("GİRİŞ YAP", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Hesabın yok mu? Kayıt Ol",
                color = Color.LightGray,
                fontSize = 13.sp,
                modifier = Modifier
                    .clickable { viewModel.navigateTo(Screen.Register) }
                    .padding(8.dp)
            )
        }
    }
}

// 2. REGISTER SCREEN
@Composable
fun RegisterScreen(viewModel: RaveViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf("avatar1") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                RaveLogoCircle(64)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "RAVE CO",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "Yeni Profil Oluştur",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Avatar customize selection
                Text(
                    text = "Avatarını Seç",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AVATARS.take(4).forEach { (key, _) ->
                        AvatarBadge(
                            avatarKey = key,
                            selected = selectedAvatar == key,
                            size = 50,
                            modifier = Modifier.clickable { selectedAvatar = key }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AVATARS.drop(4).forEach { (key, _) ->
                        AvatarBadge(
                            avatarKey = key,
                            selected = selectedAvatar == key,
                            size = 50,
                            modifier = Modifier.clickable { selectedAvatar = key }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Kullanıcı Adı", color = Color.White) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_username_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Şifre", color = Color.White) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.register(username, password, selectedAvatar) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("register_submit_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("KAYIT OL VE GİRİŞ YAP", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Zaten üye misin? Giriş Yap",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(Screen.Login) }
                        .padding(8.dp)
                )
            }
        }
    }
}

// 3. ROOMS LIST SCREEN
@Composable
fun RoomsListScreen(viewModel: RaveViewModel) {
    val rooms by viewModel.rooms.collectAsState()
    var isCreateOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadRooms()
    }

    Scaffold(
        topBar = {
            RaveHeader(
                title = "Rave Co.",
                roomText = "Aktif Odalar",
                rightContent = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.DmsList) }, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.ChatBubble, contentDescription = "Mesajlar", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.Friends) }, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.People, contentDescription = "Arkadaşlar", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.Profile) }, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profil", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreateOpen = true },
                containerColor = Color.White,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Oda Oluştur")
            }
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            RaveSyncStatusBar()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
            if (rooms.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = "No Rooms",
                        modifier = Modifier.size(64.dp),
                        tint = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aktif Bir Oda Bulunmuyor",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aşağıdan yeni bir oda kurup arkadaşlarınla eş zamanlı video izlemeye başlayabilirsin!",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(rooms) { r ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.joinRoom(r.id) },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0C)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = r.name,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Oynatılıyor: ${if (r.videoTitle.isEmpty()) "Video Yok" else r.videoTitle}",
                                        color = Color.LightGray,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AvatarBadge(avatarKey = r.ownerAvatar, size = 16)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Yönetici: ${r.ownerName}",
                                            color = Color.LightGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(
                                            Color.White.copy(alpha = 0.1f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "İzleyiciler",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = r.participantCount.toString(),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // CREATE ROOM MODAL Dialog
        if (isCreateOpen) {
            var roomName by remember { mutableStateOf("") }
            var videoUrl by remember { mutableStateOf("") }
            var videoTitle by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { isCreateOpen = false },
                title = { Text("Yeni Oda Oluştur", color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = roomName,
                            onValueChange = { roomName = it },
                            label = { Text("Oda Adı", color = Color.White) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = videoUrl,
                            onValueChange = { videoUrl = it },
                            label = { Text("Video URL (YouTube veya Direkt MP4/M3U8)", color = Color.White) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = videoTitle,
                            onValueChange = { videoTitle = it },
                            label = { Text("Video Başlığı (İsteğe bağlı)", color = Color.White) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (roomName.isNotEmpty()) {
                                isCreateOpen = false
                                viewModel.createRoom(roomName, videoUrl, videoTitle)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Oluştur")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isCreateOpen = false }) {
                        Text("İptal", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E1E1E)
            )
        }
    }
}

// 4. ROOM VIEW SCREEN
@Composable
fun RoomViewScreen(viewModel: RaveViewModel, roomId: Int) {
    val syncState by viewModel.roomSyncState.collectAsState()
    var currentTab by remember { mutableStateOf(0) } // 0 = Chat, 1 = Participants
    var chatMessage by remember { mutableStateOf("") }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    val currentUserId by viewModel.userId.collectAsState()
    var activeMenuMessage by remember { mutableStateOf<com.example.data.model.RoomMessage?>(null) }
    var expandedMessageId by remember { mutableStateOf<Int?>(null) }
    var replyingToMessage by remember { mutableStateOf<com.example.data.model.RoomMessage?>(null) }
    val context = LocalContext.current

    DisposableEffect(isFullscreen) {
        val activity = context as? android.app.Activity
        if (activity != null) {
            if (isFullscreen) {
                activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    activity.window.decorView.windowInsetsController?.let { controller ->
                        controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                        controller.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                } else {
                    @Suppress("DEPRECATION")
                    activity.window.decorView.systemUiVisibility = (
                        android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                        android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
                }
            } else {
                activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    activity.window.decorView.windowInsetsController?.show(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                } else {
                    @Suppress("DEPRECATION")
                    activity.window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_VISIBLE
                }
            }
        }
        onDispose {
            val act = context as? android.app.Activity
            if (act != null) {
                act.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    act.window.decorView.windowInsetsController?.show(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                } else {
                    @Suppress("DEPRECATION")
                    act.window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_VISIBLE
                }
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val chatListState = rememberLazyListState()

    // Automatic chat scrolls
    LaunchedEffect(syncState?.newMessages?.size) {
        syncState?.newMessages?.size?.let { size ->
            if (size > 0) {
                chatListState.animateScrollToItem(size - 1)
            }
        }
    }

    if (syncState == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    val sync = syncState!!
    val isOwnerOrMod = sync.myRole == "owner" || sync.myRole == "moderator"

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Player Area (Tops)
            SyncVideoPlayer(
                viewModel = viewModel,
                videoUrl = sync.videoUrl,
                isFullscreen = isFullscreen,
                isFullscreenToggle = { isFullscreen = !isFullscreen }
            )

            if (!isFullscreen) {
                // Info Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sync.videoTitle.ifEmpty { "Video Yok" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Oda: ${sync.roomId} • Kurucu: ${sync.participants.firstOrNull { it.role == "owner" }?.username ?: "Yok"}",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    Row {
                        if (isOwnerOrMod) {
                            IconButton(onClick = { isSettingsOpen = true }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Kaynağı Değiştir", tint = Color.White)
                            }
                        }
                        IconButton(onClick = { viewModel.leaveRoom(roomId) }) {
                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Odadan Ayrıl", tint = RedWarning)
                        }
                    }
                }

                // Split Tabs (Chat / Participants)
                TabRow(
                    selectedTabIndex = currentTab,
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            color = Color.White,
                            modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab])
                        )
                    }
                ) {
                    Tab(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        text = { Text("Sohbet (${sync.newMessages.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        text = { Text("Katılımcılar (${sync.participants.size})", fontWeight = FontWeight.Bold) }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (currentTab == 0) {
                        // SOHBET STREAM
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                state = chatListState,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(sync.newMessages) { m ->
                                    if (m.isSystem) {
                                        // System message formatting
                                        val sysParts = m.message.split(":")
                                        val command = sysParts.getOrNull(0) ?: ""
                                        val payload = sysParts.getOrNull(1) ?: ""
                                        val cleanText = when (command) {
                                            "system_join" -> "🎉 $payload odaya katıldı!"
                                            "system_left" -> "🚪 $payload odadan ayrıldı."
                                            "system_kick" -> "🚫 $payload odadan atıldı."
                                            "system_mute" -> "🔇 $payload sessize alındı."
                                            "system_unmute" -> "🔊 $payload konuşma kilidi kalktı."
                                            "system_promote" -> "⭐ $payload moderatör atandı!"
                                            "system_demote" -> "📉 $payload moderatörlük yetkisi alındı."
                                            "system_video" -> "🎬 Video Değiştirildi: $payload"
                                            "system_transfer" -> "👑 Oda Sahibi Devri: $payload yeni yönetici!"
                                            else -> m.message
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = cleanText,
                                                color = Color.LightGray,
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .background(
                                                        Color.White.copy(alpha = 0.05f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else {
                                        // Regular Chat message balloon
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (m.isDeleted != true) {
                                                        expandedMessageId = if (expandedMessageId == m.id) null else m.id
                                                    }
                                                }
                                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                        ) {
                                            AvatarBadge(avatarKey = m.senderAvatar, size = 32)
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = m.senderName,
                                                        color = Color.LightGray,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            Color(0xFF1E1E1E),
                                                            RoundedCornerShape(
                                                                topStart = 0.dp,
                                                                topEnd = 12.dp,
                                                                bottomStart = 12.dp,
                                                                bottomEnd = 12.dp
                                                            )
                                                        )
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Column {
                                                        // Reply Context Box
                                                        if (m.replyToId != null) {
                                                            val rName = m.replyToName ?: "Bilinmeyen"
                                                            val rMsg = m.replyToMsg ?: ""
                                                            Row(
                                                                modifier = Modifier
                                                                    .padding(bottom = 6.dp)
                                                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                                                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(4.dp))
                                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Column {
                                                                    Text(
                                                                        text = "↩ $rName",
                                                                        color = Color.LightGray,
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontSize = 10.sp
                                                                    )
                                                                    Text(
                                                                        text = rMsg,
                                                                        color = Color.Gray,
                                                                        fontSize = 10.sp,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        
                                                        Text(
                                                            text = m.message,
                                                            color = if (m.isDeleted == true) Color.Gray else Color.White,
                                                            fontSize = 13.sp,
                                                            fontStyle = if (m.isDeleted == true) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
                                                        )
                                                    }
                                                }

                                                // Dynamic inline action buttons when clicked
                                                if (expandedMessageId == m.id) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        // Reply inline action
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                            modifier = Modifier
                                                                .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
                                                                .clickable {
                                                                    replyingToMessage = m
                                                                    expandedMessageId = null
                                                                }
                                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Reply,
                                                                contentDescription = "Yanıtla",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(11.dp)
                                                            )
                                                            Text(
                                                                text = "Yanıtla",
                                                                color = Color.White,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                        // Copy inline action
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                            modifier = Modifier
                                                                .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
                                                                .clickable {
                                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                                    val clip = android.content.ClipData.newPlainText("rave_msg", m.message)
                                                                    clipboard.setPrimaryClip(clip)
                                                                    viewModel.showToast("Mesaj kopyalandı!")
                                                                    expandedMessageId = null
                                                                }
                                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.ContentCopy,
                                                                contentDescription = "Kopyala",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(11.dp)
                                                            )
                                                            Text(
                                                                text = "Kopyala",
                                                                color = Color.White,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                        // Delete inline action
                                                        if (m.userId == currentUserId || isOwnerOrMod) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                                modifier = Modifier
                                                                    .background(Color(0xFFE53935), RoundedCornerShape(12.dp))
                                                                    .clickable {
                                                                        viewModel.deleteRoomMessage(roomId, m.id)
                                                                        expandedMessageId = null
                                                                    }
                                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Delete,
                                                                    contentDescription = "Mesajı Sil",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(11.dp)
                                                                )
                                                                Text(
                                                                    text = "Sil",
                                                                    color = Color.White,
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Mod tools bar
                            if (isOwnerOrMod) {
                                RaveModToolsBar(
                                    onMuteRoomClick = {
                                        viewModel.sendRoomMessage(roomId, "system_mute_all:Herkes")
                                    },
                                    onModerateAllClick = {
                                        currentTab = 1
                                    }
                                )
                            }

                            // Reply Target Preview Panel
                            if (replyingToMessage != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF141414))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${replyingToMessage!!.senderName} kullanıcısına yanıt veriyorsun",
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = replyingToMessage!!.message,
                                            color = Color.Gray,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(
                                        onClick = { replyingToMessage = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Yanıtı Temizle",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Message Composer (High Density style from design)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF090909))
                                    .navigationBarsPadding()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Add button
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFF1C1C1C), RoundedCornerShape(12.dp))
                                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.showToast("Görsel/Video veya link ekleme desteği aktif!")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Ekle",
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                
                                OutlinedTextField(
                                    value = chatMessage,
                                    onValueChange = { chatMessage = it },
                                    placeholder = { Text(if (sync.myMuteStatus) "Sessiz modundasınız..." else "Mesaj yaz veya link yapıştır...", color = Color(0xFF6E6E6E), fontSize = 12.sp) },
                                    singleLine = true,
                                    enabled = !sync.myMuteStatus,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = {
                                        if (chatMessage.isNotEmpty()) {
                                            if (replyingToMessage != null) {
                                                viewModel.sendRoomMessage(
                                                    roomId = roomId,
                                                    msg = chatMessage,
                                                    replyToId = replyingToMessage!!.id,
                                                    replyToName = replyingToMessage!!.senderName,
                                                    replyToMsg = replyingToMessage!!.message
                                                )
                                                replyingToMessage = null
                                            } else {
                                                viewModel.sendRoomMessage(roomId, chatMessage)
                                            }
                                            chatMessage = ""
                                        }
                                    }),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.White.copy(alpha = 0.3f),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                        focusedContainerColor = Color(0xFF141414),
                                        unfocusedContainerColor = Color(0xFF141414),
                                        disabledContainerColor = Color(0xFF0E0E0E),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Send button
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (!sync.myMuteStatus && chatMessage.isNotEmpty()) Color.White else Color(0xFF1E1E1E),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            BorderStroke(1.dp, Color.White.copy(alpha = if (chatMessage.isNotEmpty()) 0.2f else 0.05f)),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable(enabled = !sync.myMuteStatus && chatMessage.isNotEmpty()) {
                                            if (replyingToMessage != null) {
                                                viewModel.sendRoomMessage(
                                                    roomId = roomId,
                                                    msg = chatMessage,
                                                    replyToId = replyingToMessage!!.id,
                                                    replyToName = replyingToMessage!!.senderName,
                                                    replyToMsg = replyingToMessage!!.message
                                                )
                                                replyingToMessage = null
                                            } else {
                                                viewModel.sendRoomMessage(roomId, chatMessage)
                                            }
                                            chatMessage = ""
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Gönder",
                                        tint = if (!sync.myMuteStatus && chatMessage.isNotEmpty()) Color.Black else Color.LightGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Centered Interactive Options Dialog Menu
                            if (false && activeMenuMessage != null) {
                                val msg = activeMenuMessage!!
                                AlertDialog(
                                    onDismissRequest = { activeMenuMessage = null },
                                    title = {
                                        Text(
                                            text = "${msg.senderName} • Mesaj Seçenekleri",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    text = {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "\"${msg.message}\"",
                                                color = Color.Gray,
                                                fontSize = 13.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            
                                            // Reply Button
                                            Button(
                                                onClick = {
                                                    replyingToMessage = msg
                                                    activeMenuMessage = null
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Yanıtla", color = Color.White)
                                            }
                                            
                                            // Copy Button
                                            Button(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                    val clip = android.content.ClipData.newPlainText("rave_msg", msg.message)
                                                    clipboard.setPrimaryClip(clip)
                                                    viewModel.showToast("Mesaj kopyalandı!")
                                                    activeMenuMessage = null
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Metni Kopyala", color = Color.White)
                                            }
                                            
                                            // Delete Button (if owned by me OR I am creator/moderator)
                                            if (msg.userId == currentUserId || isOwnerOrMod) {
                                                Button(
                                                    onClick = {
                                                        viewModel.deleteRoomMessage(roomId, msg.id)
                                                        activeMenuMessage = null
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("Mesajı Sil", color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {},
                                    dismissButton = {
                                        TextButton(onClick = { activeMenuMessage = null }) {
                                            Text("Kapat", color = Color.White)
                                        }
                                    },
                                    containerColor = Color(0xFF141414)
                                )
                            }
                        }
                    } else {
                        // PARTICIPANTS PANEL (With moderation actions)
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(sync.participants) { p ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                                    border = BorderStroke(1.dp, Color.DarkGray)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            AvatarBadge(avatarKey = p.avatar, size = 36)
                                            Column {
                                                Text(
                                                    text = p.username + if (p.userId == viewModel.userId.value) " (Sen)" else "",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = when (p.role) {
                                                        "owner" -> "👑 Kurucu"
                                                        "moderator" -> "⭐ Moderatör"
                                                        else -> "🗣️ Katılımcı"
                                                    } + if (p.isMuted) " • 🔇 Sessiz" else "",
                                                    color = Color.LightGray,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        // Moderation tools triggered only by higher rankings
                                        if (p.userId != viewModel.userId.value && isOwnerOrMod) {
                                            val myRank = sync.myRole
                                            var showMenu by remember { mutableStateOf(false) }

                                            Box {
                                                IconButton(onClick = { showMenu = true }) {
                                                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Yönet", tint = Color.White)
                                                }
                                                DropdownMenu(
                                                    expanded = showMenu,
                                                    onDismissRequest = { showMenu = false },
                                                    modifier = Modifier.background(Color(0xFF1E1E1E))
                                                ) {
                                                    // Kick Action
                                                    DropdownMenuItem(
                                                        text = { Text("Odadan At (Kick)", color = RedWarning) },
                                                        onClick = {
                                                            showMenu = false
                                                            viewModel.moderateParticipant(roomId, p.userId, "kick")
                                                        }
                                                    )
                                                    // Mute / Unmute
                                                    if (p.isMuted) {
                                                        DropdownMenuItem(
                                                            text = { Text("Sesi Aç (Unmute)", color = Color.White) },
                                                            onClick = {
                                                                showMenu = false
                                                                viewModel.moderateParticipant(roomId, p.userId, "unmute")
                                                            }
                                                        )
                                                    } else {
                                                        DropdownMenuItem(
                                                            text = { Text("Sessize Al (Mute)", color = Color.LightGray) },
                                                            onClick = {
                                                                showMenu = false
                                                                viewModel.moderateParticipant(roomId, p.userId, "mute")
                                                            }
                                                        )
                                                    }

                                                    // Promoter configs only for room owner
                                                    if (myRank == "owner") {
                                                        if (p.role == "moderator") {
                                                            DropdownMenuItem(
                                                                text = { Text("Moderatörlüğü Kaldır (Demote)", color = Color.LightGray) },
                                                                onClick = {
                                                                    showMenu = false
                                                                    viewModel.moderateParticipant(roomId, p.userId, "demote")
                                                                }
                                                            )
                                                        } else if (p.role == "member") {
                                                            DropdownMenuItem(
                                                                text = { Text("Moderatör Yap (Promote)", color = Color.White) },
                                                                onClick = {
                                                                    showMenu = false
                                                                    viewModel.moderateParticipant(roomId, p.userId, "promote")
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // SETTINGS DIALOG (Source URL updates)
        if (isSettingsOpen) {
            var inputUrl by remember { mutableStateOf("") }
            var inputTitle by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { isSettingsOpen = false },
                title = { Text("Oynatılan Videoyu Değiştir", color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            label = { Text("Video URL (YouTube, MP4, M3U8)", color = Color.White) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = inputTitle,
                            onValueChange = { inputTitle = it },
                            label = { Text("Video Başlığı", color = Color.White) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputUrl.isNotEmpty()) {
                                isSettingsOpen = false
                                viewModel.updateRoomVideoSource(roomId, inputUrl, inputTitle)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Değiştir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isSettingsOpen = false }) {
                        Text("İptal", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E1E1E)
            )
        }
    }
}

// 5. FRIENDS SCREEN
@Composable
fun FriendsScreen(viewModel: RaveViewModel) {
    val friends by viewModel.friends.collectAsState()
    val incoming by viewModel.incomingRequests.collectAsState()
    val outgoing by viewModel.outgoingRequests.collectAsState()
    var searchUsername by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadFriends()
    }

    Scaffold(
        topBar = {
            RaveHeader(
                title = "Sosyal & Arkadaşlar",
                roomText = "Arkadaş Ekleme ve Sosyal Ağ",
                onBackClick = { viewModel.navigateBack() }
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
                .padding(16.dp)
        ) {
            // Send Request Box
            Text(
                text = "Arkadaşlık İsteği Gönder",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchUsername,
                    onValueChange = { searchUsername = it },
                    placeholder = { Text("Kullanıcı adı girin...", color = Color.DarkGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (searchUsername.isNotEmpty()) {
                            viewModel.handleFriendAction(searchUsername, "send_request")
                            searchUsername = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("EKLE")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Scroll lists
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Incoming Requests (Gelen İstekler)
                if (incoming.isNotEmpty()) {
                    item {
                        Text(
                            text = "Gelen İstekler (${incoming.size})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    items(incoming) { item ->
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                                border = BorderStroke(1.dp, Color.DarkGray)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AvatarBadge(avatarKey = item.avatar, size = 36)
                                        Column {
                                            Text(text = item.username, color = Color.White, fontWeight = FontWeight.Bold)
                                            Text(text = item.statusText, color = Color.LightGray, fontSize = 11.sp)
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { viewModel.handleFriendAction(item.username, "accept") },
                                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
                                        ) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = "Accept", tint = Color.Black)
                                        }
                                        IconButton(
                                            onClick = { viewModel.handleFriendAction(item.username, "reject") },
                                            colors = IconButtonDefaults.iconButtonColors(containerColor = RedWarning.copy(alpha = 0.2f))
                                        ) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "Reject", tint = RedWarning)
                                        }
                                    }
                                }
                            }
                    }
                }

                // Friends (Arkadaşlar)
                item {
                    Text(
                        text = "Arkadaşların (${friends.size})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (friends.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Henüz arkadaş eklenmemiş.", color = Color.DarkGray, fontSize = 13.sp)
                        }
                    }
                } else {
                    items(friends) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                            border = BorderStroke(1.dp, Color.DarkGray)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AvatarBadge(avatarKey = item.avatar, size = 36)
                                    Column {
                                        Text(text = item.username, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text(text = item.statusText, color = Color.LightGray, fontSize = 11.sp)
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            viewModel.navigateTo(Screen.DmChat(item.userId, item.username, item.avatar))
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
                                    ) {
                                        Icon(imageVector = Icons.Default.Chat, contentDescription = "Sohbet", tint = Color.Black)
                                    }
                                    IconButton(
                                        onClick = { viewModel.handleFriendAction(item.username, "remove") }
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Çıkar", tint = Color.LightGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



// 6. DMS CONVERSATIONS LIST SCREEN
@Composable
fun DmsListScreen(viewModel: RaveViewModel) {
    val conversations by viewModel.conversations.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    Scaffold(
        topBar = {
            RaveHeader(
                title = "Sohbetler ve DM",
                roomText = "Özel mesaj kutusu",
                onBackClick = { viewModel.navigateBack() }
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            if (conversations.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "No DMs",
                        modifier = Modifier.size(64.dp),
                        tint = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sohbet Bulunmuyor",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Arkadaşların listesine giderek doğrudan birebir özel sohbet başlatabilirsin!",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(conversations) { c ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.navigateTo(Screen.DmChat(c.userId, c.username, c.avatar))
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                            border = BorderStroke(1.dp, Color.DarkGray)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    AvatarBadge(avatarKey = c.avatar, size = 44)
                                    Column {
                                        Text(text = c.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(
                                            text = c.lastMessage.ifEmpty { "Son mesaj bulunmuyor" },
                                            color = Color.LightGray,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (c.unreadCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White, CircleShape)
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = c.unreadCount.toString(),
                                            color = Color.Black,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 7. DM PRIVATE CHAT SCREEN
@Composable
fun DmChatScreen(
    viewModel: RaveViewModel,
    partnerId: Int,
    partnerUsername: String,
    partnerAvatar: String
) {
    val dmMessages by viewModel.dmMessages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Load DM Messages Polling
    LaunchedEffect(partnerId) {
        while (true) {
            viewModel.loadDmMessages(partnerId)
            kotlinx.coroutines.delay(1500)
        }
    }

    LaunchedEffect(dmMessages.size) {
        if (dmMessages.isNotEmpty()) {
            listState.animateScrollToItem(dmMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            RaveHeader(
                title = partnerUsername,
                roomText = "Birebir Özel Mesajlaşma",
                onBackClick = { viewModel.navigateBack() },
                rightContent = {
                    AvatarBadge(avatarKey = partnerAvatar, size = 32)
                }
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dmMessages) { m ->
                    val isMe = m.senderId != partnerId
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) Color.White else Color(0xFF1E1E1E)
                            ),
                            shape = RoundedCornerShape(
                                topStart = if (isMe) 12.dp else 0.dp,
                                topEnd = if (isMe) 0.dp else 12.dp,
                                bottomStart = 12.dp,
                                bottomEnd = 12.dp
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = m.message,
                                color = if (isMe) Color.Black else Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Input Bar (High Density Style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090909))
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF1C1C1C), RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.showToast("Özel görsel/dosya ekleme yakında aktif!")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ekle",
                        tint = Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Özel mesaj yazın...", color = Color(0xFF6E6E6E), fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color(0xFF141414),
                        unfocusedContainerColor = Color(0xFF141414),
                        disabledContainerColor = Color(0xFF0E0E0E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                // Send button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (messageText.isNotEmpty()) Color.White else Color(0xFF1E1E1E),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            BorderStroke(1.dp, Color.White.copy(alpha = if (messageText.isNotEmpty()) 0.2f else 0.05f)),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = messageText.isNotEmpty()) {
                            viewModel.sendDmMessage(partnerId, messageText)
                            messageText = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Gönder",
                        tint = if (messageText.isNotEmpty()) Color.Black else Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// 8. PROFILE SETTINGS SCREEN (Dynamic Avatars and API url configs)
@Composable
fun ProfileScreen(viewModel: RaveViewModel) {
    val context = LocalContext.current
    var inputUrl by remember { mutableStateOf(RaveApiFactory.getApiUrl(context)) }
    val userAvatarState by viewModel.userAvatar.collectAsState()
    val statusTextState by viewModel.statusText.collectAsState()
    var currentAvatar by remember(userAvatarState) { mutableStateOf(userAvatarState) }
    var inputStatus by remember(statusTextState) { mutableStateOf(statusTextState) }
    var inputPassword by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            RaveHeader(
                title = "Profil Yönetimi",
                roomText = "Avatar, Şifre ve Bağlantı Ayarları",
                onBackClick = { viewModel.navigateBack() }
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AvatarBadge(avatarKey = currentAvatar, size = 64)
                    Column {
                        Text(
                            text = viewModel.username.collectAsState().value,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = statusTextState,
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            item {
                Divider(color = Color.DarkGray)
            }

            item {
                Text("Profilini Düzenle", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // Avatar selection Row
            item {
                Text("Yeni Avatar Seç", color = Color.LightGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AVATARS.take(4).forEach { (key, _) ->
                        AvatarBadge(
                            avatarKey = key,
                            selected = currentAvatar == key,
                            size = 46,
                            modifier = Modifier.clickable { currentAvatar = key }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AVATARS.drop(4).forEach { (key, _) ->
                        AvatarBadge(
                            avatarKey = key,
                            selected = currentAvatar == key,
                            size = 46,
                            modifier = Modifier.clickable { currentAvatar = key }
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = inputStatus,
                    onValueChange = { inputStatus = it },
                    label = { Text("Durum Mesajı", color = Color.White) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = inputPassword,
                    onValueChange = { inputPassword = it },
                    label = { Text("Yeni Şifre (Boş bırakılırsa değişmez)", color = Color.White) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Button(
                    onClick = {
                        viewModel.updateProfile(
                            avatar = currentAvatar,
                            status = inputStatus,
                            newPass = if (inputPassword.isBlank()) null else inputPassword
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Değişiklikleri Kaydet")
                }
            }

            item {
                Divider(color = Color.DarkGray)
            }

            // API Server Configurations
            item {
                Text("Gelişmiş Web Sunucusu", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Uygulamanın Bosforlab API'lerini çekeceği adres burasıdır. Hosting adresinizi değiştiyseniz buradan güncelleyin.",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }

            item {
                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    label = { Text("Hosting API Adresi (bosforlab.online)", color = Color.White) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Button(
                    onClick = {
                        if (inputUrl.isNotEmpty()) {
                            RaveApiFactory.setApiUrl(context, inputUrl)
                            RaveApiFactory.clearCache()
                            viewModel.showToast("Hosting sunucusu başarıyla değiştirildi!")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray, contentColor = Color.White),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Sunucu Adresini Güncelle")
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = RedWarning),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Oturumu Kapat", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
