package com.example.artsphere.data.model

import com.google.firebase.firestore.PropertyName

// Data class to store participant information
data class ParticipantData(
    @PropertyName("userId") val userId: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("email") val email: String = ""
)

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