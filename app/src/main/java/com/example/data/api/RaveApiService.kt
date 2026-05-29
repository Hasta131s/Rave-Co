package com.example.data.api

import android.content.Context
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface RaveApiService {

    @POST("api.php")
    suspend fun register(
        @Query("action") action: String = "register",
        @Body body: Map<String, String>
    ): BaseResponse

    @POST("api.php")
    suspend fun login(
        @Query("action") action: String = "login",
        @Body body: Map<String, String>
    ): BaseResponse

    @POST("api.php")
    suspend fun getProfile(
        @Query("action") action: String = "get_profile",
        @Body body: Map<String, Int>
    ): ProfileResponse

    @POST("api.php")
    suspend fun updateProfile(
        @Query("action") action: String = "update_profile",
        @Body body: Map<String, String>
    ): BaseResponse

    @POST("api.php")
    suspend fun getRooms(
        @Query("action") action: String = "rooms",
        @Body body: Map<String, Int>
    ): RoomsResponse

    @POST("api.php")
    suspend fun createRoom(
        @Query("action") action: String = "create_room",
        @Body body: Map<String, String>
    ): BaseResponse

    @POST("api.php")
    suspend fun joinRoom(
        @Query("action") action: String = "join_room",
        @Body body: Map<String, Int>
    ): BaseResponse

    @POST("api.php")
    suspend fun leaveRoom(
        @Query("action") action: String = "leave_room",
        @Body body: Map<String, Int>
    ): BaseResponse

    @POST("api.php")
    suspend fun roomSync(
        @Query("action") action: String = "room_sync",
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): RoomSyncResponse

    @POST("api.php")
    suspend fun sendRoomChat(
        @Query("action") action: String = "room_chat",
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): BaseResponse

    @POST("api.php")
    suspend fun deleteRoomMessage(
        @Query("action") action: String = "delete_room_message",
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): BaseResponse

    @POST("api.php")
    suspend fun deleteDmMessage(
        @Query("action") action: String = "delete_dm_message",
        @Body body: Map<String, Int>
    ): BaseResponse

    @POST("api.php")
    suspend fun reactMessage(
        @Query("action") action: String = "react_message",
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): BaseResponse

    @POST("api.php")
    suspend fun setTypingStatus(
        @Query("action") action: String = "set_typing_status",
        @Body body: Map<String, Int>
    ): BaseResponse

    @POST("api.php")
    suspend fun toggleChatLock(
        @Query("action") action: String = "toggle_chat_lock",
        @Body body: Map<String, Int>
    ): BaseResponse

    @POST("api.php")
    suspend fun moderateRoom(
        @Query("action") action: String = "room_moderate",
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): BaseResponse

    @POST("api.php")
    suspend fun getFriends(
        @Query("action") action: String = "get_friends",
        @Body body: Map<String, Int>
    ): FriendsResponse

    @POST("api.php")
    suspend fun friendAction(
        @Query("action") action: String = "friend_action",
        @Body body: Map<String, String>
    ): BaseResponse

    @POST("api.php")
    suspend fun getDms(
        @Query("action") action: String = "get_dms",
        @Body body: Map<String, Int>
    ): DMConversationsResponse

    @POST("api.php")
    suspend fun getDmMessages(
        @Query("action") action: String = "get_dm_messages",
        @Body body: Map<String, Int>
    ): DMMessagesResponse

    @POST("api.php")
    suspend fun sendDm(
        @Query("action") action: String = "send_dm",
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): BaseResponse

    @POST("api.php")
    suspend fun pollNotifications(
        @Query("action") action: String = "poll_notifications",
        @Body body: Map<String, Int>
    ): PollNotificationsResponse

    @POST("api.php")
    suspend fun getSharedContents(
        @Query("action") action: String = "get_shared_contents",
        @Body body: Map<String, Int> = emptyMap()
    ): SharedContentsResponse

    @POST("api.php")
    suspend fun addSharedContent(
        @Query("action") action: String = "add_shared_content",
        @Body body: Map<String, String>
    ): BaseResponse

    @POST("api.php")
    suspend fun searchYouTube(
        @Query("action") action: String = "search_youtube",
        @Body body: Map<String, String>
    ): SearchYouTubeResponse
}

object RaveApiFactory {

    private const val PREFS_NAME = "rave_settings"
    private const val KEY_API_URL = "api_url"
    const val DEFAULT_API_URL = "https://bosforlab.online/"

    fun getApiUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var url = prefs.getString(KEY_API_URL, DEFAULT_API_URL) ?: DEFAULT_API_URL
        var cleaned = url.trim()
        if (cleaned.endsWith("api.php")) {
            cleaned = cleaned.substring(0, cleaned.length - "api.php".length)
        } else if (cleaned.contains("/api.php")) {
            cleaned = cleaned.replace("/api.php", "")
        }
        if (!cleaned.endsWith("/")) {
            cleaned += "/"
        }
        return cleaned
    }

    fun setApiUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var cleaned = url.trim()
        if (cleaned.endsWith("api.php")) {
            cleaned = cleaned.substring(0, cleaned.length - "api.php".length)
        } else if (cleaned.contains("/api.php")) {
            cleaned = cleaned.replace("/api.php", "")
        }
        val formatted = if (cleaned.endsWith("/")) cleaned else "$cleaned/"
        prefs.edit().putString(KEY_API_URL, formatted).apply()
    }

    private var currentRetrofit: Retrofit? = null
    private var currentService: RaveApiService? = null
    private var currentCachedUrl: String? = null

    fun getService(context: Context): RaveApiService {
        val url = getApiUrl(context)
        if (currentService != null && currentCachedUrl == url) {
            return currentService!!
        }

        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logger)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val service = retrofit.create(RaveApiService::class.java)
        currentRetrofit = retrofit
        currentService = service
        currentCachedUrl = url

        return service
    }

    fun clearCache() {
        currentService = null
        currentCachedUrl = null
    }
}
