package com.example.artsphere

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.artsphere.Screen.Home.icon
import com.example.artsphere.AddArtworkScreen
import com.example.artsphere.CameraScreen

//home page after we login

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {

    data object Home : Screen("home", "Home", Icons.Default.Home, )
    data object Map : Screen("map", "Map", Icons.Default.LocationOn, )
    data object Inbox : Screen("inbox", "Inbox", Icons.Default.Email, )
    data object Profile : Screen("profile", "Profile", Icons.Default.AccountCircle, )

}

val screens = listOf(
    Screen.Home,
    Screen.Map,
    Screen.Inbox,
    Screen.Profile
)
@Composable
fun MainScreenWithBottomBar(
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    val currentRoute = currentDestination?.route
    val showBottomBar = currentRoute !in listOf("camera", "add_artwork")

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEach { screen ->
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
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Map.route) { MapScreen() }
            composable(Screen.Inbox.route) { InboxScreen() }
            composable(Screen.Profile.route) {
                ProfileScreen(onSignOut = onSignOut)
            }
            composable("camera") {
                CameraScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onPhotoTaken = { uri ->
                        capturedImageUri = uri
                        navController.navigate("add_artwork")
                    }
                )
            }

            composable("add_artwork") {
                capturedImageUri?.let { uri ->
                    AddArtworkScreen(
                        imageUri = uri,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSaveSuccess = {
                            navController.popBackStack(Screen.Home.route, false)
                        }
                    )
                }
            }
        }
    }
}
