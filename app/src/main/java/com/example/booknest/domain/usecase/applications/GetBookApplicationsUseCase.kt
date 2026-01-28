package com.example.booknest.domain.usecase.applications

import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.repository.ApplicationsRepository

class GetBookApplicationsUseCase(
    private val applicationsRepository: ApplicationsRepository
) {
    suspend operator fun invoke(bookId: String): Result<List<ApplicationResponse>> {
        return applicationsRepository.getBookApplications(bookId)
    }
}


