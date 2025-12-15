package com.example.artsphere.ui.saved

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artsphere.data.model.Artwork
import com.example.artsphere.data.repository.ArtworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SavedArtworkUiState(
    val savedArtworks: List<Artwork> = emptyList(),
    val savedArtworkIds: Set<String> = emptySet(),  // For quick lookup
    val isLoading: Boolean = false,
    val error: String? = null
)

class SavedArtworkViewModel : ViewModel() {

    // 1. Initialize Repository
    private val repository = ArtworkRepository()
    private val currentUserId = repository.getCurrentUserId()

    private val _uiState = MutableStateFlow(SavedArtworkUiState())
    val uiState: StateFlow<SavedArtworkUiState> = _uiState

    init {
        loadSavedArtworks()
    }

    fun loadSavedArtworks() {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // 2. Get the IDs from Repository
                val savedIds = repository.getSavedArtworkIds(userId)
                Log.d("SAVED_VM", "Found ${savedIds.size} saved artwork IDs")

                // 3. Fetch the objects from Repository
                val artworks = repository.getArtworksByIds(savedIds)

                _uiState.value = _uiState.value.copy(
                    savedArtworks = artworks,
                    savedArtworkIds = savedIds,
                    isLoading = false
                )

                Log.d("SAVED_VM", "Loaded ${artworks.size} saved artworks")

            } catch (e: Exception) {
                Log.e("SAVED_VM", "Error loading saved artworks", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false,
                    savedArtworkIds = emptySet()
                )
            }
        }
    }

    fun toggleSaveArtwork(artworkId: String) {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            try {
                val currentSavedIds = _uiState.value.savedArtworkIds.toMutableSet()
                val isCurrentlySaved = currentSavedIds.contains(artworkId)

                if (isCurrentlySaved) {
                    currentSavedIds.remove(artworkId)
                    Log.d("SAVED_VM", "Removing artwork $artworkId")
                } else {
                    currentSavedIds.add(artworkId)
                    Log.d("SAVED_VM", "Saving artwork $artworkId")
                }

                // Update UI immediately for responsiveness
                _uiState.value = _uiState.value.copy(
                    savedArtworkIds = currentSavedIds
                )

                // 4. Update Firestore via Repository
                repository.updateSavedArtworkIds(userId, currentSavedIds)

                Log.d("SAVED_VM", "Successfully updated Firestore. Total saved: ${currentSavedIds.size}")

                // Reload full data to update the saved artworks list
                loadSavedArtworks()

            } catch (e: Exception) {
                Log.e("SAVED_VM", "Error toggling save: ${e.message}", e)
                _uiState.value = _uiState.value.copy(error = "Failed to save: ${e.message}")
                // Reload to restore correct state on error
                loadSavedArtworks()
            }
        }
    }

    // Overload for consistency if you start passing full objects elsewhere,
    // but keeps your main logic intact
    fun toggleSave(artwork: Artwork) {
        toggleSaveArtwork(artwork.id)
    }

    fun isArtworkSaved(artworkId: String): Boolean {
        return _uiState.value.savedArtworkIds.contains(artworkId)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
