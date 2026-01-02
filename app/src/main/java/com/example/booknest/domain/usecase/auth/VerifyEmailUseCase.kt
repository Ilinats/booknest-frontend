package com.example.booknest.domain.usecase.auth

import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.domain.repository.AuthRepository

class VerifyEmailUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(code: String): Result<UserResponse> {
        return authRepository.verifyEmail(code)
    }
}
