package com.example.booknest.domain.usecase.auth

import com.example.booknest.domain.model.response.AuthTokenResponse
import com.example.booknest.domain.repository.AuthRepository

class RefreshTokenUseCase(private val authRepository: Lazy<AuthRepository>) {
    suspend operator fun invoke(): Result<AuthTokenResponse> = authRepository.value.refresh()
}

