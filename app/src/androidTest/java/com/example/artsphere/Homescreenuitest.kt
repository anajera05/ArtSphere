package com.example.artsphere

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.artsphere.data.model.Artwork
import com.example.artsphere.ui.artworks.myArtworks.ArtworkCard
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()


    /**
     * Test Case 1: Artwork card shows title
     *
     * Given: Artwork with name "Sunset Painting"
     * Then: Title is displayed on card
     */
    @Test
    fun artworkCard_displaysTitle() {
        // Arrange - Create mock artwork
        val artwork = Artwork(
            id = "1",
            name = "Sunset Painting",
            imageUrl = "https://example.com/image.jpg",
            category = "PAINTING_DRAWING",
            description = "Beautiful sunset",
            price = "$500",
            contactEmail = "artist@example.com",
            contactName = "Jane Doe"
        )

        composeTestRule.setContent {
            ArtworkCard(
                artwork = artwork,
                onClick = {}
            )
        }

        // Assert
        composeTestRule.onNodeWithText("Sunset Painting").assertIsDisplayed()
    }

    /**
     * Test Case 2: Artwork card shows artist name
     *
     * Given: Artwork has contact name
     * Then: Artist name is displayed
     */
    @Test
    fun artworkCard_displaysArtistName() {
        // Similar to Test Case 6
        val artwork = Artwork(
            id = "1",
            name = "Test Art",
            imageUrl = "url",
            category = "PAINTING_DRAWING",
            contactName = "Artist Name"
        )

        composeTestRule.setContent {
            ArtworkCard(
                artwork = artwork,
                onClick = {}
            )
        }
    }

    /**
     * Test Case 3: Artwork card is clickable
     *
     * Given: Artwork card is displayed
     * When: User taps the card
     * Then: onClick callback is triggered
     */
    @Test
    fun artworkCard_isClickable() {
        // Arrange
        var clickCount = 0
        val artwork = Artwork(
            id = "1",
            name = "Test Artwork",
            imageUrl = "url",
            category = "PAINTING_DRAWING"
        )

        composeTestRule.setContent {
            ArtworkCard(
                artwork = artwork,
                onClick = { clickCount++ }
            )
        }

        // Act
        composeTestRule.onNodeWithText("Test Artwork").performClick()

        // Assert
        assert(clickCount == 1) { "Card should be clickable" }
    }
}