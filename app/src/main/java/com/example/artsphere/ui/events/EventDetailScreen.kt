package com.example.artsphere.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.artsphere.data.model.Event
import com.example.artsphere.ui.theme.ArtSphereTheme
import com.google.firebase.auth.FirebaseAuth

/**
 * Main composable for displaying detailed information about a specific event.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This composable determines the user's relationship to the event (organizer, participant,
 * or neither) and passes this information to the content composable along with appropriate
 * callbacks for event actions (delete, join, leave).
 *
 * @param event The Event object containing all event details to display.
 * @param onBackClick Callback invoked when the user navigates back from this screen.
 * @param viewModel The EventViewModel that handles event operations.
 */
@Composable
fun EventDetailScreen(
    event: Event,
    onBackClick: () -> Unit,
    viewModel: EventViewModel
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isOrganizer = event.organizerId == currentUserId
    val isParticipant = event.participantIds.contains(currentUserId)

    EventDetailScreenContent(
        event = event,
        isOrganizer = isOrganizer,
        isParticipant = isParticipant,
        currentUserId = currentUserId,
        onBackClick = onBackClick,
        onDeleteClick = {
             viewModel.deleteEvent(event.id) {
                 onBackClick()
             }
        },
        onJoinClick = {
             if (!event.isFull) {
                 viewModel.joinEvent(event.id)
             }
        },
        onLeaveClick = {
            viewModel.leaveEvent(event.id)
        }
    )
}

/**
 * Content composable that displays the event details UI.
 *
 * KDoc generated with AI; reviewed and modified for accuracy.
 *
 * This composable renders a comprehensive event details screen including:
 * - Event image or placeholder
 * - Title and category
 * - Date, time, and location information
 * - Event description
 * - Organizer details
 * - Participant count and list
 * - Join/Leave/Delete buttons based on user permissions
 *
 * @param event The Event object to display.
 * @param isOrganizer True if the current user is the event organizer.
 * @param isParticipant True if the current user is registered as a participant.
 * @param currentUserId The ID of the currently authenticated user, or null if not logged in.
 * @param onBackClick Callback invoked when the back button is pressed.
 * @param onDeleteClick Callback invoked when the organizer confirms event deletion.
 * @param onJoinClick Callback invoked when a user clicks to join the event.
 * @param onLeaveClick Callback invoked when a participant clicks to leave the event.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreenContent(
    event: Event,
    isOrganizer: Boolean,
    isParticipant: Boolean,
    currentUserId: String?,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onJoinClick: () -> Unit,
    onLeaveClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showParticipantsSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isOrganizer) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
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
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Event Image
            if (event.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFFE8DEF8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title & Category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text(event.categoryEnum.displayName, color = MaterialTheme.colorScheme.onSecondary ) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                }

                HorizontalDivider()

                // Date & Time
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    "Date",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    event.date,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    "Time",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    event.time,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Location
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "Location",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                event.location,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Description
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }

                HorizontalDivider()

                // Organizer
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Column {
                            Text(
                                "Organized by",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                event.organizerName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiary
                            )
                            Text(
                                event.organizerEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Participants
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    ),
                    onClick = {
                        showParticipantsSheet = true
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    "Participants",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    if (event.maxParticipants > 0) {
                                        "${event.participantIds.size} / ${event.maxParticipants}"
                                    } else {
                                        "${event.participantIds.size} registered"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }

                // Join/Leave Button
                if (!isOrganizer) {
                    Button(
                        onClick = {
                            if (isParticipant) {
                                onLeaveClick()
                            } else {
                                onJoinClick()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isParticipant) Color.Gray else MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color.LightGray
                        ),
                        enabled = isParticipant || !event.isFull
                    ) {
                        Icon(
                            imageVector = if (isParticipant) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer

                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when {
                                isParticipant -> "Leave Event"
                                event.isFull -> "Event Full"
                                else -> "Join Event"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text="Delete Event", color = MaterialTheme.colorScheme.onSecondary) },
            text = { Text("Are you sure you want to delete this event? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showParticipantsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showParticipantsSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Participants (${event.participantIds.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (event.participants.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No participants yet",
                            color = Color.Gray
                        )
                    }
                } else {
                    event.participants.forEach { participant ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        if (participant.userId == currentUserId) {
                                            "${participant.name} (You)"
                                        } else {
                                            participant.name
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (participant.email.isNotEmpty()) {
                                        Text(
                                            participant.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EventDetailScreenPreview() {
    val dummyEvent = Event(
        id = "1",
        title = "Gallery Opening Night",
        description = "Join us for an evening of fine art and wine. Meet the artists and explore the latest collection.",
        date = "Oct 25, 2023",
        time = "18:00",
        location = "ArtSphere Gallery, Downtown",
        organizerName = "ArtSphere Team",
        organizerEmail = "events@artsphere.com",
        maxParticipants = 50,
        participantIds = List(12) { "user_$it" }
    )

    ArtSphereTheme {
        EventDetailScreenContent(
            event = dummyEvent,
            isOrganizer = false,
            isParticipant = false,
            currentUserId = null,
            onBackClick = {},
            onDeleteClick = {},
            onJoinClick = {},
            onLeaveClick = {}
        )
    }
}
