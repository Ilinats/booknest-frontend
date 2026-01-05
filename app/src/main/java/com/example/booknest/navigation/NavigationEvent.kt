package com.example.booknest.navigation

/**
 * Sealed class representing navigation events from ViewModels.
 * ViewModels should emit these events instead of using callbacks.
 * Screens observe these events and handle navigation accordingly.
 */
sealed class NavigationEvent {
    /**
     * Navigate to a specific route
     */
    data class NavigateTo(
        val route: String,
        val popUpTo: String? = null,
        val inclusive: Boolean = false,
        val launchSingleTop: Boolean = false
    ) : NavigationEvent()

    /**
     * Navigate back
     */
    object NavigateBack : NavigationEvent()

    /**
     * Pop back stack to a specific route
     */
    data class PopBackTo(
        val route: String,
        val inclusive: Boolean = false
    ) : NavigationEvent()

    /**
     * Clear back stack and navigate to route
     */
    data class NavigateAndClearStack(
        val route: String
    ) : NavigationEvent()
}

