package com.example.booknest.ui.toast

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

object GlobalToastHandler {
    private val _toastMessage = MutableSharedFlow<ToastMessage>(replay = 0)
    val toastMessage: SharedFlow<ToastMessage> = _toastMessage.asSharedFlow()
    private val toastScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun showSuccess(message: String) {
        toastScope.launch {
            _toastMessage.emit(ToastMessage(message, ToastType.SUCCESS))
        }
    }

    fun showError(message: String) {
        toastScope.launch {
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

