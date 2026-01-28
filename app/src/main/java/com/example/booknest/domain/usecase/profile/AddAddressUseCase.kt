package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.model.request.CreateAddressRequest
import com.example.booknest.domain.model.response.ReaderAddressResponse
import com.example.booknest.domain.repository.ProfileRepository

class AddAddressUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(request: CreateAddressRequest): Result<ReaderAddressResponse> {
        return profileRepository.addAddress(request)
    }
}


