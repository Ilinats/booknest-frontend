package com.example.booknest.viewmodel.common

import com.example.booknest.port.ToastNotifier
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Bridges in-screen error/success state with global [ToastNotifier] toasts.
 */
class UserFeedback(
    private val toastNotifier: ToastNotifier
) {
    fun success(message: String, state: MutableStateFlow<String?>? = null) {
        state?.value = message
        toastNotifier.showSuccess(message)
    }

    fun error(message: String, state: MutableStateFlow<String?>? = null) {
        state?.value = message
        toastNotifier.showError(message)
    }

    fun error(throwable: Throwable, state: MutableStateFlow<String?>? = null) {
        toastNotifier.showError(throwable)
        state?.value = throwable.message ?: "An error occurred"
    }
}
