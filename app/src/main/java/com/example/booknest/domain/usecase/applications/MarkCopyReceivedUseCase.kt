package com.example.booknest.domain.usecase.applications

import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.repository.ApplicationsRepository

class MarkCopyReceivedUseCase(
    private val applicationsRepository: ApplicationsRepository
) {
    suspend operator fun invoke(applicationId: String): Result<ApplicationResponse> {
        return applicationsRepository.markCopyReceived(applicationId)
    }
}


