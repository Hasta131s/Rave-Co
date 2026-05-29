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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Rave Co",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { viewModel.navigateTo(Screen.DmsList) }) {
                        Icon(imageVector = Icons.Default.ChatBubble, contentDescription = "Mesajlar", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.Friends) }) {
                        Icon(imageVector = Icons.Default.People, contentDescription = "Arkadaşlar", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.navigateTo(Screen.Profile) }) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profil", tint = Color.White)
                    }
                }
            }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                            border = BorderStroke(1.dp, Color.DarkGray)
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
                                            modifier = Modifier.padding(horizontal = 4.dp)
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
                                                    Text(
                                                        text = m.message,
                                                        color = Color.White,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Message Composer
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.Black),
                                border = BorderStroke(1.dp, Color.DarkGray)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .navigationBarsPadding()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = chatMessage,
                                        onValueChange = { chatMessage = it },
                                        placeholder = { Text(if (sync.myMuteStatus) "Sessiz modundasınız..." else "Mesaj yazın...", color = Color.DarkGray) },
                                        singleLine = true,
                                        enabled = !sync.myMuteStatus,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                        keyboardActions = KeyboardActions(onSend = {
                                            if (chatMessage.isNotEmpty()) {
                                                viewModel.sendRoomMessage(roomId, chatMessage)
                                                chatMessage = ""
                                            }
                                        }),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.White,
                                            unfocusedBorderColor = Color.DarkGray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            if (chatMessage.isNotEmpty()) {
                                                viewModel.sendRoomMessage(roomId, chatMessage)
                                                chatMessage = ""
                                            }
                                        },
                                        enabled = !sync.myMuteStatus && chatMessage.isNotEmpty(),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            contentColor = Color.Black,
                                            containerColor = Color.White,
                                            disabledContainerColor = Color.DarkGray
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.Send, contentDescription = "Gönder")
                                    }
                                }
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri Git", tint = Color.White)
                }
                Text(
                    text = "Sosyal & Arkadaşlar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri Git", tint = Color.White)
                }
                Text(
                    text = "Sohbetler ve DM",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
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
    LaunchedEffect(Unit) {
        viewModel.loadDmMessages(partnerId)
    }

    LaunchedEffect(dmMessages.size) {
        if (dmMessages.isNotEmpty()) {
            listState.animateScrollToItem(dmMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri Git", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                AvatarBadge(avatarKey = partnerAvatar, size = 36)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = partnerUsername,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
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

            // Input Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                border = BorderStroke(1.dp, Color.DarkGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Özel mesaj yazın...", color = Color.DarkGray) },
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
                    IconButton(
                        onClick = {
                            if (messageText.isNotEmpty()) {
                                viewModel.sendDmMessage(partnerId, messageText)
                                messageText = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Gönder")
                    }
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri Git", tint = Color.White)
                }
                Text(
                    text = "Profil Yönetimi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
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
