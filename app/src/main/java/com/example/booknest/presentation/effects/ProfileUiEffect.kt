package com.example.booknest.presentation.effects

/**
 * Profile-related navigation intents emitted from ViewModels.
 * UI maps these to Nav routes.
 */
sealed interface ProfileUiEffect {
    data object NavigateToLandingClearingStack : ProfileUiEffect
}
