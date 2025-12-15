package com.example.artsphere.ui.inbox

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artsphere.data.model.Conversation
import com.example.artsphere.data.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class InboxUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class InboxViewModel : ViewModel() {

    // Initialize Repository
    private val repository = MessageRepository()

    private val _uiState = MutableStateFlow(InboxUiState())
    val uiState: StateFlow<InboxUiState> = _uiState

    init {
        loadConversations()
    }

    fun loadConversations() {
        val userId = repository.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                Log.d("INBOX_VM", "Loading conversations for user: $userId")

                // Call Repository
                val conversationList = repository.getConversations(userId)

                _uiState.value = _uiState.value.copy(
                    conversations = conversationList,
                    isLoading = false
                )
                Log.d("INBOX_VM", "Loaded ${conversationList.size} conversations")

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
        val userId = repository.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                // Call Repository
                repository.markConversationAsRead(userId, conversationId)

                // Refresh list to update unread counts
                loadConversations()

            } catch (e: Exception) {
                Log.e("INBOX_VM", "Error marking as read", e)
            }
        }
    }

    fun deleteConversation(conversationId: String) {
        val userId = repository.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                Log.d("INBOX_VM", "Deleting conversation: $conversationId")

                // Call Repository
                repository.deleteConversation(userId, conversationId)

                Log.d("INBOX_VM", "Conversation deleted")
                loadConversations()

            } catch (e: Exception) {
                Log.e("INBOX_VM", "Error deleting conversation", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // Helper to clear errors
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
