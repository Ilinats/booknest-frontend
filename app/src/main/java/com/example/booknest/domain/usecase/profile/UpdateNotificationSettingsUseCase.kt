package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.model.request.UpdateNotificationSettingsRequest
import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.domain.repository.ProfileRepository

class UpdateNotificationSettingsUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(request: UpdateNotificationSettingsRequest): Result<UserProfileResponse> {
        return profileRepository.updateNotificationSettings(request)
    }
}


