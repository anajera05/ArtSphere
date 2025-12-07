package com.example.artsphere.ui.events

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artsphere.data.model.Event
import com.example.artsphere.data.model.ParticipantData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class EventUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null
)

class EventViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val user get() = auth.currentUser
    private val db = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance("gs://artsphere-android.firebasestorage.app")
        .reference.child("event_images")

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val snapshot = db.collection("events")
                    .get()
                    .await()

                val eventList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Event::class.java)?.copy(id = doc.id)
                }.sortedByDescending { it.createdAt }

                _uiState.value = _uiState.value.copy(
                    events = eventList,
                    isLoading = false
                )

                Log.d("EVENT_VM", "Loaded ${eventList.size} events")

            } catch (e: Exception) {
                Log.e("EVENT_VM", "Error loading events", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun createEvent(
        title: String,
        description: String,
        date: String,
        time: String,
        location: String,
        latitude: Double,
        longitude: Double,
        category: String,
        maxParticipants: Int,
        imageUri: Uri?,
        onSuccess: () -> Unit
    ) {
        val userId = user?.uid ?: return
        val userName = user?.displayName ?: user?.email?.substringBefore("@") ?: "Unknown"
        val userEmail = user?.email ?: ""

        viewModelScope.launch {
            try {
                Log.d("EVENT_VM", "Creating event")
                _uiState.value = _uiState.value.copy(isCreating = true, error = null)

                val eventId = db.collection("events").document().id
                var imageUrl = ""

                if (imageUri != null) {
                    val imageRef = storageRef.child("$userId/$eventId.jpg")
                    imageRef.putFile(imageUri).await()
                    imageUrl = imageRef.downloadUrl.await().toString()
                }

                val event = Event(
                    id = eventId,
                    title = title,
                    description = description,
                    date = date,
                    time = time,
                    location = location,
                    latitude = latitude,
                    longitude = longitude,
                    organizerId = userId,
                    organizerName = userName,
                    organizerEmail = userEmail,
                    category = category,
                    maxParticipants = maxParticipants,
                    imageUrl = imageUrl,
                    createdAt = System.currentTimeMillis(),
                    participantIds = emptyList(),
                    participants = emptyList()
                )

                db.collection("events")
                    .document(eventId)
                    .set(event)
                    .await()

                Log.d("EVENT_VM", "Event created successfully")
                loadEvents()
                _uiState.value = _uiState.value.copy(isCreating = false)
                onSuccess()

            } catch (e: Exception) {
                Log.e("EVENT_VM", "Event creation failed", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isCreating = false
                )
            }
        }
    }

    fun joinEvent(eventId: String, onSuccess: () -> Unit = {}) {
        val userId = user?.uid ?: return
        val userName = user?.displayName ?: user?.email?.substringBefore("@") ?: "User"
        val userEmail = user?.email ?: ""

        viewModelScope.launch {
            try {
                // Create participant data with user info
                val participantData = hashMapOf(
                    "userId" to userId,
                    "name" to userName,
                    "email" to userEmail
                )

                // Update both participantIds and participants arrays
                db.collection("events")
                    .document(eventId)
                    .update(
                        mapOf(
                            "participantIds" to FieldValue.arrayUnion(userId),
                            "participants" to FieldValue.arrayUnion(participantData)
                        )
                    )
                    .await()

                Log.d("EVENT_VM", "User $userName joined event: $eventId")
                loadEvents()
                onSuccess()

            } catch (e: Exception) {
                Log.e("EVENT_VM", "Error joining event", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun leaveEvent(eventId: String, onSuccess: () -> Unit = {}) {
        val userId = user?.uid ?: return

        viewModelScope.launch {
            try {
                // First, get the event to find the participant data
                val eventDoc = db.collection("events")
                    .document(eventId)
                    .get()
                    .await()

                val event = eventDoc.toObject(Event::class.java)
                val participantToRemove = event?.participants?.find { it.userId == userId }

                // Build update map
                val updates = mutableMapOf<String, Any>(
                    "participantIds" to FieldValue.arrayRemove(userId)
                )

                // Remove participant data if found
                if (participantToRemove != null) {
                    val participantMap = hashMapOf(
                        "userId" to participantToRemove.userId,
                        "name" to participantToRemove.name,
                        "email" to participantToRemove.email
                    )
                    updates["participants"] = FieldValue.arrayRemove(participantMap)
                }

                db.collection("events")
                    .document(eventId)
                    .update(updates)
                    .await()

                Log.d("EVENT_VM", "User left event: $eventId")
                loadEvents()
                onSuccess()

            } catch (e: Exception) {
                Log.e("EVENT_VM", "Error leaving event", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteEvent(eventId: String, onSuccess: () -> Unit = {}) {
        val userId = user?.uid ?: return

        viewModelScope.launch {
            try {
                db.collection("events")
                    .document(eventId)
                    .delete()
                    .await()

                try {
                    storageRef.child("$userId/$eventId.jpg").delete().await()
                } catch (e: Exception) {
                    Log.w("EVENT_VM", "No image to delete")
                }

                Log.d("EVENT_VM", "Event deleted: $eventId")
                loadEvents()
                onSuccess()

            } catch (e: Exception) {
                Log.e("EVENT_VM", "Error deleting event", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}