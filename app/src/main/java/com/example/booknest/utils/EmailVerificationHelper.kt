package com.example.booknest.utils

import androidx.navigation.NavController
import com.example.booknest.navigation.Screen

object EmailVerificationHelper {

    fun navigateToEmailVerification(navController: NavController, token: String? = null) {
        navController.navigate(Screen.EmailVerification.createRoute(token))
    }

    fun createTestDeepLink(token: String): String {
        return "booknest://verify-email?token=$token"
    }

    fun isValidTokenFormat(token: String): Boolean {
        return token.isNotBlank() && token.length >= 6
    }

    fun extractTokenFromEmailLink(emailLink: String): String? {
        return try {
            when {
                emailLink.contains("booknest://verify-email") -> {
                    val uri = android.net.Uri.parse(emailLink)
                    uri.getQueryParameter("token")
                }

                emailLink.contains("token=") -> {
                    val tokenParam = emailLink.substringAfter("token=")
                    tokenParam.substringBefore("&").substringBefore(" ")
                }

                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}
