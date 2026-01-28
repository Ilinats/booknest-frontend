package com.example.booknest.domain.usecase.applications

import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.repository.ApplicationsRepository
class GetMyApplicationsUseCase(
    private val repository: ApplicationsRepository
) {
    suspend operator fun invoke(): Result<List<ApplicationResponse>> =
        repository.getMyApplications()
}
