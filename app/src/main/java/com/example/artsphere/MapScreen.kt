package com.example.artsphere

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.artsphere.MapViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
    onArtworkClick: ((Artwork) -> Unit)? = null,
    onAddArtworkAtLocation: ((LatLng) -> Unit)? = null
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
                    onArtworkClick = onArtworkClick,
                    onAddArtworkAtLocation = onAddArtworkAtLocation
                )
            }

            else -> {
                MapWithArtwork(
                    currentLocation = LatLng(40.7128, -74.0060),
                    onArtworkClick = onArtworkClick,
                    onAddArtworkAtLocation = onAddArtworkAtLocation
                )
            }
        }
    }
}

@Composable
private fun MapWithArtwork(
    currentLocation: LatLng,
    onArtworkClick: ((Artwork) -> Unit)?,
    onAddArtworkAtLocation: ((LatLng) -> Unit)?
) {
    // ViewModel to load artworks from Firestore
    val mapViewModel: MapViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uiState by mapViewModel.uiState.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 14f)
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = true
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = true
            ),
            onMapClick = { latLng ->
                selectedLocation = latLng
                showAddDialog = true
            }
        ) {
            uiState.artworkMarkers.forEach { artworkMarker ->
                ArtworkImageMarker(
                    artworkMarker = artworkMarker,
                    onMarkerClick = {
                        onArtworkClick?.invoke(artworkMarker.artwork)
                        true
                    }
                )
            }
        }

        FloatingActionButton(
            onClick = {
                // Use current camera position as default location
                selectedLocation = cameraPositionState.position.target
                showAddDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF7B61FF)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Artwork",
                tint = Color.White
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Color(0xFF7B61FF)
                )
            }
        }
    }

    // Dialog to confirm adding artwork at location
    if (showAddDialog && selectedLocation != null) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Artwork Here?") },
            text = { Text("Do you want to add artwork at this location on the map?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAddArtworkAtLocation?.invoke(selectedLocation!!)
                        showAddDialog = false
                    }
                ) {
                    Text("Yes, Add Artwork")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ArtworkImageMarker(
    artworkMarker: ArtworkMarker,
    onMarkerClick: () -> Boolean
) {
    val context = LocalContext.current
    var markerBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // Load image and convert to bitmap for marker
    LaunchedEffect(artworkMarker.imageUrl) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val loader = coil.ImageLoader(context)
                val request = coil.request.ImageRequest.Builder(context)
                    .data(artworkMarker.imageUrl)
                    .size(150, 150) // Resize for marker
                    .allowHardware(false) // Required for bitmap conversion
                    .build()

                val result = (loader.execute(request) as? coil.request.SuccessResult)?.drawable
                val bitmap = (result as? android.graphics.drawable.BitmapDrawable)?.bitmap

                bitmap?.let {
                    // Create rounded bitmap for marker
                    markerBitmap = createCircularBitmap(it, 150)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Marker(
        state = MarkerState(position = artworkMarker.position),
        title = artworkMarker.title,
        snippet = "by ${artworkMarker.artist}",
        icon = markerBitmap?.let { BitmapDescriptorFactory.fromBitmap(it) },
        onClick = { onMarkerClick() }
    )
}

private fun createCircularBitmap(bitmap: android.graphics.Bitmap, size: Int): android.graphics.Bitmap {
    val output = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(output)

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
    }

    val rect = android.graphics.Rect(0, 0, size, size)
    val rectF = android.graphics.RectF(rect)
    val radius = size / 2f

    canvas.drawCircle(radius, radius, radius, paint)

    paint.color = android.graphics.Color.parseColor("#6200EE")
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 8f
    canvas.drawCircle(radius, radius, radius - 4f, paint)

    paint.reset()
    paint.isAntiAlias = true
    paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)

    val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, size, size, true)
    canvas.drawBitmap(scaledBitmap, rect, rect, paint)

    return output
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