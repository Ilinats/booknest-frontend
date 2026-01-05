package com.example.booknest.ui.state

/**
 * Unified UI state sealed class for consistent state management across ViewModels.
 * 
 * @param T The type of data returned on success
 */
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()

    object Loading : UiState<Nothing>()
    
    data class Success<out T>(val data: T) : UiState<T>()

    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>()
}

