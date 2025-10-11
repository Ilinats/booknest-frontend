package com.example.booknest.ui.author

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.example.booknest.data.AuthManager
import com.example.booknest.navigation.AuthorBottomBarScreen
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.applications.BookApplicationDetailScreen
import com.example.booknest.ui.author.AuthorHomeScreen
import com.example.booknest.ui.author.AuthorProfileScreen
import com.example.booknest.ui.author.BookCreationWizard
import com.example.booknest.ui.author.MyBooksScreen
import com.example.booknest.ui.author.SeriesManagementScreen
import com.example.booknest.ui.profile.ProfileEditScreen
import com.example.booknest.ui.profile.ProfileScreen
import com.example.booknest.ui.profile.StatsScreen
import com.example.booknest.ui.analytics.AuthorAnalyticsScreen
import com.example.booknest.ui.analytics.BookAnalyticsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorMainScreen(authManager: AuthManager) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { AuthorBottomBar(navController = navController) }
    ) { paddingValues ->
        AuthorNavGraph(
            navController = navController, 
            authManager = authManager,
            modifier = Modifier.padding(paddingValues)
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

    NavigationBar {
        screens.forEach { screen ->
            AddItem(screen = screen, currentDestination = currentDestination, navController = navController)
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: AuthorBottomBarScreen,
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

@Composable
fun AuthorNavGraph(
    navController: NavHostController, 
    authManager: AuthManager,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController, 
        startDestination = AuthorBottomBarScreen.Home.route, 
        modifier = modifier
    ) {
        composable(route = AuthorBottomBarScreen.Home.route) {
            AuthorHomeScreen(navController, authManager)
        }
        composable(route = AuthorBottomBarScreen.MyBooks.route) {
            MyBooksScreen(navController, authManager)
        }
        composable(route = AuthorBottomBarScreen.Profile.route) {
            AuthorProfileScreen(navController, authManager)
        }
        
        // Author-specific screens
        composable(route = Screen.BookCreation.route) {
            BookCreationWizard(navController, authManager)
        }
        
        composable(route = Screen.SeriesManagement.route) {
            SeriesManagementScreen(navController, authManager)
        }
        
        composable(
            route = Screen.BookApplicationDetail.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookApplicationDetailScreen(navController, authManager, bookId)
        }
        
        // Profile and Stats screens
        composable(
            route = "profile/{userId?}",
            arguments = listOf(navArgument("userId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProfileScreen(navController, authManager, userId)
        }
        
        composable("profile_edit") {
            ProfileEditScreen(navController, authManager)
        }
        
        composable(
            route = "stats/{authorId?}",
            arguments = listOf(navArgument("authorId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val authorId = backStackEntry.arguments?.getString("authorId")
            StatsScreen(navController, authManager, authorId)
        }
        
        // Analytics screens
        composable(
            route = "book_analytics/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookAnalyticsScreen(navController, authManager, bookId)
        }
        
        composable("author_analytics") {
            AuthorAnalyticsScreen(navController, authManager)
        }
    }
}
