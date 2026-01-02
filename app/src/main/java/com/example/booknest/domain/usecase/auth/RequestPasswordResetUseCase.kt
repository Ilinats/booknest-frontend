package com.example.booknest.domain.usecase.auth

import com.example.booknest.domain.model.request.RequestPasswordResetRequest
import com.example.booknest.domain.repository.AuthRepository

class RequestPasswordResetUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        val request = RequestPasswordResetRequest(email = email)
        return authRepository.requestPasswordReset(request)
    }
}
