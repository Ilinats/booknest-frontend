package com.example.booknest.domain.usecase.auth

import com.example.booknest.domain.model.request.AddressDto
import com.example.booknest.domain.model.request.RegisterRequest
import com.example.booknest.domain.model.response.RegisterResponse
import com.example.booknest.domain.repository.AuthRepository

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        username: String,
        email: String,
        password: String,
        userType: String,
        firstName: String,
        lastName: String,
        birthDate: String? = null,
        bio: String? = null,
        avatarUrl: String? = null,
        address: AddressDto? = null
    ): Result<RegisterResponse> {
        val request = RegisterRequest(
            username = username,
            email = email,
            password = password,
            userType = userType,
            firstName = firstName,
            lastName = lastName,
            birthDate = birthDate,
            bio = bio,
            avatarUrl = avatarUrl,
            address = address
        )
        return authRepository.register(request)
    }
}
