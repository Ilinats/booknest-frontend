package com.example.booknest.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.navigation.AuthorBottomBarScreen
import com.example.booknest.navigation.AuthorNavGraph
import com.example.booknest.navigation.NotificationLaunchEffect
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.viewmodel.main.MainViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorMainScreen(
    sessionManager: SessionManager = koinInject(),
    mainViewModel: MainViewModel = getViewModel()
) {
    val navController = rememberNavController()
    val currentUser by sessionManager.currentUser.collectAsState()
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val canPopInnerStack = navController.previousBackStackEntry != null
    BackHandler(enabled = !canPopInnerStack) {
        // Consume system back at tab root so the outer graph never reveals login/landing.
    }
    val currentRoute = navBackStackEntry?.destination?.route
    val arguments = navBackStackEntry?.arguments

    val viewingOtherProfile = when {
        currentRoute == Screen.Profile.route -> {
            arguments?.getString("userId")?.let { userId ->
                userId != currentUser?.id && userId != currentUser?.username
            } ?: false
        }
        else -> false
    }

    val shouldShowBottomBar =
        currentRoute != Screen.ProfileEdit.route &&
                currentRoute != Screen.SocialMediaManagement.route &&
                currentRoute?.startsWith(Screen.BookEdit.route.substringBefore("/")) != true &&
                !viewingOtherProfile

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == true && currentUser == null) {
            mainViewModel.fetchCurrentUser()
        }
    }

    NotificationLaunchEffect(
        navController = navController,
        isLoggedIn = isLoggedIn,
        popUpToRoute = AuthorBottomBarScreen.Home.route,
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (shouldShowBottomBar) {
                AuthorBottomBar(navController = navController)
            }
        }
    ) { paddingValues ->
        AuthorNavGraph(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
fun AuthorBottomBar(navController: NavHostController) {
    val screens = listOf(
        AuthorBottomBarScreen.Home,
        AuthorBottomBarScreen.MyBooks,
        AuthorBottomBarScreen.Profile,
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
    screen: AuthorBottomBarScreen,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    val isSelected = currentDestination?.hierarchy?.any {
        it.route == screen.route
    } == true

    NavigationBarItem(
        label = { Text(text = screen.title) },
        icon = { Icon(imageVector = screen.icon, contentDescription = "Navigation Icon") },
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
