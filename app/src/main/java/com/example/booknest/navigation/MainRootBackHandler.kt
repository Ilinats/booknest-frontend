package com.example.booknest.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

/**
 * When the inner main NavHost is at its root (e.g. home tab), consume the system back press
 * so it does not pop the outer graph and reveal login/landing underneath [Screen.Main].
 */
@Composable
fun MainRootBackHandler(navController: NavHostController) {
    val canPopInnerStack = navController.previousBackStackEntry != null
    BackHandler(enabled = !canPopInnerStack) {
        // Intentionally empty — blocks back from leaving the authenticated shell.
    }
}
