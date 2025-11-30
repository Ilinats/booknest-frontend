package com.example.booknest.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.booknest.data.AuthManager
import com.example.booknest.navigation.BottomBarScreen
import com.example.booknest.navigation.HomeNavGraph
import com.example.booknest.navigation.Screen
import com.example.booknest.network.UserData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(authManager: AuthManager) {
    val navController = rememberNavController()
    val currentUser by authManager.currentUser.collectAsState(initial = null)
    
    // Load unread count for notification badge
    val notificationViewModel: com.example.booknest.viewmodel.NotificationViewModel = 
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = com.example.booknest.viewmodel.NotificationViewModelFactory(authManager)
        )
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val isLoggedIn by authManager.isLoggedIn.collectAsState()
    
    // Get current route to detect when MainScreen is displayed
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Check if app was opened from a push notification and navigate to notifications screen
    val context = LocalContext.current
    
    // Check intent for notification extras whenever MainScreen is displayed and user is logged in
    androidx.compose.runtime.LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && context is android.app.Activity) {
            val intent = context.intent
            val notificationId = intent?.getStringExtra("notificationId")
            val notificationType = intent?.getStringExtra("notificationType")
            val hasNotificationExtras = notificationId != null || notificationType != null
            
            if (hasNotificationExtras) {
                // Wait a bit for navigation graph to be ready
                kotlinx.coroutines.delay(500)
                try {
                    navController.navigate(Screen.Notifications.route) {
                        // Pop back to home if we're not already there
                        popUpTo(BottomBarScreen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                    println("DEBUG: Navigated to notifications screen from push notification (ID: $notificationId, Type: $notificationType)")
                    // Clear the intent extras to prevent re-navigation
                    intent.removeExtra("notificationId")
                    intent.removeExtra("notificationType")
                } catch (e: Exception) {
                    println("DEBUG: Error navigating to notifications from push: ${e.message}")
                }
            }
        }
    }
    
    // Load unread count when user is logged in and whenever navigating to a main screen
    // Refresh whenever the route changes to any main screen (Home, MyApplications, Browse)
    androidx.compose.runtime.LaunchedEffect(isLoggedIn, currentRoute) {
        if (isLoggedIn && currentRoute != null) {
            // Check if we're on one of the main bottom bar screens
            val isMainScreen = currentRoute == BottomBarScreen.Home.route ||
                    currentRoute == BottomBarScreen.MyApplications.route ||
                    currentRoute.startsWith("browse")
            
            if (isMainScreen) {
                notificationViewModel.loadUnreadCount()
            }
        }
    }

    Scaffold(
        topBar = {
            MainTopBar(
                currentUser = currentUser,
                onFriendsClick = {
                    navController.navigate(Screen.Friends.route)
                },
                onNotificationsClick = {
                    navController.navigate(Screen.Notifications.route)
                },
                onAccountClick = {
                    currentUser?.id?.let { navController.navigate(Screen.Profile.createRoute(it)) }
                },
                onSettingsClick = {
                    navController.navigate(Screen.PrivacySettings.route)
                },
                onFavoriteGenresClick = {
                    navController.navigate(Screen.FavoriteGenres.route)
                },
                onSignOut = {
                    // Logout is handled by AuthManager; keep user in home graph for now
                    navController.navigate(BottomBarScreen.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                authManager = authManager,
                unreadCount = unreadCount
            )
        },
        bottomBar = { BottomBar(navController = navController) }
    ) { paddingValues ->
        HomeNavGraph(
            navController = navController, 
            authManager = authManager,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.MyApplications,
        BottomBarScreen.Browse
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        screens.forEach { screen ->
            AddItem(screen = screen, currentDestination = currentDestination, navController = navController)
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomBarScreen,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    NavigationBarItem(
        label = { Text(text = screen.title) },
        icon = { Icon(imageVector = screen.icon, contentDescription = "Navigation Icon") },
        selected = currentDestination?.hierarchy?.any {
            it.route == screen.route
        } == true,
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    currentUser: UserData?,
    onFriendsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAccountClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFavoriteGenresClick: () -> Unit,
    onSignOut: () -> Unit,
    authManager: AuthManager,
    unreadCount: Int = 0
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // BookNest title on the left
            Text(
                text = "BookNest",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            // Icons on the right
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Notifications with badge
            BadgedBox(
                badge = {
                    if (unreadCount > 0) {
                        Badge {
                            Text(
                                text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            ) {
                IconButton(onClick = onNotificationsClick) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications"
                    )
            }
            }

                // Friends icon
            IconButton(onClick = onFriendsClick) {
                Icon(
                    imageVector = Icons.Outlined.Group,
                    contentDescription = "Friends"
                )
            }

                // Profile avatar with dropdown menu
            Box {
                ProfileAvatar(user = currentUser, onClick = { menuExpanded = true })
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Account") },
                        onClick = {
                            menuExpanded = false
                            onAccountClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = {
                            menuExpanded = false
                            onSettingsClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Friends") },
                        onClick = {
                            menuExpanded = false
                            onFriendsClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Favorite Genres") },
                        onClick = {
                            menuExpanded = false
                            onFavoriteGenresClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Sign out", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            coroutineScope.launch {
                                authManager.logout()
                                onSignOut()
                            }
                        }
                    )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(user: UserData?, onClick: () -> Unit) {
    val placeholderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    val initials = remember(user) {
        val source = when {
            !user?.firstName.isNullOrBlank() -> user?.firstName
            !user?.username.isNullOrBlank() -> user?.username
            else -> null
        }
        source?.firstOrNull()?.uppercaseChar()?.toString()
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(placeholderColor)
            .clickable { onClick() }
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        val avatarUrl = user?.profilePictureUrl ?: user?.avatarUrl
        if (!avatarUrl.isNullOrEmpty()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Profile",
                modifier = Modifier
                    .clip(CircleShape)
                    .size(36.dp)
                    .background(Color.Transparent)
            )
        } else {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .matchParentSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials ?: "U",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
