package com.example.artsphere.ui.artworks.gallery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artsphere.data.model.Artwork
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}