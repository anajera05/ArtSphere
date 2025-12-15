package com.example.artsphere.data.repository

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Helper to check if a user is currently logged in
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // Helper to get current user ID
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Wrapper for signInWithEmailAndPassword using coroutines
     */
    suspend fun login(email: String, password: String): AuthResult {
        return auth.signInWithEmailAndPassword(email, password).await()
    }

    /**
     * Wrapper for createUserWithEmailAndPassword using coroutines.
     * Also updates the display name immediately after creation.
     */
    suspend fun signup(email: String, password: String, username: String): AuthResult {
        // 1. Create the user
        val result = auth.createUserWithEmailAndPassword(email, password).await()

        // 2. Set the display name (Username) immediately
        val user = result.user
        if (user != null && username.isNotBlank()) {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build()
            user.updateProfile(profileUpdates).await()
        }

        return result
    }

    fun signOut() {
        auth.signOut()
    }
}
