package com.example.booknest.utils

import androidx.navigation.NavController
import com.example.booknest.navigation.Screen

object ComposeDeepLinkHandler {

    private const val SCHEME_BOOKNEST = "booknest"
    private const val HOST_VERIFY_EMAIL = "verify-email"
    private const val PARAM_TOKEN = "token"
    
    /**
     * Handles email verification deep links in Compose navigation
     */
    fun handleEmailVerificationDeepLink(
        navController: NavController,
        deepLinkUrl: String
    ): Boolean {
        return try {
            val uri = android.net.Uri.parse(deepLinkUrl)
            
            // Check if it's our email verification deep link
            if (uri.scheme == SCHEME_BOOKNEST && 
                uri.host == HOST_VERIFY_EMAIL) {
                
                val token = uri.getQueryParameter(PARAM_TOKEN)
                
                if (token != null) {
                    // Navigate to email verification screen with token
                    navController.navigate(Screen.EmailVerification.createRoute(token)) {
                        // Clear back stack to prevent going back to login
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                    true
                } else {
                    // Navigate to email verification screen without token
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
    
    /**
     * Creates a test deep link for development
     */
    fun createTestDeepLink(token: String): String {
        return "$SCHEME_BOOKNEST://$HOST_VERIFY_EMAIL?$PARAM_TOKEN=$token"
    }
    
    /**
     * Validates if a URL is a valid email verification deep link
     */
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
