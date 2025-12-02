package com.example.artsphere

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artsphere.Artwork
import com.example.artsphere.ArtworkMarker
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class MapUiState(
    val artworkMarkers: List<ArtworkMarker> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class MapViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    init {
        loadArtworkMarkers()
    }

    fun loadArtworkMarkers() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Fetch all artworks from Firestore
                val snapshot = db.collection("artworks")
                    .get()
                    .await()

                val markers = snapshot.documents.mapNotNull { doc ->
                    try {
                        val artwork = doc.toObject(Artwork::class.java)?.copy(id = doc.id)
                        artwork?.let {
                            // Get location from document
                            val latitude = doc.getDouble("latitude")
                            val longitude = doc.getDouble("longitude")

                            if (latitude != null && longitude != null) {
                                ArtworkMarker(
                                    id = it.id,
                                    position = LatLng(latitude, longitude),
                                    title = it.name,
                                    artist = it.contactName.ifBlank { "Unknown Artist" },
                                    imageUrl = it.imageUrl,
                                    artwork = it
                                )
                            } else {
                                null
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MAP_VM", "Error parsing artwork: ${e.message}")
                        null
                    }
                }

                _uiState.value = _uiState.value.copy(
                    artworkMarkers = markers,
                    isLoading = false
                )

                Log.d("MAP_VM", "Loaded ${markers.size} artwork markers")

            } catch (e: Exception) {
                Log.e("MAP_VM", "Error loading artwork markers", e)
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