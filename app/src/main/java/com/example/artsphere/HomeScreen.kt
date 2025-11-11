package com.example.artsphere

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.artsphere.features.NewsViewModel
import com.example.artsphere.ui.theme.ArtSphereTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Sculptures", "Photography", "Digital", "Paintings")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // This search bar is not part of the grid and will remain at the top
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Search Artsphere", color = Color.Gray) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
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
            )
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
                val viewModel: NewsViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                val context = LocalContext.current

                Column(modifier = Modifier.padding(bottom = 16.dp)) {
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
                                .clickable { viewModel.loadNews() }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    when {
                        uiState.isLoading -> {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                                                context.startActivity(intent)
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0))
                                    ) {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            article.imageUrl?.let { imageUrl ->
                                                AsyncImage(
                                                    model = imageUrl,
                                                    contentDescription = article.title,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .weight(1f),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
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

            // Sticky Filter Chips Header
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5))
                        .padding(vertical = 8.dp)
                ) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filters) { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) },
                                leadingIcon = if (selectedFilter == filter) {
                                    { Icon(imageVector = Icons.Default.Close, contentDescription = "Remove filter", modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE8DEF8),
                                    selectedLabelColor = Color(0xFF6750A4)
                                )
                            )
                        }
                    }
                }
            }

            // Spacer for visual separation after sticky header
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Artwork Gallery
            items(20) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Artwork ${index + 1}", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Home Screen")
@Composable
fun HomeScreenPreview() {
    ArtSphereTheme {
        HomeScreen()
    }
}

