package com.example.booknest.ui.notifications

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.FilterChip
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import com.example.booknest.ui.components.BackButton
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.viewmodel.notifications.NotificationViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import com.example.booknest.ui.notifications.components.empty.EmptyNotificationsState
import com.example.booknest.ui.notifications.components.item.NotificationItem
import com.example.booknest.ui.notifications.utils.handleNotificationNavigation
import com.example.booknest.ui.components.BackgroundDecoration

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

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BackgroundDecoration(modifier = Modifier.fillMaxSize())

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

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                    isLoading && notifications.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
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
                            contentPadding = PaddingValues(
                                start = 8.dp,
                                top = 8.dp,
                                end = 8.dp,
                                bottom = 12.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(notifications) { notification ->
                                NotificationItem(
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
