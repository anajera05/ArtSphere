package com.example.artsphere.ui.auth

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.artsphere.R
import com.example.artsphere.data.model.Artwork
import com.example.artsphere.ui.artworks.gallery.GalleryViewModel
import com.example.artsphere.ui.theme.ArtSphereTheme


@Composable
fun LandingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignup: () -> Unit,
    viewModel: GalleryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LandingScreenContent(
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToSignup = onNavigateToSignup,
        artworks = uiState.artworks
    )
}

@Composable
fun LandingScreenContent(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignup: () -> Unit,
    artworks: List<Artwork>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    ),
                    start = Offset(0f, Float.POSITIVE_INFINITY),
                    end = Offset(Float.POSITIVE_INFINITY, 0f)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 1.dp.toPx())
            val circleColor = Color.White.copy(alpha = 0.15f)

            drawCircle(
                color = circleColor,
                radius = size.width * 0.7f,
                center = Offset(size.width * 0.2f, size.height * 0.5f),
                style = stroke
            )
            drawCircle(
                color = circleColor,
                radius = size.width * 0.5f,
                center = Offset(size.width * 0.8f, size.height * 0.3f),
                style = stroke
            )
            drawLine(
                color = circleColor,
                start = Offset(0f, size.height * 0.8f),
                end = Offset(size.width, size.height * 0.4f),
                strokeWidth = 1.dp.toPx()
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier = Modifier.height(70.dp))

            MarqueeEffect(artworks = artworks)
            MarqueeEffect(direction = -30, artworks = artworks)
            MarqueeEffect(artworks = artworks)

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "EXPAND YOUR",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = "ARTSPHERE",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Explore different art pieces",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = " all in one place",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "LOGIN",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToSignup,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "SIGN UP",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MarqueeEffect(
    direction: Int = 30,
    artworks: List<Artwork>
) {
    val shuffledArtworks = remember(artworks) { artworks.shuffled() }
    val randomInitialDelay = remember { (0..1500).random() }

    Box(modifier = Modifier.padding(top = 10.dp)) {
        Row(
            modifier = Modifier.basicMarquee(
                iterations = Int.MAX_VALUE,
                initialDelayMillis = randomInitialDelay,
                repeatDelayMillis = 0,
                spacing = MarqueeSpacing(0.dp),
                velocity = (direction).dp
            )
        ) {
            // shuffle existing artworks in landing screen
            if (shuffledArtworks.isNotEmpty()) {
                val displayList = if (shuffledArtworks.size < 10) {
                    List(10) { shuffledArtworks[it % shuffledArtworks.size] }
                } else {
                    shuffledArtworks.take(20)
                }

                displayList.forEach { artwork ->
                    MarqueeArtworkImage(artwork = artwork)
                    Spacer(modifier = Modifier.width(8.dp))
                }
            } else {
                // if no artworks, display placeholder
                repeat(10) {
                    Image(
                        painter = painterResource(id = R.drawable.dickson),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(130.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
private fun MarqueeArtworkImage(artwork: Artwork) {
    var isLoaded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isLoaded) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    AsyncImage(
        model = artwork.imageUrl,
        contentDescription = artwork.name,
        placeholder = ColorPainter(Color.Gray.copy(0f)),
        error = painterResource(id = R.drawable.dickson),
        contentScale = ContentScale.Crop,
        onSuccess = { isLoaded = true },
        modifier = Modifier
            .size(130.dp)
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
    )
}

@Preview(showBackground = true)
@Composable
fun LandingScreenPreview() {
    ArtSphereTheme {
        LandingScreenContent(
            onNavigateToLogin = {},
            onNavigateToSignup = {},
            artworks = emptyList()
        )
    }
}
