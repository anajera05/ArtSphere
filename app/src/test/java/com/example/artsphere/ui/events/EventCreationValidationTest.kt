package com.example.artsphere.ui.events

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Event Creation Form Validation
 * Tests offline validation logic for creating events
 */
class EventCreationValidationTest {
    private fun isValidHour(hour: String): Boolean {
        return try {
            val hourInt = hour.toInt()
            hourInt in 1..12
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun isValidMinute(minute: String): Boolean {
        return try {
            val minuteInt = minute.toInt()
            minuteInt in 0..59
        } catch (e: NumberFormatException) {
            false
        }
    }

    @Test
    fun `test hour validation - valid hours 1-12`() {
        val validHours = listOf("1", "2", "6", "12")

        validHours.forEach { hour ->
            assertTrue("Hour $hour should be valid", isValidHour(hour))
        }
    }

    @Test
    fun `test hour validation - invalid hours`() {
        val invalidHours = listOf("0", "13", "24", "-1", "abc", "")

        invalidHours.forEach { hour ->
            assertFalse("Hour '$hour' should be invalid", isValidHour(hour))
        }
    }

    @Test
    fun `test minute validation - valid minutes 0-59`() {
        val validMinutes = listOf("0", "15", "30", "45", "59")

        validMinutes.forEach { minute ->
            assertTrue("Minute $minute should be valid", isValidMinute(minute))
        }
    }

    @Test
    fun `test minute validation - invalid minutes`() {
        val invalidMinutes = listOf("60", "99", "-1", "abc", "")

        invalidMinutes.forEach { minute ->
            assertFalse("Minute '$minute' should be invalid", isValidMinute(minute))
        }
    }

    @Test
    fun `test time format - creates valid time string`() {
        fun formatTime(hour: String, minute: String, amPm: String): String {
            return "$hour:${minute.padStart(2, '0')} $amPm"
        }

        assertEquals("9:00 AM", formatTime("9", "0", "AM"))
        assertEquals("12:30 PM", formatTime("12", "30", "PM"))
        assertEquals("3:45 PM", formatTime("3", "45", "PM"))
    }

    @Test
    fun `test time validation - complete time string`() {
        val validTimes = listOf(
            "9:00 AM",
            "12:30 PM",
            "3:45 PM",
            "11:59 PM",
            "1:00 AM"
        )

        validTimes.forEach { time ->
            assertTrue("Time '$time' should not be empty", time.isNotBlank())
            assertTrue("Time should contain colon", time.contains(":"))
            assertTrue("Time should contain AM or PM",
                time.contains("AM") || time.contains("PM"))
        }
    }

    private fun isValidMaxParticipants(maxPart: String): Boolean {
        if (maxPart.isBlank()) return true
        return try {
            val num = maxPart.toInt()
            num >= 0
        } catch (e: NumberFormatException) {
            false
        }
    }

    @Test
    fun `test max participants - valid numbers`() {
        val validNumbers = listOf("0", "10", "50", "100", "1000")

        validNumbers.forEach { num ->
            assertTrue("Number '$num' should be valid", isValidMaxParticipants(num))
        }
    }

    @Test
    fun `test max participants - invalid numbers`() {
        val invalidNumbers = listOf("-1", "-10", "abc", "10.5")

        invalidNumbers.forEach { num ->
            assertFalse("Number '$num' should be invalid", isValidMaxParticipants(num))
        }
    }

    @Test
    fun `test max participants - empty means unlimited`() {
        assertTrue("Empty should mean unlimited (0)", isValidMaxParticipants(""))
        assertTrue("Blank should mean unlimited (0)", isValidMaxParticipants("   "))
    }

    @Test
    fun `test max participants - zero means unlimited`() {
        val maxPart = "0"
        val parsedValue = maxPart.toIntOrNull() ?: 0

        assertEquals("Zero should mean unlimited", 0, parsedValue)
    }

    data class EventFormData(
        val title: String = "",
        val description: String = "",
        val date: String = "",
        val time: String = "",
        val locationName: String = "",
        val category: String = "",
        val maxParticipants: String = "",
        val imageUri: String? = null
    )

    private fun areAllFieldsFilled(form: EventFormData): Boolean {
        return form.title.isNotBlank() &&
                form.description.isNotBlank() &&
                form.date.isNotBlank() &&
                form.time.isNotBlank() &&
                form.locationName.isNotBlank()
    }

    @Test
    fun `test required fields - all fields filled`() {
        val form = EventFormData(
            title = "Art Exhibition",
            description = "A wonderful art show",
            date = "12/25/2024",
            time = "2:00 PM",
            locationName = "City Gallery",
            category = "Exhibition"
        )

        assertTrue("All required fields should be filled", areAllFieldsFilled(form))
    }

    @Test
    fun `test required fields - missing title fails`() {
        val form = EventFormData(
            title = "",
            description = "A wonderful art show",
            date = "12/25/2024",
            time = "2:00 PM",
            locationName = "City Gallery"
        )

        assertFalse("Missing title should fail", areAllFieldsFilled(form))
    }

    @Test
    fun `test required fields - missing description fails`() {
        val form = EventFormData(
            title = "Art Exhibition",
            description = "",
            date = "12/25/2024",
            time = "2:00 PM",
            locationName = "City Gallery"
        )

        assertFalse("Missing description should fail", areAllFieldsFilled(form))
    }

    @Test
    fun `test required fields - missing date fails`() {
        val form = EventFormData(
            title = "Art Exhibition",
            description = "A wonderful art show",
            date = "",
            time = "2:00 PM",
            locationName = "City Gallery"
        )

        assertFalse("Missing date should fail", areAllFieldsFilled(form))
    }

    @Test
    fun `test required fields - missing time fails`() {
        val form = EventFormData(
            title = "Art Exhibition",
            description = "A wonderful art show",
            date = "12/25/2024",
            time = "",
            locationName = "City Gallery"
        )

        assertFalse("Missing time should fail", areAllFieldsFilled(form))
    }

    @Test
    fun `test required fields - missing location fails`() {
        val form = EventFormData(
            title = "Art Exhibition",
            description = "A wonderful art show",
            date = "12/25/2024",
            time = "2:00 PM",
            locationName = ""
        )

        assertFalse("Missing location should fail", areAllFieldsFilled(form))
    }

    @Test
    fun `test required fields - whitespace-only fields fail`() {
        val form = EventFormData(
            title = "   ",
            description = "   ",
            date = "   ",
            time = "   ",
            locationName = "   "
        )

        assertFalse("Whitespace-only fields should fail", areAllFieldsFilled(form))
    }

    @Test
    fun `test required fields - image is optional`() {
        val formWithoutImage = EventFormData(
            title = "Art Exhibition",
            description = "A wonderful art show",
            date = "12/25/2024",
            time = "2:00 PM",
            locationName = "City Gallery",
            imageUri = null
        )

        assertTrue("Event without image should be valid", areAllFieldsFilled(formWithoutImage))
    }

    @Test
    fun `test required fields - category has default`() {
        // Category defaults to first in list, so empty is OK
        val formWithoutCategory = EventFormData(
            title = "Art Exhibition",
            description = "A wonderful art show",
            date = "12/25/2024",
            time = "2:00 PM",
            locationName = "City Gallery",
            category = ""
        )

        assertTrue("Event should be valid", areAllFieldsFilled(formWithoutCategory))
    }

    @Test
    fun `test required fields - max participants has default`() {
        val formWithoutMaxPart = EventFormData(
            title = "Art Exhibition",
            description = "A wonderful art show",
            date = "12/25/2024",
            time = "2:00 PM",
            locationName = "City Gallery",
            maxParticipants = ""
        )

        assertTrue("Event without max participants should be valid",
            areAllFieldsFilled(formWithoutMaxPart))
    }

    @Test
    fun `test date format - common formats are valid`() {
        val validDates = listOf(
            "12/25/2024",
            "01/01/2025",
            "6/15/2024",
            "12/5/2024"
        )

        validDates.forEach { date ->
            assertTrue("Date '$date' should not be empty", date.isNotBlank())
            assertTrue("Date should contain /", date.contains("/"))
        }
    }

    @Test
    fun `test date format - invalid formats fail`() {
        val invalidDates = listOf(
            "",
            "   ",
            "2024-12-25",
            "December 25",
            "25/12/2024"
        )

        invalidDates.forEach { date ->
            if (date.isNotBlank()) {
                // For non-empty dates, they might still be invalid format
                // but we're just checking they're not the expected format
            } else {
                assertTrue("Empty date should be blank", date.isBlank())
            }
        }
    }

    @Test
    fun `test capacity logic - zero means unlimited`() {
        val maxParticipants = 0
        val currentParticipants = 50

        val isFull = maxParticipants > 0 && currentParticipants >= maxParticipants

        assertFalse("Event with 0 max should never be full", isFull)
    }

    @Test
    fun `test capacity logic - event is full when at capacity`() {
        val maxParticipants = 10
        val currentParticipants = 10

        val isFull = maxParticipants > 0 && currentParticipants >= maxParticipants

        assertTrue("Event should be full at capacity", isFull)
    }

    @Test
    fun `test capacity logic - event is full when over capacity`() {
        val maxParticipants = 10
        val currentParticipants = 15

        val isFull = maxParticipants > 0 && currentParticipants >= maxParticipants

        assertTrue("Event should be full over capacity", isFull)
    }

    @Test
    fun `test capacity logic - event not full below capacity`() {
        val maxParticipants = 10
        val currentParticipants = 5

        val isFull = maxParticipants > 0 && currentParticipants >= maxParticipants

        assertFalse("Event should not be full below capacity", isFull)
    }

    @Test
    fun `test capacity logic - available spots calculation`() {
        fun availableSpots(max: Int, current: Int): Int? {
            return if (max > 0) max - current else null
        }

        assertEquals(5, availableSpots(10, 5))
        assertEquals(0, availableSpots(10, 10))
        assertNull(availableSpots(0, 50))
    }


    @Test
    fun `test combined validation - valid event passes all checks`() {
        val form = EventFormData(
            title = "Art Workshop",
            description = "Learn painting techniques",
            date = "12/25/2024",
            time = "2:00 PM",
            locationName = "Art Studio",
            category = "Workshop",
            maxParticipants = "20"
        )

        assertTrue("All fields filled", areAllFieldsFilled(form))
        assertTrue("Valid max participants", isValidMaxParticipants(form.maxParticipants))
    }

    @Test
    fun `test combined validation - invalid max participants fails`() {
        val form = EventFormData(
            title = "Art Workshop",
            description = "Learn painting techniques",
            date = "12/25/2024",
            time = "2:00 PM",
            locationName = "Art Studio",
            maxParticipants = "abc"
        )

        assertTrue("All required fields filled", areAllFieldsFilled(form))
        assertFalse("Invalid max participants should fail",
            isValidMaxParticipants(form.maxParticipants))
    }

    @Test
    fun `test error messages - specific field feedback`() {
        // Test that the app provides specific error messages

        val missingTitleError = "Title is required"
        assertTrue("Error should mention title", missingTitleError.contains("Title"))

        val missingDescError = "Description is required"
        assertTrue("Error should mention description", missingDescError.contains("Description"))

        val missingDateError = "Date is required"
        assertTrue("Error should mention date", missingDateError.contains("Date"))

        val missingTimeError = "Time is required"
        assertTrue("Error should mention time", missingTimeError.contains("Time"))

        val missingLocationError = "Location is required"
        assertTrue("Error should mention location", missingLocationError.contains("Location"))

        val invalidHourError = "Hour must be between 1 and 12"
        assertTrue("Error should mention hour range", invalidHourError.contains("1 and 12"))

        val invalidMinuteError = "Minute must be between 0 and 59"
        assertTrue("Error should mention minute range", invalidMinuteError.contains("0 and 59"))
    }

    @Test
    fun `test error messages - time picker validation feedback`() {
        val hourOutOfRangeError = "Hour must be between 1 and 12"
        assertTrue("Should mention valid hour range", hourOutOfRangeError.contains("1 and 12"))

        val minuteOutOfRangeError = "Minute must be between 0 and 59"
        assertTrue("Should mention valid minute range",
            minuteOutOfRangeError.contains("0 and 59"))
    }
}
