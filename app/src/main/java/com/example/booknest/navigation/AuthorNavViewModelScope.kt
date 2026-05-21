package com.example.booknest.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.booknest.viewmodel.author.AuthorBooksViewModel
import com.example.booknest.viewmodel.author.AuthorDashboardViewModel
import org.koin.androidx.compose.getViewModel

/** Shared [ViewModel] instance across all routes in the author [NavController] graph. */
@Composable
inline fun <reified T : ViewModel> rememberAuthorNavGraphViewModel(
    navController: NavController,
): T {
    val graphEntry = remember(navController) {
        navController.getBackStackEntry(navController.graph.id)
    }
    return getViewModel(viewModelStoreOwner = graphEntry)
}

@Composable
fun rememberAuthorBooksViewModel(navController: NavController): AuthorBooksViewModel =
    rememberAuthorNavGraphViewModel(navController)

@Composable
fun rememberAuthorDashboardViewModel(navController: NavController): AuthorDashboardViewModel =
    rememberAuthorNavGraphViewModel(navController)
