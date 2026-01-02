package com.example.booknest.ui.notifications

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.booknest.ui.theme.SkyBluePeriwinkle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.material.icons.Icons
import com.example.booknest.ui.components.BackButton
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.NotificationResponse
import com.example.booknest.navigation.Screen
import com.example.booknest.viewmodel.NotificationViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    notificationViewModel: NotificationViewModel = getViewModel()
) {
    val notifications by notificationViewModel.notifications.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val isLoading by notificationViewModel.isLoading.collectAsState()
    val error by notificationViewModel.error.collectAsState()
    val processingNotifications by notificationViewModel.processingNotifications.collectAsState()
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState()

    var showUnreadOnly by remember { mutableStateOf(false) }
    var showClearAllDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == true) {
            notificationViewModel.loadNotifications(refresh = true)
            notificationViewModel.loadUnreadCount()
        }
    }

    LaunchedEffect(showUnreadOnly, isLoggedIn) {
        if (isLoggedIn == true) {
            notificationViewModel.loadNotifications(unreadOnly = showUnreadOnly, refresh = true)
        }
    }

    LaunchedEffect(Unit) {
        notificationViewModel.error.collectLatest { errorMessage ->
            errorMessage?.let {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.shadow(elevation = 4.dp)
            ) {
                TopAppBar(
                    title = { Text("Notifications") },
                    navigationIcon = {
                        BackButton(onClick = { navController.popBackStack() })
                    },
                    actions = {
                        if (notifications.isNotEmpty()) {
                            TextButton(
                                onClick = { showClearAllDialog = true }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear all")
                            }
                        }
                        if (unreadCount > 0) {
                            TextButton(
                                onClick = {
                                    notificationViewModel.markAllAsRead()
                                }
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mark all read")
                            }
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1E9EE))
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-175).dp, y = (-175).dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(SkyBluePeriwinkle.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-135).dp, y = (-135).dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(SkyBluePeriwinkle)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 175.dp, y = 175.dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(SkyBluePeriwinkle.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 135.dp, y = 135.dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(SkyBluePeriwinkle)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !showUnreadOnly,
                        onClick = { showUnreadOnly = false },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = showUnreadOnly,
                        onClick = { showUnreadOnly = true },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Unread only")
                                if (unreadCount > 0) {
                                    Badge {
                                        Text(unreadCount.toString())
                                    }
                                }
                            }
                        }
                    )
                }

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
                        EmptyNotificationsState(
                            showUnreadOnly = showUnreadOnly,
                            onNavigateToSettings = {
                                navController.navigate(Screen.PrivacySettings.route)
                            }
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(notifications) { notification ->
                                SwipeableNotificationItem(
                                    notification = notification,
                                    onNotificationClick = {
                                        if (!notification.isRead) {
                                            notificationViewModel.markAsRead(notification.id)
                                        }
                                        handleNotificationNavigation(navController, notification)
                                    },
                                    onAcceptFriendRequest = { requesterId: String ->
                                        notificationViewModel.acceptFriendRequest(
                                            requesterId,
                                            notification.id
                                        )
                                    },
                                    onDeclineFriendRequest = { requesterId: String ->
                                        notificationViewModel.declineFriendRequest(
                                            requesterId,
                                            notification.id
                                        )
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

                if (showClearAllDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearAllDialog = false },
                        title = { Text("Clear All Notifications") },
                        text = {
                            Text("Are you sure you want to delete all notifications? This action cannot be undone.")
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showClearAllDialog = false
                                    notificationViewModel.deleteAllNotifications()
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Clear All")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearAllDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SwipeableNotificationItem(
    notification: NotificationResponse,
    onNotificationClick: () -> Unit,
    onAcceptFriendRequest: (String) -> Unit = {},
    onDeclineFriendRequest: (String) -> Unit = {},
    onDeleteClick: () -> Unit = {},
    isProcessing: Boolean = false
) {
    NotificationItem(
        notification = notification,
        onNotificationClick = onNotificationClick,
        onAcceptFriendRequest = onAcceptFriendRequest,
        onDeclineFriendRequest = onDeclineFriendRequest,
        onDeleteClick = onDeleteClick,
        isProcessing = isProcessing
    )
}

@Composable
fun EmptyNotificationsState(
    showUnreadOnly: Boolean,
    onNavigateToSettings: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.NotificationsOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (showUnreadOnly) "No unread notifications" else "All caught up!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (showUnreadOnly)
                    "You're all caught up with your notifications"
                else
                    "You have no notifications at the moment",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Notification Settings")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationItem(
    notification: NotificationResponse,
    onNotificationClick: () -> Unit,
    onAcceptFriendRequest: (String) -> Unit = {},
    onDeclineFriendRequest: (String) -> Unit = {},
    onDeleteClick: () -> Unit = {},
    isProcessing: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = if (notification.isRead) 1.dp else 2.dp,
        shadowElevation = if (notification.isRead) 2.dp else 4.dp,
        color = if (notification.isRead) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = notification.type != "friend_request_received",
                            onClick = onNotificationClick
                        ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
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
                                fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!notification.isRead) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error
                                ) {
                                    Text("")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = notification.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (notification.type == "friend_request_received") 2 else 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = formatNotificationTime(notification.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                if (notification.type == "friend_request_received") {
                    Spacer(modifier = Modifier.height(12.dp))
                    NotificationActions(
                        notification = notification,
                        onAcceptFriendRequest = onAcceptFriendRequest,
                        onDeclineFriendRequest = onDeclineFriendRequest,
                        onDeleteClick = onDeleteClick,
                        onNotificationClick = onNotificationClick,
                        isProcessing = isProcessing
                    )
                }
            }

            if (notification.type != "friend_request_received") {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
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

@Composable
fun NotificationActions(
    notification: NotificationResponse,
    onAcceptFriendRequest: (String) -> Unit,
    onDeclineFriendRequest: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onNotificationClick: () -> Unit,
    isProcessing: Boolean
) {
    if (notification.type == "friend_request_received" && notification.relatedUserId != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onDeclineFriendRequest(notification.relatedUserId!!) },
                enabled = !isProcessing,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Decline")
                }
            }
            Button(
                onClick = { onAcceptFriendRequest(notification.relatedUserId!!) },
                enabled = !isProcessing,
                modifier = Modifier.weight(1f)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept")
                }
            }
        }
    }
}

private fun handleNotificationNavigation(
    navController: NavController,
    notification: NotificationResponse
) {
    when (notification.type) {
        "friend_request_received", "friend_request_accepted" -> {
            notification.relatedUserId?.let { userId ->
                navController.navigate(Screen.Profile.createRoute(userId))
            } ?: run {
                navController.navigate(Screen.Friends.route)
            }
        }

        "application_approved", "application_rejected" -> {
            notification.applicationId?.let { applicationId ->
                navController.navigate("my_applications")
            } ?: notification.bookId?.let { bookId ->
                navController.navigate(Screen.BookDetails.createRoute(bookId))
            }
        }

        "review_deadline_reminder" -> {
            notification.applicationId?.let { applicationId ->
                navController.navigate("review_submission/$applicationId")
            } ?: notification.bookId?.let { bookId ->
                navController.navigate(Screen.BookDetails.createRoute(bookId))
            }
        }

        "author_book_published" -> {
            notification.bookId?.let { bookId ->
                navController.navigate(Screen.BookDetails.createRoute(bookId))
            }
        }

        "book_copy_sent" -> {
            notification.applicationId?.let { applicationId ->
                navController.navigate("my_applications")
            } ?: notification.bookId?.let { bookId ->
                navController.navigate(Screen.BookDetails.createRoute(bookId))
            }
        }

        "new_review_on_book", "application_received" -> {
            notification.bookId?.let { bookId ->
                navController.navigate(Screen.BookDetails.createRoute(bookId))
            }
        }
    }
}

