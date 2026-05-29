package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseResponse(
    val success: Boolean,
    val error: String? = null,
    val userId: Int? = null,
    val username: String? = null,
    val avatar: String? = null,
    val statusText: String? = null,
    val messageId: Int? = null,
    val roomId: Int? = null,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class UserProfile(
    val userId: Int,
    val username: String,
    val avatar: String,
    val statusText: String,
    val friendStatus: String // "none", "sent_pending", "received_pending", "friend"
)

@JsonClass(generateAdapter = true)
data class ProfileResponse(
    val success: Boolean,
    val error: String? = null,
    val profile: UserProfile? = null
)

@JsonClass(generateAdapter = true)
data class Room(
    val id: Int,
    val name: String,
    val ownerId: Int,
    val ownerName: String,
    val ownerAvatar: String,
    val videoUrl: String,
    val videoTitle: String,
    val isPlaying: Boolean,
    val playbackTime: Float,
    val participantCount: Int
)

@JsonClass(generateAdapter = true)
data class RoomsResponse(
    val success: Boolean,
    val error: String? = null,
    val rooms: List<Room>? = null
)

@JsonClass(generateAdapter = true)
data class RoomParticipant(
    val userId: Int,
    val username: String,
    val avatar: String,
    val role: String, // "owner", "moderator", "member"
    val isMuted: Boolean
)

@JsonClass(generateAdapter = true)
data class RoomMessage(
    val id: Int,
    val roomId: Int,
    val userId: Int,
    val senderName: String,
    val senderAvatar: String,
    val message: String,
    val isSystem: Boolean,
    val timestamp: Long,
    val isDeleted: Boolean? = null,
    val replyToId: Int? = null,
    val replyToName: String? = null,
    val replyToMsg: String? = null
)

@JsonClass(generateAdapter = true)
data class SyncState(
    val roomId: Int,
    val videoUrl: String,
    val videoTitle: String,
    val isPlaying: Boolean,
    val playbackTime: Float,
    val lastSyncTime: Long,
    val ownerId: Int,
    val myRole: String,
    val myMuteStatus: Boolean,
    val participants: List<RoomParticipant>,
    val newMessages: List<RoomMessage>
)

@JsonClass(generateAdapter = true)
data class RoomSyncResponse(
    val success: Boolean,
    val error: String? = null,
    val kicked: Boolean? = null,
    val sync: SyncState? = null
)

@JsonClass(generateAdapter = true)
data class Friend(
    val userId: Int,
    val username: String,
    val avatar: String,
    val statusText: String
)

@JsonClass(generateAdapter = true)
data class FriendsResponse(
    val success: Boolean,
    val error: String? = null,
    val friends: List<Friend>? = null,
    val incomingRequests: List<Friend>? = null,
    val outgoingRequests: List<Friend>? = null
)

@JsonClass(generateAdapter = true)
data class DMConversation(
    val userId: Int,
    val username: String,
    val avatar: String,
    val statusText: String,
    val lastMessage: String,
    val unreadCount: Int,
    val lastMessageTimestamp: Long
)

@JsonClass(generateAdapter = true)
data class DMConversationsResponse(
    val success: Boolean,
    val error: String? = null,
    val conversations: List<DMConversation>? = null
)

@JsonClass(generateAdapter = true)
data class DMMessage(
    val id: Int,
    val senderId: Int,
    val receiverId: Int,
    val senderName: String,
    val senderAvatar: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean
)

@JsonClass(generateAdapter = true)
data class DMMessagesResponse(
    val success: Boolean,
    val error: String? = null,
    val messages: List<DMMessage>? = null
)

@JsonClass(generateAdapter = true)
data class DMNotification(
    val id: Int,
    val senderId: Int,
    val senderName: String,
    val senderAvatar: String,
    val message: String,
    val timestamp: Long
)

@JsonClass(generateAdapter = true)
data class PollNotificationsResponse(
    val success: Boolean,
    val error: String? = null,
    val newDMs: List<DMNotification>? = null
)
