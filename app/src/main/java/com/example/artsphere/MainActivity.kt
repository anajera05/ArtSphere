package com.example.artsphere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.artsphere.ui.theme.ArtSphereTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArtSphereTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    // THIS is what shows your login/signup/home
                    ArtSphereApp()
                }
            }
        }
    }
}
