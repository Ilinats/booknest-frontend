package com.example.booknest.network

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        var token = TokenCache.accessToken

        // Add Authorization header if token exists
        val requestBuilder = originalRequest.newBuilder()
        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        var request = requestBuilder.build()
        var response = chain.proceed(request)

        // Handle 401 Unauthorized responses
        // Skip token refresh for auth endpoints to avoid infinite loops
        val requestPath = originalRequest.url.encodedPath
        val isAuthEndpoint = requestPath == "/auth/login" || 
                            requestPath == "/auth/register" ||
                            requestPath == "/auth/refresh-token" ||
                            requestPath.startsWith("/auth/verify") ||
                            requestPath.startsWith("/auth/resend")
        
        if (response.code == 401 && !isAuthEndpoint) {
            val tokenRefreshCallback = RetrofitInstance.tokenRefreshCallback
            if (tokenRefreshCallback != null) {
                response.close() // Close the original response
                
                // Attempt to refresh the token
                val refreshSuccess = runBlocking {
                    tokenRefreshCallback()
                }
                
                if (refreshSuccess) {
                    // Get the new token
                    val newToken = TokenCache.accessToken
                    if (newToken != null) {
                        // Retry the original request with the new token
                        request = originalRequest.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                        response = chain.proceed(request)
                    }
                }
            }
        }

        return response
    }
}