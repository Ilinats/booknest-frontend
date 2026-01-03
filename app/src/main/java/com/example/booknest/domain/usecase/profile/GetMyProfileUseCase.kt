package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.domain.repository.ProfileRepository

class GetMyProfileUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): Result<UserProfileResponse> {
        return repository.getMyProfile()
    }
}
