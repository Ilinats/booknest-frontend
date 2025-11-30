package com.example.booknest.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.booknest.data.AuthManager
import com.example.booknest.ui.account.AccountTypeScreen
import com.example.booknest.ui.account.BioScreen
import com.example.booknest.ui.account.GenresScreen
import com.example.booknest.ui.account.LandingScreen
import com.example.booknest.ui.account.SocialMediaScreen
import com.example.booknest.ui.account.LoginScreen
import com.example.booknest.ui.account.PersonalInfoScreen
import com.example.booknest.ui.account.ProfileDetailsScreen
import com.example.booknest.ui.auth.EmailVerificationScreen
import com.example.booknest.ui.author.AuthorMainScreen
import com.example.booknest.ui.books.BookDetailsScreen
import com.example.booknest.ui.main.MainScreen
import com.example.booknest.ui.profile.ProfileEditScreen
import com.example.booknest.ui.profile.ProfileScreen
import com.example.booknest.ui.profile.StatsScreen
import com.example.booknest.ui.analytics.AuthorAnalyticsScreen
import com.example.booknest.ui.analytics.BookAnalyticsScreen
import com.example.booknest.viewmodel.LoginViewModel
import com.example.booknest.viewmodel.SignupViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    signupViewModel: SignupViewModel,
    loginViewModel: LoginViewModel,
    authManager: AuthManager
) {
    NavHost(navController, startDestination = Screen.Landing.route) {
        composable(Screen.Landing.route) {
            LandingScreen(navController, authManager)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController, loginViewModel, authManager)
        }
        composable(
            route = "email_verification?email={email}",
            arguments = listOf(navArgument("email") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email")
            val userEmail = email ?: authManager.getCurrentUser()?.email
            EmailVerificationScreen(navController, authManager, userEmail)
        }
        composable(Screen.AccountType.route) {
            AccountTypeScreen(navController, signupViewModel)
        }
        composable(Screen.PersonalInfo.route) {
            PersonalInfoScreen(navController, signupViewModel)
        }
        composable(Screen.ProfileDetails.route) {
            ProfileDetailsScreen(navController, signupViewModel)
        }
        composable(Screen.Bio.route) {
            BioScreen(navController, signupViewModel)
        }
        composable(Screen.Genres.route) {
            GenresScreen(navController, signupViewModel)
        }
        composable(Screen.SocialMedia.route) {
            SocialMediaScreen(navController, authManager)
        }
        composable(Screen.Main.route) {
            // Check user type and show appropriate main screen
            val currentUser = authManager.getCurrentUser()
            if (currentUser?.userType == "author") {
                AuthorMainScreen(authManager)
            } else {
                MainScreen(authManager) // Reader view
            }
        }
        
        composable(
            route = Screen.BookDetails.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookDetailsScreen(navController, authManager, bookId)
        }
        
        // Profile and Stats screens
        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("userId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProfileScreen(navController, authManager, userId)
        }
        
        composable(Screen.ProfileEdit.route) {
            ProfileEditScreen(navController, authManager)
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
            StatsScreen(navController, authManager, authorId)
        }
        
        // Analytics screens
        composable(
            route = Screen.BookAnalytics.route,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookAnalyticsScreen(navController, authManager, bookId)
        }
        
        composable(Screen.AuthorAnalytics.route) {
            AuthorAnalyticsScreen(navController, authManager)
        }
    }
}
