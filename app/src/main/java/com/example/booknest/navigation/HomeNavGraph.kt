package com.example.booknest.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.booknest.data.AuthManager
import com.example.booknest.ui.applications.MyApplicationsScreen
import com.example.booknest.ui.books.BookDetailsScreen
import com.example.booknest.ui.books.BookListScreen
import com.example.booknest.ui.home.HomeScreen
import com.example.booknest.ui.profile.ProfileEditScreen
import com.example.booknest.ui.profile.ProfileScreen
import com.example.booknest.ui.profile.StatsScreen
import com.example.booknest.ui.analytics.AuthorAnalyticsScreen
import com.example.booknest.ui.analytics.BookAnalyticsScreen
import com.example.booknest.ui.friends.FriendsScreen
import com.example.booknest.ui.profile.PrivacySettingsScreen
import com.example.booknest.ui.profile.SocialMediaManagementScreen
import com.example.booknest.ui.reviews.ReviewSubmissionScreen

@Composable
fun HomeNavGraph(
    navController: NavHostController, 
    authManager: AuthManager,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = BottomBarScreen.Home.route, modifier = modifier) {
        composable(route = BottomBarScreen.Home.route) {
            HomeScreen(navController, authManager)
        }
        composable(route = BottomBarScreen.MyApplications.route) {
            MyApplicationsScreen(navController, authManager)
        }
        composable(
            route = BottomBarScreen.Browse.route,
            arguments = listOf(navArgument("searchQuery") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val searchQuery = backStackEntry.arguments?.getString("searchQuery")
            BookListScreen(
                navController = navController,
                authManager = authManager,
                searchQuery = searchQuery
            )
        }
        composable(route = BottomBarScreen.Friends.route) {
            FriendsScreen(navController, authManager)
        }
        
        composable(
            route = "book_details/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookDetailsScreen(navController, authManager, bookId)
        }
        
        // Profile and Stats screens
        composable(
            route = "profile/{userId}",
            arguments = listOf(navArgument("userId") { 
                type = NavType.StringType
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
        
        // Friend screens
        composable("friends") {
            FriendsScreen(navController, authManager)
        }
        
        // Privacy settings
        composable("privacy_settings") {
            PrivacySettingsScreen(navController, authManager)
        }
        
        // Social media management
        composable("social_media_management") {
            SocialMediaManagementScreen(navController, authManager)
        }
        
        // Review submission
        composable(
            route = "review_submission/{applicationId}",
            arguments = listOf(navArgument("applicationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getString("applicationId") ?: ""
            ReviewSubmissionScreen(navController, authManager, applicationId)
        }
    }
}