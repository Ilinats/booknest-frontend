package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.model.response.PublicUserProfileResponse
import com.example.booknest.domain.repository.ProfileRepository

class GetPublicUserProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(username: String): Result<PublicUserProfileResponse> {
        return profileRepository.getPublicUserProfile(username)
    }
}


