package com.example.booknest.domain.usecase.applications

import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.repository.ApplicationsRepository

class GetOverdueReviewsUseCase(
    private val applicationsRepository: ApplicationsRepository
) {
    suspend operator fun invoke(): Result<List<ApplicationResponse>> {
        return applicationsRepository.getOverdueReviews()
    }
}


