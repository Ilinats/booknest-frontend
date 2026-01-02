package com.example.booknest.ui.error

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.booknest.data.datasource.extractErrorMessage
import com.example.booknest.data.error.BNError
import com.example.booknest.ui.components.ErrorToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object GlobalErrorHandler {
    private val _errorMessage = MutableSharedFlow<String>(replay = 0)
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()
    private val errorScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun showError(message: String) {
        errorScope.launch {
            _errorMessage.emit(message)
        }
    }

    fun showError(exception: Throwable) {
        errorScope.launch {
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
            _errorMessage.emit(message)
        }
    }

    fun showErrorFromResponse(errorBody: String?) {
        errorScope.launch {
            val message = extractErrorMessage(errorBody)
            _errorMessage.emit(message)
        }
    }
}

@Composable
fun GlobalErrorHandler(
    modifier: Modifier = Modifier
) {
    var currentError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        GlobalErrorHandler.errorMessage.collectLatest { message ->
            currentError = message
        }
    }

    ErrorToast(
        message = currentError,
        onDismiss = { currentError = null },
        modifier = modifier
    )
}

