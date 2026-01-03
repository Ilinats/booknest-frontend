package com.example.booknest.domain.usecase.files

import com.example.booknest.domain.repository.ProfileRepository
import okhttp3.MultipartBody

class UploadProfileImageUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(avatarPart: MultipartBody.Part): Result<String> {
        return profileRepository.uploadAvatar(avatarPart)
            .mapCatching { uploadResponse ->
                uploadResponse.avatar.url
            }
    }
}
