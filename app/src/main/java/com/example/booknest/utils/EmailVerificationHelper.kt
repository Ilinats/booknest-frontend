package com.example.booknest.utils

import androidx.navigation.NavController
import com.example.booknest.navigation.Screen

object EmailVerificationHelper {
    
    /**
     * Navigates to email verification screen with optional token
     */
    fun navigateToEmailVerification(navController: NavController, token: String? = null) {
        navController.navigate(Screen.EmailVerification.createRoute(token))
    }
    
    /**
     * Creates a test deep link for development/testing purposes
     */
    fun createTestDeepLink(token: String): String {
        return "booknest://verify-email?token=$token"
    }
    
    /**
     * Validates email verification token format
     */
    fun isValidTokenFormat(token: String): Boolean {
        // Basic validation - adjust based on your backend token format
        return token.isNotBlank() && token.length >= 6
    }
    
    /**
     * Extracts token from various email verification link formats
     */
    fun extractTokenFromEmailLink(emailLink: String): String? {
        return try {
            // Handle different link formats
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
