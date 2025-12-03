package com.example.artsphere.ui.profile.savedArtworks

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

data class SavedArtworkUiState(
    val savedArtworks: List<Artwork> = emptyList(),
    val savedArtworkIds: Set<String> = emptySet(),  // For quick lookup
    val isLoading: Boolean = false,
    val error: String? = null
)

class SavedArtworkViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val user get() = auth.currentUser
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(SavedArtworkUiState())
    val uiState: StateFlow<SavedArtworkUiState> = _uiState

    init {
        loadSavedArtworks()
    }

    fun loadSavedArtworks() {
        val userId = user?.uid ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Get the user's saved artwork IDs
                val savedDoc = db.collection("savedArtworks")
                    .document(userId)
                    .get()
                    .await()

                val savedIds = (savedDoc.get("artworkIds") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.toSet() ?: emptySet()

                Log.d("SAVED_VM", "Found ${savedIds.size} saved artwork IDs")

                // Fetch the actual artwork objects
                val artworks = if (savedIds.isNotEmpty()) {
                    // Firestore whereIn has a limit of 10 items, so we need to batch
                    val artworksList = mutableListOf<Artwork>()

                    savedIds.chunked(10).forEach { chunk ->
                        val snapshot = db.collection("artworks")
                            .whereIn("__name__", chunk)
                            .get()
                            .await()

                        snapshot.documents.mapNotNullTo(artworksList) { doc ->
                            doc.toObject(Artwork::class.java)?.copy(id = doc.id)
                        }
                    }

                    artworksList.sortedByDescending { it.createdAt }
                } else {
                    emptyList()
                }

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
        val userId = user?.uid ?: return

        viewModelScope.launch {
            try {
                val currentSavedIds = _uiState.value.savedArtworkIds.toMutableSet()
                val isCurrentlySaved = currentSavedIds.contains(artworkId)

                if (isCurrentlySaved) {
                    // Remove from saved
                    currentSavedIds.remove(artworkId)
                    Log.d("SAVED_VM", "Removing artwork $artworkId")
                } else {
                    // Add to saved
                    currentSavedIds.add(artworkId)
                    Log.d("SAVED_VM", "Saving artwork $artworkId")
                }

                // Update UI immediately for responsiveness
                _uiState.value = _uiState.value.copy(
                    savedArtworkIds = currentSavedIds
                )

                // Update Firestore
                db.collection("savedArtworks")
                    .document(userId)
                    .set(mapOf("artworkIds" to currentSavedIds.toList()))
                    .await()

                Log.d("SAVED_VM", "Successfully updated Firestore. Total saved: ${currentSavedIds.size}")

                // Reload full data to update the saved artworks list
                loadSavedArtworks()

            } catch (e: Exception) {
                Log.e("SAVED_VM", "Error toggling save: ${e.message}", e)
                _uiState.value = _uiState.value.copy(error = "Failed to save: ${e.message}")
                // Reload to restore correct state
                loadSavedArtworks()
            }
        }
    }

    fun isArtworkSaved(artworkId: String): Boolean {
        return _uiState.value.savedArtworkIds.contains(artworkId)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}