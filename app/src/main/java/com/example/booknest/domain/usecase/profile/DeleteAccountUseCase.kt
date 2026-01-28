package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.repository.ProfileRepository

class DeleteAccountUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return profileRepository.deleteAccount()
    }
}


