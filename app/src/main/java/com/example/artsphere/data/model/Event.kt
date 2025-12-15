package com.example.artsphere.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Data class representing a participant in an event.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This class stores essential information about users who have registered for an event.
 * Firebase PropertyName annotations ensure proper serialization/deserialization with Firestore.
 *
 * @property userId Unique identifier for the user in the authentication system.
 * @property name Display name of the participant.
 * @property email Email address of the participant.
 */
// Data class to store participant information
data class ParticipantData(
    @PropertyName("userId") val userId: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("email") val email: String = ""
)

/**
 * Data class representing an art-related event in the ArtSphere application.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This class models all the information for an art event including basic details,
 * location data, organizer information, participant tracking, and capacity management.
 * It includes Firebase PropertyName annotations for Firestore integration and provides
 * computed properties for convenient access to derived values.
 *
 * @property id Unique identifier for the event in Firestore.
 * @property title Name or headline of the event.
 * @property description Detailed description of the event, its purpose, and activities.
 * @property date Date when the event takes place (format should be consistent, e.g., "YYYY-MM-DD").
 * @property time Time when the event starts (format should be consistent, e.g., "HH:MM").
 * @property location Human-readable location/address of the event venue.
 * @property latitude Geographic latitude coordinate of the event location.
 * @property longitude Geographic longitude coordinate of the event location.
 * @property organizerId User ID of the person who created/organized the event.
 * @property organizerName Display name of the event organizer.
 * @property organizerEmail Contact email of the event organizer.
 * @property category String representation of the event category (stored as enum name).
 * @property maxParticipants Maximum number of participants allowed. 0 indicates unlimited.
 * @property imageUrl URL of the event's promotional or representative image.
 * @property createdAt Timestamp (in milliseconds) when the event was created.
 * @property participantIds List of user IDs for all participants who have registered.
 * @property participants List of detailed participant information including names and emails.
 */
data class Event(
    @PropertyName("id") val id: String = "",
    @PropertyName("title") val title: String = "",
    @PropertyName("description") val description: String = "",
    @PropertyName("date") val date: String = "",
    @PropertyName("time") val time: String = "",
    @PropertyName("location") val location: String = "",
    @PropertyName("latitude") val latitude: Double = 0.0,
    @PropertyName("longitude") val longitude: Double = 0.0,
    @PropertyName("organizerId") val organizerId: String = "",
    @PropertyName("organizerName") val organizerName: String = "",
    @PropertyName("organizerEmail") val organizerEmail: String = "",
    @PropertyName("category") val category: String = EventCategory.EXHIBITION.name,
    @PropertyName("maxParticipants") val maxParticipants: Int = 0,
    @PropertyName("imageUrl") val imageUrl: String = "",
    @PropertyName("createdAt") val createdAt: Long = System.currentTimeMillis(),
    @PropertyName("participantIds") val participantIds: List<String> = emptyList(),
    @PropertyName("participants") val participants: List<ParticipantData> = emptyList()
) {
    val categoryEnum: EventCategory
        get() = try {
            EventCategory.valueOf(category)
        } catch (e: Exception) {
            EventCategory.OTHER
        }

    val participantCount: Int
        get() = participantIds.size

    val isFull: Boolean
        get() = maxParticipants > 0 && participantIds.size >= maxParticipants
}

/**
 * Enum representing different categories of art events.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This enum defines the types of events that can be created in the ArtSphere application.
 * Each category has a display name suitable for showing to users in the UI.
 *
 * @property displayName User-friendly name for the category to be shown in the UI.
 */
enum class EventCategory(val displayName: String) {
    EXHIBITION("Exhibition"),
    WORKSHOP("Workshop"),
    GALLERY_OPENING("Gallery Opening"),
    ART_FAIR("Art Fair"),
    NETWORKING("Networking Event"),
    AUCTION("Auction"),
    PERFORMANCE("Performance"),
    OTHER("Other")
}