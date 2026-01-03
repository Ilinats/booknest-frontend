package com.example.booknest.data.datasource

import com.example.booknest.domain.model.request.BulkActionRequest
import com.example.booknest.domain.model.request.CreateApplicationRequest
import com.example.booknest.domain.model.request.UpdateApplicationRequest
import com.example.booknest.domain.model.request.UpdateApplicationCompleteRequest
import com.example.booknest.domain.model.request.UpdateReadingStatusRequest
import com.example.booknest.domain.model.response.ApplicationCheckResponse
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.model.response.BulkActionResponse

interface ApplicationsDataSource {
    suspend fun createApplication(application: CreateApplicationRequest): Result<ApplicationResponse>
    suspend fun getMyApplications(): Result<List<ApplicationResponse>>
    suspend fun checkApplication(bookId: String): Result<ApplicationCheckResponse>
    suspend fun getApplication(applicationId: String): Result<ApplicationResponse>
    suspend fun updateApplication(
        applicationId: String,
        application: UpdateApplicationRequest
    ): Result<ApplicationResponse>

    suspend fun updateApplicationComplete(
        applicationId: String,
        dto: UpdateApplicationCompleteRequest
    ): Result<ApplicationResponse>

    suspend fun withdrawApplication(applicationId: String): Result<Unit>
    suspend fun markCopyReceived(applicationId: String): Result<ApplicationResponse>
    suspend fun updateReadingStatus(
        applicationId: String,
        status: UpdateReadingStatusRequest
    ): Result<ApplicationResponse>

    suspend fun getReadingProgress(): Result<List<ApplicationResponse>>
    suspend fun getBookApplications(bookId: String): Result<List<ApplicationResponse>>
    suspend fun bulkActionApplications(
        bookId: String,
        action: BulkActionRequest
    ): Result<BulkActionResponse>

    suspend fun markCopySent(applicationId: String): Result<ApplicationResponse>
    suspend fun getOverdueReviews(): Result<List<ApplicationResponse>>
    suspend fun runLotterySelection(bookId: String): Result<com.example.booknest.domain.model.response.LotteryResponse>
}

