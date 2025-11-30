package com.example.artsphere

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ArtworkData(
    val imageUri: Uri? = null,
    val name: String = "",
    val artist: String = "",
    val description: String = "",
    val cost: String = "",
    val year: String = "",
    val medium: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class AddArtworkViewModel : ViewModel() {
    private val _artworkData = MutableStateFlow(ArtworkData())
    val artworkData: StateFlow<ArtworkData> = _artworkData

    fun setImageUri(uri: Uri) {
        _artworkData.value = _artworkData.value.copy(imageUri = uri)
    }

    fun updateName(name: String) {
        _artworkData.value = _artworkData.value.copy(name = name, error = null)
    }

    fun updateArtist(artist: String) {
        _artworkData.value = _artworkData.value.copy(artist = artist, error = null)
    }

    fun updateDescription(description: String) {
        _artworkData.value = _artworkData.value.copy(description = description, error = null)
    }

    fun updateCost(cost: String) {
        _artworkData.value = _artworkData.value.copy(cost = cost, error = null)
    }

    fun updateYear(year: String) {
        _artworkData.value = _artworkData.value.copy(year = year, error = null)
    }

    fun updateMedium(medium: String) {
        _artworkData.value = _artworkData.value.copy(medium = medium, error = null)
    }

    fun saveArtwork(onSuccess: () -> Unit) {
        val data = _artworkData.value

        // Validation
        if (data.name.isBlank()) {
            _artworkData.value = data.copy(error = "Artwork name is required")
            return
        }
        if (data.artist.isBlank()) {
            _artworkData.value = data.copy(error = "Artist name is required")
            return
        }

        _artworkData.value = data.copy(isLoading = true, error = null)

        // For now, just simulate success (need firebase later)
        _artworkData.value = data.copy(isLoading = false)
        onSuccess()
    }

    fun reset() {
        _artworkData.value = ArtworkData()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddArtworkScreen(
    imageUri: Uri,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: AddArtworkViewModel = viewModel()
) {
    val artworkData by viewModel.artworkData.collectAsState()

    LaunchedEffect(imageUri) {
        viewModel.setImageUri(imageUri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Artwork Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveArtwork(onSaveSuccess) },
                        enabled = !artworkData.isLoading
                    ) {
                        if (artworkData.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF7B61FF),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F5F5))
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Captured artwork",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Artwork Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (artworkData.error != null) {
                    Text(
                        text = artworkData.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.errorContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    )
                }

                OutlinedTextField(
                    value = artworkData.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Artwork Name *") },
                    placeholder = { Text("e.g., Starry Night") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = artworkData.artist,
                    onValueChange = { viewModel.updateArtist(it) },
                    label = { Text("Artist Name *") },
                    placeholder = { Text("e.g., Vincent van Gogh") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = artworkData.year,
                    onValueChange = { viewModel.updateYear(it) },
                    label = { Text("Year") },
                    placeholder = { Text("e.g., 1889") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = artworkData.medium,
                    onValueChange = { viewModel.updateMedium(it) },
                    label = { Text("Medium") },
                    placeholder = { Text("e.g., Oil on canvas") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = artworkData.cost,
                    onValueChange = { viewModel.updateCost(it) },
                    label = { Text("Estimated Value") },
                    placeholder = { Text("e.g., 5000") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("$ ") },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = artworkData.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("Description") },
                    placeholder = { Text("Add details about the artwork...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                Button(
                    onClick = { viewModel.saveArtwork(onSaveSuccess) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    enabled = !artworkData.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B61FF)
                    )
                ) {
                    if (artworkData.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...")
                    } else {
                        Text(
                            text = "Save Artwork",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}