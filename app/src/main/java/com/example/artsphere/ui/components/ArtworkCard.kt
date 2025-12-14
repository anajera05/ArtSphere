package com.example.artsphere.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.artsphere.data.model.Artwork
import kotlinx.coroutines.delay

@Composable
fun ArtworkCard(
    artwork: Artwork,
    isSaved: Boolean,
    onArtworkClick: () -> Unit,
    onLikeClick: () -> Unit,
    isGallery: Boolean = false,
    isShop: Boolean = false,
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val heartScale by animateFloatAsState(
        targetValue = if (isSaved) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "heartScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = {
            isPressed = true
            onArtworkClick()
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = artwork.imageUrl,
                contentDescription = artwork.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // SOLD badge (top left)
            if (artwork.isSold) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE91E63),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "SOLD",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if(!isShop){
                if (isGallery){
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f)
                                    ),
                                    startY = 400f
                                )
                            )
                    )
                }




                // Like Button (top right)
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(30.dp)
                        .scale(heartScale)
                        .shadow(10.dp, CircleShape)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isSaved) Color(0xFFE91E63) else Color(0xFF424242),
                        modifier = Modifier.size(22.dp)
                    )
                }

                if(isGallery) {
                    Text(
                        text = artwork.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                }
            }

        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
fun ArtworkCardPreview() {// Mock data for preview
    val sampleArtwork = Artwork(
        id = "1",
        userId = "user1",
        name = "Sunset Boulevard",
        category = "Painting",
        description = "A beautiful sunset.",
        price = "150.00",
        imageUrl = "", // Network images won't load in preview without specific config
        contactEmail = "test@example.com",
        contactName = "John Doe",
        createdAt = System.currentTimeMillis(),
        isSold = false,
        isHidden = false
    )

    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ArtworkCard(
                artwork = sampleArtwork,
                isSaved = true,
                onArtworkClick = {},
                onLikeClick = {},
                isGallery = true // Shows the name text overlay
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
fun ArtworkCardSoldPreview() {
    val sampleArtwork = Artwork(
        id = "2",
        userId = "user1",
        name = "Sold Item",
        category = "Sculpture",
        description = "A sold item.",
        price = "500.00",
        imageUrl = "",
        contactEmail = "test@example.com",
        contactName = "Jane Doe",
        createdAt = System.currentTimeMillis(),
        isSold = true, // Enables the SOLD badge
        isHidden = false
    )

    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ArtworkCard(
                artwork = sampleArtwork,
                isSaved = false,
                onArtworkClick = {},
                onLikeClick = {},
                isGallery = false // Hides the name overlay (clean view)
            )
        }
    }
}
