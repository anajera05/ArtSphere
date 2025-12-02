package com.example.artsphere

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.artsphere.components.ArtworkCard
import com.google.android.gms.maps.model.LatLng


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    profileViewModel: ProfileViewModel,
    artViewModel: ArtworkViewModel,
    onUploadClick: () -> Unit
) {
    var state by remember { mutableIntStateOf(0) }
    val titles = listOf("Shop", "Saved")
    val uiState by profileViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedArtwork by remember { mutableStateOf<Artwork?>(null) }



    LaunchedEffect(Unit) {
        profileViewModel.refreshPhotoFromFirebase()
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch(_: Exception) {}
            profileViewModel.uploadProfilePhoto(uri)
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
                    text = "@username",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    IconButton(
                        onClick = { onUploadClick() },
                    ){
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
                    }
                    IconButton(
                        onClick = {
                            navController.navigate("settings")
                        },
                    ){
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                ){
                    when {
                        uiState.isUploading -> {
                            CircularProgressIndicator(
                                color = Color(0xFF6200EE),
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        uiState.photoUrl != null -> {
                            AsyncImage(
                                model = uiState.photoUrl,
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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(50.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("42", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("followers", style = MaterialTheme.typography.bodySmall)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("30", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("following", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Column {
                SecondaryTabRow(selectedTabIndex = state) {
                    titles.forEachIndexed { index, title ->
                        Tab(
                            selected = state == index,
                            onClick = { state = index },
                            text = { Text(text = title)},
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                when (state) {
                    0 -> { MyArtworkScreen(
                        onBackClick = { navController.popBackStack() },
                        onUploadClick = {
                            capturedImageUri = null
                            selectedLocation = null
                            navController.navigate("upload_artwork")
                        },
                        onArtworkClick = { artwork ->
                            selectedArtwork = artwork
                            navController.navigate("artwork_detail")
                        },
                        viewModel = artViewModel
                    )}
                    1 -> {

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(5) { index ->
                                ArtworkCard(index, 120, navController)

                            }
                        }
                    }
                }

            }

        }
    }
}