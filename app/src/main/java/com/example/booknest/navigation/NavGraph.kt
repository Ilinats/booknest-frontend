package com.example.booknest.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.booknest.ui.onboarding.AccountTypeScreen
import com.example.booknest.ui.onboarding.GenresScreen
import com.example.booknest.ui.auth.LandingScreen
import com.example.booknest.ui.auth.SplashScreen
import com.example.booknest.ui.onboarding.SocialMediaScreen
import com.example.booknest.ui.auth.LoginScreen
import com.example.booknest.ui.onboarding.PersonalInfoScreen
import com.example.booknest.ui.onboarding.ProfileDetailsScreen
import com.example.booknest.ui.auth.EmailVerificationScreen
import com.example.booknest.ui.main.AuthorMainScreen
import com.example.booknest.ui.main.MainScreen
import com.example.booknest.data.session.SessionManager
import com.example.booknest.viewmodel.auth.LoginViewModel
import com.example.booknest.viewmodel.main.MainViewModel
import com.example.booknest.viewmodel.auth.SignupViewModel
import org.koin.compose.koinInject

@Composable
fun NavGraph(
    navController: NavHostController,
    signupViewModel: SignupViewModel,
    loginViewModel: LoginViewModel
) {
    val sessionManager: SessionManager = koinInject()
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == false &&
            currentRoute != Screen.Splash.route &&
            currentRoute != Screen.Landing.route
        ) {
            navController.navigate(Screen.Splash.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController, sessionManager)
        }
        composable(Screen.Landing.route) {
            LandingScreen(navController, sessionManager)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController, loginViewModel, sessionManager)
        }
        composable(
            route = Screen.PasswordReset.route,
            arguments = listOf(navArgument("email") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            com.example.booknest.ui.auth.PasswordResetScreen(navController, sessionManager, email)
        }
        composable(
            route = "${Screen.EmailVerification.route}?email={email}",
            arguments = listOf(navArgument("email") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email")
            EmailVerificationScreen(navController, sessionManager, email)
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
        composable(Screen.Genres.route) {
            GenresScreen(navController, signupViewModel)
        }
        composable(Screen.SocialMedia.route) {
            SocialMediaScreen(navController, sessionManager)
        }
        composable(Screen.Main.route) {
            val mainViewModel: MainViewModel = koinInject()
            val isLoggedIn by sessionManager.isLoggedIn.collectAsState()
            val isAuthor by mainViewModel.isAuthor.collectAsState()

            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn == true) {
                    mainViewModel.resolveUserType()
                }
            }

            if (isAuthor) {
                AuthorMainScreen(mainViewModel = mainViewModel)
            } else {
                MainScreen(mainViewModel = mainViewModel)
            }
        }

    }
}
