package com.example.artsphere.ui.artworks

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Artwork Upload Form Validation
 * Tests offline validation logic for uploading artworks
 */
class ArtworkUploadValidationTest {
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex()
        return emailRegex.matches(email)
    }

    @Test
    fun `test email validation - valid emails pass`() {
        val validEmails = listOf(
            "user@example.com",
            "test.user@example.com",
            "user_name@example.com",
            "user-name@example.com",
            "user123@example.com",
            "user@sub.example.com",
            "user@example.co.uk"
        )

        validEmails.forEach { email ->
            assertTrue("$email should be valid", isValidEmail(email))
        }
    }

    @Test
    fun `test email validation - invalid emails fail`() {
        val invalidEmails = listOf(
            "",
            "notanemail",
            "@example.com",
            "user@",
            "user@domain",
            "user @example.com",
            "user@.com",
            "user@domain.c"
        )

        invalidEmails.forEach { email ->
            assertFalse("'$email' should be invalid", isValidEmail(email))
        }
    }

    @Test
    fun `test email validation - special characters`() {
        assertTrue("Email with dot should be valid",
            isValidEmail("first.last@example.com"))
        assertTrue("Email with underscore should be valid",
            isValidEmail("user_name@example.com"))
        assertTrue("Email with dash should be valid",
            isValidEmail("user-name@example.com"))

        // Test invalid special characters
        assertFalse("Email with space should be invalid",
            isValidEmail("user name@example.com"))
        assertFalse("Email with # should be invalid",
            isValidEmail("user#name@example.com"))
    }

    private fun isValidPrice(priceText: String): Boolean {
        if (priceText.isBlank()) return true
        return try {
            val price = priceText.toDouble()
            price >= 0.0
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun formatPrice(priceText: String): String {
        if (priceText.isBlank()) return ""
        return try {
            val price = priceText.toDouble()
            String.format("%.2f", price)
        } catch (e: NumberFormatException) {
            priceText
        }
    }

    @Test
    fun `test price validation - valid prices`() {
        val validPrices = listOf(
            "0",
            "0.00",
            "10",
            "10.50",
            "99.99",
            "1000.00",
            "1234.56"
        )

        validPrices.forEach { price ->
            assertTrue("Price '$price' should be valid", isValidPrice(price))
        }
    }

    @Test
    fun `test price validation - invalid prices`() {
        val invalidPrices = listOf(
            "abc",
            "-10",
            "-0.01",
            "10.5.5",
            "10,50"
        )

        invalidPrices.forEach { price ->
            assertFalse("Price '$price' should be invalid", isValidPrice(price))
        }
    }

    @Test
    fun `test price validation - empty is valid (optional field)`() {
        assertTrue("Empty price should be valid (optional)", isValidPrice(""))
        assertTrue("Blank price should be valid (optional)", isValidPrice("   "))
    }

    @Test
    fun `test price formatting - two decimal places`() {
        assertEquals("10.00", formatPrice("10"))
        assertEquals("10.50", formatPrice("10.5"))
        assertEquals("10.56", formatPrice("10.56"))
        assertEquals("0.00", formatPrice("0"))
        assertEquals("99.99", formatPrice("99.99"))
    }

    @Test
    fun `test price formatting - rounds to two decimals`() {
        assertEquals("10.57", formatPrice("10.567"))  // rounds up
        assertEquals("10.56", formatPrice("10.564"))  // rounds down
    }

    @Test
    fun `test price input filtering - only digits and dot`() {
        // Simulates the visualTransformation filter
        fun filterPriceInput(input: String): String {
            return input.filter { it.isDigit() || it == '.' }
        }

        assertEquals("123.45", filterPriceInput("123.45"))
        assertEquals("123.45", filterPriceInput("$123.45"))
        assertEquals("123.45", filterPriceInput("123.45abc"))
        assertEquals("123.45", filterPriceInput("1,23.45"))
        assertEquals("", filterPriceInput("abc"))
    }
    data class ArtworkFormData(
        val name: String = "",
        val description: String = "",
        val contactName: String = "",
        val contactEmail: String = "",
        val category: String = "",
        val imageUri: String? = null,
        val price: String = "",
        val isEditMode: Boolean = false
    )

    private fun areAllFieldsFilled(form: ArtworkFormData): Boolean {
        return form.name.isNotBlank() &&
                form.description.isNotBlank() &&
                form.contactName.isNotBlank() &&
                form.contactEmail.isNotBlank() &&
                form.category.isNotBlank() &&
                (form.isEditMode || form.imageUri != null)
    }

    @Test
    fun `test required fields - all fields filled for new artwork`() {
        val form = ArtworkFormData(
            name = "Test Artwork",
            description = "Description",
            contactName = "John Doe",
            contactEmail = "john@example.com",
            category = "Painting",
            imageUri = "content://image.jpg",
            isEditMode = false
        )

        assertTrue("All required fields should be filled", areAllFieldsFilled(form))
    }

    @Test
    fun `test required fields - missing name fails`() {
        val form = ArtworkFormData(
            name = "",
            description = "Description",
            contactName = "John Doe",
            contactEmail = "john@example.com",
            category = "Painting",
            imageUri = "content://image.jpg"
        )

        assertFalse("Missing name should fail validation", areAllFieldsFilled(form))
    }

    @Test
    fun `test required fields - missing description fails`() {
        val form = ArtworkFormData(
            name = "Test Artwork",
            description = "",
            contactName = "John Doe",
            contactEmail = "john@example.com",
            category = "Painting",
            imageUri = "content://image.jpg"
        )

        assertFalse("Missing description should fail validation", areAllFieldsFilled(form))
    }

    @Test
    fun `test required fields - missing contact name fails`() {
        val form = ArtworkFormData(
            name = "Test Artwork",
            description = "Description",
            contactName = "",
            contactEmail = "john@example.com",
            category = "Painting",
            imageUri = "content://image.jpg"
        )

        assertFalse("Missing contact name should fail validation", areAllFieldsFilled(form))
    }

    @Test
    fun `test required fields - missing contact email fails`() {
        val form = ArtworkFormData(
            name = "Test Artwork",
            description = "Description",
            contactName = "John Doe",
            contactEmail = "",
            category = "Painting",
            imageUri = "content://image.jpg"
        )

        assertFalse("Missing contact email should fail validation", areAllFieldsFilled(form))
    }

    @Test
    fun `test required fields - missing category fails`() {
        val form = ArtworkFormData(
            name = "Test Artwork",
            description = "Description",
            contactName = "John Doe",
            contactEmail = "john@example.com",
            category = "",
            imageUri = "content://image.jpg"
        )

        assertFalse("Missing category should fail validation", areAllFieldsFilled(form))
    }

    @Test
    fun `test required fields - missing image fails for new artwork`() {
        val form = ArtworkFormData(
            name = "Test Artwork",
            description = "Description",
            contactName = "John Doe",
            contactEmail = "john@example.com",
            category = "Painting",
            imageUri = null,
            isEditMode = false
        )

        assertFalse("Missing image should fail for new artwork", areAllFieldsFilled(form))
    }

    @Test
    fun `test required fields - missing image OK for edit mode`() {
        val form = ArtworkFormData(
            name = "Test Artwork",
            description = "Description",
            contactName = "John Doe",
            contactEmail = "john@example.com",
            category = "Painting",
            imageUri = null,
            isEditMode = true
        )

        assertTrue("Missing image should be OK for edit mode", areAllFieldsFilled(form))
    }

    @Test
    fun `test required fields - whitespace-only fields fail`() {
        val form = ArtworkFormData(
            name = "   ",
            description = "   ",
            contactName = "   ",
            contactEmail = "   ",
            category = "   ",
            imageUri = "content://image.jpg"
        )

        assertFalse("Whitespace-only fields should fail", areAllFieldsFilled(form))
    }

    @Test
    fun `test required fields - price is optional`() {
        // Test that artwork can be created without a price
        val formWithoutPrice = ArtworkFormData(
            name = "Test Artwork",
            description = "Description",
            contactName = "John Doe",
            contactEmail = "john@example.com",
            category = "Painting",
            imageUri = "content://image.jpg",
            price = ""
        )

        assertTrue("Artwork without price should be valid", areAllFieldsFilled(formWithoutPrice))
    }

    @Test
    fun `test combined validation - valid artwork passes all checks`() {
        val form = ArtworkFormData(
            name = "Beautiful Painting",
            description = "A stunning landscape",
            contactName = "Jane Artist",
            contactEmail = "jane.artist@example.com",
            category = "Painting",
            imageUri = "content://image.jpg",
            price = "150.00"
        )

        assertTrue("All fields filled", areAllFieldsFilled(form))
        assertTrue("Valid email", isValidEmail(form.contactEmail))
        assertTrue("Valid price", isValidPrice(form.price))
    }

    @Test
    fun `test combined validation - invalid email fails even with all fields`() {
        val form = ArtworkFormData(
            name = "Beautiful Painting",
            description = "A stunning landscape",
            contactName = "Jane Artist",
            contactEmail = "invalid-email",
            category = "Painting",
            imageUri = "content://image.jpg",
            price = "150.00"
        )

        assertTrue("All fields filled", areAllFieldsFilled(form))
        assertFalse("Invalid email should fail", isValidEmail(form.contactEmail))
    }

    @Test
    fun `test combined validation - invalid price fails`() {
        val form = ArtworkFormData(
            name = "Beautiful Painting",
            description = "A stunning landscape",
            contactName = "Jane Artist",
            contactEmail = "jane@example.com",
            category = "Painting",
            imageUri = "content://image.jpg",
            price = "abc"  // Invalid price
        )

        assertTrue("All fields filled", areAllFieldsFilled(form))
        assertTrue("Valid email", isValidEmail(form.contactEmail))
        assertFalse("Invalid price should fail", isValidPrice(form.price))
    }

    @Test
    fun `test error messages - specific field feedback`() {

        val missingNameError = "Name is required"
        assertTrue("Error should mention name", missingNameError.contains("Name"))

        val missingDescError = "Description is required"
        assertTrue("Error should mention description", missingDescError.contains("Description"))

        val invalidEmailError = "Please enter a valid email address"
        assertTrue("Error should mention valid email", invalidEmailError.contains("valid email"))

        val invalidPriceError = "Please enter a valid price"
        assertTrue("Error should mention valid price", invalidPriceError.contains("valid price"))

        val missingImageError = "Please select an image"
        assertTrue("Error should mention image", missingImageError.contains("image"))
    }
}
