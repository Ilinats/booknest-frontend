package com.example.booknest.network

import com.example.booknest.data.AuthManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(private val authManager: AuthManager) : Interceptor {
    
    // Track if we're already in a refresh attempt to prevent infinite loops
    private var isRefreshing = false
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = TokenCache.accessToken

        val requestBuilder = originalRequest.newBuilder()
        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        var response = chain.proceed(request)

        // Only attempt refresh if we get 401, have a token, and aren't already refreshing
        // Also exclude auth endpoints from token refresh logic
        val isAuthEndpoint = originalRequest.url.toString().contains("/auth/")
        if (response.code == 401 && token != null && !isRefreshing && !isAuthEndpoint) {
            println("Received 401, attempting token refresh...")
            response.close()
            
            // Check if this is a refresh token request to avoid infinite loops
            if (originalRequest.url.toString().contains("/auth/refresh-token")) {
                println("Refresh token request failed with 401, clearing user data")
                runBlocking {
                    authManager.logout()
                }
                throw IOException("Refresh token expired, user needs to login again")
            }
            
            isRefreshing = true
            try {
                val refreshSuccess = runBlocking {
                    authManager.refreshTokenIfNeeded()
                }
                
                if (refreshSuccess) {
                    println("Token refresh successful, retrying request...")
                    val newToken = TokenCache.accessToken
                    if (newToken != null) {
                        val newRequest = originalRequest.newBuilder()
                            .removeHeader("Authorization")
                            .addHeader("Authorization", "Bearer $newToken")
                            .build()
                        response = chain.proceed(newRequest)
                        println("Request retry successful: ${response.code}")
                    }
                } else {
                    println("Token refresh failed, user needs to login again")
                    runBlocking {
                        authManager.logout()
                    }
                    throw IOException("Token refresh failed")
                }
            } catch (e: Exception) {
                println("Token refresh exception: ${e.message}")
                runBlocking {
                    authManager.logout()
                }
                throw IOException("Token refresh failed: ${e.message}")
            } finally {
                isRefreshing = false
            }
        }

        return response
    }
}
