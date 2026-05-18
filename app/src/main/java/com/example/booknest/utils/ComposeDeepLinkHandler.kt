package com.example.booknest.utils

import androidx.navigation.NavController
import com.example.booknest.presentation.navigation.Screen

object ComposeDeepLinkHandler {

    private const val SCHEME_BOOKNEST = "booknest"
    private const val HOST_VERIFY_EMAIL = "verify-email"
    private const val PARAM_TOKEN = "token"

    fun handleEmailVerificationDeepLink(
        navController: NavController,
        deepLinkUrl: String
    ): Boolean {
        return try {
            val uri = android.net.Uri.parse(deepLinkUrl)

            if (uri.scheme == SCHEME_BOOKNEST &&
                uri.host == HOST_VERIFY_EMAIL
            ) {

                val token = uri.getQueryParameter(PARAM_TOKEN)

                if (token != null) {
                    navController.navigate(Screen.EmailVerification.createRoute(token)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                    true
                } else {
                    navController.navigate(Screen.EmailVerification.createRoute()) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                    true
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun createTestDeepLink(token: String): String {
        return "$SCHEME_BOOKNEST://$HOST_VERIFY_EMAIL?$PARAM_TOKEN=$token"
    }

    fun isValidEmailVerificationDeepLink(url: String): Boolean {
        return try {
            val uri = android.net.Uri.parse(url)
            uri.scheme == SCHEME_BOOKNEST &&
                    uri.host == HOST_VERIFY_EMAIL
        } catch (e: Exception) {
            false
        }
    }
}
