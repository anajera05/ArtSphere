package com.example.artsphere.ui.artworks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.artsphere.data.model.Artwork
import com.example.artsphere.data.model.ArtworkCategory
import com.example.artsphere.ui.artworks.gallery.GalleryViewModel
import com.example.artsphere.ui.artworks.savedArtworks.SavedArtworkUiState
import com.example.artsphere.ui.artworks.savedArtworks.SavedArtworkViewModel
import com.example.artsphere.ui.profile.ArtistProfileSheet
import com.example.artsphere.ui.theme.ArtSphereTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ArtworkDetailScreen(
    artwork: Artwork,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMessageClick: () -> Unit,
    onNavigateToArtwork: (Artwork) -> Unit,
    galleryViewModel: GalleryViewModel,
    savedViewModel: SavedArtworkViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val savedState by savedViewModel.uiState.collectAsState()
    val galleryState by galleryViewModel.uiState.collectAsState()

    var currentArtwork by remember(artwork, galleryState) {
        mutableStateOf(galleryState.artworks.find { it.id == artwork.id } ?: artwork)
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showArtistProfile by remember { mutableStateOf(false) } // State for profile sheet
    val isOwnArtwork = currentArtwork.userId == FirebaseAuth.getInstance().currentUser?.uid
    val isLoading = galleryState.isLoading

    fun reloadArtwork() {
        val refreshedArtwork = galleryViewModel.uiState.value.artworks.find { it.id == artwork.id }
        if (refreshedArtwork != null) {
            currentArtwork = refreshedArtwork
        }
    }

    ArtworkDetailContent(
        currentArtwork = currentArtwork,
        savedState = savedState,
        isOwnArtwork = isOwnArtwork,
        isLoading = isLoading,
        showDeleteDialog = showDeleteDialog,
        onShowDeleteDialogChange = { showDeleteDialog = it },
        showArtistProfile = showArtistProfile,
        onShowArtistProfileChange = { showArtistProfile = it }, // Pass state and callback
        onBackClick = onBackClick,
        onLikeClick = { savedViewModel.toggleSaveArtwork(currentArtwork.id) },
        onMessageClick = onMessageClick,
        onNavigateToArtwork = onNavigateToArtwork,
        onToggleHidden = {
            coroutineScope.launch {
                galleryViewModel.toggleHiddenStatus(currentArtwork.id, currentArtwork.isHidden)
                delay(500)
                reloadArtwork()
            }
        },
        onMarkAsSold = {
            coroutineScope.launch {
                galleryViewModel.toggleSoldStatus(currentArtwork.id, currentArtwork.isSold)
                delay(500)
                reloadArtwork()
            }
        },
        onDeleteConfirm = onDeleteClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtworkDetailContent(
    currentArtwork: Artwork,
    savedState: SavedArtworkUiState,
    isOwnArtwork: Boolean,
    isLoading: Boolean,
    showDeleteDialog: Boolean,
    onShowDeleteDialogChange: (Boolean) -> Unit,
    showArtistProfile: Boolean,
    onShowArtistProfileChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onLikeClick: () -> Unit,
    onMessageClick: () -> Unit,
    onNavigateToArtwork: (Artwork) -> Unit,
    onToggleHidden: () -> Unit,
    onMarkAsSold: () -> Unit,
    onDeleteConfirm: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artwork") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (isOwnArtwork) {
                        IconButton(onClick = onToggleHidden, enabled = !isLoading) {
                            Icon(
                                imageVector = if (currentArtwork.isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (currentArtwork.isHidden) "Hidden" else "Visible",
                                tint = if (currentArtwork.isHidden) Color(0xFFE91E63) else Color.White
                            )
                        }
                        IconButton(onClick = { onShowDeleteDialogChange(true) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
            ) {
                if (isOwnArtwork && currentArtwork.isHidden) {
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFFFEBEE)) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("This artwork is hidden from others", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Artist info row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isOwnArtwork) { onShowArtistProfileChange(true) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {
                        Text(text = currentArtwork.contactName, fontWeight = FontWeight.Bold)
                        Text(text = currentArtwork.contactEmail, style = MaterialTheme.typography.bodySmall, textDecoration = TextDecoration.Underline)

                    }
                    if (!isOwnArtwork) {
                        IconButton(
                            onClick = onMessageClick,
                            modifier = Modifier.background(MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(50))
                        ) {
                            Icon(imageVector = Icons.Default.Email, contentDescription = "Message Artist", tint = Color.White)
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = currentArtwork.imageUrl,
                        contentDescription = currentArtwork.name,
                        modifier = Modifier.fillMaxWidth().height(400.dp),
                        contentScale = ContentScale.Crop
                    )
                    if (currentArtwork.isSold) {
                        Surface(
                            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE91E63),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = "SOLD",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color =MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = currentArtwork.categoryEnum.displayName,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(text = currentArtwork.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                        }
                            IconButton(
                                onClick = onLikeClick,
                                modifier = Modifier.background(Color.White.copy(alpha = 0.8f), shape = RoundedCornerShape(50))
                            ) {
                                Icon(
                                    imageVector = if (savedState.savedArtworkIds.contains(currentArtwork.id)) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (savedState.savedArtworkIds.contains(currentArtwork.id)) Color(0xFFE91E63) else Color(0xFF6200EE)
                                )
                            }


                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isOwnArtwork) {
                        Button(
                            onClick = onMarkAsSold,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = if (currentArtwork.isSold) Color(0xFF4CAF50) else Color(0xFFE91E63)),
                            enabled = !isLoading
                        ) {
                            Icon(imageVector = if (currentArtwork.isSold) Icons.Default.CheckCircle else Icons.Default.Sell, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (currentArtwork.isSold) "Mark as Available" else "Mark as Sold")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (currentArtwork.description.isNotBlank()) {
                        Row {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(currentArtwork.description)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))


                        Row {
                            Text(
                                text = "Price",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            if (currentArtwork.price.isNotBlank()) {
                            Text("$${currentArtwork.price}")}
                            else {
                                    Text("Contact for price")
                                }
                            }
                        }
                    }
                }

            }

            // Show Artist Profile Sheet
            if (showArtistProfile) {
                ArtistProfileSheet(
                    userId = currentArtwork.userId,
                    userName = currentArtwork.contactName,
                    onDismiss = { onShowArtistProfileChange(false) },
                    onArtworkClick = {
                        onShowArtistProfileChange(false)
                        onNavigateToArtwork(it)
                    }
                )
            }

            // Delete confirmation dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { onShowDeleteDialogChange(false) },
                    title = { Text(text ="Delete Artwork", color = MaterialTheme.colorScheme.onSecondary) },
                    text = { Text("Are you sure you want to permanently delete this artwork? This action cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onShowDeleteDialogChange(false)
                                onDeleteConfirm()
                            }
                        ) {
                            Text("Delete", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { onShowDeleteDialogChange(false) }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }



@Preview(showBackground = true, name = "Personal Detail")
@Composable
fun ArtworkDetailScreenPreview() {
    ArtSphereTheme {
        ArtworkDetailContent(
            currentArtwork = Artwork(
                id = "1",
                name = "Starry Night",
                category = ArtworkCategory.PAINTING_DRAWING.name,
                price = "1000",
                imageUrl = "",
                description = "A famous painting by Vincent van Gogh.",
                contactName = "Vincent van Gogh",
                contactEmail = "vincent@example.com",
                isSold = true
            ),
            savedState = SavedArtworkUiState(savedArtworkIds = setOf("1")),
            isOwnArtwork = true,
            isLoading = false,
            showDeleteDialog = false,
            onShowDeleteDialogChange = {},
            showArtistProfile = false,
            onShowArtistProfileChange = {},
            onBackClick = {},
            onLikeClick = {},
            onMessageClick = {},
            onNavigateToArtwork = {},
            onToggleHidden = {},
            onMarkAsSold = {},
            onDeleteConfirm = {}
        )
    }
}

@Preview(showBackground = true, name = "Not Owned Detail")
@Composable
fun ArtworkDetailScreenNotOwnedPreview() {
    ArtSphereTheme {
        ArtworkDetailContent(
            currentArtwork = Artwork(
                id = "2",
                name = "The Kiss",
                category = ArtworkCategory.PAINTING_DRAWING.name,
                price = "2500",
                imageUrl = "",
                description = "An iconic painting by Gustav Klimt.",
                contactName = "Gustav Klimt",
                contactEmail = "gustav@example.com",
                isSold = false
            ),
            savedState = SavedArtworkUiState(savedArtworkIds = emptySet()),
            isOwnArtwork = false,
            isLoading = false,
            showDeleteDialog = false,
            onShowDeleteDialogChange = {},
            showArtistProfile = true,
            onShowArtistProfileChange = {},
            onBackClick = {},
            onLikeClick = {},
            onMessageClick = {},
            onNavigateToArtwork = {},
            onToggleHidden = {},
            onMarkAsSold = {},
            onDeleteConfirm = {}
        )
    }
}
