package com.example.booknest.data.datasource

import com.example.booknest.data.service.ApplicationsService
import com.example.booknest.domain.model.request.BulkActionRequest
import com.example.booknest.domain.model.request.CreateApplicationRequest
import com.example.booknest.domain.model.request.UpdateApplicationRequest
import com.example.booknest.domain.model.request.UpdateApplicationCompleteRequest
import com.example.booknest.domain.model.request.UpdateReadingStatusRequest
import com.example.booknest.domain.model.response.ApplicationCheckResponse
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.model.response.BulkActionResponse

class BNApplicationsDataSource(private val applicationsService: ApplicationsService) :
    ApplicationsDataSource {

    override suspend fun createApplication(application: CreateApplicationRequest): Result<ApplicationResponse> {
        return requestBody(applicationsService.createApplication(application))
    }

    override suspend fun getMyApplications(): Result<List<ApplicationResponse>> {
        return requestPaginatedBody(applicationsService.getMyApplications())
    }

    override suspend fun checkApplication(bookId: String): Result<ApplicationCheckResponse> {
        return requestBody(applicationsService.checkApplication(bookId))
    }

    override suspend fun getApplication(applicationId: String): Result<ApplicationResponse> {
        return requestBody(applicationsService.getApplication(applicationId))
    }

    override suspend fun updateApplication(
        applicationId: String,
        application: UpdateApplicationRequest
    ): Result<ApplicationResponse> {
        return requestBody(applicationsService.updateApplication(applicationId, application))
    }

    override suspend fun updateApplicationComplete(
        applicationId: String,
        dto: UpdateApplicationCompleteRequest
    ): Result<ApplicationResponse> {
        return requestBody(applicationsService.updateApplicationComplete(applicationId, dto))
    }

    override suspend fun withdrawApplication(applicationId: String): Result<Unit> {
        return requestBodyUnit(applicationsService.withdrawApplication(applicationId))
    }

    override suspend fun markCopyReceived(applicationId: String): Result<ApplicationResponse> {
        return requestBody(applicationsService.markCopyReceived(applicationId))
    }

    override suspend fun updateReadingStatus(
        applicationId: String,
        status: UpdateReadingStatusRequest
    ): Result<ApplicationResponse> {
        return requestBody(applicationsService.updateReadingStatus(applicationId, status))
    }

    override suspend fun getReadingProgress(): Result<List<ApplicationResponse>> {
        return requestBody(applicationsService.getReadingProgress())
    }

    override suspend fun getBookApplications(bookId: String): Result<List<ApplicationResponse>> {
        return requestPaginatedBody(applicationsService.getBookApplications(bookId))
    }

    override suspend fun bulkActionApplications(
        bookId: String,
        action: BulkActionRequest
    ): Result<BulkActionResponse> {
        return requestBody(applicationsService.bulkActionApplications(bookId, action))
    }

    override suspend fun markCopySent(applicationId: String): Result<ApplicationResponse> {
        return requestBody(applicationsService.markCopySent(applicationId))
    }

    override suspend fun getOverdueReviews(): Result<List<ApplicationResponse>> {
        return requestBody(applicationsService.getOverdueReviews())
    }
}

