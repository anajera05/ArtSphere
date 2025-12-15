package com.example.artsphere.ui.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.artsphere.data.model.Event
import com.example.artsphere.ui.events.EventViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.*

/**
 * Main composable for displaying a map screen with event markers and location features.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This screen handles location permissions, fetches the user's current location,
 * and displays a Google Map with event markers. It provides functionality to view
 * event details and create new events at specific locations on the map.
 *
 * @param modifier Modifier to be applied to the root composable.
 * @param onEventClick Optional callback invoked when a user clicks on an event marker.
 *                     Receives the clicked Event object.
 * @param onCreateEventAtLocation Optional callback invoked when user wants to create
 *                                an event at a specific location. Receives the LatLng coordinates.
 */
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    onEventClick: ((Event) -> Unit)? = null,
    onCreateEventAtLocation: ((LatLng) -> Unit)? = null
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
    var showPermissionRationale by remember { mutableStateOf(false) }

    //Launcher for requesting locations
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        hasLocationPermission = fineLocationGranted || coarseLocationGranted

        if (hasLocationPermission) {
            showPermissionRationale = false
            getCurrentLocation(context) { location ->
                currentLocation = location
                isLoadingLocation = false
            }
        } else {
            showPermissionRationale = true
            isLoadingLocation = false
        }
    }

    //Request permission on initial load if not granted
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text(
                            "Loading map...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            !hasLocationPermission -> {
                LocationPermissionDenied(
                    showRationale = showPermissionRationale,
                    onRequestPermission = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            }

            currentLocation != null -> {
                MapWithEvents(
                    currentLocation = currentLocation!!,
                    onEventClick = onEventClick,
                    onCreateEventAtLocation = onCreateEventAtLocation
                )
            }

            else -> {
                MapWithEvents(
                    currentLocation = LatLng(40.7128, -74.0060),
                    onEventClick = onEventClick,
                    onCreateEventAtLocation = onCreateEventAtLocation
                )
            }
        }
    }
}

/**
 * Private composable that displays the Google Map with event markers and interactive features.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This composable renders the Google Map with all event markers, provides a floating
 * action button for creating events, displays an event count badge, and handles
 * map interactions like tapping locations to create events.
 *
 * @param currentLocation The user's current location to center the map.
 * @param onEventClick Optional callback invoked when an event marker is clicked.
 * @param onCreateEventAtLocation Optional callback invoked when user confirms
 *                                creating an event at a tapped location.
 */
@Composable
private fun MapWithEvents(
    currentLocation: LatLng,
    onEventClick: ((Event) -> Unit)?,
    onCreateEventAtLocation: ((LatLng) -> Unit)?
) {
    val eventViewModel: EventViewModel = viewModel()
    val uiState by eventViewModel.uiState.collectAsState()

    //Remember camera position state to centered on the user
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 14f)
    }

    // Track dialog visibility and selected location for event creation
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
            // Add event markers
            uiState.events.forEach { event ->
                Marker(
                    state = MarkerState(position = LatLng(event.latitude, event.longitude)),
                    title = event.title,
                    snippet = "${event.date} at ${event.time}",
                    icon = BitmapDescriptorFactory.fromBitmap(
                        createEventMarkerBitmap()
                    ),
                    onClick = {
                        onEventClick?.invoke(event)
                        true
                    },

                )
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = {
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
                contentDescription = "Create Event",
                tint = Color.White
            )
        }

        // Event count badge
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Event,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "${uiState.events.size} Events",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
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
                    color = MaterialTheme.colorScheme.primary

                )
            }
        }
    }

    if (showAddDialog && selectedLocation != null) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            icon = {
                Icon(
                    Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(text= "Create Event Here?", color = MaterialTheme.colorScheme.onSecondary) },
            text = { Text("Do you want to create an event at this location?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCreateEventAtLocation?.invoke(selectedLocation!!)
                        showAddDialog = false
                    }
                ) {
                    Text("Create Event")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            },
            textContentColor = MaterialTheme.colorScheme.onSecondary,


        )
    }
}

/**
 * Private function that creates a custom bitmap for event markers on the map.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This function programmatically draws a circular marker icon with a colored fill
 * and white stroke border. The marker is rendered as a bitmap that can be used
 * as a custom marker icon in Google Maps.
 *
 * @return Bitmap representing the custom event marker icon.
 */
private fun createEventMarkerBitmap(): Bitmap {
    val size = 100
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val paint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.parseColor("#6200EE")
    }

    val radius = size / 2f
    canvas.drawCircle(radius, radius, radius, paint)

    paint.color = android.graphics.Color.WHITE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 6f
    canvas.drawCircle(radius, radius, radius - 3f, paint)

    return output
}

/**
 * Private composable that displays a screen when location permission is denied.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This screen provides information about why location permission is needed and
 * offers options to either grant permission or open app settings (if permission
 * was previously denied).
 *
 * @param showRationale If true, shows the "Open Settings" option for users who
 *                      permanently denied permission. If false, shows "Grant Permission".
 * @param onRequestPermission Callback invoked when user clicks to grant permission.
 */
@Composable
private fun LocationPermissionDenied(
    showRationale: Boolean,
    onRequestPermission: () -> Unit
) {
    val context = LocalContext.current

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
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (showRationale) {
                    "Location permission was denied. Please enable it in app settings to see events on the map."
                } else {
                    "To show events and your location, please grant location permission."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            if (showRationale) {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B61FF)
                    )
                ) {
                    Text("Open Settings")
                }

                OutlinedButton(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Try Again")
                }
            } else {
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
}

/**
 * Private function that retrieves the user's current location using Google Play Services.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This function attempts to get the most recent location with high accuracy priority.
 * If the current location is unavailable, it falls back to the last known location.
 * If all location fetching fails, it defaults to New York City coordinates.
 *
 * @param context Android context needed to access location services.
 * @param onLocationReceived Callback invoked with the retrieved LatLng location.
 */
private fun getCurrentLocation(
    context: Context,
    onLocationReceived: (LatLng) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    try {
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location: android.location.Location? ->
            if (location != null) {
                onLocationReceived(LatLng(location.latitude, location.longitude))
            } else {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { lastLocation: android.location.Location? ->
                        if (lastLocation != null) {
                            onLocationReceived(LatLng(lastLocation.latitude, lastLocation.longitude))
                        } else {
                            onLocationReceived(LatLng(40.7128, -74.0060))
                        }
                    }
                    .addOnFailureListener {
                        onLocationReceived(LatLng(40.7128, -74.0060))
                    }
            }
        }.addOnFailureListener {
            onLocationReceived(LatLng(40.7128, -74.0060))
        }
    } catch (e: SecurityException) {
        onLocationReceived(LatLng(40.7128, -74.0060))
    }
}