package com.example.artsphere.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.artsphere.data.model.Artwork
import com.example.artsphere.data.source.remote.NewsViewModel
import com.example.artsphere.ui.artworks.gallery.GalleryViewModel
import com.example.artsphere.ui.artworks.savedArtworks.SavedArtworkViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onArtworkClick: (Artwork) -> Unit = {},
    savedViewModel: SavedArtworkViewModel = viewModel(),
    galleryViewModel: GalleryViewModel = viewModel()
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    val filters = listOf("All", "Painting & Drawing", "Photographic", "Digital", "Other")

    // val galleryViewModel: GalleryViewModel = viewModel()
    val galleryState by galleryViewModel.uiState.collectAsState()
    val savedState by savedViewModel.uiState.collectAsState()

    // Animated title
    var titleVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        titleVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Cute Header with Gradient
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF6200EE),
                                Color(0xFF8E24AA),
                                Color(0xFFAB47BC)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    // Animated Title
                    AnimatedVisibility(
                        visible = titleVisible,
                        enter = fadeIn(animationSpec = tween(800)) +
                                slideInVertically(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Sparkle emoji with rotation
                            var rotation by remember { mutableStateOf(0f) }
                            LaunchedEffect(Unit) {
                                while (true) {
                                    animate(
                                        initialValue = 0f,
                                        targetValue = 360f,
                                        animationSpec = tween(3000, easing = LinearEasing)
                                    ) { value, _ ->
                                        rotation = value
                                    }
                                }
                            }

                            Text(
                                text = " 🎨",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.rotate(rotation)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Discover Art",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(28.dp)),
                        placeholder = {
                            Text("Search for art...", color = Color.Gray)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF6200EE)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF6200EE)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = Color(0xFF6200EE)
                        ),
                        shape = RoundedCornerShape(28.dp),
                        singleLine = true
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
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
                            text = " Trending Art News",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6200EE)
                        )
                        IconButton(onClick = { newsViewModel.loadNews() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Color(0xFF6200EE)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    when {
                        uiState.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF6200EE))
                            }
                        }

                        uiState.error != null -> {
                            Text(
                                text = "Error loading news",
                                color = Color.Gray,
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
                                        elevation = CardDefaults.cardElevation(4.dp)
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
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Filter Chips - Scrollable Horizontal
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA))
                        .padding(vertical = 8.dp)
                ) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filters) { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6200EE),
                                    selectedLabelColor = Color.White,
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

            // Artwork Gallery
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎨", style = MaterialTheme.typography.displayLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No artworks yet", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                else -> {
                    val filteredArtworks = galleryState.artworks.filter { artwork ->
                        val matchesCategory = selectedFilter == "All" ||
                                artwork.categoryEnum.displayName == selectedFilter
                        val matchesSearch = searchQuery.isBlank() ||
                                artwork.name.contains(searchQuery, ignoreCase = true)
                        matchesCategory && matchesSearch
                    }

                    if (filteredArtworks.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🔍", style = MaterialTheme.typography.displayMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No results found", color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(filteredArtworks) { artwork ->
                            var cardVisible by remember { mutableStateOf(false) }

                            LaunchedEffect(Unit) {
                                delay(filteredArtworks.indexOf(artwork) * 50L)
                                cardVisible = true
                            }

                            AnimatedVisibility(
                                visible = cardVisible,
                                enter = fadeIn() + scaleIn(
                                    initialScale = 0.8f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy
                                    )
                                )
                            ) {
                                CuteArtworkCard(
                                    artwork = artwork,
                                    isSaved = savedState.savedArtworkIds.contains(artwork.id),
                                    onArtworkClick = { onArtworkClick(artwork) },
                                    onLikeClick = { savedViewModel.toggleSaveArtwork(artwork.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CuteArtworkCard(
    artwork: Artwork,
    isSaved: Boolean,
    onArtworkClick: () -> Unit,
    onLikeClick: () -> Unit
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

            // SOLD badge (top left)
            if (artwork.isSold) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
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

            // Like Button (top right)
            IconButton(
                onClick = onLikeClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(40.dp)
                    .scale(heartScale)
                    .shadow(4.dp, CircleShape)
                    .background(Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isSaved) Color(0xFFE91E63) else Color(0xFF424242),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Info at bottom
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

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}