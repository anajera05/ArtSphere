package com.example.artsphere.ui.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.artsphere.data.model.Conversation
import com.example.artsphere.ui.theme.ArtSphereTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    onConversationClick: (Conversation) -> Unit = {},
    viewModel: InboxViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    InboxScreenContent(
        uiState = uiState,
        onConversationClick = { conversation ->
            viewModel.markConversationAsRead(conversation.conversationId)
            onConversationClick(conversation)
        },
        onDeleteConversations = { conversationIds ->
            conversationIds.forEach { id ->
                viewModel.deleteConversation(id)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreenContent(
    uiState: InboxUiState,
    onConversationClick: (Conversation) -> Unit,
    onDeleteConversations: (Set<String>) -> Unit
) {
    var isSelectMode by remember { mutableStateOf(false) }
    var selectedConversations by remember { mutableStateOf(setOf<String>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isSelectMode) "${selectedConversations.size} selected" else "Messages"
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    if (isSelectMode) {
                        IconButton(
                            onClick = {
                                if (selectedConversations.isNotEmpty()) {
                                    showDeleteDialog = true
                                }
                            },
                            enabled = selectedConversations.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                        IconButton(
                            onClick = {
                                isSelectMode = false
                                selectedConversations = setOf()
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    } else {
                        TextButton(
                            onClick = { isSelectMode = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text("Select")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                uiState.conversations.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "💬",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No messages yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Start a conversation by messaging an artist",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.conversations) { conversation ->
                            ConversationCard(
                                conversation = conversation,
                                isSelectMode = isSelectMode,
                                isSelected = selectedConversations.contains(conversation.conversationId),
                                onClick = {
                                    if (isSelectMode) {
                                        selectedConversations = if (selectedConversations.contains(conversation.conversationId)) {
                                            selectedConversations - conversation.conversationId
                                        } else {
                                            selectedConversations + conversation.conversationId
                                        }
                                    } else {
                                        onConversationClick(conversation)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Conversations") },
            text = {
                Text("Are you sure you want to delete ${selectedConversations.size} conversation(s)? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteConversations(selectedConversations)
                        showDeleteDialog = false
                        isSelectMode = false
                        selectedConversations = setOf()
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
            },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.Red
                )
            }
        )
    }
}

@Composable
fun ConversationCard(
    conversation: Conversation,
    isSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.tertiary else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectMode) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .border(
                            width = 2.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            AsyncImage(
                model = conversation.artworkImageUrl,
                contentDescription = conversation.artworkName,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.otherUserName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (conversation.unreadCount > 0 && !isSelectMode) {
                        Badge(
                            containerColor = Color(0xFFE91E63)
                        ) {
                            Text(
                                text = conversation.unreadCount.toString(),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = conversation.artworkName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = conversation.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (conversation.unreadCount > 0) Color.Black else Color.Gray,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formatTimestamp(conversation.lastMessageTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InboxScreenPreview() {
    ArtSphereTheme {
        InboxScreenContent(
            uiState = InboxUiState(
                conversations = listOf(
                    Conversation(
                        conversationId = "1",
                        otherUserId = "u1",
                        otherUserName = "Alice Artist",
                        artworkId = "a1",
                        artworkName = "Sunset in Paris",
                        artworkImageUrl = "",
                        lastMessage = "Is this still available?",
                        lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 5, // 5 mins ago
                        unreadCount = 2
                    ),
                    Conversation(
                        conversationId = "2",
                        otherUserId = "u2",
                        otherUserName = "Bob Painter",
                        artworkId = "a2",
                        artworkName = "Abstract Blue",
                        artworkImageUrl = "",
                        lastMessage = "Thanks for your interest!",
                        lastMessageTime = System.currentTimeMillis() - 1000 * 60 * 60 * 24, // 1 day ago
                        unreadCount = 0
                    )
                )
            ),
            onConversationClick = {},
            onDeleteConversations = {}
        )
    }
}
