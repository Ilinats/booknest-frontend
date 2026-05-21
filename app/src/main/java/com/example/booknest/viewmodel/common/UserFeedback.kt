package com.example.booknest.viewmodel.common

import com.example.booknest.data.error.shouldShowErrorToast
import com.example.booknest.port.ToastNotifier
import kotlinx.coroutines.flow.MutableStateFlow

class UserFeedback(
    private val toastNotifier: ToastNotifier
) {
    fun success(message: String, state: MutableStateFlow<String?>? = null) {
        state?.value = message
        toastNotifier.showSuccess(message)
    }

    fun error(message: String, state: MutableStateFlow<String?>? = null) {
        state?.value = message
        if (shouldShowErrorToast(message = message)) {
            toastNotifier.showError(message)
        }
    }

    fun error(throwable: Throwable, state: MutableStateFlow<String?>? = null) {
        state?.value = throwable.message ?: "An error occurred"
        if (shouldShowErrorToast(throwable = throwable)) {
            toastNotifier.showError(throwable)
        }
    }
}
