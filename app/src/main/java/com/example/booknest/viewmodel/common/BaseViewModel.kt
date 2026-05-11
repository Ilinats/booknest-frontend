package com.example.booknest.viewmodel.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.error.BNError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

abstract class BaseViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val loadingCount = AtomicInteger(0)

    private fun setLoading(loading: Boolean) {
        if (loading) loadingCount.incrementAndGet() else loadingCount.decrementAndGet()
        _isLoading.value = loadingCount.get() > 0
    }

    fun clearError() {
        _error.value = null
    }

    protected fun Throwable.toErrorMessage(): String = when (this) {
        is BNError.Generic -> {
            val raw = messageString ?: error ?: "An error occurred"
            if (raw.startsWith("[") && raw.endsWith("]")) {
                raw.removeSurrounding("[", "]")
                    .split(",")
                    .map { it.trim().removeSurrounding("\"") }
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
                    .ifBlank { "An error occurred" }
            } else raw
        }
        is BNError.Network -> messageString ?: "Network error. Please check your connection."
        is BNError.Unauthorized -> messageString ?: "You are not authorized to perform this action."
        else -> message ?: "An error occurred"
    }

    protected fun <T> launchWithLoading(
        onSuccess: (T) -> Unit,
        onError: ((String) -> Unit)? = null,
        block: suspend () -> Result<T>
    ) {
        viewModelScope.launch {
            try {
                setLoading(true)
                _error.value = null
                block().fold(
                    onSuccess = onSuccess,
                    onFailure = { e ->
                        val msg = e.message ?: "Unknown error"
                        _error.value = msg
                        onError?.invoke(msg)
                    }
                )
            } catch (e: Exception) {
                val msg = e.message ?: "Unknown error"
                _error.value = msg
                onError?.invoke(msg)
            } finally {
                setLoading(false)
            }
        }
    }
}
