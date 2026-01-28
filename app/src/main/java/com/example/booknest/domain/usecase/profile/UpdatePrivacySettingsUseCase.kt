package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.model.request.UpdatePrivacyRequest
import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.domain.repository.ProfileRepository

class UpdatePrivacySettingsUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(request: UpdatePrivacyRequest): Result<UserProfileResponse> {
        return profileRepository.updatePrivacySettings(request)
    }
}


