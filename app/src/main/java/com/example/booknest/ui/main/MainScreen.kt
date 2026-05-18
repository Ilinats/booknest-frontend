package com.example.booknest.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.res.stringResource
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
import com.example.booknest.R
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.repository.AuthRepository
import com.example.booknest.navigation.BottomBarScreen
import com.example.booknest.navigation.HomeNavGraph
import com.example.booknest.navigation.consumeNotificationLaunchExtras
import com.example.booknest.navigation.readNotificationLaunchExtras
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.viewmodel.main.MainViewModel
import com.example.booknest.viewmodel.notifications.NotificationViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    sessionManager: SessionManager = koinInject(),
    authRepository: AuthRepository = koinInject(),
    mainViewModel: MainViewModel = getViewModel()
) {
    val navController = rememberNavController()
    val currentUser by sessionManager.currentUser.collectAsState()

    val notificationViewModel: NotificationViewModel = getViewModel()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState()

    androidx.compose.runtime.LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == true && currentUser == null) {
            mainViewModel.fetchCurrentUser()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(isLoggedIn) {
        if (isLoggedIn != true) return@LaunchedEffect
        val intent = (context as? android.app.Activity)?.intent ?: return@LaunchedEffect
        val extras = readNotificationLaunchExtras(intent)
        if (!extras.hasNotificationDeepLink) return@LaunchedEffect
        kotlinx.coroutines.delay(500)
        try {
            navController.navigate(Screen.Notifications.route) {
                popUpTo(BottomBarScreen.Home.route) { inclusive = false }
                launchSingleTop = true
            }
            intent.consumeNotificationLaunchExtras()
        } catch (_: Exception) {
        }
    }

    androidx.compose.runtime.LaunchedEffect(isLoggedIn, currentRoute) {
        if (isLoggedIn == true && currentRoute != null) {
            val token = sessionManager.getToken()
            if (token.isEmpty()) {
                return@LaunchedEffect
            }

            val isMainScreen = currentRoute == BottomBarScreen.Home.route ||
                    currentRoute == BottomBarScreen.MyApplications.route ||
                    currentRoute.startsWith("browse")

            if (isMainScreen) {
                notificationViewModel.loadUnreadCount()
                if (currentUser == null) {
                    mainViewModel.fetchCurrentUser()
                }
            }
        }
    }

    val shouldShowBars = currentRoute == BottomBarScreen.Home.route ||
            currentRoute == BottomBarScreen.MyApplications.route ||
            currentRoute == "browse" ||
            (currentRoute != null && currentRoute.startsWith("browse/"))

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (shouldShowBars) {
                MainTopBar(
                    currentUser = currentUser,
                    onFriendsClick = {
                        navController.navigate(Screen.Friends.route)
                    },
                    onNotificationsClick = {
                        navController.navigate(Screen.Notifications.route)
                    },
                    onAccountClick = {
                        navController.navigate(Screen.Profile.createRoute(null))
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.PrivacySettings.route)
                    },
                    onFavoriteGenresClick = {
                        navController.navigate(Screen.FavoriteGenres.route)
                    },
                    onSignOut = {
                    },
                    sessionManager = sessionManager,
                    authRepository = authRepository,
                    unreadCount = unreadCount
                )
            }
        },
        bottomBar = {
            if (shouldShowBars) {
                BottomBar(navController = navController)
            }
        }
    ) { paddingValues ->
        HomeNavGraph(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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

    Surface(
        shadowElevation = 8.dp,
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            screens.forEach { screen ->
                AddItem(
                    screen = screen,
                    currentDestination = currentDestination,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomBarScreen,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    val isSelected = currentDestination?.hierarchy?.any {
        it.route == screen.route
    } == true

    NavigationBarItem(
        label = { Text(text = screen.title) },
        icon = {
            Icon(
                imageVector = screen.icon,
                contentDescription = screen.title
            )
        },
        selected = isSelected,
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    currentUser: UserResponse?,
    onFriendsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAccountClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onFavoriteGenresClick: () -> Unit,
    onSignOut: () -> Unit,
    sessionManager: SessionManager,
    authRepository: AuthRepository,
    unreadCount: Int = 0
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val cdNotifications = stringResource(R.string.cd_notifications)
    val cdFriends = stringResource(R.string.cd_friends)
    val cdProfileMenu = stringResource(R.string.cd_profile_menu)

    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "BookNest",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = cdNotifications
                        )
                    }
                }

                IconButton(
                    onClick = onFriendsClick,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Group,
                        contentDescription = cdFriends
                    )
                }

                Box(modifier = Modifier.size(48.dp)) {
                    ProfileAvatar(
                        user = currentUser,
                        contentDescription = cdProfileMenu,
                        onClick = { menuExpanded = true }
                    )
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        Modifier.background(MaterialTheme.colorScheme.surface)
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
                                    sessionManager.logout(authRepository)
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
private fun ProfileAvatar(
    user: UserResponse?,
    contentDescription: String,
    onClick: () -> Unit
) {
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
            .fillMaxSize()
            .clip(CircleShape)
            .background(placeholderColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val avatarUrl = user?.profilePictureUrl ?: user?.avatarUrl
        if (!avatarUrl.isNullOrEmpty()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
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
