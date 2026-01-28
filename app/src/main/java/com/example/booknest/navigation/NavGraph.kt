package com.example.booknest.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.booknest.data.session.SessionManager
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
import com.example.booknest.ui.books.BookDetailsScreen
import com.example.booknest.ui.main.MainScreen
import com.example.booknest.ui.profile.ProfileEditScreen
import com.example.booknest.ui.profile.ProfileScreen
import com.example.booknest.ui.account.StatsScreen
import com.example.booknest.ui.analytics.AuthorAnalyticsScreen
import com.example.booknest.ui.analytics.BookAnalyticsScreen
import com.example.booknest.viewmodel.LoginViewModel
import com.example.booknest.viewmodel.MainViewModel
import com.example.booknest.viewmodel.SignupViewModel
import org.koin.compose.koinInject

@Composable
fun NavGraph(
    navController: NavHostController,
    signupViewModel: SignupViewModel,
    loginViewModel: LoginViewModel,
    sessionManager: SessionManager
) {
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == false) {
            android.util.Log.d("NavGraph", "isLoggedIn is false, currentRoute=$currentRoute")
            kotlinx.coroutines.delay(300)
           if (currentRoute != Screen.Splash.route && currentRoute != Screen.Landing.route) {
                android.util.Log.d("NavGraph", "Navigating to Splash")
                navController.navigate(Screen.Splash.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                android.util.Log.d("NavGraph", "Already on Splash or Landing, skipping navigation")
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
        composable(Screen.Bio.route) {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                navController.navigate(Screen.ProfileDetails.route) {
                    popUpTo(Screen.Bio.route) { inclusive = true }
                }
            }
        }
        composable(Screen.Genres.route) {
            GenresScreen(navController, signupViewModel)
        }
        composable(Screen.SocialMedia.route) {
            SocialMediaScreen(navController, sessionManager)
        }
        composable(Screen.Main.route) {
            val mainViewModel: MainViewModel = koinInject()
            val currentUser by sessionManager.currentUser.collectAsState()
            val isLoggedIn by sessionManager.isLoggedIn.collectAsState()
            var storedUserType by androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf<String?>(null)
            }

            androidx.compose.runtime.LaunchedEffect(isLoggedIn, currentUser?.id) {
                if (isLoggedIn == true) {
                    if (currentUser == null && storedUserType == null) {
                        kotlinx.coroutines.coroutineScope {
                            storedUserType =
                                sessionManager.fetchUserType().takeIf { it.isNotEmpty() }
                            println("DEBUG NavGraph: Loaded userType from DataStore: $storedUserType")
                        }
                    }

                    if (currentUser == null) {
                        println("DEBUG NavGraph: Fetching user data via ViewModel...")
                        mainViewModel.fetchCurrentUser()
                    }
                }
            }

            val effectiveUserType = currentUser?.userType ?: storedUserType
            val isAuthor = effectiveUserType?.lowercase() == "author"

            androidx.compose.runtime.LaunchedEffect(effectiveUserType, currentUser?.id) {
                println("DEBUG NavGraph: effectiveUserType=$effectiveUserType, currentUser.id=${currentUser?.id}, isAuthor=$isAuthor")
            }

            if (isAuthor) {
                AuthorMainScreen(sessionManager, mainViewModel)
            } else {
                val authRepository: com.example.booknest.domain.repository.AuthRepository = org.koin.compose.koinInject()
                MainScreen(sessionManager, authRepository, mainViewModel)
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
            com.example.booknest.ui.books.SeriesBooksScreen(navController, seriesId, seriesName)
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

        composable(Screen.AuthorAnalytics.route) {
            AuthorAnalyticsScreen(navController, sessionManager)
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
