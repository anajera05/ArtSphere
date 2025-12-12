package com.example.artsphere.ui.artworks.addArtwork

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.artsphere.data.model.ArtworkCategory
import com.example.artsphere.ui.artworks.ArtworkViewModel
import com.example.artsphere.ui.components.StyledTextField
import com.google.android.gms.maps.model.LatLng
import com.example.artsphere.ui.theme.ArtSphereTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadArtworkScreen(
    onBackClick: () -> Unit,
    viewModel: ArtworkViewModel = viewModel(),
    initialImageUri: Uri? = null,
    initialLocation: LatLng? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    UploadArtworkContent(
        onBackClick = onBackClick,
        isUploading = uiState.isUploading,
        uploadError = uiState.error,
        onUploadArtwork = { imageUri, name, category, description, price, contactEmail, contactName, latitude, longitude ->
            viewModel.uploadArtwork(
                imageUri = imageUri,
                name = name,
                category = category,
                description = description,
                price = price,
                contactEmail = contactEmail,
                contactName = contactName,
                latitude = latitude,
                longitude = longitude,
                onSuccess = onBackClick
            )
        },
        initialImageUri = initialImageUri,
        initialLocation = initialLocation
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadArtworkContent(
    onBackClick: () -> Unit,
    isUploading: Boolean,
    uploadError: String?,
    onUploadArtwork: (Uri, String, String, String, String, String, String, Double?, Double?) -> Unit,
    initialImageUri: Uri? = null,
    initialLocation: LatLng? = null
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var location by remember { mutableStateOf<LatLng?>(null) }
    LaunchedEffect(initialImageUri) {
        if (initialImageUri != null) {
            selectedImageUri = initialImageUri
        }
        if (initialLocation != null) {
            location = initialLocation
        }
    }
    var artworkName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ArtworkCategory.PAINTING_DRAWING) }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var showCategoryMenu by remember { mutableStateOf(false) }

    // Email validation
    var emailError by remember { mutableStateOf<String?>(null) }
    var isEmailModified by remember { mutableStateOf(false) }

    // Track if user has attempted to save (to show errors)
    var attemptedSave by remember { mutableStateOf(false) }

    // Email validation function
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex()
        return emailPattern.matches(email)
    }

    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            selectedImageUri = it
        }
    }

    // Check if all required fields are filled (price is optional)
    val allFieldsFilled = selectedImageUri != null &&
            artworkName.isNotBlank() &&
            description.isNotBlank() &&
            contactName.isNotBlank() &&
            contactEmail.isNotBlank() &&
            isValidEmail(contactEmail)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Artwork") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
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

            // Image Upload Box
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        2.dp,
                        if (attemptedSave && selectedImageUri == null) Color.Red else MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { pickImageLauncher.launch(arrayOf("image/*")) },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected artwork",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add image",
                            tint = if (attemptedSave && selectedImageUri == null) Color.Red else Color(0xFF6200EE),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Add Artwork Image *",
                            color = if (attemptedSave && selectedImageUri == null) Color.Red else MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
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

            // Location Info
            if (location != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8DEF8)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📍",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Location Selected",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6200EE)
                            )
                            Text(
                                text = "Lat: ${String.format("%.4f", location!!.latitude)}, " +
                                        "Lng: ${String.format("%.4f", location!!.longitude)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6200EE)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Artwork Name (Required)
            StyledTextField(
                value = artworkName,
                onValueChange = { artworkName = it },
                label = "Artwork Name *",
                isSubmitted = attemptedSave,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = showCategoryMenu,
                onExpandedChange = { showCategoryMenu = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black

                    )
                )

                ExposedDropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false },
                    modifier = Modifier.background(Color.White),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    ArtworkCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName) },
                            onClick = {
                                selectedCategory = category
                                showCategoryMenu = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Color.Black
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description (Required)
            StyledTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description *",
                isSubmitted = attemptedSave,
                lines = 5,
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Price (Optional)
            StyledTextField(
                value = description,
                onValueChange = { newValue ->
                    price = when {
                        newValue.isEmpty() -> ""
                        else -> {
                            val filtered = newValue.filter { it.isDigit() || it == '.' }
                            val hasMultipleDecimals = filtered.count { it == '.' } > 1
                            val startsWithDecimal = filtered.startsWith(".")
                            val parts = filtered.split(".")
                            val tooManyDecimals = parts.size == 2 && parts[1].length > 2

                            if (hasMultipleDecimals || startsWithDecimal || tooManyDecimals) {
                                price
                            } else {
                                filtered
                            }
                        }
                    }
                },
                label = "Price ($)",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.fillMaxWidth().onFocusChanged { focusState ->
                    if (!focusState.isFocused && price.isNotEmpty()) {
                        price.toDoubleOrNull()?.let {
                            price = String.format("%.2f", it)
                        }
                    }
                },
                supportingText = "Optional - Leave empty for 'Contact for price'"

            )
            Spacer(modifier = Modifier.height(16.dp))

            // Contact Name (Required)
            StyledTextField(
                value = contactName,
                onValueChange = { contactName = it },
                label = "Contact Name *",
                isSubmitted = attemptedSave,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Email (Required with validation)
            val showEmailError = attemptedSave && !isEmailModified && (contactEmail.isBlank() || !isValidEmail(contactEmail))
            OutlinedTextField(
                value = contactEmail,
                onValueChange = { 
                    contactEmail = it
                    isEmailModified = true
                },
                label = { Text("Contact Email *") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (showEmailError) Color.Red else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (showEmailError) Color.Red else Color.Gray,
                    focusedLabelColor = MaterialTheme.colorScheme.onSecondary,
                    focusedTextColor = if (showEmailError) Color.Red else MaterialTheme.colorScheme.onSecondary,
                    unfocusedTextColor = if (showEmailError) Color.Red else MaterialTheme.colorScheme.onSecondary
                ),
                shape = RoundedCornerShape(16.dp),
                isError = showEmailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))



            // Save Button
            Button(
                onClick = {
                    attemptedSave = true
                    isEmailModified = false
                    
                    if (contactEmail.isNotBlank() && !isValidEmail(contactEmail)) {
                        contactEmail = ""
                    }
                    
                    if (allFieldsFilled) {
                        onUploadArtwork(
                            selectedImageUri!!,
                            artworkName,
                            selectedCategory.name,
                            description,
                            price,
                            contactEmail,
                            contactName,
                            location?.latitude,
                            location?.longitude
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isUploading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Save Artwork",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Show missing fields error ONLY after save is attempted
            if (attemptedSave && !allFieldsFilled && !isUploading) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "❌ Please fill in all required fields:",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (selectedImageUri == null) Text("• Artwork Image", color = MaterialTheme.colorScheme.error)
                        if (artworkName.isBlank()) Text("• Artwork Name", color = MaterialTheme.colorScheme.error)
                        if (description.isBlank()) Text("• Description", color = MaterialTheme.colorScheme.error)
                        if (contactName.isBlank()) Text("• Contact Name", color = MaterialTheme.colorScheme.error)
                        if (contactEmail.isBlank()) Text("• Contact Email", color = MaterialTheme.colorScheme.error)
                        else if (!isValidEmail(contactEmail)) Text("• Valid Email Format", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Error message from upload
            if (uploadError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Error: $uploadError",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UploadArtworkScreenPreview() {
    ArtSphereTheme {
        UploadArtworkContent(
            onBackClick = {},
            isUploading = false,
            uploadError = null,
            onUploadArtwork = { _, _, _, _, _, _, _, _, _ -> }
        )
    }
}
