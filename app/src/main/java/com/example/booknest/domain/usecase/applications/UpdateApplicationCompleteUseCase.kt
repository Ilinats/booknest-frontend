package com.example.booknest.domain.usecase.applications

import com.example.booknest.domain.model.request.UpdateApplicationCompleteRequest
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.repository.ApplicationsRepository

class UpdateApplicationCompleteUseCase(
    private val applicationsRepository: ApplicationsRepository
) {
    suspend operator fun invoke(applicationId: String, request: UpdateApplicationCompleteRequest): Result<ApplicationResponse> {
        return applicationsRepository.updateApplicationComplete(applicationId, request)
    }
}


