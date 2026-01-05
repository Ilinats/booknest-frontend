package com.example.booknest.ui.state

import com.example.booknest.ui.toast.GlobalToastHandler

/**
 * Extension function to show error toast when UiState is Error.
 * This provides a consistent way to handle errors across screens.
 */
@androidx.compose.runtime.Composable
fun <T> UiState<T>.handleError() {
    androidx.compose.runtime.LaunchedEffect(this) {
        if (this@handleError is UiState.Error) {
            // Error is already shown via GlobalToastHandler in ViewModel
            // This extension can be used for additional error handling if needed
        }
    }
}

/**
 * Helper function to check if state is loading
 */
fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading

/**
 * Helper function to check if state is error
 */
fun <T> UiState<T>.isError(): Boolean = this is UiState.Error

/**
 * Helper function to check if state is success
 */
fun <T> UiState<T>.isSuccess(): Boolean = this is UiState.Success<*>

/**
 * Helper function to get error message if state is error
 */
fun <T> UiState<T>.getErrorMessage(): String? = 
    if (this is UiState.Error) this.message else null

/**
 * Helper function to get data if state is success
 */
fun <T> UiState<T>.getDataOrNull(): T? = 
    if (this is UiState.Success) this.data else null

