package com.example.artsphere.ui.events

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artsphere.data.model.Event
import com.example.artsphere.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Data class representing the UI state for event-related screens.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This class holds the state information for screens that display or manage events,
 * including the list of events, loading states, and error messages.
 *
 * @property events List of Event objects to be displayed in the UI.
 * @property isLoading Indicates whether events are currently being loaded from the database.
 * @property isCreating Indicates whether a new event is currently being created.
 * @property error Optional error message string if an operation failed. Null if no error.
 */
data class EventUiState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for managing event data and operations.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This ViewModel handles all event-related operations including loading events from the repository,
 * creating new events, managing event participants (joining/leaving), and deleting events.
 * It exposes UI state through a StateFlow that UI components can observe.
 */
class EventViewModel : ViewModel() {

    // Initialize Repository
    private val repository = EventRepository()

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState

    // Load events when viewModel is created
    init {
        loadEvents()
    }

    /**
     * Loads all events from Firestore and updates the UI state.
     *
     * KDoc generated with AI; reviewed and modified for accuracy.
     *
     * This method fetches all events via the repository, sorts them by creation date
     * (newest first), and updates the UI state. Any errors during loading are caught
     * and stored in the error state.
     */
    fun loadEvents() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Call Repository
                val eventList = repository.getAllEvents()

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

    /**
     * Creates a new event in Firestore with optional image upload to Firebase Storage.
     *
     * KDoc generated with AI; reviewed and modified for accuracy.
     *
     * This method handles the complete event creation workflow:
     * 1. Uploads the event image to Firebase Storage (if provided)
     * 2. Creates an Event object with all provided data
     * 3. Saves the event to Firestore
     * 4. Reloads the event list to reflect the new event
     * 5. Calls the onSuccess callback
     *
     * @param title The event's title/name.
     * @param description Detailed description of the event.
     * @param date The date when the event occurs (as a string).
     * @param time The time when the event starts (as a string).
     * @param location Human-readable location/address of the event.
     * @param latitude Geographic latitude of the event location.
     * @param longitude Geographic longitude of the event location.
     * @param category The event category (as enum name string).
     * @param maxParticipants Maximum number of participants allowed (0 for unlimited).
     * @param imageUri Optional URI of the event image to upload.
     * @param onSuccess Callback invoked when event creation succeeds.
     */
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
        val user = repository.currentUser
        val userId = user?.uid ?: return
        val userName = user?.displayName ?: user?.email?.substringBefore("@") ?: "Unknown"
        val userEmail = user?.email ?: ""

        viewModelScope.launch {
            try {
                Log.d("EVENT_VM", "Creating event")
                _uiState.value = _uiState.value.copy(isCreating = true, error = null)

                // Call Repository
                repository.createEvent(
                    userId = userId,
                    userName = userName,
                    userEmail = userEmail,
                    title = title,
                    description = description,
                    date = date,
                    time = time,
                    location = location,
                    latitude = latitude,
                    longitude = longitude,
                    category = category,
                    maxParticipants = maxParticipants,
                    imageUri = imageUri
                )

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

    /**
     * Adds the current user as a participant to the specified event.
     *
     * KDoc generated with AI; reviewed and modified for accuracy.
     *
     * This method updates the event document in Firestore by adding the current user's ID
     * to the participantIds array and their detailed information to the participants array.
     *
     * @param eventId The ID of the event to join.
     * @param onSuccess Optional callback invoked when joining succeeds.
     */
    fun joinEvent(eventId: String, onSuccess: () -> Unit = {}) {
        val user = repository.currentUser
        val userId = user?.uid ?: return
        val userName = user?.displayName ?: user?.email?.substringBefore("@") ?: "User"
        val userEmail = user?.email ?: ""

        viewModelScope.launch {
            try {
                // Call Repository
                repository.joinEvent(eventId, userId, userName, userEmail)

                Log.d("EVENT_VM", "User $userName joined event: $eventId")
                loadEvents()
                onSuccess()

            } catch (e: Exception) {
                Log.e("EVENT_VM", "Error joining event", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Removes the current user from the specified event's participant list.
     *
     * KDoc generated with AI; reviewed and modified for accuracy.
     *
     * This method first retrieves the event to find the exact participant data for the
     * current user, then removes both the user ID from participantIds and their detailed
     * information from the participants array.
     *
     * @param eventId The ID of the event to leave.
     * @param onSuccess Optional callback invoked when leaving succeeds.
     */
    fun leaveEvent(eventId: String, onSuccess: () -> Unit = {}) {
        val userId = repository.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // Call Repository
                repository.leaveEvent(eventId, userId)

                Log.d("EVENT_VM", "User left event: $eventId")
                loadEvents()
                onSuccess()

            } catch (e: Exception) {
                Log.e("EVENT_VM", "Error leaving event", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Deletes an event from Firestore and removes its associated image from Storage.
     *
     * KDoc generated with AI; reviewed and modified for accuracy.
     *
     * This method performs two operations:
     * 1. Deletes the event document from Firestore
     * 2. Attempts to delete the event's image from Firebase Storage (if it exists)
     *
     * The image deletion is handled gracefully within the repository.
     * After deletion, the event list is reloaded to reflect the change.
     *
     * @param eventId The ID of the event to delete.
     * @param onSuccess Optional callback invoked when deletion succeeds.
     */
    fun deleteEvent(eventId: String, onSuccess: () -> Unit = {}) {
        val userId = repository.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // Call Repository
                repository.deleteEvent(eventId, userId)

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
