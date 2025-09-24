package com.example.booknest.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.booknest.data.AuthManager
import com.example.booknest.ui.books.BookListScreen
import com.example.booknest.ui.home.HomeScreen
import com.example.booknest.ui.myapplications.MyApplicationsScreen

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
            MyApplicationsScreen()
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
    }
}