package com.example.artsphere.ui.artworks.savedArtworks

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.artsphere.data.model.Artwork
import com.example.artsphere.ui.components.ArtworkCard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedArtworkScreen(
    onArtworkClick: (Artwork) -> Unit,
    viewModel: SavedArtworkViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCE4EC))
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when{
                uiState.isLoading -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                }

                uiState.savedArtworks.isEmpty() -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Filled.Favorite,
                                    contentDescription = "No saved artwork",
                                    modifier = Modifier.size(80.dp),
                                    tint = Color(0xFFE91E63)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No Saved Artwork Yet",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Start exploring and save your favorite artworks!",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                else -> {
                        itemsIndexed(uiState.savedArtworks) { index, artwork ->
                            var isVisible by remember { mutableStateOf(false) }

                            LaunchedEffect(Unit) {
                                delay(index * 50L) // Staggered delay
                                isVisible = true
                            }

                            val alpha by animateFloatAsState(
                                targetValue = if (isVisible) 1f else 0f,
                                animationSpec = tween(durationMillis = 500),
                                label = "alphaAnimation"
                            )
                            val scale by animateFloatAsState(
                                targetValue = if (isVisible) 1f else 0.8f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy
                                ),
                                label = "scaleAnimation"
                            )

                            Box(
                                modifier = Modifier
                                    .graphicsLayer {
                                        this.alpha = alpha
                                        this.scaleX = scale
                                        this.scaleY = scale
                                    }
                            ) {
                                ArtworkCard(
                                    artwork = artwork,
                                    isSaved = true,
                                    onArtworkClick = { onArtworkClick(artwork) },
                                    onLikeClick = { viewModel.toggleSaveArtwork(artwork.id)},
                                )
                            }
                        }
                    }
                }
            }

        // Error message
        if (uiState.error != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(uiState.error ?: "An error occurred")
            }
        }
    }
}


@Composable
fun SavedArtworkCard(
    artwork: Artwork,
    onClick: () -> Unit,
    onUnlike: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Image
                AsyncImage(
                    model = artwork.imageUrl,
                    contentDescription = artwork.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Unlike button (top right)
            IconButton(
                onClick = onUnlike,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Unlike",
                    tint = Color(0xFFE91E63)
                )
            }
        }
    }
}