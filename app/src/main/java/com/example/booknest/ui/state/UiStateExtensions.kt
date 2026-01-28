package com.example.booknest.ui.state

import com.example.booknest.ui.toast.GlobalToastHandler

@androidx.compose.runtime.Composable
fun <T> UiState<T>.handleError() {
    androidx.compose.runtime.LaunchedEffect(this) {
        if (this@handleError is UiState.Error) {
        }
    }
}

fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading

fun <T> UiState<T>.isError(): Boolean = this is UiState.Error

fun <T> UiState<T>.isSuccess(): Boolean = this is UiState.Success<*>

fun <T> UiState<T>.getErrorMessage(): String? =
    if (this is UiState.Error) this.message else null

fun <T> UiState<T>.getDataOrNull(): T? =
    if (this is UiState.Success) this.data else null

