package com.example.booknest.domain.usecase.auth

import com.example.booknest.domain.repository.AuthRepository

class ResendVerificationCodeUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return authRepository.resendVerification(email)
    }
}
