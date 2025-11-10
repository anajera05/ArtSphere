package com.example.artsphere

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
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

//home page after we login
@Composable
fun MainScreenWithBottomBar() {
    var selectedTab by remember { mutableStateOf(BottomTab.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == BottomTab.Home,
                    onClick = { selectedTab = BottomTab.Home },
                    icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == BottomTab.Map,
                    onClick = { selectedTab = BottomTab.Map },
                    icon = { Icon(imageVector = Icons.Filled.LocationOn, contentDescription = "Map") },
                    label = { Text("Map") }
                )
                NavigationBarItem(
                    selected = selectedTab == BottomTab.Profile,
                    onClick = { selectedTab = BottomTab.Profile },
                    icon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            BottomTab.Home -> HomeScreen(modifier = Modifier.padding(innerPadding))
            BottomTab.Map -> MapScreen(modifier = Modifier.padding(innerPadding))
            BottomTab.Profile -> ProfileScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}

enum class BottomTab {
    Home, Map, Profile
}
