package com.example.booknest.data

import com.example.booknest.network.RefreshTokenRequest
import com.example.booknest.network.RefreshTokenResponse
import com.example.booknest.network.RetrofitInstance
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TokenManager {
    
    private val refreshMutex = Mutex()
    private var isRefreshing = false
    
    suspend fun refreshTokenIfNeeded(userRepository: UserRepository): Boolean {
        return refreshMutex.withLock {
            if (isRefreshing) {
                return@withLock false // Another refresh is already in progress
            }
            
            isRefreshing = true
            try {
                val refreshToken = userRepository.getRefreshToken()
                if (refreshToken.isNullOrBlank()) {
                    return@withLock false
                }
                
                val response = RetrofitInstance.api.refreshToken(
                    RefreshTokenRequest(refreshToken)
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        val refreshResponse = apiResponse.data
                        println("Token refresh successful")
                        // Update only the tokens, keep existing user data
                        userRepository.updateTokens(
                            accessToken = refreshResponse.accessToken,
                            refreshToken = refreshResponse.refreshToken
                        )
                        return@withLock true
                    } else {
                        println("Token refresh failed: ${apiResponse.message}")
                        // Refresh token is invalid, user needs to login again
                        userRepository.clearUserData()
                        return@withLock false
                    }
                } else {
                    println("Token refresh failed: ${response.code()} - ${response.message()}")
                    val errorBody = response.errorBody()?.string()
                    println("Refresh error body: $errorBody")
                    // Refresh token is invalid, user needs to login again
                    userRepository.clearUserData()
                    return@withLock false
                }
            } catch (e: Exception) {
                // Network error or other exception
                return@withLock false
            } finally {
                isRefreshing = false
            }
        }
    }
    
    fun isTokenExpired(token: String?): Boolean {
        if (token.isNullOrBlank()) return true
        
        try {
            // Decode JWT token to check expiry
            val parts = token.split(".")
            if (parts.size != 3) return true
            
            val payload = parts[1]
            val decoded = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
            val json = String(decoded)
            
            // Simple JSON parsing to get exp field
            val expIndex = json.indexOf("\"exp\":")
            if (expIndex == -1) return true
            
            val expStart = expIndex + 6
            val expEnd = json.indexOf(",", expStart).let { 
                if (it == -1) json.indexOf("}", expStart) else it 
            }
            
            val expString = json.substring(expStart, expEnd).trim()
            val exp = expString.toLongOrNull() ?: return true
            
            // Check if token expires in the next 5 minutes (300 seconds)
            val currentTime = System.currentTimeMillis() / 1000
            return exp <= (currentTime + 300)
        } catch (e: Exception) {
            return true
        }
    }
}
