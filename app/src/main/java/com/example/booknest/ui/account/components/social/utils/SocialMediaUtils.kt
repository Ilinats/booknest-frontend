package com.example.booknest.ui.account.components.social.utils

internal fun isValidUrl(url: String): Boolean {
    return url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))
}

