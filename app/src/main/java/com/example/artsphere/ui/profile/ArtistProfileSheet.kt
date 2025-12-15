package com.example.artsphere.ui.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.artsphere.data.model.Artwork
import com.example.artsphere.ui.components.ArtworkCard
import com.example.artsphere.ui.theme.ArtSphereTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistProfileSheet(
    userId: String,
    userName: String,
    onDismiss: () -> Unit,
    onArtworkClick: (Artwork) -> Unit
) {
    var artworks by remember { mutableStateOf<List<Artwork>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    // Load artist's artworks and profile picture
    LaunchedEffect(userId) {
        try {
            val db = FirebaseFirestore.getInstance()

            // Get artworks
            val artworkSnapshot = db.collection("artworks")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            artworks = artworkSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Artwork::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.createdAt }

            // Try to get profile picture from Firebase Storage
            try {
                val storageRef = FirebaseStorage.getInstance("gs://artsphere-android.firebasestorage.app")
                    .reference
                    .child("profile_photos")
                    .child("$userId/profile.jpg")

                profileImageUrl = storageRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                // No profile picture found - that's okay
                profileImageUrl = null
            }

            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    ArtistProfileSheetContent(
        userName = userName,
        artworks = artworks,
        isLoading = isLoading,
        profileImageUrl = profileImageUrl,
        onDismiss = onDismiss,
        onArtworkClick = onArtworkClick
    )
}

@Composable
fun ArtistProfileSheetContent(
    userName: String,
    artworks: List<Artwork>,
    isLoading: Boolean,
    profileImageUrl: String?,
    onDismiss: () -> Unit,
    onArtworkClick: (Artwork) -> Unit
) {
    // Animated dialog with slide up effect
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        // Background overlay with fade animation
        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            visible = true
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(onClick = onDismiss)
            )
        }

        // Bottom sheet with slide up animation
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = false) {} // Prevent clicks from passing through
            ) {
                Spacer(modifier = Modifier.weight(0.3f))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.7f),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 24.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Handle bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp),
                                shape = RoundedCornerShape(2.dp),
                                color = Color.Gray.copy(alpha = 0.3f)
                            ) {}
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Profile picture with scale animation
                                var profileVisible by remember { mutableStateOf(false) }

                                LaunchedEffect(Unit) {
                                    kotlinx.coroutines.delay(200)
                                    profileVisible = true
                                }

                                AnimatedVisibility(
                                    visible = profileVisible,
                                    enter = scaleIn(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy
                                        )
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    ) {
                                        if (profileImageUrl != null) {
                                            AsyncImage(
                                                model = profileImageUrl,
                                                contentDescription = "Profile",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.LightGray),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(48.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = userName,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${artworks.size} artworks",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }

                                IconButton(onClick = onDismiss) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        // Artworks grid
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.White
                                ),
                            contentAlignment = Alignment.Center
                        ){
                            if (isLoading) {
                                CircularProgressIndicator(color = Color(0xFF6200EE))
                            } else if (artworks.isEmpty()) {
                                Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "🎨",
                                            style = MaterialTheme.typography.displayMedium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "No artworks yet",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.Gray
                                        )

                                }
                            } else {

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()

                                ) {
                                    items(artworks) { artwork ->
                                        ArtworkCard(
                                            artwork = artwork,
                                            isSaved = false,
                                            onArtworkClick = { onArtworkClick(artwork) },
                                            onLikeClick = {},
                                            isProfile = true
                                        )
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(0.8f)
                                                .clickable { onArtworkClick(artwork) },
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                        ) {
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                AsyncImage(
                                                    model = artwork.imageUrl,
                                                    contentDescription = artwork.name,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )

                                                // Gradient overlay at bottom
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomCenter)
                                                        .fillMaxWidth()
                                                        .height(60.dp)
                                                        .background(
                                                            Brush.verticalGradient(
                                                                colors = listOf(
                                                                    Color.Transparent,
                                                                    Color.Black.copy(alpha = 0.7f)
                                                                )
                                                            )
                                                        )
                                                )

                                                Text(
                                                    text = artwork.name,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    maxLines = 1,
                                                    modifier = Modifier
                                                        .align(Alignment.BottomStart)
                                                        .padding(12.dp)
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
    }
}

@Preview(showBackground = true)
@Composable
fun ArtistProfileSheetPreview() {
    ArtSphereTheme {
        ArtistProfileSheetContent(
            userName = "Artist Name",
            artworks = listOf(
                Artwork(id = "1", name = "Artwork 1", imageUrl = ""),
                Artwork(id = "2", name = "Artwork 2", imageUrl = ""),
                Artwork(id = "3", name = "Artwork 3", imageUrl = "")
            ),
            isLoading = false,
            profileImageUrl = null,
            onDismiss = {},
            onArtworkClick = {}
        )
    }
}
