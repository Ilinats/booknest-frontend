package com.example.booknest.port

import com.example.booknest.ui.components.ToastMessage
import kotlinx.coroutines.flow.SharedFlow

/**
 * App-wide transient messages (success, error, info). UI hosts collect [messages].
 */
interface ToastNotifier {
    val messages: SharedFlow<ToastMessage>
    fun showSuccess(message: String)
    fun showError(message: String)
    fun showError(exception: Throwable)
    fun showInfo(message: String)
}
