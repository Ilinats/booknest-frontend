package com.example.booknest.domain.usecase.applications

import com.example.booknest.domain.model.request.CreateApplicationRequest
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.repository.ApplicationsRepository

class CreateApplicationUseCase(
    private val applicationsRepository: ApplicationsRepository
) {
    suspend operator fun invoke(request: CreateApplicationRequest): Result<ApplicationResponse> {
        return applicationsRepository.createApplication(request)
    }
}


