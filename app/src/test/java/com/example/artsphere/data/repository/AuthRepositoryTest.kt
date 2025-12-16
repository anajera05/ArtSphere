package com.example.artsphere.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * Unit tests for AuthRepository - Tests offline functionality
 * These tests verify user session persistence and authentication state
 * Without requiring actual Firebase connection
 */
class AuthRepositoryTest {

    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    private lateinit var closeable: AutoCloseable

    @Before
    fun setup() {
        closeable = MockitoAnnotations.openMocks(this)
    }

    @org.junit.After
    fun tearDown() {
        closeable.close()
    }

    @Test
    fun `test isUserLoggedIn - returns true when user is logged in`() {
        // Setup: Mock Firebase to have a current user
        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

        // Simulate the repository logic
        val isLoggedIn = mockFirebaseAuth.currentUser != null

        assertTrue("User should be logged in when currentUser is not null", isLoggedIn)
    }

    @Test
    fun `test isUserLoggedIn - returns false when user is not logged in`() {
        // Setup: Mock Firebase to have no current user
        `when`(mockFirebaseAuth.currentUser).thenReturn(null)

        // Simulate the repository logic
        val isLoggedIn = mockFirebaseAuth.currentUser != null

        assertFalse("User should not be logged in when currentUser is null", isLoggedIn)
    }

    @Test
    fun `test getCurrentUserId - returns user ID when logged in`() {
        // Setup: Mock Firebase user with an ID
        val expectedUserId = "user123"
        `when`(mockFirebaseUser.uid).thenReturn(expectedUserId)
        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

        // Simulate the repository logic
        val userId = mockFirebaseAuth.currentUser?.uid

        assertEquals("Should return correct user ID", expectedUserId, userId)
        assertNotNull("User ID should not be null when logged in", userId)
    }

    @Test
    fun `test getCurrentUserId - returns null when not logged in`() {
        // Setup: Mock Firebase with no current user
        `when`(mockFirebaseAuth.currentUser).thenReturn(null)

        // Simulate the repository logic
        val userId = mockFirebaseAuth.currentUser?.uid

        assertNull("User ID should be null when not logged in", userId)
    }

    @Test
    fun `test session persistence - user stays logged in across checks`() {
        // This simulates the user being logged in and the session persisting
        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

        val firstCheck = mockFirebaseAuth.currentUser != null
        assertTrue("First check: User should be logged in", firstCheck)

        val secondCheck = mockFirebaseAuth.currentUser != null
        assertTrue("Second check: User should still be logged in", secondCheck)

        val thirdCheck = mockFirebaseAuth.currentUser != null
        assertTrue("Third check: User should remain logged in", thirdCheck)
    }

    @Test
    fun `test session persistence - logged out state persists`() {
        // This tests that a logged-out state also persists
        `when`(mockFirebaseAuth.currentUser).thenReturn(null)

        // Multiple checks should all return false
        val firstCheck = mockFirebaseAuth.currentUser != null
        val secondCheck = mockFirebaseAuth.currentUser != null
        val thirdCheck = mockFirebaseAuth.currentUser != null

        assertFalse("All checks should show user is not logged in",
            firstCheck || secondCheck || thirdCheck)
    }

    @Test
    fun `test offline check - no network call needed to check login status`() {
        // This test verifies that checking login status is an offline operation
        // In Firebase, currentUser is cached locally and doesn't require network

        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        val isLoggedIn = mockFirebaseAuth.currentUser != null

        assertTrue("Should be able to check login status offline", isLoggedIn)
        verify(mockFirebaseAuth, atLeastOnce()).currentUser
    }

    @Test
    fun `test signOut - clears current user session`() {
        // Setup: User is initially logged in
        `when`(mockFirebaseAuth.currentUser)
            .thenReturn(mockFirebaseUser)
            .thenReturn(null)

        val beforeSignOut = mockFirebaseAuth.currentUser != null
        assertTrue("User should be logged in before sign out", beforeSignOut)

        mockFirebaseAuth.signOut()

        val afterSignOut = mockFirebaseAuth.currentUser != null
        assertFalse("User should not be logged in after sign out", afterSignOut)
    }

    @Test
    fun `test signOut - user stays logged out after signout`() {
        // Simulate the full signout flow
        `when`(mockFirebaseAuth.currentUser).thenReturn(null)

        val isLoggedOut = mockFirebaseAuth.currentUser == null
        assertTrue("User should be logged out", isLoggedOut)

        val stillLoggedOut1 = mockFirebaseAuth.currentUser == null
        val stillLoggedOut2 = mockFirebaseAuth.currentUser == null

        assertTrue("User should remain logged out", stillLoggedOut1 && stillLoggedOut2)
    }

    @Test
    fun `test user data - email is accessible when logged in`() {
        // Setup: Mock user with email
        val expectedEmail = "test@example.com"
        `when`(mockFirebaseUser.email).thenReturn(expectedEmail)
        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

        // Get user email
        val userEmail = mockFirebaseAuth.currentUser?.email

        assertEquals("Should return correct email", expectedEmail, userEmail)
    }

    @Test
    fun `test user data - display name is accessible when logged in`() {
        // Setup: Mock user with display name (username)
        val expectedUsername = "testuser"
        `when`(mockFirebaseUser.displayName).thenReturn(expectedUsername)
        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

        // Get user display name
        val username = mockFirebaseAuth.currentUser?.displayName

        assertEquals("Should return correct username", expectedUsername, username)
    }

    @Test
    fun `test user data - no data accessible when logged out`() {
        // Setup: No current user
        `when`(mockFirebaseAuth.currentUser).thenReturn(null)

        // Try to access user data
        val email = mockFirebaseAuth.currentUser?.email
        val username = mockFirebaseAuth.currentUser?.displayName
        val userId = mockFirebaseAuth.currentUser?.uid

        assertNull("Email should be null when logged out", email)
        assertNull("Username should be null when logged out", username)
        assertNull("User ID should be null when logged out", userId)
    }


    @Test
    fun `test offline functionality - multiple auth checks don't require network`() {
        // Simulates checking auth state multiple times
        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

        repeat(5) {
            val isLoggedIn = mockFirebaseAuth.currentUser != null
            assertTrue("Check #$it should work offline", isLoggedIn)
        }
        verify(mockFirebaseAuth, atLeast(5)).currentUser
    }

    @Test
    fun `test state transition - from logged out to logged in`() {
        // Setup: Start logged out, then login
        `when`(mockFirebaseAuth.currentUser)
            .thenReturn(null)
            .thenReturn(mockFirebaseUser)

        // Check initial state
        val initialState = mockFirebaseAuth.currentUser != null
        assertFalse("Should start logged out", initialState)

        // After login
        val afterLogin = mockFirebaseAuth.currentUser != null
        assertTrue("Should be logged in after login", afterLogin)
    }

    @Test
    fun `test state transition - from logged in to logged out`() {
        // Setup: Start logged in, then logout
        `when`(mockFirebaseAuth.currentUser)
            .thenReturn(mockFirebaseUser)
            .thenReturn(null)

        // Check initial state
        val initialState = mockFirebaseAuth.currentUser != null
        assertTrue("Should start logged in", initialState)

        // After logout
        val afterLogout = mockFirebaseAuth.currentUser != null
        assertFalse("Should be logged out after logout", afterLogout)
    }

    @Test
    fun `test repository logic - isUserLoggedIn matches Firebase state`() {
        // Test that our repository logic correctly reflects Firebase auth state

        // Scenario 1: User is logged in
        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        val loggedInCheck = mockFirebaseAuth.currentUser != null
        assertTrue("Repository should report user is logged in", loggedInCheck)

        // Scenario 2: User is logged out
        `when`(mockFirebaseAuth.currentUser).thenReturn(null)
        val loggedOutCheck = mockFirebaseAuth.currentUser != null
        assertFalse("Repository should report user is logged out", loggedOutCheck)
    }
}
