package com.example.artsphere.ui.inbox

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artsphere.data.model.Conversation
import com.example.artsphere.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class InboxUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class InboxViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val user get() = auth.currentUser
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(InboxUiState())
    val uiState: StateFlow<InboxUiState> = _uiState

    init {
        loadConversations()
    }

    fun loadConversations() {
        val userId = user?.uid ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                Log.d("INBOX_VM", "Loading conversations for user: $userId")

                val snapshot = db.collection("messages")
                    .whereEqualTo("senderId", userId)
                    .get()
                    .await()

                val receivedSnapshot = db.collection("messages")
                    .whereEqualTo("receiverId", userId)
                    .get()
                    .await()

                val allMessages = (snapshot.documents + receivedSnapshot.documents)
                    .mapNotNull { it.toObject(Message::class.java)?.copy(id = it.id) }

                val conversationMap = mutableMapOf<String, MutableList<Message>>()

                allMessages.forEach { message ->
                    val otherUserId = if (message.senderId == userId) {
                        message.receiverId
                    } else {
                        message.senderId
                    }
                    val key = "${otherUserId}_${message.artworkId}"
                    conversationMap.getOrPut(key) { mutableListOf() }.add(message)
                }

                val conversations = conversationMap.map { (key, messages) ->
                    val lastMessage = messages.maxByOrNull { it.timestamp }!!
                    val otherUserId = if (lastMessage.senderId == userId) {
                        lastMessage.receiverId
                    } else {
                        lastMessage.senderId
                    }
                    val otherUserName = if (lastMessage.senderId == userId) {
                        lastMessage.receiverName
                    } else {
                        lastMessage.senderName
                    }

                    val unreadCount = messages.count {
                        it.receiverId == userId && !it.read
                    }

                    Conversation(
                        conversationId = key,
                        otherUserId = otherUserId,
                        otherUserName = otherUserName,
                        artworkId = lastMessage.artworkId,
                        artworkName = lastMessage.artworkName,
                        artworkImageUrl = lastMessage.artworkImageUrl,
                        lastMessage = lastMessage.message,
                        lastMessageTime = lastMessage.timestamp,
                        unreadCount = unreadCount
                    )
                }.sortedByDescending { it.lastMessageTime }

                _uiState.value = _uiState.value.copy(
                    conversations = conversations,
                    isLoading = false
                )

                Log.d("INBOX_VM", "Loaded ${conversations.size} conversations")

            } catch (e: Exception) {
                Log.e("INBOX_VM", "Error loading conversations", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun markConversationAsRead(conversationId: String) {
        val userId = user?.uid ?: return

        viewModelScope.launch {
            try {
                val parts = conversationId.split("_")
                val otherUserId = parts[0]
                val artworkId = parts[1]

                val snapshot = db.collection("messages")
                    .whereEqualTo("senderId", otherUserId)
                    .whereEqualTo("receiverId", userId)
                    .whereEqualTo("artworkId", artworkId)
                    .whereEqualTo("read", false)
                    .get()
                    .await()

                snapshot.documents.forEach { doc ->
                    doc.reference.update("read", true).await()
                }

                loadConversations()

            } catch (e: Exception) {
                Log.e("INBOX_VM", "Error marking as read", e)
            }
        }
    }

    fun deleteConversation(conversationId: String) {
        val userId = user?.uid ?: return

        viewModelScope.launch {
            try {
                Log.d("INBOX_VM", "Deleting conversation: $conversationId")

                val parts = conversationId.split("_")
                val otherUserId = parts[0]
                val artworkId = parts[1]

                val sentMessages = db.collection("messages")
                    .whereEqualTo("senderId", userId)
                    .whereEqualTo("receiverId", otherUserId)
                    .whereEqualTo("artworkId", artworkId)
                    .get()
                    .await()

                val receivedMessages = db.collection("messages")
                    .whereEqualTo("senderId", otherUserId)
                    .whereEqualTo("receiverId", userId)
                    .whereEqualTo("artworkId", artworkId)
                    .get()
                    .await()

                val allDocs = sentMessages.documents + receivedMessages.documents

                allDocs.forEach { doc ->
                    doc.reference.delete().await()
                }

                Log.d("INBOX_VM", "Deleted ${allDocs.size} messages")

                loadConversations()

            } catch (e: Exception) {
                Log.e("INBOX_VM", "Error deleting conversation", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}