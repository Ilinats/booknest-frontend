package com.example.booknest.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.booknest.data.session.SessionManager
import com.example.booknest.ui.account.PrivacySettingsScreen
import com.example.booknest.ui.account.SocialMediaManagementScreen
import com.example.booknest.ui.account.StatsScreen
import com.example.booknest.ui.analytics.AuthorAnalyticsScreen
import com.example.booknest.ui.analytics.BookAnalyticsScreen
import com.example.booknest.ui.applications.BookApplicationDetailScreen
import com.example.booknest.ui.author.AuthorHomeScreen
import com.example.booknest.ui.author.AuthorProfileScreen
import com.example.booknest.ui.author.BookCreationWizard
import com.example.booknest.ui.author.BookEditScreen
import com.example.booknest.ui.author.MyBooksScreen
import com.example.booknest.ui.author.SeriesManagementScreen
import com.example.booknest.ui.books.BookDetailsScreen
import com.example.booknest.ui.books.SeriesBooksScreen
import com.example.booknest.ui.profile.ProfileEditScreen
import com.example.booknest.ui.profile.ProfileScreen
import com.example.booknest.ui.reviews.UserReviewsScreen
import org.koin.compose.koinInject

@Composable
fun AuthorNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val sessionManager: SessionManager = koinInject()
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
        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("userId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProfileScreen(navController, sessionManager, userId)
        }
        composable(route = Screen.ProfileEdit.route) {
            ProfileEditScreen(navController, sessionManager)
        }
        composable(
            route = Screen.Stats.route,
            arguments = listOf(navArgument("authorId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
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
            route = Screen.BookAnalytics.route,
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
            route = "${Screen.SeriesBooks.route}?seriesName={seriesName}",
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
            route = "${Screen.UserReviews.route}?userName={userName}",
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
                try { Uri.decode(it) } catch (e: Exception) { it }
            }
            UserReviewsScreen(navController, userId, userName)
        }
    }
}
