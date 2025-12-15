package com.example.artsphere.data.repository

import android.net.Uri
import com.example.artsphere.data.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Repository for managing event data operations with Firebase.
 *
 * This class handles the direct interactions with Firestore and Firebase Storage
 * for event-related features, separating data logic from the ViewModel.
 */
class EventRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    // Make sure this matches your specific bucket URL
    private val storageRef = FirebaseStorage.getInstance("gs://artsphere-android.firebasestorage.app")
        .reference.child("event_images")

    val currentUser get() = auth.currentUser

    /**
     * Loads all events from Firestore.
     *
     * Fetches all events from the "events" collection in Firestore,
     * converts them to Event objects, and sorts them by creation date (newest first).
     */
    suspend fun getAllEvents(): List<Event> {
        val snapshot = db.collection("events")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Event::class.java)?.copy(id = doc.id)
        }.sortedByDescending { it.createdAt }
    }

    /**
     * Creates a new event in Firestore with optional image upload to Firebase Storage.
     *
     * 1. Uploads the event image to Firebase Storage (if provided)
     * 2. Creates an Event object with all provided data
     * 3. Saves the event to Firestore
     */
    suspend fun createEvent(
        userId: String,
        userName: String,
        userEmail: String,
        title: String,
        description: String,
        date: String,
        time: String,
        location: String,
        latitude: Double,
        longitude: Double,
        category: String,
        maxParticipants: Int,
        imageUri: Uri?
    ) {
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
    }

    /**
     * Adds the current user as a participant to the specified event.
     *
     * Updates the event document in Firestore by adding the current user's ID
     * to the participantIds array and their detailed information to the participants array.
     */
    suspend fun joinEvent(eventId: String, userId: String, userName: String, userEmail: String) {
        val participantData = hashMapOf(
            "userId" to userId,
            "name" to userName,
            "email" to userEmail
        )

        db.collection("events")
            .document(eventId)
            .update(
                mapOf(
                    "participantIds" to FieldValue.arrayUnion(userId),
                    "participants" to FieldValue.arrayUnion(participantData)
                )
            )
            .await()
    }

    /**
     * Removes the current user from the specified event's participant list.
     */
    suspend fun leaveEvent(eventId: String, userId: String) {
        // First, get the event to find the participant data
        val eventDoc = db.collection("events")
            .document(eventId)
            .get()
            .await()

        val event = eventDoc.toObject(Event::class.java)
        val participantToRemove = event?.participants?.find { it.userId == userId }

        val updates = mutableMapOf<String, Any>(
            "participantIds" to FieldValue.arrayRemove(userId)
        )

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
    }

    /**
     * Deletes an event from Firestore and removes its associated image from Storage.
     */
    suspend fun deleteEvent(eventId: String, userId: String) {
        // Delete event from Firestore
        db.collection("events")
            .document(eventId)
            .delete()
            .await()

        // Best effort image deletion
        try {
            storageRef.child("$userId/$eventId.jpg").delete().await()
        } catch (e: Exception) {
            // Ignore if image doesn't exist
        }
    }
}
