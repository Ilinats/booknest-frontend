package com.example.booknest.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.booknest.ui.myapplications.MyApplicationsScreen
import com.example.booknest.ui.books.BookDetailsScreen
import com.example.booknest.ui.books.BookListScreen
import com.example.booknest.ui.books.BookCategoryScreen
import com.example.booknest.ui.books.SeriesBooksScreen
import com.example.booknest.ui.home.HomeScreen
import com.example.booknest.ui.profile.ProfileEditScreen
import com.example.booknest.ui.profile.ProfileScreen
import com.example.booknest.ui.account.StatsScreen
import com.example.booknest.ui.account.FavoriteGenresScreen
import com.example.booknest.ui.analytics.BookAnalyticsScreen
import com.example.booknest.ui.friends.FriendsScreen
import com.example.booknest.ui.account.PrivacySettingsScreen
import com.example.booknest.ui.account.SocialMediaManagementScreen
import com.example.booknest.ui.reviews.ReviewSubmissionScreen
import com.example.booknest.data.session.SessionManager
import com.example.booknest.ui.notifications.NotificationsScreen
import org.koin.compose.koinInject

@Composable
fun HomeNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val sessionManager: SessionManager = koinInject()
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Home.route,
        modifier = modifier
    ) {
        composable(route = BottomBarScreen.Home.route) {
            HomeScreen(navController, sessionManager)
        }
        composable(route = BottomBarScreen.MyApplications.route) {
            MyApplicationsScreen(navController, sessionManager)
        }
        composable(
            route = "${Screen.Browse.route}/{searchQuery}",
            arguments = listOf(navArgument("searchQuery") {
                type = NavType.StringType; nullable = true; defaultValue = ""
            })
        ) { backStackEntry ->
            val encodedQuery = backStackEntry.arguments?.getString("searchQuery") ?: ""
            val searchQuery = try {
                android.net.Uri.decode(encodedQuery).takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                encodedQuery.takeIf { it.isNotBlank() }
            }
            BookListScreen(
                navController = navController,
                sessionManager = sessionManager,
                searchQuery = searchQuery,
                category = "search"
            )
        }
        composable(route = Screen.Browse.route) {
            BookListScreen(
                navController = navController,
                sessionManager = sessionManager,
                searchQuery = null,
                category = null
            )
        }
        composable(
            route = Screen.Books.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            if (category in listOf("recommended", "new_releases", "followed_authors", "trending")) {
                BookCategoryScreen(
                    navController = navController,
                    category = category
                )
            } else {
                val searchQuery = backStackEntry.arguments?.getString("searchQuery")
                BookListScreen(
                    navController = navController,
                    sessionManager = sessionManager,
                    searchQuery = searchQuery,
                    category = category
                )
            }
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

        composable(Screen.ProfileEdit.route) {
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
            route = Screen.BookAnalytics.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookAnalyticsScreen(navController, sessionManager, bookId)
        }

        composable(Screen.Friends.route) {
            FriendsScreen(navController, sessionManager)
        }

        composable(Screen.FavoriteGenres.route) {
            FavoriteGenresScreen(navController, sessionManager)
        }

        composable(Screen.PrivacySettings.route) {
            PrivacySettingsScreen(navController, sessionManager)
        }

        composable(Screen.SocialMediaManagement.route) {
            SocialMediaManagementScreen(navController, sessionManager)
        }

        composable(
            route = "${Screen.ReviewSubmission.route}?reviewId={reviewId}",
            arguments = listOf(
                navArgument("applicationId") { type = NavType.StringType },
                navArgument("reviewId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getString("applicationId") ?: ""
            val reviewId = backStackEntry.arguments?.getString("reviewId")
            ReviewSubmissionScreen(navController, sessionManager, applicationId, reviewId)
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(navController, sessionManager)
        }

        composable(
            route = Screen.UserProfile.route,
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            ProfileScreen(navController, sessionManager, userId = null, username = username)
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
                try {
                    android.net.Uri.decode(it)
                } catch (e: Exception) {
                    it
                }
            }
            com.example.booknest.ui.reviews.UserReviewsScreen(navController, userId, userName)
        }
    }
}