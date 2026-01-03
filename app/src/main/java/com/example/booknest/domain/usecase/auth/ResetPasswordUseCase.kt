package com.example.booknest.domain.usecase.auth

import com.example.booknest.domain.model.request.ResetPasswordRequest
import com.example.booknest.domain.repository.AuthRepository

class ResetPasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(code: String, newPassword: String): Result<Unit> {
        val request = ResetPasswordRequest(code = code, newPassword = newPassword)
        return authRepository.resetPassword(request)
    }
}
