package com.example.artsphere.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import android.net.Uri
import com.example.artsphere.data.model.Artwork
import com.example.artsphere.data.model.Conversation
import com.example.artsphere.ui.artworks.ArtworkDetailScreen
import com.example.artsphere.ui.artworks.ArtworkViewModel
import com.example.artsphere.ui.artworks.addArtwork.CameraScreen
import com.example.artsphere.ui.artworks.addArtwork.UploadArtworkScreen
import com.example.artsphere.ui.artworks.gallery.GalleryViewModel
import com.example.artsphere.ui.artworks.savedArtworks.SavedArtworkViewModel
import com.example.artsphere.ui.home.HomeScreen
import com.example.artsphere.ui.inbox.ChatScreen
import com.example.artsphere.ui.inbox.InboxScreen
import com.example.artsphere.ui.map.MapScreen
import com.example.artsphere.ui.profile.ProfileScreen
import com.example.artsphere.ui.profile.ProfileViewModel
import com.example.artsphere.ui.profile.SettingsScreen
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.collectAsState
import com.example.artsphere.data.model.Event
import com.example.artsphere.ui.events.EventViewModel
import com.example.artsphere.ui.events.CreateEventScreen
import com.example.artsphere.ui.events.EventDetailScreen

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

    val artworkViewModel: ArtworkViewModel = viewModel()
    val savedArtworkViewModel: SavedArtworkViewModel = viewModel()
    val galleryViewModel: GalleryViewModel = viewModel() // ADD THIS - Shared instance
    val eventViewModel: EventViewModel = viewModel()

    var selectedArtwork by remember { mutableStateOf<Artwork?>(null) }
    var selectedConversation by remember { mutableStateOf<Conversation?>(null) }

    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }

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
                    savedViewModel = savedArtworkViewModel,
                    galleryViewModel = galleryViewModel ,
                    onMessagesClick = {
                        navController.navigate(Screen.Inbox.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Map.route) {
                MapScreen(
                    onEventClick = { event ->
                        selectedEvent = event
                        navController.navigate("event_detail")
                    },
                    onCreateEventAtLocation = { latLng ->
                        selectedLocation = latLng
                        navController.navigate("create_event")
                    }
                )
            }

            composable(Screen.Inbox.route) {
                InboxScreen(
                    onConversationClick = { conversation ->
                        selectedConversation = conversation
                        navController.navigate("chat")
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    profileViewModel = profileViewModel,
                    artViewModel = artworkViewModel,
                    savedArtworkViewModel = savedArtworkViewModel,
                    onArtworkClick = { artwork ->
                        selectedArtwork = artwork
                        galleryViewModel.loadAllArtworks() // ADD THIS - Refresh before navigating
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

            composable("upload_artwork") {
                UploadArtworkScreen(
                    onBackClick = {
                        capturedImageUri = null
                        selectedLocation = null
                        galleryViewModel.loadAllArtworks() // REFRESH gallery
                        navController.popBackStack()
                    },
                    viewModel = artworkViewModel,
                    initialImageUri = capturedImageUri,
                    initialLocation = selectedLocation
                )
            }

            composable("artwork_detail") {
                selectedArtwork?.let { artwork ->
                    // Reload artwork from gallery to get latest data
                    val galleryState by galleryViewModel.uiState.collectAsState()
                    val currentArtwork = galleryState.artworks.find { it.id == artwork.id } ?: artwork

                    ArtworkDetailScreen(
                        artwork = currentArtwork,  // Use refreshed artwork
                        onBackClick = {
                            galleryViewModel.loadAllArtworks() // Refresh gallery
                            navController.popBackStack()
                        },
                        onDeleteClick = {
                            galleryViewModel.loadAllArtworks() // Refresh after delete
                            navController.popBackStack()
                        },
                        onMessageClick = {
                            selectedConversation = Conversation(
                                conversationId = "${currentArtwork.userId}_${currentArtwork.id}",
                                otherUserId = currentArtwork.userId,
                                otherUserName = currentArtwork.contactName.ifBlank { "Artist" },
                                artworkId = currentArtwork.id,
                                artworkName = currentArtwork.name,
                                artworkImageUrl = currentArtwork.imageUrl,
                                lastMessage = "",
                                lastMessageTime = 0,
                                unreadCount = 0
                            )
                            navController.navigate("chat")
                        },
                        onNavigateToArtwork = { newArtwork ->
                            selectedArtwork = newArtwork
                            galleryViewModel.loadAllArtworks() // Refresh when navigating between artworks
                        },
                        viewModel = artworkViewModel,
                        galleryViewModel = galleryViewModel,
                        savedViewModel = savedArtworkViewModel
                    )
                }
            }

            composable("chat") {
                selectedConversation?.let { conversation ->
                    ChatScreen(
                        conversation = conversation,
                        onBackClick = { navController.popBackStack() },
                        onNavigateToArtwork = { artwork ->
                            selectedArtwork = artwork
                            navController.navigate("artwork_detail")
                        }
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

            composable("create_event") {
                selectedLocation?.let { location ->
                    CreateEventScreen(
                        location = location,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        viewModel = eventViewModel
                    )
                }
            }

            composable("event_detail") {
                selectedEvent?.let { event ->
                    EventDetailScreen(
                        event = event,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        viewModel = eventViewModel
                    )
                }
            }
        }
    }
}