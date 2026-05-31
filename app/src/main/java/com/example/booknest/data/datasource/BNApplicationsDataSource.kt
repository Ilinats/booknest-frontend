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
import com.example.booknest.domain.model.response.LotteryResponse

class BNApplicationsDataSource(private val applicationsService: ApplicationsService) :
    ApplicationsDataSource {

    override suspend fun createApplication(application: CreateApplicationRequest): Result<ApplicationResponse> {
        return runSuspendRequest { applicationsService.createApplication(application) }
    }

    override suspend fun getMyApplications(): Result<List<ApplicationResponse>> {
        return runSuspendRequestPaginated { applicationsService.getMyApplications() }
    }

    override suspend fun checkApplication(bookId: String): Result<ApplicationCheckResponse> {
        return runSuspendRequest { applicationsService.checkApplication(bookId) }
    }

    override suspend fun getApplication(applicationId: String): Result<ApplicationResponse> {
        return runSuspendRequest { applicationsService.getApplication(applicationId) }
    }

    override suspend fun updateApplication(
        applicationId: String,
        application: UpdateApplicationRequest
    ): Result<ApplicationResponse> {
        return runSuspendRequest { applicationsService.updateApplication(applicationId, application) }
    }

    override suspend fun updateApplicationComplete(
        applicationId: String,
        dto: UpdateApplicationCompleteRequest
    ): Result<ApplicationResponse> {
        return runSuspendRequest { applicationsService.updateApplicationComplete(applicationId, dto) }
    }

    override suspend fun withdrawApplication(applicationId: String): Result<Unit> {
        return runSuspendRequestUnit { applicationsService.withdrawApplication(applicationId) }
    }

    override suspend fun markCopyReceived(applicationId: String): Result<ApplicationResponse> {
        return runSuspendRequest { applicationsService.markCopyReceived(applicationId) }
    }

    override suspend fun updateReadingStatus(
        applicationId: String,
        status: UpdateReadingStatusRequest
    ): Result<ApplicationResponse> {
        return runSuspendRequest { applicationsService.updateReadingStatus(applicationId, status) }
    }

    override suspend fun getReadingProgress(): Result<List<ApplicationResponse>> {
        return runSuspendRequestPaginated {
            applicationsService.getMyApplications(page = 1, limit = 100)
        }.map { list ->
            list.filter { it.isActiveReadingProgress() }
        }
    }

    private fun ApplicationResponse.isActiveReadingProgress(): Boolean {
        if (!status.equals("approved", ignoreCase = true)) return false
        if (readingStatus.equals("reviewed", ignoreCase = true)) return false
        return true
    }

    override suspend fun getBookApplications(bookId: String): Result<List<ApplicationResponse>> {
        return runSuspendRequestPaginated {
            applicationsService.getBookApplications(bookId, page = 1, limit = 100)
        }
    }

    override suspend fun bulkActionApplications(
        bookId: String,
        action: BulkActionRequest
    ): Result<BulkActionResponse> {
        return runSuspendRequest { applicationsService.bulkActionApplications(bookId, action) }
    }

    override suspend fun markCopySent(applicationId: String): Result<ApplicationResponse> {
        return runSuspendRequest { applicationsService.markCopySent(applicationId) }
    }

    override suspend fun getOverdueReviews(): Result<List<ApplicationResponse>> {
        return runSuspendRequest { applicationsService.getOverdueReviews() }
    }

    override suspend fun runLotterySelection(bookId: String): Result<LotteryResponse> {
        return runSuspendRequest { applicationsService.runLottery(bookId) }
    }
}
