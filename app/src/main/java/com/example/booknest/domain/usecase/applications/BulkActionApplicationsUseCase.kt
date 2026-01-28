package com.example.booknest.domain.usecase.applications

import com.example.booknest.domain.model.request.BulkActionRequest
import com.example.booknest.domain.model.response.BulkActionResponse
import com.example.booknest.domain.repository.ApplicationsRepository

class BulkActionApplicationsUseCase(
    private val applicationsRepository: ApplicationsRepository
) {
    suspend operator fun invoke(bookId: String, request: BulkActionRequest): Result<BulkActionResponse> {
        return applicationsRepository.bulkActionApplications(bookId, request)
    }
}


