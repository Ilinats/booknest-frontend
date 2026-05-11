package com.example.booknest.ui.toast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.booknest.data.datasource.extractErrorMessage
import com.example.booknest.data.error.BNError
import com.example.booknest.data.session.SessionManager
import com.example.booknest.ui.components.Toast
import com.example.booknest.ui.components.ToastMessage
import com.example.booknest.ui.components.ToastType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object GlobalToastHandler {
    private val _toastMessage = MutableSharedFlow<ToastMessage>(replay = 0)
    val toastMessage: SharedFlow<ToastMessage> = _toastMessage.asSharedFlow()
    private val toastScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private fun shouldSuppressStaleAuthToast(message: String): Boolean {
        val m = message.lowercase()
        val looksLikeAuthNoise = m.contains("token") ||
            m.contains("missing") ||
            m.contains("unauthorized") ||
            m.contains("not authorized") ||
            m.contains("jwt") ||
            m.contains("bearer") ||
            m.contains("expired session")
        if (!looksLikeAuthNoise) return false
        val loggedOut = SessionManager.isLoggedIn.value != true
        val noAccessToken = SessionManager.getToken().isEmpty()
        return loggedOut || noAccessToken
    }

    fun showSuccess(message: String) {
        toastScope.launch {
            _toastMessage.emit(ToastMessage(message, ToastType.SUCCESS))
        }
    }

    fun showError(message: String) {
        if (shouldSuppressStaleAuthToast(message)) return
        toastScope.launch {
            _toastMessage.emit(ToastMessage(message, ToastType.ERROR))
        }
    }

    fun showError(exception: Throwable) {
        toastScope.launch {
            val message = when (exception) {
                is BNError.Generic -> {
                    val msg = exception.messageString ?: exception.error ?: "An error occurred"
                    if (msg.startsWith("[") && msg.endsWith("]")) {
                        try {
                            val messages = msg.removeSurrounding("[", "]")
                                .split(",")
                                .map { it.trim().removeSurrounding("\"") }
                                .filter { it.isNotBlank() }
                            if (messages.isNotEmpty()) {
                                messages.joinToString(", ")
                            } else {
                                "An error occurred"
                            }
                        } catch (e: Exception) {
                            msg
                        }
                    } else {
                        msg
                    }
                }

                is BNError.Network -> exception.messageString
                    ?: "Network error. Please check your connection."

                is BNError.Unauthorized -> exception.messageString
                    ?: "You are not authorized to perform this action."

                else -> {
                    val errorMsg = exception.message ?: "An error occurred"
                    extractErrorMessage(errorMsg)
                }
            }
            if (shouldSuppressStaleAuthToast(message)) return@launch
            _toastMessage.emit(ToastMessage(message, ToastType.ERROR))
        }
    }

    fun showInfo(message: String) {
        toastScope.launch {
            _toastMessage.emit(ToastMessage(message, ToastType.INFO))
        }
    }
}

@Composable
fun GlobalToastHandler(
    modifier: Modifier = Modifier
) {
    var currentToast by remember { mutableStateOf<ToastMessage?>(null) }

    LaunchedEffect(Unit) {
        GlobalToastHandler.toastMessage.collectLatest { message ->
            currentToast = message
        }
    }

    Toast(
        toastMessage = currentToast,
        onDismiss = { currentToast = null },
        modifier = modifier
    )
}

