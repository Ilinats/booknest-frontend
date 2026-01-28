package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.model.request.UpdateProfileRequest
import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.domain.repository.ProfileRepository

class UpdateMyProfileUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(request: UpdateProfileRequest): Result<UserProfileResponse> {
        return profileRepository.updateMyProfile(request)
    }
}


