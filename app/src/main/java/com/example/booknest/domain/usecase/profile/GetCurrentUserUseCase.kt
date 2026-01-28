package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.domain.repository.ProfileRepository

class GetCurrentUserUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): Result<UserResponse> {
        return repository.getMe()
    }
}

