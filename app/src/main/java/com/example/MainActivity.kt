package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.RaveViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val viewModel: RaveViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create DM Notification Channel
        createNotificationChannel(this)

        // Request Push Notification Permissions on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        // Keep observing Toast events from our ViewModel
        lifecycleScope.launch {
            viewModel.toastMessage.collect { msg ->
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val activeRoomMsg by fullscreenChatSender.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Black
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    ) {
                        // Core Screen Rotations
                        when (val scr = currentScreen) {
                            is Screen.Login -> LoginScreen(viewModel)
                            is Screen.Register -> RegisterScreen(viewModel)
                            is Screen.RoomsList -> RoomsListScreen(viewModel)
                            is Screen.RoomView -> RoomViewScreen(viewModel, scr.roomId)
                            is Screen.Friends -> FriendsScreen(viewModel)
                            is Screen.DmsList -> DmsListScreen(viewModel)
                            is Screen.DmChat -> DmChatScreen(
                                viewModel,
                                scr.partnerId,
                                scr.partnerUsername,
                                scr.partnerAvatar
                            )
                            is Screen.Profile -> ProfileScreen(viewModel)
                        }

                        // Fullscreen transparent floating message preview hud
                        AnimatedVisibility(
                            visible = activeRoomMsg != null,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 80.dp)
                                .padding(horizontal = 24.dp)
                        ) {
                            activeRoomMsg?.let { msgPair ->
                                val sender = msgPair.first
                                val text = msgPair.second
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "$sender:",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = text,
                                            color = Color.LightGray,
                                            fontSize = 12.sp,
                                            maxLines = 1
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

    // PICTURE IN PICTURE TRIGGER ON EXIT WITH ACTIVE SHOWS
    override fun onUserLeaveHint() {
        val current = viewModel.currentScreen.value
        if (current is Screen.RoomView) {
            // Trigger Picture-In-Picture Mode if device supports it
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val aspectRatio = Rational(16, 9)
                    val params = PictureInPictureParams.Builder()
                        .setAspectRatio(aspectRatio)
                        .build()
                    enterPictureInPictureMode(params)
                } catch (e: Exception) {
                    // Suppress or fallback
                }
            }
        }
    }

    companion object {
        // Fullscreen dynamic preview states
        private val fullscreenChatSender = MutableStateFlow<Pair<String, String>?>(null)

        fun showFullscreenChatOverview(sender: String, message: String) {
            fullscreenChatSender.value = Pair(sender, message)
            // Auto hide after 3 seconds
            kotlinx.coroutines.GlobalScope.launch(Dispatchers.Main) {
                delay(3000)
                if (fullscreenChatSender.value?.first == sender && fullscreenChatSender.value?.second == message) {
                    fullscreenChatSender.value = null
                }
            }
        }

        // Native push notifier builder
        fun triggerSystemNotification(context: Context, senderName: String, messageContent: String) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "rave_dms"

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.stat_notify_chat) // Standard Android chat icon
                .setContentTitle(senderName)
                .setContentText(messageContent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        }

        private fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "rave_dms"
                val channelName = "Rave Co Özel Mesajlar"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(channelId, channelName, importance).apply {
                    description = "Rave Co uygulamasında gelen birebir mesajların bildirimleri"
                }
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
