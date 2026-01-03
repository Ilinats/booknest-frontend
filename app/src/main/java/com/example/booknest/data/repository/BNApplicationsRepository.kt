package com.example.booknest.data.repository

import com.example.booknest.data.datasource.ApplicationsDataSource
import com.example.booknest.data.datasource.resultBody
import com.example.booknest.domain.model.request.BulkActionRequest
import com.example.booknest.domain.model.request.CreateApplicationRequest
import com.example.booknest.domain.model.request.UpdateApplicationRequest
import com.example.booknest.domain.model.request.UpdateApplicationCompleteRequest
import com.example.booknest.domain.model.request.UpdateReadingStatusRequest
import com.example.booknest.domain.model.response.ApplicationCheckResponse
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.model.response.BulkActionResponse
import com.example.booknest.domain.repository.ApplicationsRepository

class BNApplicationsRepository(private val applicationsDataSource: ApplicationsDataSource) : ApplicationsRepository {
    
    override suspend fun createApplication(application: CreateApplicationRequest): Result<ApplicationResponse> {
        return resultBody(applicationsDataSource.createApplication(application))
    }

    override suspend fun getMyApplications(): Result<List<ApplicationResponse>> {
        return resultBody(applicationsDataSource.getMyApplications())
    }

    override suspend fun checkApplication(bookId: String): Result<ApplicationCheckResponse> {
        return resultBody(applicationsDataSource.checkApplication(bookId))
    }

    override suspend fun getApplication(applicationId: String): Result<ApplicationResponse> {
        return resultBody(applicationsDataSource.getApplication(applicationId))
    }

    override suspend fun updateApplication(applicationId: String, application: UpdateApplicationRequest): Result<ApplicationResponse> {
        return resultBody(applicationsDataSource.updateApplication(applicationId, application))
    }

    override suspend fun updateApplicationComplete(applicationId: String, dto: UpdateApplicationCompleteRequest): Result<ApplicationResponse> {
        return resultBody(applicationsDataSource.updateApplicationComplete(applicationId, dto))
    }

    override suspend fun withdrawApplication(applicationId: String): Result<Unit> {
        return resultBody(applicationsDataSource.withdrawApplication(applicationId))
    }

    override suspend fun markCopyReceived(applicationId: String): Result<ApplicationResponse> {
        return resultBody(applicationsDataSource.markCopyReceived(applicationId))
    }

    override suspend fun updateReadingStatus(applicationId: String, status: UpdateReadingStatusRequest): Result<ApplicationResponse> {
        return resultBody(applicationsDataSource.updateReadingStatus(applicationId, status))
    }

    override suspend fun getReadingProgress(): Result<List<ApplicationResponse>> {
        return resultBody(applicationsDataSource.getReadingProgress())
    }

    override suspend fun getBookApplications(bookId: String): Result<List<ApplicationResponse>> {
        return resultBody(applicationsDataSource.getBookApplications(bookId))
    }

    override suspend fun bulkActionApplications(bookId: String, action: BulkActionRequest): Result<BulkActionResponse> {
        return resultBody(applicationsDataSource.bulkActionApplications(bookId, action))
    }

    override suspend fun markCopySent(applicationId: String): Result<ApplicationResponse> {
        return resultBody(applicationsDataSource.markCopySent(applicationId))
    }

    override suspend fun getOverdueReviews(): Result<List<ApplicationResponse>> {
        return resultBody(applicationsDataSource.getOverdueReviews())
    }
}

