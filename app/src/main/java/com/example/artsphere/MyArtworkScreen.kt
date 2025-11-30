package com.example.artsphere

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyArtworkScreen(
    onBackClick: () -> Unit,
    onUploadClick: () -> Unit,
    onArtworkClick: (Artwork) -> Unit,
    viewModel: ArtworkViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Artwork") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onUploadClick,
                containerColor = Color(0xFF6200EE)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add artwork",
                    tint = Color.White
                )
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    // Empty state
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
                    // Grid of artworks
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.artworks) { artwork ->
                            ArtworkCard(
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
}

@Composable
fun ArtworkCard(
    artwork: Artwork,
    onClick: () -> Unit
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
        Column {
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

            // Name
            Text(
                text = artwork.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}