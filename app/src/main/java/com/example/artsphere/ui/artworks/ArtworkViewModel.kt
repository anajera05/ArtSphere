package com.example.artsphere.ui.artworks

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artsphere.data.model.Artwork
import com.example.artsphere.data.repository.ArtworkRepository // Import the repo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ArtworkUiState(
    val artworks: List<Artwork> = emptyList(),
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null
)

class ArtworkViewModel : ViewModel() {

    // 1. Initialize Repository
    private val repository = ArtworkRepository()

    private val _uiState = MutableStateFlow(ArtworkUiState())
    val uiState: StateFlow<ArtworkUiState> = _uiState
    val artworkCount: Int
        get() = _uiState.value.artworks.size

    init {
        loadArtworks()
    }

    fun loadArtworks() {
        val userId = repository.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // 2. Use Repository
                val artworkList = repository.getArtworksForUser(userId)

                _uiState.value = _uiState.value.copy(
                    artworks = artworkList,
                    isLoading = false
                )
                Log.d("ARTWORK_VM", "Loaded ${artworkList.size} artworks")

            } catch (e: Exception) {
                Log.e("ARTWORK_VM", "Error loading artworks", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun uploadArtwork(
        imageUri: Uri,
        name: String,
        category: String,
        description: String,
        price: String,
        contactEmail: String,
        contactName: String,
        latitude: Double? = null,
        longitude: Double? = null,
        onSuccess: () -> Unit
    ) {
        val userId = repository.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                Log.d("ARTWORK_VM", "Starting artwork upload")
                _uiState.value = _uiState.value.copy(isUploading = true, error = null)

                // Create a temporary object (ID and URL set by Repo)
                val tempArtwork = Artwork(
                    id = "",
                    imageUrl = "",
                    name = name,
                    category = category,
                    description = description,
                    price = price,
                    contactEmail = contactEmail,
                    contactName = contactName,
                    createdAt = System.currentTimeMillis()
                )

                // 3. Use Repository
                repository.uploadArtwork(
                    userId = userId,
                    imageUri = imageUri,
                    artwork = tempArtwork,
                    latitude = latitude,
                    longitude = longitude
                )

                Log.d("ARTWORK_VM", "Artwork uploaded successfully")
                loadArtworks()
                _uiState.value = _uiState.value.copy(isUploading = false)
                onSuccess()

            } catch (e: Exception) {
                Log.e("ARTWORK_VM", "Upload failed", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isUploading = false
                )
            }
        }
    }

    fun updateArtwork(
        artworkId: String,
        imageUri: Uri?,
        name: String,
        category: String,
        description: String,
        price: String,
        contactEmail: String,
        contactName: String,
        latitude: Double? = null,
        longitude: Double? = null,
        currentImageUrl: String,
        onSuccess: () -> Unit
    ) {
        val userId = repository.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                Log.d("ARTWORK_VM", "Starting artwork update")
                _uiState.value = _uiState.value.copy(isUploading = true, error = null)

                val updatedFields = mapOf(
                    "name" to name,
                    "category" to category,
                    "description" to description,
                    "price" to price,
                    "contactEmail" to contactEmail,
                    "contactName" to contactName
                )

                // 4. Use Repository
                repository.updateArtwork(
                    userId = userId,
                    artworkId = artworkId,
                    imageUri = imageUri,
                    currentImageUrl = currentImageUrl,
                    updatedFields = updatedFields,
                    latitude = latitude,
                    longitude = longitude
                )

                Log.d("ARTWORK_VM", "Artwork updated successfully")
                loadArtworks()
                _uiState.value = _uiState.value.copy(isUploading = false)
                onSuccess()

            } catch (e: Exception) {
                Log.e("ARTWORK_VM", "Update failed", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isUploading = false
                )
            }
        }
    }

    fun deleteArtwork(artworkId: String) {
        val userId = repository.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                // 5. Use Repository
                repository.deleteArtwork(userId, artworkId)
                loadArtworks()
            } catch (e: Exception) {
                Log.e("ARTWORK_VM", "Error deleting artwork", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
