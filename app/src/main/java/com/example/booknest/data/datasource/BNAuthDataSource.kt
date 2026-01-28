package com.example.booknest.data.datasource

import android.content.Context
import com.example.booknest.data.error.BNError
import com.example.booknest.data.service.AuthService
import com.example.booknest.data.service.ProfilesService
import com.example.booknest.data.session.SessionManager
import com.example.booknest.dataStore
import com.example.booknest.domain.model.request.LoginRequest
import com.example.booknest.domain.model.request.RefreshTokenRequest
import com.example.booknest.domain.model.request.RegisterRequest
import com.example.booknest.domain.model.request.RequestPasswordResetRequest
import com.example.booknest.domain.model.request.ResendVerificationRequest
import com.example.booknest.domain.model.request.ResetPasswordRequest
import com.example.booknest.domain.model.request.VerifyEmailRequest
import com.example.booknest.domain.model.response.AuthTokenResponse
import com.example.booknest.domain.model.response.LoginDataResponse
import com.example.booknest.domain.model.response.RegisterResponse
import com.example.booknest.domain.model.response.UserResponse

class BNAuthDataSource(
    private val authService: AuthService,
    private val profilesService: ProfilesService,
    private val context: Context
) : AuthDataSource {

    override suspend fun login(body: LoginRequest): Result<LoginDataResponse> {
        val response = authService.login(body)
        return if (response.isSuccessful && response.body() != null) {
            val loginResponse = response.body()!!
            Result.success(
                LoginDataResponse(
                    accessToken = loginResponse.accessToken,
                    refreshToken = loginResponse.refreshToken
                )
            )
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = extractErrorMessage(errorBody)
            Result.failure(
                BNError.Generic(
                    messageString = errorMessage,
                    error = null,
                    statusCode = response.code()
                )
            )
        }
    }

    override suspend fun register(body: RegisterRequest): Result<RegisterResponse> {
        val registerResponse = authService.register(body)
        return if (registerResponse.isSuccessful && registerResponse.body() != null) {
            val authResponse = registerResponse.body()!!
            val sessionManager = SessionManager.getInstance(context.dataStore)
            sessionManager.updateTokens(
                accessToken = authResponse.accessToken,
                refreshToken = authResponse.refreshToken
            )

            val userResponse = profilesService.getMe()
            if (userResponse.isSuccessful && userResponse.body() != null) {
                val user = userResponse.body()!!
                Result.success(
                    RegisterResponse(
                        user = user,
                        accessToken = authResponse.accessToken,
                        refreshToken = authResponse.refreshToken
                    )
                )
            } else {
                Result.failure(
                    BNError.Generic(
                        messageString = "Registration successful but failed to fetch user profile: ${
                            userResponse.errorBody()?.string()
                        }",
                        error = null,
                        statusCode = userResponse.code()
                    )
                )
            }
        } else {
            val errorBody = registerResponse.errorBody()?.string()
            val errorMessage = extractErrorMessage(errorBody)
            Result.failure(
                BNError.Generic(
                    messageString = errorMessage,
                    error = null,
                    statusCode = registerResponse.code()
                )
            )
        }
    }

    override suspend fun refresh(refreshToken: String): Result<AuthTokenResponse> {
        return requestBody(authService.refresh(RefreshTokenRequest(refreshToken)))
    }

    override suspend fun logout(refreshToken: String): Result<Unit> {
        val response = authService.logout(RefreshTokenRequest(refreshToken))
        return if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun verifyEmail(code: String): Result<UserResponse> {
        val response = authService.verifyEmail(VerifyEmailRequest(code))
        return if (response.isSuccessful && response.body() != null) {
            val verifyResponse = response.body()!!
            if (verifyResponse.user != null) {
                Result.success(verifyResponse.user)
            } else {
                Result.failure(
                    BNError.Generic(
                        messageString = verifyResponse.message ?: "Email verification failed",
                        error = null,
                        statusCode = null
                    )
                )
            }
        } else {
            Result.failure(
                BNError.Generic(
                    messageString = response.errorBody()?.string() ?: "Email verification failed",
                    error = null,
                    statusCode = response.code()
                )
            )
        }
    }

    override suspend fun resendVerification(email: String): Result<Unit> {
        val response = authService.resendVerification(ResendVerificationRequest(email))
        return if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(
                BNError.Generic(
                    messageString = response.errorBody()?.string()
                        ?: "Failed to resend verification",
                    error = null,
                    statusCode = response.code()
                )
            )
        }
    }

    override suspend fun requestPasswordReset(body: RequestPasswordResetRequest): Result<Unit> {
        val response = authService.requestPasswordReset(body)
        return if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(
                BNError.Generic(
                    messageString = response.errorBody()?.string()
                        ?: "Failed to request password reset",
                    error = null,
                    statusCode = response.code()
                )
            )
        }
    }

    override suspend fun resetPassword(body: ResetPasswordRequest): Result<Unit> {
        val response = authService.resetPassword(body)
        return if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(
                BNError.Generic(
                    messageString = response.errorBody()?.string() ?: "Failed to reset password",
                    error = null,
                    statusCode = response.code()
                )
            )
        }
    }
}

