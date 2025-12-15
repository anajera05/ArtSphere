package com.example.artsphere.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val auth = FirebaseAuth.getInstance()
    // Ensure this matches the bucket URL from your original code
    private val storageRef = FirebaseStorage.getInstance("gs://artsphere-android.firebasestorage.app")
        .reference.child("profile_photos")

    /**
     * Helper to get the current user.
     */
    val currentUser get() = auth.currentUser

    /**
     * Reloads the Firebase Auth user to ensure data (like photoUrl) is fresh.
     */
    suspend fun reloadUser() {
        auth.currentUser?.reload()?.await()
    }

    /**
     * Uploads an image to Storage, then updates the Firebase Auth Profile photo URI.
     * Returns the new download URL string.
     */
    suspend fun uploadProfilePhoto(imageUri: Uri): String {
        val user = auth.currentUser ?: throw Exception("No user logged in")

        // 1. Upload to Storage
        val ref = storageRef.child("${user.uid}/profile.jpg")
        ref.putFile(imageUri).await()
        val downloadUrl = ref.downloadUrl.await().toString()

        // 2. Update Auth Profile
        val updates = UserProfileChangeRequest.Builder()
            .setPhotoUri(Uri.parse(downloadUrl))
            .build()

        user.updateProfile(updates).await()

        // 3. Reload to confirm changes propagate
        user.reload().await()

        return downloadUrl
    }
}
