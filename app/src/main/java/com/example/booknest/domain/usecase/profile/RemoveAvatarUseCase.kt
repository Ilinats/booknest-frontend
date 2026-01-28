package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.domain.repository.ProfileRepository

class RemoveAvatarUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(): Result<UserResponse> {
        return profileRepository.removeAvatar()
    }
}


