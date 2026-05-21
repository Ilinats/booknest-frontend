package com.example.booknest.presentation.navigation

sealed class NavigationEvent {
    data class NavigateTo(
        val route: String,
        val popUpTo: String? = null,
        val inclusive: Boolean = false,
        val launchSingleTop: Boolean = false
    ) : NavigationEvent()

    data object NavigateBack : NavigationEvent()

    data class PopBackTo(
        val route: String,
        val inclusive: Boolean = false
    ) : NavigationEvent()

    data class NavigateAndClearStack(
        val route: String
    ) : NavigationEvent()
}
