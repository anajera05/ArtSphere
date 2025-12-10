package com.example.artsphere.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.artsphere.data.model.Artwork
import com.example.artsphere.ui.artworks.ArtworkUiState
import com.example.artsphere.ui.artworks.ArtworkViewModel
import com.example.artsphere.ui.artworks.myArtworks.MyArtworkScreen
import com.example.artsphere.ui.artworks.savedArtworks.SavedArtworkScreen
import com.example.artsphere.ui.artworks.savedArtworks.SavedArtworkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    profileViewModel: ProfileViewModel,
    savedArtworkViewModel: SavedArtworkViewModel,
    artViewModel: ArtworkViewModel,
    onUploadClick: () -> Unit,
    onArtworkClick: (Artwork) -> Unit
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val artworkUiState by artViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.refreshPhotoFromFirebase()
    }

    ProfileScreenContent(
        modifier = modifier,
        profileUiState = uiState,
        artworkUiState = artworkUiState,
        savedArtworkViewModel = savedArtworkViewModel,
        artViewModel = artViewModel,
        onNavigateToUpload = { navController.navigate("upload_artwork") },
        onNavigateToSettings = { navController.navigate("settings") },
        onUploadProfilePhoto = { uri -> profileViewModel.uploadProfilePhoto(uri) },
        onUploadClick = onUploadClick,
        onArtworkClick = onArtworkClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    profileUiState: ProfileUiState,
    artworkUiState: ArtworkUiState,
    savedArtworkViewModel: SavedArtworkViewModel? = null,
    artViewModel: ArtworkViewModel? = null,
    onNavigateToUpload: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onUploadProfilePhoto: (Uri) -> Unit,
    onUploadClick: () -> Unit,
    onArtworkClick: (Artwork) -> Unit
) {
    var state by remember { mutableIntStateOf(0) }
    val titles = listOf("Shop", "Saved")
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch(_: Exception) {}
            onUploadProfilePhoto(it)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ){
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "My Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    IconButton(
                        onClick = onNavigateToUpload,
                    ){
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                    ){
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ){
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable { pickImageLauncher.launch(arrayOf("image/*")) },
                ){
                    when {
                        profileUiState.isUploading -> {
                            CircularProgressIndicator(
                                color = Color(0xFF6200EE),
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        profileUiState.photoUrl != null -> {
                            AsyncImage(
                                model = profileUiState.photoUrl,
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = "Default profile icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                    }
                }


                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = artworkUiState.artworks.size.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("artworks", style = MaterialTheme.typography.bodySmall)
                }


            }
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                SecondaryTabRow(
                    selectedTabIndex = state,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    titles.forEachIndexed { index, title ->
                        Tab(
                            selected = state == index,
                            onClick = { state = index },
                            text = { Text(text = title)},
                        )
                    }
                }
                when (state) {
                    0 -> {
                        if (artViewModel != null) {
                            MyArtworkScreen(
                                onUploadClick = onUploadClick,
                                onArtworkClick = onArtworkClick,
                                viewModel = artViewModel
                            )
                        }
                    }
                    1 -> {
                        if (savedArtworkViewModel != null) {
                            SavedArtworkScreen(
                                onArtworkClick = onArtworkClick,
                                viewModel = savedArtworkViewModel
                            )
                        }
                    }
                }

            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreenContent(
        profileUiState = ProfileUiState(photoUrl = null),
        artworkUiState = ArtworkUiState(artworks = emptyList()),
        savedArtworkViewModel = null,
        artViewModel = null,
        onNavigateToUpload = {},
        onNavigateToSettings = {},
        onUploadProfilePhoto = {},
        onUploadClick = {},
        onArtworkClick = {}
    )
}
