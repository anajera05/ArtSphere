package com.example.artsphere

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtworkDetailScreen(
    artwork: Artwork,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    viewModel: ArtworkViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    savedViewModel: SavedArtworkViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val savedState by savedViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artwork") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)

            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = artwork.contactName, fontWeight = FontWeight.Bold)
                    Text(
                        text = artwork.contactEmail,
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = TextDecoration.Underline
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Black
                    )
                }
            }

            AsyncImage(
                model = artwork.imageUrl,
                contentDescription = artwork.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = artwork.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        // Category Chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFE8DEF8),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = artwork.categoryEnum.displayName,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6200EE),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Row {
                        IconButton(
                            onClick = {
                                Log.d("DETAIL_SCREEN", "Like button clicked for artwork: ${artwork.id}")
                                Log.d("DETAIL_SCREEN", "Current saved state: ${savedState.savedArtworkIds.contains(artwork.id)}")
                                savedViewModel.toggleSaveArtwork(artwork.id)
                                Log.d("DETAIL_SCREEN", "After toggle: ${savedState.savedArtworkIds.contains(artwork.id)}")
                            },
                            modifier = Modifier
                                .padding(8.dp)
                                .background(
                                    Color.White.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(50)
                                )
                        ) {
                            Icon(
                                imageVector = if (savedState.savedArtworkIds.contains(artwork.id)) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Filled.FavoriteBorder
                                },
                                contentDescription = "Like",
                                tint = if (savedState.savedArtworkIds.contains(artwork.id)) {
                                    Color(0xFFE91E63)  // Pink when liked
                                } else {
                                    Color(0xFF6200EE)  // Purple when not liked
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                if (artwork.description.isNotBlank()) {
                    Row() {
                        Text(text = "Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,  color = Color(0xFF6200EE),)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(artwork.description)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                if (artwork.price.isNotBlank()) {
                    Row() {
                        Text(text = "Price", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,  color = Color(0xFF6200EE),)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(artwork.price)
                    }
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Artwork") },
                    text = { Text("Are you sure you want to delete \"${artwork.name}\"? This action cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteArtwork(artwork.id)
                                showDeleteDialog = false
                                onDeleteClick()
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.Red
                            )
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}



