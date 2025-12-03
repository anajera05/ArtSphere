package com.example.artsphere.ui.inbox

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artsphere.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)

class ChatViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val user get() = auth.currentUser
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    fun loadMessages(otherUserId: String, artworkId: String) {
        val userId = user?.uid ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

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

                val allMessages = (sentMessages.documents + receivedMessages.documents)
                    .mapNotNull { it.toObject(Message::class.java)?.copy(id = it.id) }
                    .sortedBy { it.timestamp }

                _uiState.value = _uiState.value.copy(
                    messages = allMessages,
                    isLoading = false
                )

                receivedMessages.documents.forEach { doc ->
                    if (doc.getBoolean("read") == false) {
                        doc.reference.update("read", true)
                    }
                }

            } catch (e: Exception) {
                Log.e("CHAT_VM", "Error loading messages", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun sendMessage(
        receiverId: String,
        receiverName: String,
        artworkId: String,
        artworkName: String,
        artworkImageUrl: String,
        messageText: String,
        onSuccess: () -> Unit
    ) {
        val userId = user?.uid ?: return
        val senderName = user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")
            ?: "User"

        if (messageText.isBlank()) return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSending = true)

                val messageId = db.collection("messages").document().id

                val message = Message(
                    id = messageId,
                    senderId = userId,
                    senderName = senderName,
                    receiverId = receiverId,
                    receiverName = receiverName,
                    artworkId = artworkId,
                    artworkName = artworkName,
                    artworkImageUrl = artworkImageUrl,
                    message = messageText,
                    timestamp = System.currentTimeMillis(),
                    read = false
                )

                db.collection("messages")
                    .document(messageId)
                    .set(message)
                    .await()

                _uiState.value = _uiState.value.copy(isSending = false)

                loadMessages(receiverId, artworkId)

                onSuccess()

            } catch (e: Exception) {
                Log.e("CHAT_VM", "Error sending message", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isSending = false
                )
            }
        }
    }
}