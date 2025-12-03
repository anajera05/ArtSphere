package com.example.artsphere.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.artsphere.ui.artworks.gallery.GalleryViewModel
import com.example.artsphere.ui.artworks.savedArtworks.SavedArtworkViewModel
import com.example.artsphere.data.model.Artwork
import com.example.artsphere.data.source.remote.NewsViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onArtworkClick: (Artwork) -> Unit = {},
    savedViewModel: SavedArtworkViewModel = viewModel()
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }  //  Add search state
    val filters = listOf("All", "Painting & Drawing", "Photographic", "Digital", "Other")

    val galleryViewModel: GalleryViewModel = viewModel()
    val galleryState by galleryViewModel.uiState.collectAsState()

    // Use the passed-in savedViewModel instead of creating a new one
    val savedState by savedViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Search bar - NOW FUNCTIONAL
        OutlinedTextField(
            value = searchQuery,  // ⭐ Bind to state
            onValueChange = { searchQuery = it },  // ⭐ Update on change
            placeholder = { Text("Search Artsphere", color = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    // Show X button to clear search
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = Color.Gray,
                        modifier = Modifier.clickable { searchQuery = "" }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            singleLine = true  // Keep it single line
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Trending Art News Section
            item(span = { GridItemSpan(maxLineSpan) }) {
                val newsViewModel: NewsViewModel = viewModel()
                val uiState by newsViewModel.uiState.collectAsState()
                val context = LocalContext.current

                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trending Art News",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh news",
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { newsViewModel.loadNews() }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    when {
                        uiState.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        uiState.error != null -> {
                            Text(
                                text = "Error: ${uiState.error}",
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        else -> {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(uiState.articles) { article ->
                                    Card(
                                        modifier = Modifier
                                            .width(200.dp)
                                            .height(180.dp)
                                            .clickable {
                                                val intent = Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse(article.url)
                                                )
                                                context.startActivity(intent)
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(
                                                0xFFE0E0E0
                                            )
                                        )
                                    ) {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            article.imageUrl?.let { imageUrl ->
                                                AsyncImage(
                                                    model = imageUrl,
                                                    contentDescription = article.title,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .weight(1f),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            Text(
                                                text = article.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Filter Chips Header - Purple themed like profile page
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5))
                        .padding(vertical = 4.dp)
                ) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filters) { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE8DEF8),  // Purple background
                                    selectedLabelColor = Color(0xFF6200EE),      // Purple text
                                    containerColor = Color.White,
                                    labelColor = Color.Gray
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedFilter == filter,
                                    borderColor = if (selectedFilter == filter) Color(0xFF6200EE) else Color.LightGray,
                                    selectedBorderColor = Color(0xFF6200EE)
                                )
                            )
                        }
                    }
                }
            }

            // Artwork Gallery from Firebase
            when {
                galleryState.isLoading -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF6200EE))
                        }
                    }
                }

                galleryState.artworks.isEmpty() -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No artworks available yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    }
                }

                else -> {
                    // Filter artworks based on selected category AND search query
                    val filteredArtworks = galleryState.artworks
                        .filter { artwork ->
                            // Category filter
                            val matchesCategory = if (selectedFilter == "All") {
                                true
                            } else {
                                artwork.categoryEnum.displayName == selectedFilter
                            }

                            // Search filter (case-insensitive)
                            val matchesSearch = if (searchQuery.isBlank()) {
                                true
                            } else {
                                artwork.name.contains(searchQuery, ignoreCase = true)
                            }

                            // Both conditions must be true
                            matchesCategory && matchesSearch
                        }

                    // Show message if no results found
                    if (filteredArtworks.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "No artworks found",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        if (searchQuery.isNotBlank()) {
                                            "Try a different search term"
                                        } else {
                                            "No artworks in this category"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredArtworks) { artwork ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clickable { onArtworkClick(artwork) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        // Artwork Image
                                        AsyncImage(
                                            model = artwork.imageUrl,
                                            contentDescription = artwork.name,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f),
                                            contentScale = ContentScale.Crop
                                        )

                                        // Artwork Name
                                        Text(
                                            text = artwork.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }

                                    // Like Button (top right corner)
                                    IconButton(
                                        onClick = {
                                            savedViewModel.toggleSaveArtwork(artwork.id)
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .background(
                                                Color.White.copy(alpha = 0.8f),
                                                shape = RoundedCornerShape(50)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = if (savedState.savedArtworkIds.contains(
                                                    artwork.id
                                                )
                                            ) {
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
                        }
                    }
                }
            }
        }
    }
}