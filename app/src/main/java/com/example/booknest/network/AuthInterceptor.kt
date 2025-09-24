package com.example.booknest.network

import com.example.booknest.data.AuthManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(private val authManager: AuthManager) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = TokenCache.accessToken

        val requestBuilder = originalRequest.newBuilder()
        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        var response = chain.proceed(request)

        if (response.code == 401 && token != null) {
            println("Received 401, attempting token refresh...")
            response.close()
            
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
                    throw IOException("Token refresh failed")
                }
            } catch (e: Exception) {
                println("Token refresh exception: ${e.message}")
                throw IOException("Token refresh failed: ${e.message}")
            }
        }

        return response
    }
}
