package com.example.artsphere

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadArtworkScreen(
    onBackClick: () -> Unit,
    viewModel: ArtworkViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var artworkName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ArtworkCategory.PAINTING_DRAWING) }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var showCategoryMenu by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
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
                    containerColor = Color(0xFF6200EE),
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
                    .border(2.dp, Color(0xFF6200EE), RoundedCornerShape(16.dp))
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
                            tint = Color(0xFF6200EE),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Add Artwork Image",
                            color = Color(0xFF6200EE),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Artwork Name
            OutlinedTextField(
                value = artworkName,
                onValueChange = { artworkName = it },
                label = { Text("Artwork Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6200EE),
                    focusedLabelColor = Color(0xFF6200EE)
                )
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6200EE),
                        focusedLabelColor = Color(0xFF6200EE)
                    )
                )

                ExposedDropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    ArtworkCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName) },
                            onClick = {
                                selectedCategory = category
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6200EE),
                    focusedLabelColor = Color(0xFF6200EE)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Price
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (e.g., $500 or Contact for price)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6200EE),
                    focusedLabelColor = Color(0xFF6200EE)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Name
            OutlinedTextField(
                value = contactName,
                onValueChange = { contactName = it },
                label = { Text("Contact Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6200EE),
                    focusedLabelColor = Color(0xFF6200EE)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Email
            OutlinedTextField(
                value = contactEmail,
                onValueChange = { contactEmail = it },
                label = { Text("Contact Email") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6200EE),
                    focusedLabelColor = Color(0xFF6200EE)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = {
                    if (selectedImageUri != null && artworkName.isNotBlank()) {
                        viewModel.uploadArtwork(
                            imageUri = selectedImageUri!!,
                            name = artworkName,
                            category = selectedCategory.name,  // Pass as string
                            description = description,
                            price = price,
                            contactEmail = contactEmail,
                            contactName = contactName,
                            onSuccess = onBackClick
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedImageUri != null && artworkName.isNotBlank() && !uiState.isUploading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                )
            ) {
                if (uiState.isUploading) {
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

            // Error message
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Error: ${uiState.error}",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}