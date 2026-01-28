package com.example.booknest.domain.usecase.profile

import com.example.booknest.domain.model.request.UpdateAddressRequest
import com.example.booknest.domain.model.response.ReaderAddressResponse
import com.example.booknest.domain.repository.ProfileRepository

class UpdateAddressUseCase(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(addressId: String, request: UpdateAddressRequest): Result<ReaderAddressResponse> {
        return profileRepository.updateAddress(addressId, request)
    }
}


