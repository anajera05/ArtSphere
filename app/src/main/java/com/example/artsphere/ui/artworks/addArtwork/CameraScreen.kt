package com.example.artsphere.ui.artworks.addArtwork

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main composable function for the camera screen that handles artwork photography.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This screen manages camera permission requests and displays either the camera preview
 * or a permission denied screen based on the permission state. It uses CameraX APIs
 * for camera functionality.
 *
 * @param onBackClick Callback invoked when the user clicks the back button.
 * @param onPhotoTaken Callback invoked when a photo is successfully captured,
 *                     receives the URI of the saved photo file.
 */
@Composable
fun CameraScreen(
    onBackClick: () -> Unit,
    onPhotoTaken: (Uri) -> Unit
) {
    val context = LocalContext.current

    //Track the status
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    //Launcher used for requesting camera premission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    //Request permission
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    //Based off the status
    if (hasCameraPermission) {
        CameraPreview(
            onBackClick = onBackClick,
            onPhotoTaken = onPhotoTaken
        )
    } else {
        PermissionDeniedScreen(
            onBackClick = onBackClick,
            onRequestPermission = {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )
    }
}

/**
 * Private composable that displays the camera preview with capture functionality.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This composable sets up CameraX with a preview surface and image capture use case.
 * It displays the camera feed using AndroidView with a PreviewView, and provides
 * UI controls for capturing photos and navigating back.
 *
 * @param onBackClick Callback invoked when the back button is pressed.
 * @param onPhotoTaken Callback invoked when a photo is captured, receives the photo URI.
 */
@Composable
private fun CameraPreview(
    onBackClick: () -> Unit,
    onPhotoTaken: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProvider = cameraProviderFuture.get()

                //Build Preview use case
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                //Build image capture
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                //Using the back camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                //Unbind and binding
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        IconButton(
            onClick = {
                capturePhoto(context, imageCapture, onPhotoTaken)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .size(72.dp)
                .background(Color.White, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = "Take Photo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Private composable that displays a permission denied screen with options to grant permission.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This screen is shown when camera permission has been denied. It explains why the
 * permission is needed and provides buttons to either grant permission or go back.
 *
 * @param onBackClick Callback invoked when the "Go Back" button is clicked.
 * @param onRequestPermission Callback invoked when the "Grant Permission" button is clicked.
 */
@Composable
private fun PermissionDeniedScreen(
    onBackClick: () -> Unit,
    onRequestPermission: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📷",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Camera Permission Required",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "To take photos of artwork, please grant camera permission.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onRequestPermission()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Grant Permission")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    onBackClick()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go Back", color = MaterialTheme.colorScheme.onSecondary)
            }
        }
    }
}


/**
 * Private function that handles the photo capture process using CameraX ImageCapture.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This function creates a timestamped file, configures output options, and executes
 * the image capture. On success, it invokes the callback with the photo URI.
 * On failure, it prints the stack trace for debugging.
 *
 * @param context Android context used to access external files directory and main executor.
 * @param imageCapture ImageCapture use case instance. Returns early if null.
 * @param onPhotoTaken Callback invoked with the URI of the saved photo on successful capture.
 */
private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    onPhotoTaken: (Uri) -> Unit
) {
    val imageCapture = imageCapture ?: return

    //Create file with timestamp to ensure unique
    val photoFile = File(
        context.getExternalFilesDir(null),
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    //Execute image capture with callback
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                //Convert file to URI
                val savedUri = Uri.fromFile(photoFile)
                onPhotoTaken(savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
            }
        }
    )
}