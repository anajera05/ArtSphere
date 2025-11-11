package com.example.artsphere

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun ArtSphereApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onCreateAccountClick = {
                    navController.navigate("signup")
                }
            )
        }
        composable("signup") {
            SignupScreen(
                viewModel = authViewModel,
                onSignupSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }
        composable("main") {
            // ⬇️ give the bottom-bar screen a way to sign out
            MainScreenWithBottomBar(
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate("login") {
                        // remove main so user can’t go back to it
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}

