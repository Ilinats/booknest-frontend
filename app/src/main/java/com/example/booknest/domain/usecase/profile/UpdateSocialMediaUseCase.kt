package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.model.request.UpdateSocialMediaRequest
import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.domain.repository.ProfileRepository

class UpdateSocialMediaUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(request: UpdateSocialMediaRequest): Result<UserProfileResponse> {
        return profileRepository.updateSocialMedia(request)
    }
}


