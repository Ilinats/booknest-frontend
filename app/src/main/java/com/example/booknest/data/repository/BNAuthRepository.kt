package com.example.booknest.data.repository

import com.example.booknest.data.datasource.AuthDataSource
import com.example.booknest.data.datasource.resultBody
import com.example.booknest.domain.model.request.LoginRequest
import com.example.booknest.domain.model.request.RegisterRequest
import com.example.booknest.domain.model.request.RequestPasswordResetRequest
import com.example.booknest.domain.model.request.ResetPasswordRequest
import com.example.booknest.domain.model.response.AuthTokenResponse
import com.example.booknest.domain.model.response.GoogleAuthDataResponse
import com.example.booknest.domain.model.response.LoginDataResponse
import com.example.booknest.domain.model.response.RegisterResponse
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.domain.repository.AuthRepository

class BNAuthRepository(private val authDataSource: AuthDataSource) : AuthRepository {

    override suspend fun login(body: LoginRequest): Result<LoginDataResponse> {
        return resultBody(authDataSource.login(body))
    }

    override suspend fun register(body: RegisterRequest): Result<RegisterResponse> {
        return resultBody(authDataSource.register(body))
    }

    override suspend fun refresh(): Result<AuthTokenResponse> {
        return resultBody(
            authDataSource.refresh(
                com.example.booknest.data.session.SessionManager.getRefreshToken()
            )
        )
    }

    override suspend fun googleLogin(
        idToken: String,
        userType: String
    ): Result<GoogleAuthDataResponse> {
        return resultBody(authDataSource.googleLogin(idToken, userType))
    }

    override suspend fun verifyEmail(code: String): Result<UserResponse> {
        return resultBody(authDataSource.verifyEmail(code))
    }

    override suspend fun resendVerification(email: String): Result<Unit> {
        return resultBody(authDataSource.resendVerification(email))
    }

    override suspend fun requestPasswordReset(body: RequestPasswordResetRequest): Result<Unit> {
        return resultBody(authDataSource.requestPasswordReset(body))
    }

    override suspend fun resetPassword(body: ResetPasswordRequest): Result<Unit> {
        return resultBody(authDataSource.resetPassword(body))
    }
}

