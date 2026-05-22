package com.example.booknest.presentation.navigation

import androidx.navigation.NavController
import com.example.booknest.presentation.effects.AuthUiEffect
import com.example.booknest.presentation.effects.ProfileUiEffect

/** Main is the only root destination while logged in — auth routes must not stay on the back stack. */
fun NavController.navigateToMainAsRoot() {
    navigate(Screen.Main.route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavController.applyAuthUiEffect(effect: AuthUiEffect) {
    when (effect) {
        AuthUiEffect.NavigateToMainClearingStack -> navigateToMainAsRoot()

        is AuthUiEffect.NavigateToEmailVerification -> {
            navigate(Screen.EmailVerification.createRoute(effect.email)) {
                popUpTo(Screen.ProfileDetails.route) { inclusive = true }
            }
        }
    }
}

fun NavController.applyProfileUiEffect(effect: ProfileUiEffect) {
    when (effect) {
        ProfileUiEffect.NavigateToLandingClearingStack -> {
            navigate(Screen.Landing.route) {
                popUpTo(graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}
