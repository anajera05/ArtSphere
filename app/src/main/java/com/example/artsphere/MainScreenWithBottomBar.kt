package com.example.artsphere

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.ui.graphics.vector.ImageVector
import android.net.Uri
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.LatLng


sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Map : Screen("map", "Map", Icons.Default.LocationOn)
    data object Inbox : Screen("inbox", "Inbox", Icons.Default.Email)
    data object Profile : Screen("profile", "Profile", Icons.Default.AccountCircle)
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Map,
    Screen.Inbox,
    Screen.Profile
)

@Composable
fun MainScreenWithBottomBar(
    profileViewModel: ProfileViewModel,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Create shared ViewModels
    val artworkViewModel: ArtworkViewModel = viewModel()
    val savedArtworkViewModel: SavedArtworkViewModel = viewModel()

    // Track selected artwork for detail view
    var selectedArtwork by remember { mutableStateOf<Artwork?>(null) }

    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }

    // Only show bottom bar on main screens
    val showBottomBar = currentDestination?.route in bottomNavScreens.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavScreens.forEach { screen ->
                        NavigationBarItem(
                            label = { Text(screen.title) },
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate("camera")
                    },
                    containerColor = Color(0xFF7B61FF)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Take Photo",
                        tint = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Screen.Home.route) {
                HomeScreen(
                    onArtworkClick = { artwork ->
                        selectedArtwork = artwork
                        navController.navigate("artwork_detail")
                    },
                    savedViewModel = savedArtworkViewModel
                )
            }
            composable(Screen.Map.route) {
                MapScreen(
                    onArtworkClick = { artwork ->
                        selectedArtwork = artwork
                        navController.navigate("artwork_detail")
                    },
                    onAddArtworkAtLocation = { latLng ->
                        selectedLocation = latLng
                        capturedImageUri = null
                        navController.navigate("upload_artwork")
                    }
                )
            }
            composable(Screen.Inbox.route) { InboxScreen() }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    profileViewModel = profileViewModel,
                    artViewModel = artworkViewModel,
                    savedArtworkViewModel = savedArtworkViewModel,
                    onArtworkClick = { artwork ->
                        selectedArtwork = artwork
                        navController.navigate("artwork_detail")
                    },
                    onUploadClick = {
                        capturedImageUri = null
                        selectedLocation = null
                        navController.navigate("upload_artwork")
                    },
                )
            }

            composable("settings") {
                SettingsScreen(
                    onSignOut = onSignOut,
                    onBackClick = { navController.popBackStack() },
                )
            }

            // Upload Artwork Screen
            composable("upload_artwork") {
                UploadArtworkScreen(
                    onBackClick = {
                        capturedImageUri = null
                        selectedLocation = null
                        navController.popBackStack()
                    },
                    viewModel = artworkViewModel,
                    initialImageUri = capturedImageUri,
                    initialLocation = selectedLocation
                )
            }

            // Artwork Detail Screen
            composable("artwork_detail") {
                selectedArtwork?.let { artwork ->
                    ArtworkDetailScreen(
                        artwork = artwork,
                        onBackClick = { navController.popBackStack() },
                        onDeleteClick = {
                            navController.popBackStack()
                        },
                        viewModel = artworkViewModel,
                        savedViewModel = savedArtworkViewModel
                    )
                }
            }

            composable("camera") {
                CameraScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onPhotoTaken = { uri ->
                        capturedImageUri = uri
                        selectedLocation = null
                        navController.navigate("upload_artwork") {
                            popUpTo("camera") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}