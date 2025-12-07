package com.example.artsphere.ui.artworks.gallery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artsphere.data.model.Artwork
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class GalleryUiState(
    val artworks: List<Artwork> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GalleryViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState

    init {
        loadAllArtworks()
    }

    fun loadAllArtworks() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Fetch ALL artworks from all users
                val snapshot = db.collection("artworks")
                    .get()
                    .await()

                val artworkList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Artwork::class.java)?.copy(id = doc.id)
                }.filter { artwork ->
                    // Show artwork if:
                    // 1. It's not hidden, OR
                    // 2. It's hidden but belongs to current user
                    !artwork.isHidden || artwork.userId == currentUserId
                }.sortedByDescending { it.createdAt }

                _uiState.value = _uiState.value.copy(
                    artworks = artworkList,
                    isLoading = false
                )

                Log.d("GALLERY_VM", "Loaded ${artworkList.size} artworks from all users")

            } catch (e: Exception) {
                Log.e("GALLERY_VM", "Error loading artworks", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    // Toggle sold status
    fun toggleSoldStatus(artworkId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                db.collection("artworks")
                    .document(artworkId)
                    .update("isSold", !currentStatus)
                    .await()

                // Reload artworks to reflect changes
                loadAllArtworks()

                Log.d("GALLERY_VM", "Toggled sold status for artwork: $artworkId")
            } catch (e: Exception) {
                Log.e("GALLERY_VM", "Error toggling sold status", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    // Toggle hidden status
    fun toggleHiddenStatus(artworkId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            try {
                db.collection("artworks")
                    .document(artworkId)
                    .update("isHidden", !currentStatus)
                    .await()

                // Reload artworks to reflect changes
                loadAllArtworks()

                Log.d("GALLERY_VM", "Toggled hidden status for artwork: $artworkId")
            } catch (e: Exception) {
                Log.e("GALLERY_VM", "Error toggling hidden status", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}