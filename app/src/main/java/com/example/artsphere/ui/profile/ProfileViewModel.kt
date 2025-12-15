package com.example.artsphere.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artsphere.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val photoUrl: String? = null,
    val isUploading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {

    // Initialize Repository
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    // Load initial state
    init {
        refreshPhotoFromFirebase()
    }

    // Load latest photo from Firebase
    fun refreshPhotoFromFirebase() {
        val user = userRepository.currentUser
        // We can use a coroutine here to make it cleaner,
        // though your original code used a callback (addOnSuccessListener).
        // Since we are moving to repo pattern, coroutines are preferred.
        viewModelScope.launch {
            try {
                userRepository.reloadUser()
                _uiState.value = _uiState.value.copy(
                    photoUrl = userRepository.currentUser?.photoUrl?.toString()
                )
            } catch (e: Exception) {
                // Handle silent reload failure if needed
            }
        }
    }

    fun uploadProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            try {
                Log.d("PROFILE_UPLOAD", "Upload started")
                _uiState.value = _uiState.value.copy(isUploading = true)

                // Delegate logic to repository
                val newPhotoUrl = userRepository.uploadProfilePhoto(uri)

                Log.d("PROFILE_UPLOAD", "NEW PHOTO URL: $newPhotoUrl")

                // Update UI state
                _uiState.value = _uiState.value.copy(
                    photoUrl = newPhotoUrl,
                    isUploading = false,
                    error = null
                )

            } catch (e: Exception) {
                Log.e("PROFILE_UPLOAD", "Upload failed", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isUploading = false
                )
            }
        }
    }
}
