package com.example.booknest.ui.author

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.booknest.data.session.SessionManager
import com.example.booknest.navigation.AuthorBottomBarScreen
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.applications.BookApplicationDetailScreen
import com.example.booknest.ui.author.AuthorHomeScreen
import com.example.booknest.ui.author.AuthorProfileScreen
import com.example.booknest.ui.author.BookCreationWizard
import com.example.booknest.ui.author.BookEditScreen
import com.example.booknest.ui.author.MyBooksScreen
import com.example.booknest.ui.author.SeriesManagementScreen
import com.example.booknest.ui.profile.ProfileEditScreen
import com.example.booknest.ui.profile.ProfileScreen
import com.example.booknest.ui.profile.StatsScreen
import com.example.booknest.ui.profile.PrivacySettingsScreen
import com.example.booknest.ui.profile.SocialMediaManagementScreen
import com.example.booknest.ui.analytics.AuthorAnalyticsScreen
import com.example.booknest.ui.analytics.BookAnalyticsScreen
import com.example.booknest.ui.books.BookDetailsScreen
import com.example.booknest.ui.books.SeriesBooksScreen
import com.example.booknest.ui.auth.PasswordResetScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorMainScreen(sessionManager: SessionManager) {
    val navController = rememberNavController()
    val currentUser by sessionManager.currentUser.collectAsState()
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val arguments = navBackStackEntry?.arguments

    val viewingOtherProfile = when {
        currentRoute?.startsWith("profile/") == true -> {
            arguments?.getString("userId")?.let { userId ->
                userId != currentUser?.id && userId != currentUser?.username
            } ?: false
        }

        currentRoute == "profile" -> false
        else -> false
    }

    val shouldShowBottomBar =
        currentRoute != "profile_edit" &&
                currentRoute != "social_media_management" &&
                currentRoute?.startsWith(Screen.BookEdit.route.substringBefore("/")) != true &&
                !viewingOtherProfile

    androidx.compose.runtime.LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == true && currentUser == null) {
            try {
                val profilesService = org.koin.core.context.GlobalContext.get()
                    .get<com.example.booknest.data.service.ProfilesService>()
                val response = profilesService.getMe()
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        sessionManager.updateUser(user)
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Failed to fetch user in AuthorMainScreen: ${e.message}")
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                AuthorBottomBar(navController = navController)
            }
        }
    ) { paddingValues ->
        AuthorNavGraph(
            navController = navController,
            sessionManager = sessionManager,
            modifier = Modifier.fillMaxSize()
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

@Composable
fun AuthorNavGraph(
    navController: NavHostController,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AuthorBottomBarScreen.Home.route,
        modifier = modifier
    ) {
        composable(route = AuthorBottomBarScreen.Home.route) {
            AuthorHomeScreen(navController, sessionManager)
        }
        composable(route = AuthorBottomBarScreen.MyBooks.route) {
            MyBooksScreen(navController, sessionManager)
        }
        composable(route = AuthorBottomBarScreen.Profile.route) {
            AuthorProfileScreen(navController, sessionManager)
        }

        composable(route = Screen.BookCreation.route) {
            BookCreationWizard(navController, sessionManager)
        }

        composable(route = Screen.SeriesManagement.route) {
            SeriesManagementScreen(navController, sessionManager)
        }

        composable(
            route = Screen.BookApplicationDetail.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookApplicationDetailScreen(navController, sessionManager, bookId)
        }

        composable(route = "profile") {
            ProfileScreen(navController, sessionManager, null)
        }
        composable(
            route = "profile/{userId}",
            arguments = listOf(navArgument("userId") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProfileScreen(navController, sessionManager, userId)
        }

        composable("profile_edit") {
            ProfileEditScreen(navController, sessionManager)
        }

        composable(route = "stats") {
            StatsScreen(navController, sessionManager, null)
        }
        composable(
            route = "stats/{authorId}",
            arguments = listOf(navArgument("authorId") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val authorId = backStackEntry.arguments?.getString("authorId")
            StatsScreen(navController, sessionManager, authorId)
        }

        composable(
            route = Screen.BookEdit.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookEditScreen(navController, bookId, sessionManager)
        }

        composable(
            route = "book_analytics/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookAnalyticsScreen(navController, sessionManager, bookId)
        }

        composable(Screen.AuthorAnalytics.route) {
            AuthorAnalyticsScreen(navController, sessionManager)
        }

        composable(
            route = Screen.BookDetails.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookDetailsScreen(navController, sessionManager, bookId)
        }

        composable(
            route = "series_books/{seriesId}?seriesName={seriesName}",
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("seriesName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getString("seriesId") ?: ""
            val seriesName = backStackEntry.arguments?.getString("seriesName")
            SeriesBooksScreen(navController, seriesId, seriesName)
        }

        composable(Screen.PrivacySettings.route) {
            PrivacySettingsScreen(navController, sessionManager)
        }

        composable(Screen.SocialMediaManagement.route) {
            SocialMediaManagementScreen(navController, sessionManager)
        }

        composable(
            route = "user_reviews/{userId}?userName={userName}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("userName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val encodedUserName = backStackEntry.arguments?.getString("userName")
            val userName = encodedUserName?.let {
                try {
                    android.net.Uri.decode(it)
                } catch (e: Exception) {
                    it
                }
            }
            com.example.booknest.ui.reviews.UserReviewsScreen(navController, userId, userName)
        }

        composable(
            route = Screen.PasswordReset.route,
            arguments = listOf(navArgument("email") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            PasswordResetScreen(navController, sessionManager, email)
        }
    }
}
