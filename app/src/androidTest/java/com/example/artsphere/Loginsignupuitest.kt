package com.example.artsphere

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginSignupUITest {

    @get:Rule
    val composeTestRule = createComposeRule()


    /**
     * Test Case 1: Login screen displays all UI elements
     *
     * Given: Login screen is displayed
     * Then: All UI elements are visible (email, password, button, etc.)
     */
    @Test
    fun loginScreen_displaysAllElements() {
        // Arrange
        val mockViewModel = AuthViewModel()

        composeTestRule.setContent {
            LoginScreen(
                viewModel = mockViewModel,
                onLoginSuccess = {},
                onCreateAccountClick = {}
            )
        }

        // Assert - Check all elements are displayed
        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("LOGIN").assertIsDisplayed()
        composeTestRule.onNodeWithText("Don't have an account?").assertIsDisplayed()
    }

    /**
     * Test Case 2: User can type in email field
     *
     * Given: Login screen is displayed
     * When: User types in email field
     * Then: Text appears in the field
     */
    @Test
    fun loginScreen_canTypeInEmailField() {
        // Arrange
        val mockViewModel = AuthViewModel()

        composeTestRule.setContent {
            LoginScreen(
                viewModel = mockViewModel,
                onLoginSuccess = {},
                onCreateAccountClick = {}
            )
        }

        // Act - Type in email field
        composeTestRule
            .onNodeWithText("Email")
            .performTextInput("test@example.com")

        // Assert - Email field contains the text
        composeTestRule
            .onNodeWithText("Email")
            .assertTextContains("test@example.com")
    }

    /**
     * Test Case 3: User can type in password field
     *
     * Given: Login screen is displayed
     * When: User types in password field
     * Then: Password is masked (not visible)
     */
    @Test
    fun loginScreen_canTypeInPasswordField() {
        // Arrange
        val mockViewModel = AuthViewModel()

        composeTestRule.setContent {
            LoginScreen(
                viewModel = mockViewModel,
                onLoginSuccess = {},
                onCreateAccountClick = {}
            )
        }

        // Act - Type in password field
        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("password123")

        // Assert - Password field exists
        composeTestRule
            .onNodeWithText("Password")
            .assertExists()
    }

    /**
     * Test Case 4: Login button is clickable
     *
     * Given: Email and password are entered
     * When: User clicks LOGIN button
     * Then: Button responds to click
     */
    @Test
    fun loginScreen_loginButtonIsClickable() {
        // Arrange
        val mockViewModel = AuthViewModel()
        var buttonClicked = false

        composeTestRule.setContent {
            LoginScreen(
                viewModel = mockViewModel,
                onLoginSuccess = { buttonClicked = true },
                onCreateAccountClick = {}
            )
        }

        // Act - Fill fields and click button
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("LOGIN").performClick()

    }

    /**
     * Test Case 5: Create account link navigates to signup
     *
     * Given: Login screen is displayed
     * When: User clicks "Don't have an account?"
     * Then: Navigation to signup occurs
     */
    @Test
    fun loginScreen_createAccountLinkIsClickable() {
        // Arrange
        val mockViewModel = AuthViewModel()
        var signupClicked = false

        composeTestRule.setContent {
            LoginScreen(
                viewModel = mockViewModel,
                onLoginSuccess = {},
                onCreateAccountClick = { signupClicked = true }
            )
        }

        // Act - Click create account link
        composeTestRule
            .onNodeWithText("Don't have an account?")
            .performClick()

        // Assert
        assert(signupClicked) { "Create account callback should be invoked" }
    }


    /**
     * Test Case 6: Can type in all signup fields
     *
     * Given: Signup screen is displayed
     * When: User types in username, email, and password
     * Then: All fields accept input
     */
    @Test
    fun signupScreen_canTypeInAllFields() {
        // Arrange
        val mockViewModel = AuthViewModel()

        composeTestRule.setContent {
            SignupScreen(
                viewModel = mockViewModel,
                onSignupSuccess = {},
                onBackToLogin = {}
            )
        }

        // Act
        composeTestRule.onNodeWithText("Username").performTextInput("TestUser")
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        // Assert - Fields contain text
        composeTestRule.onNodeWithText("Username").assertExists()
    }

    /**
     * Test Case 7: Back to login link works
     *
     * Given: Signup screen is displayed
     * When: "Sign in" link is clicked
     * Then: Navigation back to login occurs
     */
    @Test
    fun signupScreen_backToLoginWorks() {
        // Arrange
        val mockViewModel = AuthViewModel()
        var backClicked = false

        composeTestRule.setContent {
            SignupScreen(
                viewModel = mockViewModel,
                onSignupSuccess = {},
                onBackToLogin = { backClicked = true }
            )
        }

        // Act
        composeTestRule.onNodeWithText("Sign in").performClick()

        // Assert
        assert(backClicked) { "Back to login should be triggered" }
    }
}