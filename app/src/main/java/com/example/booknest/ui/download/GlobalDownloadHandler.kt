package com.example.booknest.ui.download

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

object GlobalDownloadHandler {
    private val _toastMessage = MutableSharedFlow<ToastMessage>(replay = 0)
    val toastMessage: SharedFlow<ToastMessage> = _toastMessage.asSharedFlow()
    private val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun showDownloadStarted(bookTitle: String? = null) {
        downloadScope.launch {
            val message = if (bookTitle != null) {
                "Downloading: $bookTitle"
            } else {
                "Download started..."
            }
            _toastMessage.emit(ToastMessage(message, ToastType.DOWNLOAD_STARTED))
        }
    }

    fun showDownloadCompleted(bookTitle: String? = null) {
        downloadScope.launch {
            val message = if (bookTitle != null) {
                "Download completed: $bookTitle"
            } else {
                "Download completed successfully"
            }
            _toastMessage.emit(ToastMessage(message, ToastType.DOWNLOAD_COMPLETED))
        }
    }

    fun showDownloadError(errorMessage: String) {
        downloadScope.launch {
            _toastMessage.emit(ToastMessage(errorMessage, ToastType.ERROR))
        }
    }

    fun showSuccess(message: String) {
        downloadScope.launch {
            _toastMessage.emit(ToastMessage(message, ToastType.SUCCESS))
        }
    }

    fun showInfo(message: String) {
        downloadScope.launch {
            _toastMessage.emit(ToastMessage(message, ToastType.INFO))
        }
    }
}

@Composable
fun GlobalDownloadHandler(
    modifier: Modifier = Modifier
) {
    var currentToast by remember { mutableStateOf<ToastMessage?>(null) }

    LaunchedEffect(Unit) {
        GlobalDownloadHandler.toastMessage.collectLatest { message ->
            currentToast = message
        }
    }

    Toast(
        toastMessage = currentToast,
        onDismiss = { currentToast = null },
        modifier = modifier
    )
}

