package com.example.artsphere.ui.artworks

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artsphere.data.model.Artwork
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ArtworkUiState(
    val artworks: List<Artwork> = emptyList(),
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null
)

class ArtworkViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val user get() = auth.currentUser
    private val db = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance("gs://artsphere-android.firebasestorage.app")
        .reference.child("artwork_images")

    private val _uiState = MutableStateFlow(ArtworkUiState())
    val uiState: StateFlow<ArtworkUiState> = _uiState
    val artworkCount: Int
        get() = _uiState.value.artworks.size


    init {
        loadArtworks()
    }

    fun loadArtworks() {
        val userId = user?.uid ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val snapshot = db.collection("artworks")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val artworkList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Artwork::class.java)?.copy(id = doc.id)
                }.sortedByDescending { it.createdAt }  // Sort in code instead

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
        val userId = user?.uid ?: return

        viewModelScope.launch {
            try {
                Log.d("ARTWORK_VM", "Starting artwork upload")
                _uiState.value = _uiState.value.copy(isUploading = true, error = null)

                // Upload image to Firebase Storage
                val artworkId = db.collection("artworks").document().id
                val imageRef = storageRef.child("$userId/$artworkId.jpg")

                imageRef.putFile(imageUri).await()
                val downloadUrl = imageRef.downloadUrl.await().toString()

                // Create artwork object
                val artwork = Artwork(
                    id = artworkId,
                    imageUrl = downloadUrl,
                    name = name,
                    category = category,
                    description = description,
                    price = price,
                    contactEmail = contactEmail,
                    contactName = contactName,
                    createdAt = System.currentTimeMillis()
                )

                val artworkData = artwork.toMap().toMutableMap()
                artworkData["userId"] = userId

                // Add location if provided
                if (latitude != null && longitude != null) {
                    artworkData["latitude"] = latitude
                    artworkData["longitude"] = longitude
                    Log.d("ARTWORK_VM", "✅ Adding location to Firestore: Lat=$latitude, Lng=$longitude")
                } else {
                    Log.d("ARTWORK_VM", "⚠️ No location data to save")
                }

                // Save to Firestore
                db.collection("artworks")
                    .document(artworkId)
                    .set(artworkData)
                    .await()

                Log.d("ARTWORK_VM", "Artwork uploaded successfully")

                // Reload artworks
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

    fun deleteArtwork(artworkId: String) {
        val userId = user?.uid ?: return

        viewModelScope.launch {
            try {
                // Delete from Firestore
                db.collection("artworks")
                    .document(artworkId)
                    .delete()
                    .await()

                // Delete image from Storage
                storageRef.child("$userId/$artworkId.jpg").delete().await()

                // Reload artworks
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

// Extension function to convert Artwork to Map for Firestore
private fun Artwork.toMap(): Map<String, Any> {
    return mapOf(
        "imageUrl" to imageUrl,
        "name" to name,
        "category" to category,
        "description" to description,
        "price" to price,
        "contactEmail" to contactEmail,
        "contactName" to contactName,
        "createdAt" to createdAt
    )
}