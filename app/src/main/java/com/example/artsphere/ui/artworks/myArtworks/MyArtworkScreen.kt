package com.example.artsphere.ui.artworks.myArtworks

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.artsphere.data.model.Artwork
import com.example.artsphere.ui.artworks.ArtworkViewModel
import com.example.artsphere.ui.components.ArtworkCard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyArtworkScreen(
    onUploadClick: () -> Unit,
    onArtworkClick: (Artwork) -> Unit,
    viewModel: ArtworkViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F1FA))
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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

                uiState.artworks.isEmpty() -> {
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
                                Text(
                                        "🎨",
                                style = MaterialTheme.typography.displayLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No Artwork Yet",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Upload your first artwork to get started",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = onUploadClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF6200EE)
                                    )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Upload Artwork")
                                }
                            }
                        }
                    }
                }

                else -> {
                    itemsIndexed(uiState.artworks) { index, artwork ->
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
                                onLikeClick = { },
                                isShop = true
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

//@Composable
//fun ArtworkCard(
//    height: Int,
//    onClick: () -> Unit,
//    artwork: Artwork
//){
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(height.dp)
//            .padding(6.dp)
//            .clickable ( onClick = onClick ),
//        shape = RoundedCornerShape(16.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
//    ) {
//        AsyncImage(
//            model = artwork.imageUrl,
//            contentDescription = artwork.name,
//            modifier = Modifier
//                .fillMaxSize()
//                .weight(1f)
//                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
//            contentScale = ContentScale.Crop,
//        )
//
//
//    }
//}
//
