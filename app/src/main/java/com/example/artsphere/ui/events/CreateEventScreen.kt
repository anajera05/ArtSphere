package com.example.artsphere.ui.events

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.artsphere.data.model.EventCategory
import com.example.artsphere.ui.components.StyledTextField
import com.example.artsphere.ui.theme.ArtSphereTheme
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Main composable for the event creation screen that integrates with the ViewModel.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This composable serves as a wrapper that connects the UI state from the EventViewModel
 * to the CreateEventScreenContent composable. It handles event creation by calling the
 * ViewModel's createEvent method with user-provided data.
 *
 * @param location The geographic coordinates (latitude/longitude) where the event will take place.
 * @param onBackClick Callback invoked when the user navigates back from this screen.
 * @param viewModel The EventViewModel that manages event creation and state.
 */
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

/**
 * Content composable for the event creation screen with form inputs.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This composable displays a comprehensive form for creating an event, including fields
 * for title, description, date, time, location name, category selection, participant limit,
 * and optional image upload. It validates required fields and shows appropriate error messages.
 *
 * @param location The geographic coordinates for the event location.
 * @param onBackClick Callback invoked when the back button is pressed.
 * @param uiState The current UI state containing loading status and errors.
 * @param onCreateEvent Callback invoked when the create button is pressed with all form data.
 *                      Parameters: title, description, date, time, locationName, category,
 *                      maxParticipants, imageUri.
 */
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
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

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
                title = { Text("Create Event", style = MaterialTheme.typography.titleLarge) },
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
                .background(MaterialTheme.colorScheme.background)
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
            OutlinedTextField(
                value = date,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date *") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (attemptedSave && date.isBlank()) Color.Red else Color.Gray,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSecondary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
                    disabledBorderColor = if (attemptedSave && date.isBlank()) Color.Red else Color.Gray,
                    disabledTextColor = MaterialTheme.colorScheme.onSecondary,
                    disabledLabelColor = MaterialTheme.colorScheme.onSecondary,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = time,
                onValueChange = {},
                readOnly = true,
                label = { Text("Time *") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePicker = true },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (attemptedSave && time.isBlank()) Color.Red else Color.Gray,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSecondary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
                    disabledBorderColor = if (attemptedSave && time.isBlank()) Color.Red else Color.Gray,
                    disabledTextColor = MaterialTheme.colorScheme.onSecondary,
                    disabledLabelColor = MaterialTheme.colorScheme.onSecondary,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = false
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
                    containerColor = MaterialTheme.colorScheme.tertiary

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
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            calendar.timeInMillis = millis

                            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                            date = dateFormat.format(calendar.time)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,
                weekdayContentColor = MaterialTheme.colorScheme.onSurface,
                subheadContentColor = MaterialTheme.colorScheme.onSurface,
                yearContentColor = MaterialTheme.colorScheme.onSurface,
                currentYearContentColor = MaterialTheme.colorScheme.primary,
                selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    headlineContentColor = MaterialTheme.colorScheme.onSurface,
                    weekdayContentColor = MaterialTheme.colorScheme.onSurface,
                    subheadContentColor = MaterialTheme.colorScheme.onSurface,
                    yearContentColor = MaterialTheme.colorScheme.onSurface,
                    currentYearContentColor = MaterialTheme.colorScheme.primary,
                    selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                    selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                    dayContentColor = MaterialTheme.colorScheme.onSurface,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    todayContentColor = MaterialTheme.colorScheme.primary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = { selectedTime ->
                time = selectedTime
                showTimePicker = false
            }
        )
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var hour by remember { mutableStateOf("") }
    var minute by remember { mutableStateOf("") }
    var isAM by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Time", color = Color.Black) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour input
                    OutlinedTextField(
                        value = hour,
                        onValueChange = { newValue ->
                            // Only allow numbers 1-12
                            if (newValue.isEmpty()) {
                                hour = ""
                            } else if (newValue.all { it.isDigit() }) {
                                val num = newValue.toIntOrNull()
                                if (num != null && num in 1..12) {
                                    hour = newValue
                                }
                            }
                        },
                        label = { Text("Hour") },
                        placeholder = { Text("12") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Text(":", style = MaterialTheme.typography.headlineMedium)

                    // Minute input
                    OutlinedTextField(
                        value = minute,
                        onValueChange = { newValue ->
                            // Only allow numbers 00-59
                            if (newValue.isEmpty()) {
                                minute = ""
                            } else if (newValue.all { it.isDigit() } && newValue.length <= 2) {
                                val num = newValue.toIntOrNull()
                                if (num != null && num in 0..59) {
                                    minute = newValue
                                }
                            }
                        },
                        label = { Text("Minute") },
                        placeholder = { Text("00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(90.dp)
                            .heightIn(max = 60.dp),
                        singleLine = true,
                        maxLines = 1,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // AM/PM Selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isAM,
                        onClick = { isAM = true },
                        label = { Text("AM") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isAM,
                        onClick = { isAM = false },
                        label = { Text("PM") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (hour.isNotEmpty() && minute.isNotEmpty()) {
                        val formattedMinute = minute.padStart(2, '0')
                        val period = if (isAM) "AM" else "PM"
                        onConfirm("$hour:$formattedMinute $period")
                    }
                },
                enabled = hour.isNotEmpty() && minute.isNotEmpty()
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
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
