package com.example.booknest.data.error

internal fun shouldShowErrorToast(throwable: Throwable? = null, message: String? = null): Boolean {
    if (throwable is BNError.Generic && throwable.statusCode == 500) {
        return false
    }
    val text = (message ?: throwable?.message).orEmpty().lowercase()
    if (text.contains("internal server error")) {
        return false
    }
    return true
}
