package com.example.artsphere

import ads_mobile_sdk.h5
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.artsphere.ui.theme.ArtSphereTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArtSphereTheme {
                MainScreen()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val isNav : Boolean, val icon: ImageVector? = null) {
    data object Login : Screen("login", "Login" , false)

    data object Home : Screen("home", "Home", true, Icons.Default.Home)
    data object Map : Screen("map", "Map", true, Icons.Default.LocationOn)
    data object Inbox : Screen("inbox", "Inbox", true,Icons.Default.Email)
    data object Profile : Screen("profile", "Profile", true, Icons.Default.AccountCircle)

}

val screens = listOf(
    Screen.Login,
    Screen.Home,
    Screen.Map,
    Screen.Inbox,
    Screen.Profile
)

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun TopBar(navController: NavController, currentScreen: Screen?) {
//    TopAppBar(
//        title = { Text(currentScreen?.title ?: "Explore Boston") },
//        navigationIcon = {
//            if (currentScreen != Screen.Home && currentScreen != null) {
//                IconButton(onClick = { navController.navigateUp() }) {
//                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
//                }
//            }
//        },
//        actions = {
//            if (currentScreen != Screen.Home && currentScreen != null) {
//                IconButton(onClick = {
//                    navController.navigate(Screen.Home.route) {
//                        popUpTo(Screen.Home.route) { inclusive = true }
//                    }
//                }) {
//                    Icon(Icons.Filled.Home, contentDescription = "Home")
//                }
//            }
//        }
//    )
//}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentScreen = screens.find { currentRoute?.startsWith(it.route.substringBefore("/{")) == true }
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            if (currentScreen != Screen.Login) {
                NavigationBar {
                    screens.forEach { screen ->
                        if (screen.isNav) {
                            NavigationBarItem(
                                label = { Text(screen.title) },
                                icon = {
                                    screen.icon?.let { icon ->
                                    Icon(icon, contentDescription = screen.title) }
                                    },
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
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) { LoginScreen(navController) }
            composable(Screen.Home.route) { GenericScreen(Screen.Home) }
            composable(Screen.Map.route) { GenericScreen(Screen.Map) }
            composable(Screen.Inbox.route) { GenericScreen(Screen.Inbox) }
            composable(Screen.Profile.route) { GenericScreen(Screen.Profile) }

        }
    }
}


@Composable
fun GenericScreen(screen: Screen) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            screen.icon?.let {
                Icon(imageVector = it, contentDescription = null, modifier = Modifier.padding(bottom = 8.dp))
            }
            Text(text = screen.title, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray.copy(alpha = 0.1f)), // placeholder background
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Welcome Back", style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .background(Color.White.copy(alpha = 0.85f), shape = RoundedCornerShape(16.dp))
                    .padding(horizontal = 30.dp, vertical = 32.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email Icon"
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password Icon"
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Forgot your password?", color = Color.Gray, style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.End),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { navController.navigate(Screen.Home.route) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7B61FF) // purple shade
                        )
                    ) {
                        Text(text = "LOGIN", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ArtSphereTheme {
        MainScreen()
    }
}
