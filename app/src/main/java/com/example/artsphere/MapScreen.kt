package com.example.artsphere

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

data class ArtworkMarker(
    val id: String,
    val position: LatLng,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val artwork: Artwork
)

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    onArtworkClick: ((Artwork) -> Unit)? = null
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var isLoadingLocation by remember { mutableStateOf(true) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            getCurrentLocation(context) { location ->
                currentLocation = location
                isLoadingLocation = false
            }
        } else {
            isLoadingLocation = false
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getCurrentLocation(context) { location ->
                currentLocation = location
                isLoadingLocation = false
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoadingLocation -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF7B61FF))
                }
            }

            !hasLocationPermission -> {
                LocationPermissionDenied(
                    onRequestPermission = {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                )
            }

            currentLocation != null -> {
                MapWithArtwork(
                    currentLocation = currentLocation!!,
                    onArtworkClick = onArtworkClick
                )
            }

            else -> {
                MapWithArtwork(
                    currentLocation = LatLng(40.7128, -74.0060),
                    onArtworkClick = onArtworkClick
                )
            }
        }
    }
}

@Composable
private fun MapWithArtwork(
    currentLocation: LatLng,
    onArtworkClick: ((Artwork) -> Unit)?
) {
    val artworkMarkers = remember(currentLocation) {
        generateMockArtworkMarkers(currentLocation)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 14f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = true
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = true
        )
    ) {
        artworkMarkers.forEach { artworkMarker ->
            Marker(
                state = MarkerState(position = artworkMarker.position),
                title = artworkMarker.title,
                snippet = "by ${artworkMarker.artist}",
                onClick = {
                    onArtworkClick?.invoke(artworkMarker.artwork)
                    true
                }
            )
        }
    }
}

@Composable
private fun LocationPermissionDenied(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "📍",
                style = MaterialTheme.typography.displayLarge
            )

            Text(
                text = "Location Permission Required",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "To show nearby artwork, please grant location permission.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7B61FF)
                )
            ) {
                Text("Grant Permission")
            }
        }
    }
}

private fun getCurrentLocation(
    context: android.content.Context,
    onLocationReceived: (LatLng) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    try {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onLocationReceived(LatLng(location.latitude, location.longitude))
                } else {
                    onLocationReceived(LatLng(40.7128, -74.0060))
                }
            }
            .addOnFailureListener {
                onLocationReceived(LatLng(40.7128, -74.0060))
            }
    } catch (e: SecurityException) {
        onLocationReceived(LatLng(40.7128, -74.0060))
    }
}

private fun generateMockArtworkMarkers(centerLocation: LatLng): List<ArtworkMarker> {
    val mockArtworks = listOf(
        Triple("Starry Night Replica", "Vincent van Gogh", "https://picsum.photos/seed/art1/400/400"),
        Triple("Modern Abstract", "Sarah Johnson", "https://picsum.photos/seed/art2/400/400"),
        Triple("City Lights", "Michael Chen", "https://picsum.photos/seed/art3/400/400"),
        Triple("Ocean Dreams", "Emma Wilson", "https://picsum.photos/seed/art4/400/400"),
        Triple("Urban Poetry", "David Martinez", "https://picsum.photos/seed/art5/400/400"),
        Triple("Sunset Boulevard", "Lisa Anderson", "https://picsum.photos/seed/art6/400/400"),
        Triple("Digital Horizons", "Alex Rivera", "https://picsum.photos/seed/art7/400/400"),
        Triple("Street Art Canvas", "Jordan Lee", "https://picsum.photos/seed/art8/400/400")
    )

    return mockArtworks.mapIndexed { index, (name, artist, imageUrl) ->
        // Distribute markers in a circle around the center location
        val angle = (index * 45.0) * (Math.PI / 180.0) // Convert to radians
        val radius = 0.01 // Approximately 1km radius

        val lat = centerLocation.latitude + radius * Math.cos(angle)
        val lng = centerLocation.longitude + radius * Math.sin(angle)

        val mockArtwork = Artwork(
            id = "mock_$index",
            name = name,
            imageUrl = imageUrl,
            category = ArtworkCategory.PAINTING_DRAWING.name,
            description = "A beautiful piece of art located near you.",
            price = "$${(500..5000).random()}",
            contactEmail = "${artist.lowercase().replace(" ", ".")}@example.com",
            contactName = artist,
            createdAt = System.currentTimeMillis()
        )

        ArtworkMarker(
            id = "marker_$index",
            position = LatLng(lat, lng),
            title = name,
            artist = artist,
            imageUrl = imageUrl,
            artwork = mockArtwork
        )
    }
}