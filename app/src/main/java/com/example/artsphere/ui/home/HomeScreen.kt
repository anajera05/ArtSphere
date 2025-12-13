package com.example.artsphere.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.artsphere.data.model.Artwork
import com.example.artsphere.data.model.ArtworkCategory
import com.example.artsphere.data.source.remote.Article
import com.example.artsphere.data.source.remote.NewsUiState
import com.example.artsphere.data.source.remote.NewsViewModel
import com.example.artsphere.ui.artworks.gallery.GalleryUiState
import com.example.artsphere.ui.artworks.gallery.GalleryViewModel
import com.example.artsphere.ui.artworks.savedArtworks.SavedArtworkUiState
import com.example.artsphere.ui.artworks.savedArtworks.SavedArtworkViewModel
import com.example.artsphere.ui.profile.ProfileUiState
import com.example.artsphere.ui.profile.ProfileViewModel
import com.example.artsphere.ui.theme.ArtSphereTheme
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onArtworkClick: (Artwork) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onArticleClick: (Article) -> Unit = {},
    savedViewModel: SavedArtworkViewModel = viewModel(),
    galleryViewModel: GalleryViewModel = viewModel(),
    newsViewModel: NewsViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
) {
    val galleryState by galleryViewModel.uiState.collectAsState()
    val savedState by savedViewModel.uiState.collectAsState()
    val newsUiState by newsViewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()

    // Refresh profile photo when screen opens
    LaunchedEffect(Unit) {
        profileViewModel.refreshPhotoFromFirebase()
    }

    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    val filters = listOf("All", "Painting & Drawing", "Photographic", "Digital", "Other")

    HomeContent(
        modifier = modifier,
        galleryState = galleryState,
        savedState = savedState,
        newsUiState = newsUiState,
        profileState = profileState,
        searchQuery = searchQuery,
        onSearchQueryChange = { searchQuery = it },
        selectedFilter = selectedFilter,
        onFilterChange = { selectedFilter = it },
        filters = filters,
        onArtworkClick = onArtworkClick,
        onLikeClick = { savedViewModel.toggleSaveArtwork(it) },
        onRefreshNews = { newsViewModel.loadNews() },
        onProfileClick = onProfileClick,
        onMessagesClick = onMessagesClick,
                onArticleClick = onArticleClick
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    galleryState: GalleryUiState,
    savedState: SavedArtworkUiState,
    newsUiState: NewsUiState,
    profileState: ProfileUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    filters: List<String>,
    onArtworkClick: (Artwork) -> Unit,
    onLikeClick: (String) -> Unit,
    onRefreshNews: () -> Unit,
    onProfileClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onArticleClick: (Article) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    ),
                    start = Offset(0f, Float.POSITIVE_INFINITY),
                    end = Offset(Float.POSITIVE_INFINITY, 0f)
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                // Profile Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onProfileClick)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .border(2.dp, Color(0xFF2196F3), CircleShape), // Blue border
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                profileState.isUploading -> {
                                    CircularProgressIndicator(
                                        color = Color(0xFF6200EE),
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                                profileState.photoUrl != null -> {
                                    AsyncImage(
                                        model = profileState.photoUrl,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                else -> {
                                    Icon(
                                        Icons.Filled.Person,
                                        contentDescription = "Default profile icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Welcome to ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "ArtSphere",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Chat Icon
                    IconButton(onClick = onMessagesClick) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Messages",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(28.dp)),
                    placeholder = {
                        Text("Search Artsphere", color = Color.Gray)
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = MaterialTheme.colorScheme.onSecondary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(28.dp),
                    singleLine = true
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
                    .padding(top = 12.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Trending Art News Section (Original Style)
                    item(span = { GridItemSpan(maxLineSpan) }) {
//                        val context = LocalContext.current

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
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                IconButton(onClick = onRefreshNews) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            if (newsUiState.isLoading) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            } else if (newsUiState.error != null) {
                                Text(text = "Error: ${newsUiState.error}", modifier = Modifier.padding(16.dp))
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(newsUiState.articles) { article ->
                                        Card(
                                            modifier = Modifier
                                                .width(200.dp)
                                                .height(180.dp)
                                                .clickable {
                                                    onArticleClick(article)
                                                },
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(4.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = MaterialTheme.colorScheme.onSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Filter Chips (Original Style)
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
                                        onClick = { onFilterChange(filter) },
                                        label = { Text(filter) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White,
                                            containerColor = Color.White,
                                            labelColor = MaterialTheme.colorScheme.secondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = selectedFilter == filter,
                                            borderColor = if (selectedFilter == filter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                            selectedBorderColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Artwork Gallery (Original Style)
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
                                // Correct way to animate items in a lazy grid
                                itemsIndexed(filteredArtworks) { index, artwork ->
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
                                        CuteArtworkCard(
                                            artwork = artwork,
                                            isSaved = savedState.savedArtworkIds.contains(artwork.id),
                                            onArtworkClick = { onArtworkClick(artwork) },
                                            onLikeClick = { onLikeClick(artwork.id) }
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

// Restored Original CuteArtworkCard
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
            .shadow(8.dp, RoundedCornerShape(20.dp)), // Original larger shadow
        shape = RoundedCornerShape(20.dp), // Original corner radius
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
                    .size(40.dp) // Original size
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
//
//@Preview(showBackground = true)
//@Composable
//fun HomeScreenPreview() {
//    ArtSphereTheme {
//        HomeContent(
//            galleryState = GalleryUiState(
//                artworks = listOf(
//                    Artwork(
//                        id = "1",
//                        name = "Starry Night",
//                        category = ArtworkCategory.PAINTING_DRAWING.name,
//                        price = "1000",
//                        imageUrl = "https://example.com/image.jpg"
//                    ),
//                    Artwork(
//                        id = "2",
//                        name = "Mona Lisa",
//                        category = ArtworkCategory.PAINTING_DRAWING.name,
//                        price = "2000",
//                        imageUrl = "https://example.com/image2.jpg",
//                        isSold = true
//                    )
//                )
//            ),
//            savedState = SavedArtworkUiState(
//                savedArtworkIds = setOf("1")
//            ),
//            newsUiState = NewsUiState(
//                articles = listOf(
//                    Article(
//                        title = "New Art Exhibition in NYC",
//                        url = "https://nytimes.com",
//                        imageUrl = null
//                    )
//                )
//            ),
//            profileState = ProfileUiState(),
//            searchQuery = "",
//            onSearchQueryChange = {},
//            selectedFilter = "All",
//            onFilterChange = {},
//            filters = listOf("All", "Painting & Drawing", "Photographic", "Digital", "Other"),
//            onArtworkClick = {},
//            onLikeClick = {},
//            onRefreshNews = {},
//            onProfileClick = {},
//            onMessagesClick = {}
//        )
//    }
//}
