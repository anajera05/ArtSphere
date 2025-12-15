package com.example.artsphere.ui.inbox

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artsphere.data.model.Message
import com.example.artsphere.data.repository.MessageRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)

class ChatViewModel : ViewModel() {

    // Initialize Repository
    private val repository = MessageRepository()

    // We keep a local Auth instance just to grab the User's Display Name for the message object
    // The actual data operations go through the repository
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    fun loadMessages(otherUserId: String, artworkId: String) {
        val userId = repository.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Call Repository to get merged and sorted messages
                val allMessages = repository.getMessages(userId, otherUserId, artworkId)

                _uiState.value = _uiState.value.copy(
                    messages = allMessages,
                    isLoading = false
                )

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
        val userId = repository.getCurrentUserId() ?: return
        val user = auth.currentUser

        // Logic to determine display name (Presentation Logic)
        val senderName = user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")
            ?: "User"

        if (messageText.isBlank()) return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSending = true)

                val message = Message(
                    id = "", // Repository will generate the ID
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

                // Call Repository
                repository.sendMessage(message)

                _uiState.value = _uiState.value.copy(isSending = false)

                // Reload to show the new message
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
