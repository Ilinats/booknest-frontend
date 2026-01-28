package com.example.booknest.domain.usecase.applications

import com.example.booknest.domain.model.request.UpdateReadingStatusRequest
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.repository.ApplicationsRepository

class UpdateReadingStatusUseCase(
    private val applicationsRepository: ApplicationsRepository
) {
    suspend operator fun invoke(applicationId: String, request: UpdateReadingStatusRequest): Result<ApplicationResponse> {
        return applicationsRepository.updateReadingStatus(applicationId, request)
    }
}


