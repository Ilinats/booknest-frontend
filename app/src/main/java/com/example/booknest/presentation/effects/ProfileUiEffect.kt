package com.example.booknest.presentation.effects

sealed interface ProfileUiEffect {
    data object NavigateToLandingClearingStack : ProfileUiEffect
}
