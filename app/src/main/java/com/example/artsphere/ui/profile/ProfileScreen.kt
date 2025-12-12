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
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import com.example.artsphere.ui.theme.ArtSphereTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    profileViewModel: ProfileViewModel,
    savedArtworkViewModel: SavedArtworkViewModel,
    artViewModel: ArtworkViewModel,
    onUploadClick: () -> Unit,
    onArtworkClick: (Artwork) -> Unit,
    onSignOut: () -> Unit
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val artworkUiState by artViewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }


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
        onUploadProfilePhoto = { uri -> profileViewModel.uploadProfilePhoto(uri) },
        onUploadClick = onUploadClick,
        onArtworkClick = onArtworkClick,
        onSignOut = onSignOut,
        showLogoutDialog = showLogoutDialog,
        onShowLogoutDialogChange = { showLogoutDialog = it },
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
    onUploadProfilePhoto: (Uri) -> Unit,
    onUploadClick: () -> Unit,
    onArtworkClick: (Artwork) -> Unit,
    onSignOut: () -> Unit,
    showLogoutDialog: Boolean,
    onShowLogoutDialogChange: (Boolean) -> Unit,
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
        modifier = modifier.fillMaxSize()
            .background(
            Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceVariant
                ),
                start = Offset(0f, Float.POSITIVE_INFINITY),
                end = Offset(Float.POSITIVE_INFINITY, 0f)
            )
        ),
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
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurface
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
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(
                        onClick = { onShowLogoutDialogChange(true) },
                    ){
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Settings",
                            tint = Color.White,
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
                    Text(text = artworkUiState.artworks.size.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("artworks", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }


            }
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                SecondaryTabRow(
                    selectedTabIndex = state,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,

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
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { onShowLogoutDialogChange(false) },
                title = { Text(text ="Log Out", color = MaterialTheme.colorScheme.onSecondary) },
                text = { Text("Are you sure you want to Log Out?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onShowLogoutDialogChange(false)
                            onSignOut()
                        }
                    ) {
                        Text("Logout", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onShowLogoutDialogChange(false) }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ArtSphereTheme{
        ProfileScreenContent(
            profileUiState = ProfileUiState(photoUrl = null),
            artworkUiState = ArtworkUiState(artworks = emptyList()),
            savedArtworkViewModel = null,
            artViewModel = null,
            onNavigateToUpload = {},
            onUploadProfilePhoto = {},
            onUploadClick = {},
            onArtworkClick = {},
            onSignOut = {},
            showLogoutDialog = true,
            onShowLogoutDialogChange = {}
        )
    }
}

