package com.example.player

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.viewmodel.RaveViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.SolidColor

import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Deferred
import java.util.concurrent.TimeUnit

val IconFullscreen = ImageVector.Builder(
    name = "Fullscreen",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).path(
    fill = null,
    stroke = SolidColor(Color.White),
    strokeLineWidth = 2f
) {
    moveTo(4f, 9f)
    lineTo(4f, 4f)
    lineTo(9f, 4f)
    moveTo(20f, 9f)
    lineTo(20f, 4f)
    lineTo(15f, 4f)
    moveTo(4f, 15f)
    lineTo(4f, 20f)
    lineTo(9f, 20f)
    moveTo(20f, 15f)
    lineTo(20f, 20f)
    lineTo(15f, 20f)
}.build()

val IconFullscreenExit = ImageVector.Builder(
    name = "FullscreenExit",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).path(
    fill = null,
    stroke = SolidColor(Color.White),
    strokeLineWidth = 2f
) {
    moveTo(4f, 8f)
    lineTo(8f, 8f)
    lineTo(8f, 4f)
    moveTo(20f, 8f)
    lineTo(16f, 8f)
    lineTo(16f, 4f)
    moveTo(4f, 16f)
    lineTo(8f, 16f)
    lineTo(8f, 20f)
    moveTo(20f, 16f)
    lineTo(16f, 16f)
    lineTo(16f, 20f)
}.build()

fun extractYouTubeId(url: String): String? {
    val cleanUrl = url.trim()
    val pattern = "(?i)(?:https?:\\/\\/)?(?:www\\.)?(?:youtube\\.com\\/(?:[^\\/\\n\\s]+\\/\\S+\\/|(?:v|e(?:mbed)?)\\/|\\S*?[?&]v=)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})"
    val regex = Regex(pattern)
    return regex.find(cleanUrl)?.groupValues?.getOrNull(1)
}

@Composable
fun SyncVideoPlayer(
    viewModel: RaveViewModel,
    videoUrl: String,
    modifier: Modifier = Modifier,
    isFullscreen: Boolean = false,
    isFullscreenToggle: () -> Unit = {}
) {
    val cleanUrl = videoUrl.trim()
    val isYoutube = extractYouTubeId(cleanUrl) != null

    Box(
        modifier = modifier
            .then(if (isFullscreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth().height(230.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (cleanUrl.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Aynada Oynatılan Bir Kaynak Yok",
                    color = Color.White,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Oda sahibi yukarıdaki arama panelini veya link girişini kullanarak bir video yüklemelidir.",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    modifier = Modifier.wrapContentSize()
                )
            }
        } else if (isYoutube) {
            val videoId = extractYouTubeId(cleanUrl) ?: ""
            YouTubePlayerCompose(
                viewModel = viewModel,
                videoId = videoId,
                isFullscreen = isFullscreen,
                isFullscreenToggle = isFullscreenToggle
            )
        } else {
            ExoPlayerCompose(
                viewModel = viewModel,
                videoUrl = cleanUrl,
                isFullscreen = isFullscreen,
                isFullscreenToggle = isFullscreenToggle
            )
        }


    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayerCompose(
    viewModel: RaveViewModel,
    videoId: String,
    isFullscreen: Boolean,
    isFullscreenToggle: () -> Unit
) {
    val context = LocalContext.current
    val syncState by viewModel.roomSyncState.collectAsState()
    
    // Remember reference to WebView to invoke JavaScript functions
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val isOwner = syncState?.myRole == "owner" || syncState?.myRole == "moderator"

    // Load HTML
    val htmlContent = remember(videoId) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                body, html { margin:0; padding:0; width:100%; height:100%; background-color:black; overflow:hidden; }
                #player { width:100%; height:100%; }
            </style>
        </head>
        <body>
            <div id="player"></div>
            <script>
                var tag = document.createElement('script');
                tag.src = "https://www.youtube.com/iframe_api";
                var firstScriptTag = document.getElementsByTagName('script')[0];
                firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                var player;
                var currentPlayState = 0; // -1, 0, 1, 2, 3, 5

                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                        videoId: '$videoId',
                        playerVars: {
                            'autoplay': 1,
                            'controls': 1,
                            'rel': 0,
                            'showinfo': 0,
                            'iv_load_policy': 3,
                            'modestbranding': 1,
                            'playsinline': 1
                        },
                        events: {
                            'onReady': onPlayerReady,
                            'onStateChange': onPlayerStateChange
                        }
                    });
                }

                function onPlayerReady(event) {
                    // Start muted or playing sync
                }

                function onPlayerStateChange(event) {
                    currentPlayState = event.data;
                    AndroidNative.onStateChanged(player.getCurrentTime(), event.data == 1);
                }

                function play() { if(player && player.playVideo) player.playVideo(); }
                function pause() { if(player && player.pauseVideo) player.pauseVideo(); }
                function seek(sec) { if(player && player.seekTo) player.seekTo(sec, true); }
                function getSecs() { return player ? player.getCurrentTime() : 0.0; }
                function getIsPlaying() { return player ? (currentPlayState == 1) : false; }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    // Capture heartbeat checks
    LaunchedEffect(syncState) {
        val sync = syncState ?: return@LaunchedEffect
        val webView = webViewRef ?: return@LaunchedEffect
        
        if (!isOwner) {
            // slave sync
            val targetSec = sync.playbackTime
            val targetPlay = sync.isPlaying
            
            // Query current js player seeks
            webView.evaluateJavascript("getSecs()") { secStr ->
                val currentSecs = secStr.toFloatOrNull() ?: 0.0f
                if (abs(currentSecs - targetSec) > 3.0f) {
                    webView.evaluateJavascript("seek($targetSec)", null)
                }
            }

            webView.evaluateJavascript("getIsPlaying()") { playStr ->
                val localPlaying = playStr.toBoolean()
                if (localPlaying != targetPlay) {
                    if (targetPlay) webView.evaluateJavascript("play()", null)
                    else webView.evaluateJavascript("pause()", null)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        domStorageEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                    }
                    
                    webChromeClient = WebChromeClient()
                    
                    // Add interface to bridge play state to our ViewModel authority
                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onStateChanged(time: Float, playing: Boolean) {
                            viewModel.currentPlaybackTime = time
                            viewModel.isPlaying = playing
                        }
                    }, "AndroidNative")

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                        }
                    }

                    loadDataWithBaseURL("https://www.youtube.com", htmlContent, "text/html", "utf-8", null)
                    webViewRef = this
                }
            },
            update = { webView ->
                val previousVideoId = webView.tag as? String
                if (previousVideoId != videoId) {
                    webView.tag = videoId
                    webView.loadDataWithBaseURL("https://www.youtube.com", htmlContent, "text/html", "utf-8", null)
                }
            }
        )

        // Floating full-screen toggle overlay on top of YouTube Player WebView
        IconButton(
            onClick = isFullscreenToggle,
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.6f)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = if (isFullscreen) IconFullscreenExit else IconFullscreen,
                contentDescription = "Tam Ekran",
                tint = Color.White
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerCompose(
    viewModel: RaveViewModel,
    videoUrl: String,
    isFullscreen: Boolean,
    isFullscreenToggle: () -> Unit
) {
    val context = LocalContext.current
    val syncState by viewModel.roomSyncState.collectAsState()
    val isOwner = syncState?.myRole == "owner" || syncState?.myRole == "moderator"

    // Quality states
    var testedQualities by remember(videoUrl) { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var selectedQualityUrl by remember(videoUrl) { mutableStateOf(videoUrl) }
    var selectedQualityLabel by remember(videoUrl) { mutableStateOf("Varsayılan") }
    var showQualityMenu by remember { mutableStateOf(false) }
    var showQuickMsgPanel by remember { mutableStateOf(false) }

    LaunchedEffect(videoUrl) {
        checkUrlQuality(videoUrl) { qList ->
            testedQualities = qList
            val fallbackMatch = qList.firstOrNull { it.second == videoUrl }?.first
            selectedQualityLabel = fallbackMatch ?: "Varsayılan"
        }
    }

    val exoPlayer = remember(context) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
            }
    }

    // Dynamic stream swapper without destroying player instance
    LaunchedEffect(selectedQualityUrl) {
        val currentPos = exoPlayer.currentPosition
        val isPlayingNow = exoPlayer.isPlaying
        
        val cleanedUrl = selectedQualityUrl.trim()
        val lowercaseUrl = cleanedUrl.lowercase()
        val isHls = lowercaseUrl.contains("m3u8") || lowercaseUrl.contains("hls") || lowercaseUrl.contains(".m3u") || lowercaseUrl.contains("/chunk") || lowercaseUrl.contains("stream-resolution")
        val isDash = lowercaseUrl.contains(".mpd") || lowercaseUrl.contains("dash")
        
        val mediaItem = if (isHls) {
            MediaItem.Builder()
                .setUri(Uri.parse(cleanedUrl))
                .setMimeType("application/x-mpegURL")
                .build()
        } else if (isDash) {
            MediaItem.Builder()
                .setUri(Uri.parse(cleanedUrl))
                .setMimeType("application/dash+xml")
                .build()
        } else {
            MediaItem.fromUri(Uri.parse(cleanedUrl))
        }
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        if (currentPos > 0) {
            exoPlayer.seekTo(currentPos)
        }
        if (isPlayingNow) {
            exoPlayer.play()
        }
    }

    // Monitor Player changes to push up to viewmodel state if Owner
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isOwner) {
                    viewModel.isPlaying = isPlaying
                    viewModel.currentPlaybackTime = exoPlayer.currentPosition / 1000.0f
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (isOwner) {
                    viewModel.currentPlaybackTime = exoPlayer.currentPosition / 1000.0f
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("SyncVideoPlayer", "Playback error: ${error.message}", error)
                viewModel.showToast("Video oynatılamadı veya ağ bağlantısı yok.")
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Periodic time publisher if Owner
    LaunchedEffect(exoPlayer) {
        while (true) {
            if (isOwner && exoPlayer.isPlaying) {
                viewModel.currentPlaybackTime = exoPlayer.currentPosition / 1000.0f
                viewModel.isPlaying = true
            }
            delay(1000)
        }
    }

    // Sync listener if Member
    LaunchedEffect(syncState) {
        val sync = syncState ?: return@LaunchedEffect
        if (!isOwner) {
            // Play status sync
            if (sync.isPlaying != exoPlayer.isPlaying) {
                if (sync.isPlaying) exoPlayer.play() else exoPlayer.pause()
            }
            // Timeline seek sync
            val targetMillis = (sync.playbackTime * 1000).toLong()
            if (abs(exoPlayer.currentPosition - targetMillis) > 3000) {
                exoPlayer.seekTo(targetMillis)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                if (playerView.player != exoPlayer) {
                    playerView.player = exoPlayer
                }
            }
        )

        // Polished Floating overlay buttons for manual/forced sync, quality, and Fullscreen triggers
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (testedQualities.size > 1) {
                Box {
                    Button(
                        onClick = { showQualityMenu = !showQualityMenu },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Kalite",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(selectedQualityLabel, color = Color.White, fontSize = 11.sp)
                    }
                    
                    DropdownMenu(
                        expanded = showQualityMenu,
                        onDismissRequest = { showQualityMenu = false },
                        modifier = Modifier.background(Color(0xFF212121))
                    ) {
                        testedQualities.forEach { (qLabel, qUrl) ->
                            DropdownMenuItem(
                                text = { Text(qLabel, color = Color.White, fontSize = 13.sp) },
                                onClick = {
                                    selectedQualityUrl = qUrl
                                    selectedQualityLabel = qLabel
                                    showQualityMenu = false
                                    viewModel.showToast("$qLabel kalitesi seçildi.")
                                }
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = {
                    syncState?.let {
                        val pos = (it.playbackTime * 1000).toLong()
                        exoPlayer.seekTo(pos)
                        if (it.isPlaying) exoPlayer.play() else exoPlayer.pause()
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Yeniden Senkronize Et",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = isFullscreenToggle,
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = if (isFullscreen) IconFullscreenExit else IconFullscreen,
                    contentDescription = "Tam Ekran",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun MatchParentHeight(): androidx.compose.ui.unit.Dp {
    return 10000.dp // High threshold for layout stretches
}

fun checkUrlQuality(baseUrl: String, onFinished: (List<Pair<String, String>>) -> Unit) {
    if (baseUrl.isEmpty()) {
        onFinished(emptyList())
        return
    }
    
    // Check if the URL has a typical quality indicator segment
    // We match: 240, 360, 480, 576, 720, 1080, 1440, 2160 (or e.g. 720.m3u8, 1080p, etc.)
    var regex = "(360|480|720|1080)\\.(m3u8|mp4|mpd|m3u)".toRegex()
    var matchResult = regex.find(baseUrl)
    
    if (matchResult == null) {
        regex = "/(240|360|480|576|720|1080|1440|2160)/".toRegex()
        matchResult = regex.find(baseUrl)
    }
    if (matchResult == null) {
        regex = "\\b(240|360|480|576|720|1080|1440|2160)\\b".toRegex()
        matchResult = regex.find(baseUrl)
    }
    
    if (matchResult == null) {
        // Return original if no indicator found
        onFinished(listOf("Orijinal" to baseUrl))
        return
    }
    
    val matchedVal = matchResult.groupValues[1]
    val qualitiesToTest = listOf("360", "480", "720", "1080")
    
    CoroutineScope(Dispatchers.IO).launch {
        val client = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .build()
            
        val results = qualitiesToTest.map { q ->
            async {
                val testUrl = baseUrl.replace(matchedVal, q)
                try {
                    val request = Request.Builder()
                        .url(testUrl)
                        .head() // Try HEAD request for speed
                        .build()
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            return@async q + "p" to testUrl
                        }
                    }
                } catch (e: Exception) {
                    // Try simple GET if HEAD failed/is disabled on CDN
                    try {
                        val request = Request.Builder()
                            .url(testUrl)
                            .get()
                            .build()
                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                return@async q + "p" to testUrl
                            }
                        }
                    } catch (ex: Exception) {
                        // ignore
                    }
                }
                null
            }
        }.awaitAll().filterNotNull()
        
        withContext(Dispatchers.Main) {
            if (results.isEmpty()) {
                onFinished(listOf("Orijinal" to baseUrl))
            } else {
                onFinished(results)
            }
        }
    }
}
