package com.example.artsphere.data.repository

import com.example.artsphere.data.model.Conversation
import com.example.artsphere.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class MessageRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Helper to get current user ID
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Used by ChatViewModel:
     * Fetches the full chat history between two users for a specific artwork.
     * Merges sent and received messages and marks received ones as read.
     */
    suspend fun getMessages(currentUserId: String, otherUserId: String, artworkId: String): List<Message> {
        // 1. Get messages sent by current user
        val sentSnapshot = db.collection("messages")
            .whereEqualTo("senderId", currentUserId)
            .whereEqualTo("receiverId", otherUserId)
            .whereEqualTo("artworkId", artworkId)
            .get()
            .await()

        val sentMessages = sentSnapshot.documents.mapNotNull {
            it.toObject(Message::class.java)?.copy(id = it.id)
        }

        // 2. Get messages received by current user
        val receivedSnapshot = db.collection("messages")
            .whereEqualTo("senderId", otherUserId)
            .whereEqualTo("receiverId", currentUserId)
            .whereEqualTo("artworkId", artworkId)
            .get()
            .await()

        val receivedMessages = receivedSnapshot.documents.mapNotNull {
            it.toObject(Message::class.java)?.copy(id = it.id)
        }

        // 3. Mark received messages as read
        receivedSnapshot.documents.forEach { doc ->
            if (doc.getBoolean("read") == false) {
                doc.reference.update("read", true)
            }
        }

        // 4. Combine and Sort by timestamp
        return (sentMessages + receivedMessages).sortedBy { it.timestamp }
    }

    /**
     * Used by ChatViewModel:
     * Sends a new message to Firestore.
     */
    suspend fun sendMessage(message: Message) {
        // Generate a new ID for the document
        val ref = db.collection("messages").document()
        val finalMessage = message.copy(id = ref.id)

        ref.set(finalMessage).await()
    }

    /**
     * Used by InboxViewModel:
     * Fetches all unique conversations for the current user.
     * It queries all messages involving the user, groups them by conversation ID,
     * and creates summary objects (Conversation) for the Inbox list.
     */
    suspend fun getConversations(userId: String): List<Conversation> {
        // 1. Get all messages where I am the sender
        val sent = db.collection("messages")
            .whereEqualTo("senderId", userId)
            .get()
            .await()
            .toObjects(Message::class.java)

        // 2. Get all messages where I am the receiver
        val received = db.collection("messages")
            .whereEqualTo("receiverId", userId)
            .get()
            .await()
            .toObjects(Message::class.java)

        val allMessages = sent + received

        // 3. Group by a unique "conversation key"
        // We group by ArtworkID + the Other User's ID to keep threads distinct per artwork
        val grouped = allMessages.groupBy { msg ->
            val otherId = if (msg.senderId == userId) msg.receiverId else msg.senderId
            "${otherId}_${msg.artworkId}"
        }

        // 4. Map to Conversation objects
        return grouped.map { (key, messages) ->
            val sorted = messages.sortedByDescending { it.timestamp }
            val latest = sorted.first()

            // Determine the "Other User" details from the message
            val otherUserId = if (latest.senderId == userId) latest.receiverId else latest.senderId
            val otherUserName = if (latest.senderId == userId) latest.receiverName else latest.senderName

            // Count unread messages (only those received by me)
            val unreadCount = messages.count {
                it.receiverId == userId && !it.read
            }

            Conversation(
                conversationId = key,
                otherUserId = otherUserId,
                otherUserName = otherUserName,
                artworkId = latest.artworkId,
                artworkName = latest.artworkName,
                artworkImageUrl = latest.artworkImageUrl,
                lastMessage = latest.message,
                lastMessageTime = latest.timestamp,
                unreadCount = unreadCount
            )
        }.sortedByDescending { it.lastMessageTime }
    }

    /**
     * Marks all unread messages in a specific conversation as read.
     * Expects conversationId in format: "otherUserId_artworkId"
     */
    suspend fun markConversationAsRead(currentUserId: String, conversationId: String) {
        val parts = conversationId.split("_")
        if (parts.size < 2) return

        val otherUserId = parts[0]
        val artworkId = parts[1]

        val snapshot = db.collection("messages")
            .whereEqualTo("senderId", otherUserId)
            .whereEqualTo("receiverId", currentUserId)
            .whereEqualTo("artworkId", artworkId)
            .whereEqualTo("read", false)
            .get()
            .await()

        // Batch update is more efficient, but keeping it simple for now as per your original logic
        snapshot.documents.forEach { doc ->
            doc.reference.update("read", true).await()
        }
    }

    /**
     * Deletes an entire conversation (both sent and received messages) for a specific artwork context.
     */
    suspend fun deleteConversation(currentUserId: String, conversationId: String) {
        val parts = conversationId.split("_")
        if (parts.size < 2) return

        val otherUserId = parts[0]
        val artworkId = parts[1]

        // 1. Get messages I sent
        val sentMessages = db.collection("messages")
            .whereEqualTo("senderId", currentUserId)
            .whereEqualTo("receiverId", otherUserId)
            .whereEqualTo("artworkId", artworkId)
            .get()
            .await()

        // 2. Get messages I received
        val receivedMessages = db.collection("messages")
            .whereEqualTo("senderId", otherUserId)
            .whereEqualTo("receiverId", currentUserId)
            .whereEqualTo("artworkId", artworkId)
            .get()
            .await()

        // 3. Delete them all
        val allDocs = sentMessages.documents + receivedMessages.documents
        allDocs.forEach { doc ->
            doc.reference.delete().await()
        }
    }
}

