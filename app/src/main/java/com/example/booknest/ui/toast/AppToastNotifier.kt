package com.example.booknest.ui.toast

import com.example.booknest.data.datasource.extractErrorMessage
import com.example.booknest.data.error.BNError
import com.example.booknest.port.DownloadNotifier
import com.example.booknest.port.SessionReader
import com.example.booknest.port.ToastNotifier
import com.example.booknest.ui.components.ToastMessage
import com.example.booknest.ui.components.ToastType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Single toast/download pipeline with injected [SessionReader] (no GlobalContext).
 */
class AppToastNotifier(
    private val sessionReader: SessionReader
) : ToastNotifier, DownloadNotifier {

    private val _messages = MutableSharedFlow<ToastMessage>(
        extraBufferCapacity = 32
    )
    override val messages: SharedFlow<ToastMessage> = _messages.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

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
        val loggedOut = sessionReader.isLoggedIn.value != true
        val noAccessToken = sessionReader.getToken().isEmpty()
        return loggedOut || noAccessToken
    }

    override fun showSuccess(message: String) {
        scope.launch {
            _messages.emit(ToastMessage(message, ToastType.SUCCESS))
        }
    }

    override fun showError(message: String) {
        if (shouldSuppressStaleAuthToast(message)) return
        scope.launch {
            _messages.emit(ToastMessage(message, ToastType.ERROR))
        }
    }

    override fun showError(exception: Throwable) {
        scope.launch {
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
                        } catch (_: Exception) {
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
            _messages.emit(ToastMessage(message, ToastType.ERROR))
        }
    }

    override fun showInfo(message: String) {
        scope.launch {
            _messages.emit(ToastMessage(message, ToastType.INFO))
        }
    }

    override fun showDownloadStarted(bookTitle: String?) {
        scope.launch {
            val message = if (bookTitle != null) {
                "Downloading: $bookTitle"
            } else {
                "Download started..."
            }
            _messages.emit(ToastMessage(message, ToastType.DOWNLOAD_STARTED))
        }
    }

    override fun showDownloadCompleted(bookTitle: String?) {
        scope.launch {
            val message = if (bookTitle != null) {
                "Download completed: $bookTitle"
            } else {
                "Download completed successfully"
            }
            _messages.emit(ToastMessage(message, ToastType.DOWNLOAD_COMPLETED))
        }
    }

    override fun showDownloadError(errorMessage: String) {
        if (shouldSuppressStaleAuthToast(errorMessage)) return
        scope.launch {
            _messages.emit(ToastMessage(errorMessage, ToastType.ERROR))
        }
    }
}
