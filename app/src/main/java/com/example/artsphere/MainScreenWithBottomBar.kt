package com.example.artsphere

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.artsphere.Screen.Home.icon
import com.example.artsphere.components.DetailScreen
import com.example.artsphere.ui.theme.ArtSphereTheme

//home page after we login

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {

    data object Home : Screen("home", "Home", Icons.Default.Home, )
    data object Map : Screen("map", "Map", Icons.Default.LocationOn, )
    data object Inbox : Screen("inbox", "Inbox", Icons.Default.Email, )
    data object Profile : Screen("profile", "Profile", Icons.Default.AccountCircle, )
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings, )


}

val screens = listOf(
    Screen.Home,
    Screen.Map,
    Screen.Inbox,
    Screen.Profile,
    Screen.Settings
)
@Composable
fun MainScreenWithBottomBar(onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEach { screen ->
                    if (screen != Screen.Settings){
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
                                    restoreState = screen != Screen.Profile
                                }
                            }
                        )
                    }

                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController = navController) }
            composable(Screen.Map.route) { MapScreen() }
            composable(Screen.Inbox.route) { InboxScreen() }
            composable(Screen.Profile.route) { ProfileScreen(navController = navController) }
            composable(Screen.Settings.route) {SettingsScreen(onSignOut = onSignOut, navController = navController)  }
            composable(
                "artwork/{index}",
                arguments = listOf(navArgument("index") { type = NavType.IntType })
            ) { backStackEntry ->
                val index = backStackEntry.arguments?.getInt("index") ?: 0
                DetailScreen(navController = navController, index = index)
            }
        }
    }
}
