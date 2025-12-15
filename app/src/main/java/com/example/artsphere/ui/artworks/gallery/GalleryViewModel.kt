package com.example.artsphere.ui.artworks.gallery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artsphere.data.model.Artwork
import com.example.artsphere.data.repository.ArtworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class GalleryUiState(
    val artworks: List<Artwork> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GalleryViewModel : ViewModel() {

    // 1. Initialize the shared Repository
    private val repository = ArtworkRepository()

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState

    // 2. Get current user ID via repository helper
    private val currentUserId = repository.getCurrentUserId()

    init {
        loadAllArtworks()
    }

    fun loadAllArtworks() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // 3. Call Repository to get raw list
                val allArtworks = repository.getAllArtworks()

                // Filter logic remains in ViewModel (Presentation Logic)
                // Filter: Show artwork if NOT hidden, OR if it's hidden but belongs to me
                val filteredList = allArtworks.filter { artwork ->
                    !artwork.isHidden || artwork.userId == currentUserId
                }

                _uiState.value = _uiState.value.copy(
                    artworks = filteredList,
                    isLoading = false
                )

                Log.d("GALLERY_VM", "Loaded ${filteredList.size} artworks")

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
                // 4. Delegate to Repository
                repository.toggleSoldStatus(artworkId, currentStatus)

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
                // 5. Delegate to Repository
                repository.toggleHiddenStatus(artworkId, currentStatus)

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
