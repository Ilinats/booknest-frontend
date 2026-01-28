package com.example.booknest.domain.usecase.applications

import com.example.booknest.domain.model.response.ApplicationCheckResponse
import com.example.booknest.domain.repository.ApplicationsRepository

class CheckApplicationUseCase(
    private val applicationsRepository: ApplicationsRepository
) {
    suspend operator fun invoke(bookId: String): Result<ApplicationCheckResponse> {
        return applicationsRepository.checkApplication(bookId)
    }
}


