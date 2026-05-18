package com.example.booknest.port

import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.domain.repository.AuthRepository

/**
 * Mutating session operations (tokens, profile mirror, logout).
 */
interface SessionWriter {
    suspend fun logout(authRepository: AuthRepository? = null)
    suspend fun setLoggedIn()
    suspend fun setAuthEntities(
        token: String,
        refreshToken: String,
        userId: String = "",
        username: String = "",
        email: String = "",
        userType: String = ""
    )
    suspend fun updateTokens(accessToken: String, refreshToken: String)
    suspend fun updateUser(user: UserResponse)
    suspend fun setTokens()
}
