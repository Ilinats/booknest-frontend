package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.model.response.PublicUserProfileResponse
import com.example.booknest.domain.repository.ProfileRepository

class GetUserProfileUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(username: String): Result<PublicUserProfileResponse> {
        return repository.getPublicUserProfile(username)
    }
}
