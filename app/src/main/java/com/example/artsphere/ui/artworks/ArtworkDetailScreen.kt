package com.example.artsphere.ui.artworks

import android.util.Log
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.artsphere.ui.artworks.gallery.GalleryViewModel
import com.example.artsphere.ui.artworks.savedArtworks.SavedArtworkViewModel
import com.example.artsphere.ui.profile.ArtistProfileSheet
import com.example.artsphere.data.model.Artwork
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtworkDetailScreen(
    artwork: Artwork,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMessageClick: () -> Unit = {},
    onNavigateToArtwork: (Artwork) -> Unit = {},
    viewModel: ArtworkViewModel = viewModel(),
    galleryViewModel: GalleryViewModel = viewModel(),
    savedViewModel: SavedArtworkViewModel = viewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showArtistProfile by remember { mutableStateOf(false) }
    val savedState by savedViewModel.uiState.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isOwnArtwork = currentUserId == artwork.userId

    // Live artwork data
    var currentArtwork by remember { mutableStateOf(artwork) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()


    // Function to reload artwork from Firestore
    suspend fun reloadArtwork() {
        isLoading = true
        try {
            val doc = FirebaseFirestore.getInstance()
                .collection("artworks")
                .document(artwork.id)
                .get()
                .await()

            doc.toObject(Artwork::class.java)?.let {
                currentArtwork = it.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("ARTWORK_DETAIL", "Error reloading artwork", e)
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artwork") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isOwnArtwork) {
                        // Hide/Show toggle
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    galleryViewModel.toggleHiddenStatus(currentArtwork.id, currentArtwork.isHidden)
                                    delay(500)
                                    reloadArtwork()
                                }
                            },
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = if (currentArtwork.isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (currentArtwork.isHidden) "Hidden" else "Visible",
                                tint = if (currentArtwork.isHidden) Color(0xFFE91E63) else Color.White
                            )
                        }

                        // Delete button
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
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
                // Hidden badge
                if (isOwnArtwork && currentArtwork.isHidden) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "This artwork is hidden from others",
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Artist info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isOwnArtwork) {
                            if (!isOwnArtwork) {
                                showArtistProfile = true
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = currentArtwork.contactName, fontWeight = FontWeight.Bold)
                        Text(
                            text = currentArtwork.contactEmail,
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.Underline
                        )
                        if (!isOwnArtwork) {
                            Text(
                                text = "Tap to view profile",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6200EE),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Artwork Image with SOLD badge
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = currentArtwork.imageUrl,
                        contentDescription = currentArtwork.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentScale = ContentScale.Crop
                    )

                    // SOLD badge
                    if (currentArtwork.isSold) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp),
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
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentArtwork.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFE8DEF8),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = currentArtwork.categoryEnum.displayName,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF6200EE),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Like button
                            IconButton(
                                onClick = {
                                    savedViewModel.toggleSaveArtwork(currentArtwork.id)
                                },
                                modifier = Modifier
                                    .background(
                                        Color.White.copy(alpha = 0.8f),
                                        shape = RoundedCornerShape(50)
                                    )
                            ) {
                                Icon(
                                    imageVector = if (savedState.savedArtworkIds.contains(currentArtwork.id)) {
                                        Icons.Filled.Favorite
                                    } else {
                                        Icons.Filled.FavoriteBorder
                                    },
                                    contentDescription = "Like",
                                    tint = if (savedState.savedArtworkIds.contains(currentArtwork.id)) {
                                        Color(0xFFE91E63)
                                    } else {
                                        Color(0xFF6200EE)
                                    }
                                )
                            }

                            // Message Artist Button
                            if (!isOwnArtwork) {
                                IconButton(
                                    onClick = onMessageClick,
                                    modifier = Modifier
                                        .background(
                                            Color(0xFF6200EE),
                                            shape = RoundedCornerShape(50)
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Message Artist",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Mark as Sold button (owner only)
                    if (isOwnArtwork) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    galleryViewModel.toggleSoldStatus(currentArtwork.id, currentArtwork.isSold)
                                    delay(500)
                                    reloadArtwork()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentArtwork.isSold) Color(0xFF4CAF50) else Color(0xFFE91E63)
                            ),
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = if (currentArtwork.isSold) Icons.Default.CheckCircle else Icons.Default.Sell,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
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
                                color = Color(0xFF6200EE)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(currentArtwork.description)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    if (currentArtwork.price.isNotBlank()) {
                        Row {
                            Text(
                                text = "Price",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6200EE)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$${currentArtwork.price}")
                        }
                    } else {
                        Row {
                            Text(
                                text = "Price",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6200EE)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Contact for price")
                        }
                    }
                }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Artwork") },
                        text = { Text("Are you sure you want to delete \"${currentArtwork.name}\"? This action cannot be undone.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteArtwork(currentArtwork.id)
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

            // Loading overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6200EE))
                }
            }
        }
    }

    // Artist Profile Sheet
    if (showArtistProfile) {
        ArtistProfileSheet(
            userId = currentArtwork.userId,
            userName = currentArtwork.contactName.ifBlank { "Artist" },
            onDismiss = { showArtistProfile = false },
            onArtworkClick = { selectedArtwork ->
                showArtistProfile = false
                onNavigateToArtwork(selectedArtwork)
            }
        )
    }
}