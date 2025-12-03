package com.example.artsphere.data.model

import com.google.firebase.firestore.PropertyName

data class Message(
    @PropertyName("id") val id: String = "",
    @PropertyName("senderId") val senderId: String = "",
    @PropertyName("senderName") val senderName: String = "",
    @PropertyName("receiverId") val receiverId: String = "",
    @PropertyName("receiverName") val receiverName: String = "",
    @PropertyName("artworkId") val artworkId: String = "",
    @PropertyName("artworkName") val artworkName: String = "",
    @PropertyName("artworkImageUrl") val artworkImageUrl: String = "",
    @PropertyName("message") val message: String = "",
    @PropertyName("timestamp") val timestamp: Long = System.currentTimeMillis(),
    @PropertyName("read") val read: Boolean = false
)

data class Conversation(
    val conversationId: String = "",
    val otherUserId: String = "",
    val otherUserName: String = "",
    val artworkId: String = "",
    val artworkName: String = "",
    val artworkImageUrl: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val unreadCount: Int = 0
)