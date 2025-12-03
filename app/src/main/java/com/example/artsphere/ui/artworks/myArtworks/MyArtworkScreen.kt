package com.example.artsphere.ui.artworks.myArtworks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.artsphere.data.model.Artwork
import com.example.artsphere.ui.artworks.ArtworkViewModel

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

        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF6200EE)
                )
            }

            uiState.artworks.isEmpty() -> {
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

            else -> {

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    items(uiState.artworks) { artwork ->
                        ArtworkCard(
                            120,
                            artwork = artwork,
                            onClick = { onArtworkClick(artwork) }
                        )
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
fun ArtworkCard(
    height: Int,
    onClick: () -> Unit,
    artwork: Artwork
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .padding(6.dp)
            .clickable ( onClick = onClick ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        AsyncImage(
            model = artwork.imageUrl,
            contentDescription = artwork.name,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            contentScale = ContentScale.Crop,
        )


    }
}

