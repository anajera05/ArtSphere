package com.example.artsphere.data.model

import com.google.firebase.firestore.PropertyName

data class Artwork(
    @PropertyName("id") val id: String = "",
    @PropertyName("imageUrl") val imageUrl: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("category") val category: String = ArtworkCategory.PAINTING_DRAWING.name,
    @PropertyName("description") val description: String = "",
    @PropertyName("price") val price: String = "",
    @PropertyName("contactEmail") val contactEmail: String = "",
    @PropertyName("contactName") val contactName: String = "",
    @PropertyName("createdAt") val createdAt: Long = System.currentTimeMillis(),
    @PropertyName("latitude") val latitude: Double? = null,
    @PropertyName("longitude") val longitude: Double? = null,
    @PropertyName("userId") val userId: String = ""
) {
    val categoryEnum: ArtworkCategory
        get() = try {
            ArtworkCategory.valueOf(category)
        } catch (e: Exception) {
            ArtworkCategory.OTHER
        }
}

enum class ArtworkCategory(val displayName: String) {
    PAINTING_DRAWING("Painting & Drawing"),
    PHOTOGRAPHIC("Photographic"),
    DIGITAL("Digital"),
    OTHER("Other")
}