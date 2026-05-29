package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MainActivity
import com.example.data.api.RaveApiFactory
import com.example.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object RoomsList : Screen()
    data class RoomView(val roomId: Int) : Screen()
    object Friends : Screen()
    object DmsList : Screen()
    data class DmChat(val partnerId: Int, val partnerUsername: String, val partnerAvatar: String) : Screen()
    object Profile : Screen()
}

class RaveViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // SESSIONS
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userId = MutableStateFlow<Int?>(null)
    val userId: StateFlow<Int?> = _userId.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _userAvatar = MutableStateFlow("avatar1")
    val userAvatar: StateFlow<String> = _userAvatar.asStateFlow()

    private val _statusText = MutableStateFlow("Raving!")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    // NAVIGATION
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val navigationHistory = mutableListOf<Screen>()

    // DATA HOPS
    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _roomSyncState = MutableStateFlow<SyncState?>(null)
    val roomSyncState: StateFlow<SyncState?> = _roomSyncState.asStateFlow()

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    private val _incomingRequests = MutableStateFlow<List<Friend>>(emptyList())
    val incomingRequests: StateFlow<List<Friend>> = _incomingRequests.asStateFlow()

    private val _outgoingRequests = MutableStateFlow<List<Friend>>(emptyList())
    val outgoingRequests: StateFlow<List<Friend>> = _outgoingRequests.asStateFlow()

    private val _conversations = MutableStateFlow<List<DMConversation>>(emptyList())
    val conversations: StateFlow<List<DMConversation>> = _conversations.asStateFlow()

    private val _dmMessages = MutableStateFlow<List<DMMessage>>(emptyList())
    val dmMessages: StateFlow<List<DMMessage>> = _dmMessages.asStateFlow()

    // PLAYER RECEPTACLES
    var currentPlaybackTime = 0.0f
    var isPlaying = false

    // NOTIFICATIONS & POLLERS
    private var lastCheckedDmId = 0
    private var roomSyncJob: Job? = null
    private var mainPollerJob: Job? = null

    // TOASTS / ERROR CHANNELS
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    init {
        // Load session if exists
        loadSession()
        startGeneralPoller()
    }

    fun navigateTo(screen: Screen, clearHistory: Boolean = false) {
        if (clearHistory) {
            navigationHistory.clear()
        } else {
            navigationHistory.add(_currentScreen.value)
        }
        _currentScreen.value = screen

        // Sync lifecycle management
        if (screen is Screen.RoomView) {
            startRoomSync(screen.roomId)
        } else {
            stopRoomSync()
        }
    }

    fun navigateBack() {
        if (navigationHistory.isNotEmpty()) {
            val prev = navigationHistory.removeAt(navigationHistory.size - 1)
            _currentScreen.value = prev

            // Manage room jobs
            if (prev is Screen.RoomView) {
                startRoomSync(prev.roomId)
            } else {
                stopRoomSync()
            }
        } else {
            // Default fallbacks
            if (_isLoggedIn.value) {
                _currentScreen.value = Screen.RoomsList
            } else {
                _currentScreen.value = Screen.Login
            }
            stopRoomSync()
        }
    }

    private fun loadSession() {
        val prefs = context.getSharedPreferences("rave_session", Context.MODE_PRIVATE)
        val uid = prefs.getInt("user_id", -1)
        if (uid != -1) {
            _userId.value = uid
            _username.value = prefs.getString("username", "") ?: ""
            _userAvatar.value = prefs.getString("avatar", "avatar1") ?: "avatar1"
            _statusText.value = prefs.getString("status_text", "Raving!") ?: "Raving!"
            _isLoggedIn.value = true
            _currentScreen.value = Screen.RoomsList
        }
    }

    private fun saveSession(uid: Int, name: String, avatar: String, status: String) {
        val prefs = context.getSharedPreferences("rave_session", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("user_id", uid)
            .putString("username", name)
            .putString("avatar", avatar)
            .putString("status_text", status)
            .apply()

        _userId.value = uid
        _username.value = name
        _userAvatar.value = avatar
        _statusText.value = status
        _isLoggedIn.value = true
    }

    fun logout() {
        val prefs = context.getSharedPreferences("rave_session", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
        _userId.value = null
        _username.value = ""
        _userAvatar.value = "avatar1"
        _currentScreen.value = Screen.Login
        stopRoomSync()
    }

    fun showToast(msg: String) {
        viewModelScope.launch {
            _toastMessage.emit(msg)
        }
    }

    // AUTH ACTIONS
    fun login(u: String, p: String) {
        viewModelScope.launch {
            try {
                val service = RaveApiFactory.getService(context)
                val resp = service.login(body = mapOf("username" to u, "password" to p))
                if (resp.success && resp.userId != null) {
                    saveSession(resp.userId, resp.username ?: u, resp.avatar ?: "avatar1", resp.statusText ?: "Raving!")
                    navigateTo(Screen.RoomsList, clearHistory = true)
                    showToast("Başarıyla giriş yapıldı: ${resp.username}")
                } else {
                    showToast(resp.error ?: "Giriş başarısız.")
                }
            } catch (e: Exception) {
                showToast("Sunucu hatası: ${e.localizedMessage}")
                Log.e("RaveCo", "Login failed", e)
            }
        }
    }

    fun register(u: String, p: String, avatar: String) {
        viewModelScope.launch {
            try {
                val service = RaveApiFactory.getService(context)
                val resp = service.register(body = mapOf("username" to u, "password" to p, "avatar" to avatar))
                if (resp.success && resp.userId != null) {
                    saveSession(resp.userId, resp.username ?: u, resp.avatar ?: avatar, "Raving!")
                    navigateTo(Screen.RoomsList, clearHistory = true)
                    showToast("Profil oluşturuldu ve giriş yapıldı!")
                } else {
                    showToast(resp.error ?: "Kayıt başarısız.")
                }
            } catch (e: Exception) {
                showToast("Sunucu hatası: ${e.localizedMessage}")
                Log.e("RaveCo", "Registration failed", e)
            }
        }
    }

    fun updateProfile(avatar: String, status: String, newPass: String?) {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            try {
                val service = RaveApiFactory.getService(context)
                val body = mutableMapOf(
                    "userId" to uid.toString(),
                    "avatar" to avatar,
                    "statusText" to status
                )
                if (!newPass.isNullOrBlank()) {
                    body["password"] = newPass
                }
                val resp = service.updateProfile(body = body)
                if (resp.success) {
                    showToast("Profil başarıyla güncellendi.")
                    _userAvatar.value = avatar
                    _statusText.value = status
                    val prefs = context.getSharedPreferences("rave_session", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("avatar", avatar)
                        .putString("status_text", status)
                        .apply()
                } else {
                    showToast(resp.error ?: "Profil güncellenemedi.")
                }
            } catch (e: Exception) {
                showToast("Hata: ${e.localizedMessage}")
            }
        }
    }

    // ROOM ACTIONS
    fun loadRooms() {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            try {
                val service = RaveApiFactory.getService(context)
                val resp = service.getRooms(body = mapOf("userId" to uid))
                if (resp.success && resp.rooms != null) {
                    _rooms.value = resp.rooms
                }
            } catch (e: Exception) {
                Log.e("RaveCo", "Failed to load rooms", e)
            }
        }
    }

    fun createRoom(roomName: String, url: String, title: String) {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            try {
                val service = RaveApiFactory.getService(context)
                val resp = service.createRoom(
                    body = mapOf(
                        "userId" to uid.toString(),
                        "name" to roomName,
                        "videoUrl" to url,
                        "videoTitle" to title
                    )
                )
                if (resp.success && resp.roomId != null) {
                    try {
                        service.sendRoomChat(
                            body = mapOf(
                                "userId" to uid,
                                "roomId" to resp.roomId,
                                "message" to "system_join:${_username.value}",
                                "isSystem" to true
                            )
                        )
                    } catch (ex: Exception) {
                        Log.e("RaveCo", "Failed to send system_join message on create", ex)
                    }
                    navigateTo(Screen.RoomView(resp.roomId))
                    showToast("Oda başarıyla kuruldu!")
                } else {
                    showToast(resp.error ?: "Oda kurulamadı.")
                }
            } catch (e: Exception) {
                showToast("Hata: ${e.localizedMessage}")
            }
        }
    }

    fun joinRoom(roomId: Int) {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            try {
                val service = RaveApiFactory.getService(context)
                val resp = service.joinRoom(body = mapOf("userId" to uid, "roomId" to roomId))
                if (resp.success) {
                    try {
                        service.sendRoomChat(
                            body = mapOf(
                                "userId" to uid,
                                "roomId" to roomId,
                                "message" to "system_join:${_username.value}",
                                "isSystem" to true
                            )
                        )
                    } catch (ex: Exception) {
                        Log.e("RaveCo", "Failed to send system_join message", ex)
                    }
                    navigateTo(Screen.RoomView(roomId))
                } else {
                    showToast(resp.error ?: "Odaya katılım başarısız oldu.")
                }
            } catch (e: Exception) {
                showToast("Hata: ${e.localizedMessage}")
            }
        }
    }

    fun leaveRoom(roomId: Int) {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            try {
                val service = RaveApiFactory.getService(context)
                try {
                    service.sendRoomChat(
                        body = mapOf(
                            "userId" to uid,
                            "roomId" to roomId,
                            "message" to "system_left:${_username.value}",
                            "isSystem" to true
                        )
                    )
                } catch (ex: Exception) {
                    Log.e("RaveCo", "Failed to send system_left message", ex)
                }
                service.leaveRoom(body = mapOf("userId" to uid, "roomId" to roomId))
                _roomSyncState.value = null
                navigateTo(Screen.RoomsList)
            } catch (e: Exception) {
                Log.e("RaveCo", "Failed to leave room", e)
                navigateTo(Screen.RoomsList)
            }
        }
    }

    // REALTIME ROOM SYNCHRONIZATION
    private fun startRoomSync(roomId: Int) {
        stopRoomSync()
        var lastMsgId = 0
        roomSyncJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val uid = _userId.value
                if (uid == null) {
                    stopRoomSync()
                    break
                }
                try {
                    val service = RaveApiFactory.getService(context)
                    
                    val payload = mutableMapOf<String, Any>(
                        "userId" to uid,
                        "roomId" to roomId,
                        "lastMessageId" to lastMsgId
                    )

                    // If I am owner/moderator, I send my layout states as authority to back-office sync
                    val stateObj = _roomSyncState.value
                    if (stateObj != null && (stateObj.myRole == "owner" || stateObj.myRole == "moderator")) {
                        payload["owner_sync"] = true
                        payload["playbackTime"] = currentPlaybackTime
                        payload["isPlaying"] = if (isPlaying) 1 else 0
                    } else {
                        // User sync
                        payload["playbackTime"] = currentPlaybackTime
                        payload["isPlaying"] = if (isPlaying) 1 else 0
                    }

                    val resp = service.roomSync(body = payload)
                    if (resp.success && resp.sync != null) {
                        val newSync = resp.sync
                        val prevSync = _roomSyncState.value
                        
                        // Trigger visual alert preview for fullscreen overlay if anyone sends messages
                        if (newSync.newMessages.isNotEmpty() && prevSync != null && lastMsgId > 0) {
                            val fresh = newSync.newMessages.filter { it.id > lastMsgId && it.userId != uid && !it.isSystem }
                            if (fresh.isNotEmpty()) {
                                val newest = fresh.last()
                                withContext(Dispatchers.Main) {
                                    MainActivity.showFullscreenChatOverview(newest.senderName, newest.message)
                                }
                            }
                        }

                        // Adjust last known message ID
                        if (newSync.newMessages.isNotEmpty()) {
                            lastMsgId = newSync.newMessages.maxOf { it.id }
                        }

                        _roomSyncState.value = newSync
                    } else if (resp.kicked == true) {
                        // Handle Kick
                        withContext(Dispatchers.Main) {
                            showToast(resp.error ?: "Odayla bağlantınız kesildi veya atıldınız.")
                            _roomSyncState.value = null
                            navigateTo(Screen.RoomsList)
                        }
                        break
                    }
                } catch (e: Exception) {
                    Log.e("RaveCo", "Room Sync Polling Failed", e)
                }
                delay(1500) // Lower lookup latency for smoother watching sync
            }
        }
    }

    private fun stopRoomSync() {
        roomSyncJob?.cancel()
        roomSyncJob = null
    }

    // Owner pushes explicit URL switch or playback triggers
    fun updateRoomVideoSource(roomId: Int, url: String, title: String) {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            try {
                val service = RaveApiFactory.getService(context)
                val payload = mapOf<String, Any>(
                    "userId" to uid,
                    "roomId" to roomId,
                    "lastMessageId" to 0,
                    "owner_sync" to true,
                    "videoUrl" to url,
                    "videoTitle" to title,
                    "playbackTime" to 0.0f,
                    "isPlaying" to 1
                )
                service.roomSync(body = payload)
            } catch (e: Exception) {
                showToast("Video güncellenemedi: ${e.localizedMessage}")
            }
        }
    }

    fun sendRoomMessage(
        roomId: Int,
        msg: String,
        replyToId: Int? = null,
        replyToName: String? = null,
        replyToMsg: String? = null
    ) {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            try {
                val service = RaveApiFactory.getService(context)
                val payload = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "roomId" to roomId,
                    "message" to msg
                )
                if (replyToId != null) {
                    payload["replyToId"] = replyToId
                }
                if (replyToName != null) {
                    payload["replyToName"] = replyToName
                }
                if (replyToMsg != null) {
                    payload["replyToMsg"] = replyToMsg
                }
                val resp = service.sendRoomChat(body = payload)
                if (!resp.success) {
                    showToast(resp.error ?: "Mesaj gönderilemedi.")
                }
            } catch (e: Exception) {
                showToast("Bağlantı hatası.")
            }
        }
    }

    fun deleteRoomMessage(roomId: Int, messageId: Int) {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            try {
                val service = RaveApiFactory.getService(context)
                val resp = service.deleteRoomMessage(
                    body = mapOf(
                        "userId" to uid,
                        "roomId" to roomId,
                        "messageId" to messageId
                    )
                )
                if (resp.success) {
                    showToast("Mesaj silindi.")
                } else {
                    showToast(resp.error ?: "Mesaj silinemedi.")
                }
            } catch (e: Exception) {
                showToast("Bağlantı hatası.")
            }
        }
    }

    fun moderateParticipant(roomId: Int, targetUserId: Int, command: String) {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            try {
                val service = RaveApiFactory.getService(context)
                val resp = service.moderateRoom(
                    body = mapOf(
                        "userId" to uid,
                        "roomId" to roomId,
                        "targetId" to targetUserId,
                        "command" to command
                    )
                )
                if (resp.success) {
                    showToast(resp.message ?: "İşlem başarılı.")
                } else {
                    showToast(resp.error ?: "İşlem başarısız.")
                }
            } catch (e: Exception) {
                showToast("Hata: ${e.localizedMessage}")
            }
        }
    }

    // FRIEND ACTIONS
    fun loadFriends() {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            try {
                val service = RaveApiFactory.getService(context)
                val resp = service.getFriends(body = mapOf("userId" to uid))
                if (resp.success) {
                    _friends.value = resp.friends ?: emptyList()
                    _incomingRequests.value = resp.incomingRequests ?: emptyList()
                    _outgoingRequests.value = resp.outgoingRequests ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("RaveCo", "Failed to load friends", e)
            }
        }
    }

    fun handleFriendAction(targetName: String, action: String) {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            try {
                val service = RaveApiFactory.getService(context)
                val resp = service.friendAction(
                    body = mapOf(
                        "userId" to uid.toString(),
                        "targetUsername" to targetName,
                        "action" to action
                    )
                )
                if (resp.success) {
                    showToast(resp.message ?: "İşlem tamamlandı.")
                    loadFriends() // Refresh Lists
                } else {
                    showToast(resp.error ?: "İşlem başarısız.")
                }
            } catch (e: Exception) {
                showToast("Hata: ${e.localizedMessage}")
            }
        }
    }

    // DIRECT MESSAGES (DM)
    fun loadConversations() {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            try {
                val service = RaveApiFactory.getService(context)
                val resp = service.getDms(body = mapOf("userId" to uid))
                if (resp.success && resp.conversations != null) {
                    _conversations.value = resp.conversations
                }
            } catch (e: Exception) {
                Log.e("RaveCo", "Failed to load DMs", e)
            }
        }
    }

    fun loadDmMessages(partnerId: Int) {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            try {
                val service = RaveApiFactory.getService(context)
                val resp = service.getDmMessages(body = mapOf("userId" to uid, "partnerId" to partnerId))
                if (resp.success && resp.messages != null) {
                    _dmMessages.value = resp.messages
                }
            } catch (e: Exception) {
                Log.e("RaveCo", "Failed to load Dm Messages", e)
            }
        }
    }

    fun sendDmMessage(partnerId: Int, msg: String) {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            try {
                val service = RaveApiFactory.getService(context)
                val resp = service.sendDm(body = mapOf("userId" to uid, "receiverId" to partnerId, "message" to msg))
                if (resp.success) {
                    loadDmMessages(partnerId)
                } else {
                    showToast(resp.error ?: "Sohbet mesajı iletilemedi.")
                }
            } catch (e: Exception) {
                showToast("Hata: ${e.localizedMessage}")
            }
        }
    }

    // BACKGROUND/FOREGROUND NOTIFICATION POLLING
    private fun startGeneralPoller() {
        mainPollerJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val uid = _userId.value
                if (uid != null) {
                    try {
                        val service = RaveApiFactory.getService(context)
                        val resp = service.pollNotifications(body = mapOf("userId" to uid, "lastCheckedId" to lastCheckedDmId))
                        
                        if (resp.success && !resp.newDMs.isNullOrEmpty()) {
                            for (dm in resp.newDMs) {
                                // Trigger Native System Bar Notification
                                withContext(Dispatchers.Main) {
                                    MainActivity.triggerSystemNotification(
                                        context,
                                        dm.senderName,
                                        dm.message
                                    )
                                }
                                if (dm.id > lastCheckedDmId) {
                                    lastCheckedDmId = dm.id
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("RaveCo", "Notification Polling Stalled", e)
                    }
                }
                delay(6000) // Poll for notifications background/foreground safely every 6 seconds
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopRoomSync()
        mainPollerJob?.cancel()
    }
}
