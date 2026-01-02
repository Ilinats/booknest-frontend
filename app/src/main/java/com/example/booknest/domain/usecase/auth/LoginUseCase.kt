package com.example.booknest.domain.usecase.auth

import com.example.booknest.domain.model.request.LoginRequest
import com.example.booknest.domain.model.response.LoginDataResponse
import com.example.booknest.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        identifier: String,
        password: String
    ): Result<LoginDataResponse> {
        val request = LoginRequest(identifier = identifier, password = password)
        return authRepository.login(request)
    }
}
