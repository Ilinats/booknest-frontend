package com.example.booknest.ui.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import com.example.booknest.ui.components.BackButton
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.booknest.data.AuthManager
import com.example.booknest.network.Notification
import com.example.booknest.navigation.Screen
import com.example.booknest.viewmodel.NotificationViewModel
import com.example.booknest.viewmodel.NotificationViewModelFactory
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    authManager: AuthManager,
    notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(authManager)
    )
) {
    val notifications by notificationViewModel.notifications.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val isLoading by notificationViewModel.isLoading.collectAsState()
    val error by notificationViewModel.error.collectAsState()
    val processingNotifications by notificationViewModel.processingNotifications.collectAsState()
    val isLoggedIn by authManager.isLoggedIn.collectAsState()
    
    var showUnreadOnly by remember { mutableStateOf(false) }
    
    // Only load notifications when user is logged in
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            notificationViewModel.loadNotifications(refresh = true)
            notificationViewModel.loadUnreadCount()
        }
    }
    
    LaunchedEffect(showUnreadOnly, isLoggedIn) {
        if (isLoggedIn) {
            notificationViewModel.loadNotifications(unreadOnly = showUnreadOnly, refresh = true)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = {
                                notificationViewModel.markAllAsRead()
                            }
                        ) {
                            Text("Mark all read")
                        }
                    }
                    TextButton(
                        onClick = { showUnreadOnly = !showUnreadOnly }
                    ) {
                        Text(if (showUnreadOnly) "Show all" else "Unread only")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading && notifications.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            notifications.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showUnreadOnly) "No unread notifications" else "No notifications",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationItem(
                            notification = notification,
                            onNotificationClick = {
                                // Mark as read when clicked
                                if (!notification.isRead) {
                                    notificationViewModel.markAsRead(notification.id)
                                }
                                // Navigate based on notification type
                                handleNotificationNavigation(navController, notification)
                            },
                            onAcceptFriendRequest = { requesterId ->
                                notificationViewModel.acceptFriendRequest(requesterId, notification.id)
                            },
                            onDeclineFriendRequest = { requesterId ->
                                notificationViewModel.declineFriendRequest(requesterId, notification.id)
                            },
                            onDeleteClick = {
                                notificationViewModel.deleteNotification(notification.id)
                            },
                            isProcessing = processingNotifications.contains(notification.id)
                        )
                    }
                }
            }
        }
        
        if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onNotificationClick: () -> Unit,
    onAcceptFriendRequest: (String) -> Unit = {},
    onDeclineFriendRequest: (String) -> Unit = {},
    onDeleteClick: () -> Unit = {},
    isProcessing: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNotificationClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 1.dp else 3.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!notification.isRead) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Unread",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = formatNotificationTime(notification.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            // Show Accept/Decline buttons for friend request notifications
            if (notification.type == "friend_request_received" && notification.relatedUserId != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            onDeclineFriendRequest(notification.relatedUserId!!)
                        },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(0.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Decline",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    Button(
                        onClick = { 
                            onAcceptFriendRequest(notification.relatedUserId!!)
                        },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(0.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Accept",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Accept",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            } else {
                // Show delete button for other notification types
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

private fun formatNotificationTime(createdAt: String): String {
    return try {
        val instant = Instant.parse(createdAt)
        val now = Instant.now()
        val minutesAgo = ChronoUnit.MINUTES.between(instant, now)
        val hoursAgo = ChronoUnit.HOURS.between(instant, now)
        val daysAgo = ChronoUnit.DAYS.between(instant, now)
        
        when {
            minutesAgo < 1 -> "Just now"
            minutesAgo < 60 -> "$minutesAgo ${if (minutesAgo == 1L) "minute" else "minutes"} ago"
            hoursAgo < 24 -> "$hoursAgo ${if (hoursAgo == 1L) "hour" else "hours"} ago"
            daysAgo < 7 -> "$daysAgo ${if (daysAgo == 1L) "day" else "days"} ago"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
                java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                    .format(formatter)
            }
        }
    } catch (e: Exception) {
        createdAt
    }
}

private fun handleNotificationNavigation(navController: NavController, notification: Notification) {
    when (notification.type) {
        "friend_request_received" -> {
            notification.relatedUserId?.let { userId ->
                navController.navigate(Screen.Profile.createRoute(userId))
            } ?: run {
                navController.navigate(Screen.Friends.route)
            }
        }
        "friend_request_accepted" -> {
            notification.relatedUserId?.let { userId ->
                navController.navigate(Screen.Profile.createRoute(userId))
            }
        }
        "application_approved", "application_rejected" -> {
            notification.bookId?.let { bookId ->
                navController.navigate("book_details/$bookId")
            } ?: notification.applicationId?.let { applicationId ->
                // Navigate to application details if we have that route
                navController.navigate("my_applications")
            }
        }
        "review_deadline_reminder" -> {
            notification.applicationId?.let { applicationId ->
                navController.navigate("review_submission/$applicationId")
            } ?: notification.bookId?.let { bookId ->
                navController.navigate("book_details/$bookId")
            }
        }
        "author_book_published" -> {
            notification.bookId?.let { bookId ->
                navController.navigate("book_details/$bookId")
            }
        }
    }
}

