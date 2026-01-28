package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.repository.ProfileRepository

class DeleteAddressUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(addressId: String): Result<Unit> {
        return profileRepository.deleteAddress(addressId)
    }
}


