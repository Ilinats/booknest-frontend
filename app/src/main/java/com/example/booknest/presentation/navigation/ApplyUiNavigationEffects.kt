package com.example.booknest.presentation.navigation

import androidx.navigation.NavController
import com.example.booknest.presentation.effects.AuthUiEffect
import com.example.booknest.presentation.effects.ProfileUiEffect

fun NavController.applyAuthUiEffect(effect: AuthUiEffect) {
    when (effect) {
        AuthUiEffect.NavigateToMainClearingStack -> {
            navigate(Screen.Main.route) {
                popUpTo(graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }

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
