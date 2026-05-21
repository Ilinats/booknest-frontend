package com.example.booknest.presentation.effects

/**
 * Auth-related navigation intents emitted from ViewModels.
 * UI maps these to [androidx.navigation.NavController] routes (keeps route strings out of VMs).
 */
sealed interface AuthUiEffect {
    data object NavigateToMainClearingStack : AuthUiEffect

    data class NavigateToEmailVerification(
        val email: String,
    ) : AuthUiEffect
}
