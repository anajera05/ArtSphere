package com.example.artsphere.ui.events

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.artsphere.data.model.EventCategory
import com.example.artsphere.ui.components.StyledTextField
import com.example.artsphere.ui.theme.ArtSphereTheme
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    location: LatLng,
    onBackClick: () -> Unit,
    viewModel: EventViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    CreateEventScreenContent(
        location = location,
        onBackClick = onBackClick,
        uiState = uiState,
        onCreateEvent = {
            title, description, date, time, locationName, category, maxParticipants, imageUri ->
            viewModel.createEvent(
                title = title,
                description = description,
                date = date,
                time = time,
                location = locationName,
                latitude = location.latitude,
                longitude = location.longitude,
                category = category,
                maxParticipants = maxParticipants,
                imageUri = imageUri,
                onSuccess = onBackClick
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreenContent(
    location: LatLng,
    onBackClick: () -> Unit,
    uiState: EventUiState,
    onCreateEvent: (String, String, String, String, String, String, Int, Uri?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(EventCategory.EXHIBITION.name) }
    var maxParticipants by remember { mutableStateOf("0") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var expandedCategory by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }

    val allFieldsFilled = title.isNotBlank() &&
            description.isNotBlank() &&
            date.isNotBlank() &&
            time.isNotBlank() &&
            locationName.isNotBlank()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Event") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF4F1FA))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image picker
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { imagePickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(16.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Event image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Add photo",
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                            Text(
                                "Add Event Photo (Optional)",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Required fields notice
            Text(
                text = "* Indicates required fields",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Red,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))
            StyledTextField(
                value = title,
                onValueChange = { title = it },
                label = "Event Title *",
                isSubmitted = attemptedSave,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            StyledTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description *",
                isSubmitted = attemptedSave,
                lines = 5,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            StyledTextField(
                value = date,
                onValueChange = { date = it },
                label = "Date (e.g., Dec 25, 2024) *",
                isSubmitted = attemptedSave,
                icon = Icons.Default.CalendarToday
            )

            Spacer(modifier = Modifier.height(16.dp))

            StyledTextField(
                value = time,
                onValueChange = { time = it },
                label = "Time (e.g., 6:00 PM) *",
                isSubmitted = attemptedSave,
                icon = Icons.Default.Schedule
            )

            Spacer(modifier = Modifier.height(16.dp))

            StyledTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = "Location Name/Address *",
                isSubmitted = attemptedSave,
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = it }
            ) {
                OutlinedTextField(
                    value = EventCategory.valueOf(category).displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),

                )

                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false },
                    shape = RoundedCornerShape(16.dp),


                    ) {
                    EventCategory.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.displayName) },
                            onClick = {
                                category = cat.name
                                expandedCategory = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Color.Black,
                            )

                        )

                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))


            StyledTextField(
                value = maxParticipants,
                onValueChange = {
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        maxParticipants = it
                    }
                },
                label = "Max Participants (0 = Unlimited)",
            )

            Spacer(modifier = Modifier.height(16.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8DEF8)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Event Location",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "Latitude: ${String.format("%.6f", location.latitude)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Longitude: ${String.format("%.6f", location.longitude)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))


            if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    attemptedSave = true
                    if (allFieldsFilled) {
                        onCreateEvent(
                            title,
                            description,
                            date,
                            time,
                            locationName,
                            category,
                            maxParticipants.toIntOrNull() ?: 0,
                            imageUri
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isCreating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Create Event", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (attemptedSave && !allFieldsFilled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "❌ Please fill in all required fields:",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (title.isBlank()) Text("• Event Title", color = MaterialTheme.colorScheme.error)
                        if (description.isBlank()) Text("• Description", color = MaterialTheme.colorScheme.error)
                        if (date.isBlank()) Text("• Date", color = MaterialTheme.colorScheme.error)
                        if (time.isBlank()) Text("• Time", color = MaterialTheme.colorScheme.error)
                        if (locationName.isBlank()) Text("• Location Name/Address", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateEventScreenPreview() {
    ArtSphereTheme {
        CreateEventScreenContent(
            location = LatLng(0.0, 0.0),
            onBackClick = {},
            uiState = EventUiState(),
            onCreateEvent = { _, _, _, _, _, _, _, _ -> }
        )
    }
}
