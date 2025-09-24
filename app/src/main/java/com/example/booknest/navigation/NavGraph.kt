package com.example.booknest.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.booknest.data.AuthManager
import com.example.booknest.ui.account.AccountTypeScreen
import com.example.booknest.ui.account.BioScreen
import com.example.booknest.ui.account.GenresScreen
import com.example.booknest.ui.account.LandingScreen
import com.example.booknest.ui.account.LoginScreen
import com.example.booknest.ui.account.PersonalInfoScreen
import com.example.booknest.ui.account.ProfileDetailsScreen
import com.example.booknest.ui.main.MainScreen
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
            LandingScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController, loginViewModel)
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
        composable(Screen.Main.route) {
            MainScreen(authManager)
        }
    }
}
