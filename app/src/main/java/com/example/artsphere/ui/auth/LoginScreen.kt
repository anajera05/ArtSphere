package com.example.artsphere.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.artsphere.ui.components.StyledTextField
import kotlinx.coroutines.delay
import com.example.artsphere.ui.theme.ArtSphereTheme

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LoginContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = { viewModel.login(onLoginSuccess) },
        onNavigateToSignup = onNavigateToSignup
    )
}

@Composable
fun LoginContent(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    // Check if we are in preview mode
    val isPreview = LocalInspectionMode.current

    val infiniteTransition = rememberInfiniteTransition(label = "infinite transition")
    val animatedColor1 by infiniteTransition.animateColor(
        initialValue = MaterialTheme.colorScheme.surface,
        targetValue = MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedColor1"
    )
    val animatedColor2 by infiniteTransition.animateColor(
        initialValue = MaterialTheme.colorScheme.surfaceVariant,
        targetValue = MaterialTheme.colorScheme.surface,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animatedColor2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    Brush.linearGradient(
                        colors = listOf(animatedColor1, animatedColor2),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    )
                )
            }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Welcome Text
            // If in preview, show immediately. Else start false.
            var titleVisible by remember { mutableStateOf(isPreview) }
            LaunchedEffect(Unit) {
                if (!isPreview) {
                    delay(200)
                    titleVisible = true
                }
            }

            AnimatedVisibility(
                visible = titleVisible,
                enter = fadeIn(animationSpec = tween(1000)) +
                        slideInVertically(
                            initialOffsetY = { -100 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Welcome Back!",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Log in to continue",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            // Floating Login Card
            // If in preview, show immediately. Else start false.
            var cardVisible by remember { mutableStateOf(isPreview) }
            LaunchedEffect(Unit) {
                if (!isPreview) {
                    delay(400)
                    cardVisible = true
                }
            }

            AnimatedVisibility(
                visible = cardVisible,
                enter = fadeIn(animationSpec = tween(800)) +
                        scaleIn(
                            initialScale = 0.8f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(32.dp),
                            spotColor = Color.Black.copy(alpha = 0.4f)
                        ),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.8f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Email Field
                        StyledTextField(
                            value = uiState.email,
                            onValueChange = { onEmailChange(it) },
                            label = "Email",
                            icon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password Field
                        StyledTextField(
                            value = uiState.password,
                            onValueChange = { onPasswordChange(it) },
                            label = "Password",
                            icon = Icons.Default.Lock,
                            keyboardType = KeyboardType.Password,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
                        )


                        Spacer(modifier = Modifier.height(24.dp))

                        // Error Message
                        if (uiState.error != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = uiState.error ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Login Button
                        Button(
                            onClick = {
                                onLoginClick()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = "Log In",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sign Up Link
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "New to ArtSphere?",
                                color = Color.Gray
                            )
                            TextButton(onClick = onNavigateToSignup) {
                                Text(
                                    text = "Sign Up",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    ArtSphereTheme {
        LoginContent(
            uiState = AuthUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onNavigateToSignup = {}
        )
    }
}
