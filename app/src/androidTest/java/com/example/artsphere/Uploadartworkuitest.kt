package com.example.artsphere

import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.artsphere.ui.addArtwork.UploadArtworkScreen
import com.example.artsphere.ui.artwork.ArtworkViewModel
import com.google.android.gms.maps.model.LatLng
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class UploadArtworkUITest {

    @get:Rule
    val composeTestRule = createComposeRule()



    /**
     * Test Case 1: Can type in artwork name field
     *
     * Given: Upload screen is displayed
     * When: User types in artwork name
     * Then: Text appears in field
     */
    @Test
    fun uploadScreen_canTypeArtworkName() {
        // Arrange
        val mockViewModel = ArtworkViewModel()

        composeTestRule.setContent {
            UploadArtworkScreen(
                onBackClick = {},
                viewModel = mockViewModel
            )
        }

        // Act
        composeTestRule
            .onNodeWithText("Artwork Name")
            .performTextInput("Sunset Painting")

        // Assert
        composeTestRule
            .onNode(hasText("Sunset Painting"))
            .assertExists()
    }



    /**
     * Test Case 2: Can type in description field
     *
     * Given: Upload screen is displayed
     * When: User types in description
     * Then: Text appears in multiline field
     */
    @Test
    fun uploadScreen_canTypeDescription() {
        // Arrange
        val mockViewModel = ArtworkViewModel()

        composeTestRule.setContent {
            UploadArtworkScreen(
                onBackClick = {},
                viewModel = mockViewModel
            )
        }

        // Act
        val description = "Beautiful artwork created with oil on canvas"
        composeTestRule
            .onNodeWithText("Description")
            .performTextInput(description)

        // Assert
        composeTestRule
            .onNode(hasText(description))
            .assertExists()
    }

    /**
     * Test Case 3: Can type in price field
     *
     * Given: Upload screen is displayed
     * When: User types price
     * Then: Price appears in field
     */
    @Test
    fun uploadScreen_canTypePrice() {
        // Arrange
        val mockViewModel = ArtworkViewModel()

        composeTestRule.setContent {
            UploadArtworkScreen(
                onBackClick = {},
                viewModel = mockViewModel
            )
        }

        // Act
        composeTestRule
            .onNodeWithText("Price (e.g., \$500 or Contact for price)")
            .performTextInput("\$500")

        // Assert
        composeTestRule
            .onNode(hasText("\$500"))
            .assertExists()
    }

    /**
     * Test Case 4: Can type contact information
     *
     * Given: Upload screen is displayed
     * When: User types contact name and email
     * Then: Contact info appears in fields
     */
    @Test
    fun uploadScreen_canTypeContactInfo() {
        // Arrange
        val mockViewModel = ArtworkViewModel()

        composeTestRule.setContent {
            UploadArtworkScreen(
                onBackClick = {},
                viewModel = mockViewModel
            )
        }

        // Act
        composeTestRule
            .onNodeWithText("Contact Name")
            .performTextInput("Jane Doe")

        composeTestRule
            .onNodeWithText("Contact Email")
            .performTextInput("jane@example.com")

        // Assert
        composeTestRule.onNode(hasText("Jane Doe")).assertExists()
        composeTestRule.onNode(hasText("jane@example.com")).assertExists()
    }

    /**
     * Test Case 5: Location card displays when location is provided
     *
     * Given: Upload screen opens with location data
     * Then: Purple location card is displayed with coordinates
     */
    @Test
    fun uploadScreen_displaysLocationCard() {
        // Arrange
        val mockViewModel = ArtworkViewModel()
        val location = LatLng(42.3601, -71.0589)

        composeTestRule.setContent {
            UploadArtworkScreen(
                onBackClick = {},
                viewModel = mockViewModel,
                initialImageUri = null,
                initialLocation = location
            )
        }

        // Wait for LaunchedEffect to process location
        composeTestRule.waitForIdle()

        // Assert - Location card appears
        composeTestRule.onNodeWithText("Location Selected").assertIsDisplayed()
        composeTestRule.onNodeWithText("📍").assertIsDisplayed()

        // Check coordinates are displayed (formatted to 4 decimals)
        composeTestRule.onNode(
            hasText("Lat: 42.3601, Lng: -71.0589", substring = true)
        ).assertIsDisplayed()
    }

    /**
     * Test Case 6: No location card when location not provided
     *
     * Given: Upload screen opens without location
     * Then: Location card is not displayed
     */
    @Test
    fun uploadScreen_noLocationCardWithoutLocation() {
        // Arrange
        val mockViewModel = ArtworkViewModel()

        composeTestRule.setContent {
            UploadArtworkScreen(
                onBackClick = {},
                viewModel = mockViewModel,
                initialImageUri = null,
                initialLocation = null
            )
        }

        // Assert - No location card
        composeTestRule.onNodeWithText("Location Selected").assertDoesNotExist()
    }

    /**
     * Test Case 7: Save button is disabled when no image selected
     *
     * Given: No image is selected
     * When: User fills in all fields
     * Then: Save button is disabled
     */
    @Test
    fun uploadScreen_saveButtonDisabledWithoutImage() {
        // Arrange
        val mockViewModel = ArtworkViewModel()

        composeTestRule.setContent {
            UploadArtworkScreen(
                onBackClick = {},
                viewModel = mockViewModel,
                initialImageUri = null,
                initialLocation = null
            )
        }

        // Act - Fill in name (but no image)
        composeTestRule
            .onNodeWithText("Artwork Name")
            .performTextInput("Test Artwork")

        // Assert - Save button is disabled
        composeTestRule
            .onNodeWithText("Save Artwork")
            .assertIsNotEnabled()
    }

    /**
     * Test Case 8: Save button is disabled when no name entered
     *
     * Given: Image is selected but name is blank
     * Then: Save button is disabled
     */
    @Test
    fun uploadScreen_saveButtonDisabledWithoutName() {
        // Arrange
        val mockViewModel = ArtworkViewModel()
        val mockUri = Uri.parse("content://test/image.jpg")

        composeTestRule.setContent {
            UploadArtworkScreen(
                onBackClick = {},
                viewModel = mockViewModel,
                initialImageUri = mockUri,
                initialLocation = null
            )
        }

        // Wait for image to load
        composeTestRule.waitForIdle()

        // Assert - Save button is disabled (name is blank)
        composeTestRule
            .onNodeWithText("Save Artwork")
            .assertIsNotEnabled()
    }

    /**
     * Test Case 9: Save button is enabled with image and name
     *
     * Given: Image is selected and name is entered
     * Then: Save button is enabled
     */
    @Test
    fun uploadScreen_saveButtonEnabledWithImageAndName() {
        // Arrange
        val mockViewModel = ArtworkViewModel()
        val mockUri = Uri.parse("content://test/image.jpg")

        composeTestRule.setContent {
            UploadArtworkScreen(
                onBackClick = {},
                viewModel = mockViewModel,
                initialImageUri = mockUri,
                initialLocation = null
            )
        }

        // Wait for image
        composeTestRule.waitForIdle()

        // Act - Enter name
        composeTestRule
            .onNodeWithText("Artwork Name")
            .performTextInput("Test Artwork")

        // Assert - Save button is enabled
        composeTestRule
            .onNodeWithText("Save Artwork")
            .assertIsEnabled()
    }

    /**
     * Test Case 10: Back button is clickable
     *
     * Given: Upload screen is displayed
     * When: Back button is clicked
     * Then: onBackClick callback is triggered
     */
    @Test
    fun uploadScreen_backButtonWorks() {
        // Arrange
        val mockViewModel = ArtworkViewModel()
        var backClicked = false

        composeTestRule.setContent {
            UploadArtworkScreen(
                onBackClick = { backClicked = true },
                viewModel = mockViewModel
            )
        }

        // Act - Click back button (arrow icon in top bar)
        composeTestRule
            .onNodeWithContentDescription("Back")
            .performClick()

        // Assert
        assert(backClicked) { "Back button should trigger callback" }
    }

    /**
     * Test Case 11: Can scroll through form
     *
     * Given: Upload screen with all fields
     * When: User scrolls down
     * Then: All fields are accessible
     */
    @Test
    fun uploadScreen_canScrollThroughForm() {
        // Arrange
        val mockViewModel = ArtworkViewModel()

        composeTestRule.setContent {
            UploadArtworkScreen(
                onBackClick = {},
                viewModel = mockViewModel
            )
        }

        // Act - Scroll to bottom
        composeTestRule
            .onNodeWithText("Save Artwork")
            .performScrollTo()

        // Assert - Button is visible after scroll
        composeTestRule
            .onNodeWithText("Save Artwork")
            .assertIsDisplayed()
    }


    /**
     * Test Case 12: Image placeholder shows when no image
     *
     * Given: No image is selected
     * Then: "Add Artwork Image" placeholder is shown
     */
    @Test
    fun uploadScreen_showsImagePlaceholder() {
        // Arrange
        val mockViewModel = ArtworkViewModel()

        composeTestRule.setContent {
            UploadArtworkScreen(
                onBackClick = {},
                viewModel = mockViewModel,
                initialImageUri = null
            )
        }

        // Assert
        composeTestRule.onNodeWithText("Add Artwork Image").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add image").assertExists()
    }
}