package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.model.response.ReaderAddressResponse
import com.example.booknest.domain.repository.ProfileRepository

class GetMyAddressesUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(): Result<List<ReaderAddressResponse>> {
        return profileRepository.getMyAddresses()
    }
}


