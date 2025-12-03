package com.example.artsphere

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileUiState(
    val photoUrl: String? = null,
    val isUploading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val user get() = auth.currentUser

    private val storageRef =
        FirebaseStorage.getInstance("gs://artsphere-android.firebasestorage.app")
            .reference.child("profile_photos")

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun refreshPhotoFromFirebase() {
        user?.reload()?.addOnSuccessListener {
            val url = user?.photoUrl?.toString()
            _uiState.value = _uiState.value.copy(photoUrl = url)
        }
    }

    fun uploadProfilePhoto(uri: Uri) {
        val currentUser = user ?: return

        viewModelScope.launch {
            try {
                Log.d("PROFILE_UPLOAD", "Upload started")

                _uiState.value = _uiState.value.copy(isUploading = true)

                val ref = storageRef.child("${currentUser.uid}/profile.jpg")

                ref.putFile(uri).await()

                val downloadUrl = ref.downloadUrl.await().toString()

                val updates = UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(downloadUrl))
                    .build()

                currentUser.updateProfile(updates).await()

                currentUser.reload().await()

                Log.d("PROFILE_UPLOAD", "NEW PHOTO URL: ${currentUser.photoUrl}")

                _uiState.value = _uiState.value.copy(
                    photoUrl = currentUser.photoUrl?.toString(),
                    isUploading = false
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