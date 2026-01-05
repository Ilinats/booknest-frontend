package com.example.booknest.ui.auth.components.utils

import com.example.booknest.ui.auth.components.models.PasswordStrength
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength("", 0f, MaterialTheme.colorScheme.error)

    var strength = 0f
    var label = "Weak"
    var color = MaterialTheme.colorScheme.error

    if (password.length >= 8) strength += 0.25f
    if (password.length >= 12) strength += 0.15f

    if (password.any { it.isUpperCase() }) strength += 0.2f
    if (password.any { it.isLowerCase() }) strength += 0.2f
    if (password.any { it.isDigit() }) strength += 0.1f
    if (password.any { !it.isLetterOrDigit() }) strength += 0.1f

    when {
        strength >= 0.7f -> {
            label = "Strong"
            color = MaterialTheme.colorScheme.primary
        }

        strength >= 0.4f -> {
            label = "Medium"
            color = MaterialTheme.colorScheme.tertiary
        }

        else -> {
            label = "Weak"
            color = MaterialTheme.colorScheme.error
        }
    }

    return PasswordStrength(label, strength.coerceIn(0f, 1f), color)
}

fun isValidUrl(url: String): Boolean {
    return url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))
}

fun getErrorMessage(error: String?): String {
    return when {
        error == null -> "An error occurred"
        error.contains("Invalid credentials", ignoreCase = true) ->
            "Invalid username/email or password. Please check your credentials and try again."

        error.contains("User not found", ignoreCase = true) ->
            "No account found with this username or email."

        error.contains("Account not verified", ignoreCase = true) ->
            "Your account is not verified. Please check your email for verification instructions."

        error.contains("Account is disabled", ignoreCase = true) ->
            "Your account has been disabled. Please contact support."

        error.contains("Network", ignoreCase = true) || error.contains(
            "connection",
            ignoreCase = true
        ) ->
            "Network error. Please check your internet connection and try again."

        error.contains("timeout", ignoreCase = true) ->
            "Request timed out. Please try again."

        else -> error
    }
}

