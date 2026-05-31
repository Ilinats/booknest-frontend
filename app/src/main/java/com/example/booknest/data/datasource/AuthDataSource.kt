package com.example.booknest.data.datasource

import com.example.booknest.domain.model.request.LoginRequest
import com.example.booknest.domain.model.request.RegisterRequest
import com.example.booknest.domain.model.request.RequestPasswordResetRequest
import com.example.booknest.domain.model.request.ResetPasswordRequest
import com.example.booknest.domain.model.response.AuthTokenResponse
import com.example.booknest.domain.model.response.LoginDataResponse
import com.example.booknest.domain.model.response.RegisterResponse
import com.example.booknest.domain.model.response.UserResponse

interface AuthDataSource {
    suspend fun login(body: LoginRequest): Result<LoginDataResponse>
    suspend fun register(body: RegisterRequest): Result<RegisterResponse>
    suspend fun refresh(refreshToken: String): Result<AuthTokenResponse>
    suspend fun logout(refreshToken: String): Result<Unit>
    suspend fun logoutAll(): Result<Unit>
    suspend fun verifyEmail(code: String): Result<UserResponse>
    suspend fun resendVerification(email: String): Result<Unit>
    suspend fun requestPasswordReset(body: RequestPasswordResetRequest): Result<Unit>
    suspend fun resetPassword(body: ResetPasswordRequest): Result<Unit>
}

