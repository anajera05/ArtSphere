package com.example.artsphere.data.repository

import android.net.Uri
import androidx.compose.animation.core.copy
import com.example.artsphere.data.model.Artwork
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ArtworkRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance("gs://artsphere-android.firebasestorage.app")
        .reference.child("artwork_images")

    // Helper to get current user ID
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // ============================================================================================
    // SECTION 1: ARTWORK VIEW MODEL (CRUD for Artist's Own Work)
    // ============================================================================================

    /**
     * Fetch artworks created by a specific user (for Profile/Manage screens).
     */
    suspend fun getArtworksForUser(userId: String): List<Artwork> {
        val snapshot = db.collection("artworks")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Artwork::class.java)?.copy(id = doc.id)
        }.sortedByDescending { it.createdAt }
    }

    /**
     * Uploads an image, gets the URL, and creates the Firestore document.
     */
    suspend fun uploadArtwork(
        userId: String,
        imageUri: Uri,
        artwork: Artwork,
        latitude: Double?,
        longitude: Double?
    ) {
        // 1. Generate ID
        val artworkId = db.collection("artworks").document().id

        // 2. Upload Image
        val imageRef = storageRef.child("$userId/$artworkId.jpg")
        imageRef.putFile(imageUri).await()
        val downloadUrl = imageRef.downloadUrl.await().toString()

        // 3. Prepare Data
        val finalArtwork = artwork.copy(
            id = artworkId,
            imageUrl = downloadUrl,
            createdAt = System.currentTimeMillis()
        )

        val artworkData = finalArtwork.toMap().toMutableMap()
        artworkData["userId"] = userId

        if (latitude != null && longitude != null) {
            artworkData["latitude"] = latitude
            artworkData["longitude"] = longitude
        }

        // 4. Save to Firestore
        db.collection("artworks")
            .document(artworkId)
            .set(artworkData)
            .await()
    }

    /**
     * Updates an existing artwork. Handles optional image replacement.
     */
    suspend fun updateArtwork(
        userId: String,
        artworkId: String,
        imageUri: Uri?,
        currentImageUrl: String,
        updatedFields: Map<String, Any>,
        latitude: Double?,
        longitude: Double?
    ) {
        // 1. Handle Image Logic
        val finalImageUrl = if (imageUri != null) {
            val imageRef = storageRef.child("$userId/$artworkId.jpg")
            imageRef.putFile(imageUri).await()
            imageRef.downloadUrl.await().toString()
        } else {
            currentImageUrl
        }

        // 2. Prepare Update Map
        val artworkData = updatedFields.toMutableMap()
        artworkData["imageUrl"] = finalImageUrl

        if (latitude != null && longitude != null) {
            artworkData["latitude"] = latitude
            artworkData["longitude"] = longitude
        }

        // 3. Update Firestore
        db.collection("artworks")
            .document(artworkId)
            .update(artworkData)
            .await()
    }

    /**
     * Deletes artwork from Firestore and attempts to delete the image from Storage.
     */
    suspend fun deleteArtwork(userId: String, artworkId: String) {
        // 1. Delete from Firestore
        db.collection("artworks")
            .document(artworkId)
            .delete()
            .await()

        // 2. Delete from Storage (Best effort)
        try {
            storageRef.child("$userId/$artworkId.jpg").delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ============================================================================================
    // SECTION 2: GALLERY VIEW MODEL (Public Feed)
    // ============================================================================================

    /**
     * Fetch ALL artworks for the public gallery.
     */
    suspend fun getAllArtworks(): List<Artwork> {
        val snapshot = db.collection("artworks")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100) // Safety limit
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Artwork::class.java)?.copy(id = doc.id)
        }
    }

    /**
     * Toggle the "Hidden" status of an artwork.     */
    suspend fun toggleHiddenStatus(artworkId: String, currentStatus: Boolean) {
        db.collection("artworks")
            .document(artworkId)
            .update("isHidden", !currentStatus)
            .await()
    }

    /**
     * Toggle the "Sold" status of an artwork.
     */
    suspend fun toggleSoldStatus(artworkId: String, currentStatus: Boolean) {
        db.collection("artworks")
            .document(artworkId)
            .update("isSold", !currentStatus) // Flips the boolean (true -> false, false -> true)
            .await()
    }



    // ============================================================================================
    // SECTION 3: SAVED ARTWORK VIEW MODEL (Favorites)
    // ============================================================================================

        /**
         * Fetches the list of saved IDs from the "savedArtworks/{userId}" document.
         */
        suspend fun getSavedArtworkIds(userId: String): Set<String> {
            val snapshot = db.collection("savedArtworks").document(userId).get().await()
            return (snapshot.get("artworkIds") as? List<*>)
                ?.filterIsInstance<String>()
                ?.toSet() ?: emptySet()
        }

        /**
         * Fetches actual Artwork objects based on a list of IDs.
         * Handles Firestore's 10-item limit for 'whereIn' queries by chunking.
         */
        suspend fun getArtworksByIds(ids: Set<String>): List<Artwork> {
            if (ids.isEmpty()) return emptyList()

            val artworksList = mutableListOf<Artwork>()

            // Chunking to avoid Firestore limit
            ids.chunked(10).forEach { chunk ->
                val snapshot = db.collection("artworks")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()

                snapshot.documents.mapNotNullTo(artworksList) { doc ->
                    doc.toObject(Artwork::class.java)?.copy(id = doc.id)
                }
            }
            return artworksList.sortedByDescending { it.createdAt }
        }

        /**
         * Updates the user's saved ID list in Firestore.
         */
        suspend fun updateSavedArtworkIds(userId: String, newIds: Set<String>) {
            db.collection("savedArtworks")
                .document(userId)
                .set(mapOf("artworkIds" to newIds.toList()))
                .await()
        }
    }


    // Extension helper for mapping
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

