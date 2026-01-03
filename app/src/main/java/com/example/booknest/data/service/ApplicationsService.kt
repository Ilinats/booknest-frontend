package com.example.booknest.data.service

import com.example.booknest.data.constants.Applications
import com.example.booknest.data.constants.PathConstants
import com.example.booknest.domain.model.request.ApproveApplicationRequest
import com.example.booknest.domain.model.request.BulkActionRequest
import com.example.booknest.domain.model.request.CreateApplicationRequest
import com.example.booknest.domain.model.request.RejectApplicationRequest
import com.example.booknest.domain.model.request.UpdateApplicationRequest
import com.example.booknest.domain.model.request.UpdateApplicationCompleteRequest
import com.example.booknest.domain.model.request.UpdateReadingStatusRequest
import com.example.booknest.domain.model.response.ApplicationCheckResponse
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.model.response.BulkActionResponse
import com.example.booknest.domain.model.response.PaginatedResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApplicationsService {
    @POST(Applications.CREATE)
    suspend fun createApplication(@Body application: CreateApplicationRequest): Response<ApplicationResponse>

    @GET(Applications.MY_APPLICATIONS)
    suspend fun getMyApplications(): Response<PaginatedResponse<ApplicationResponse>>

    @GET(Applications.CHECK)
    suspend fun checkApplication(
        @Path(PathConstants.BOOK_ID) bookId: String
    ): Response<ApplicationCheckResponse>

    @GET(Applications.BY_ID)
    suspend fun getApplication(
        @Path(PathConstants.APPLICATION_ID) applicationId: String
    ): Response<ApplicationResponse>

    @PUT(Applications.BY_ID)
    suspend fun updateApplication(
        @Path(PathConstants.APPLICATION_ID) applicationId: String,
        @Body application: UpdateApplicationRequest
    ): Response<ApplicationResponse>

    @PATCH(Applications.BY_ID)
    suspend fun updateApplicationComplete(
        @Path(PathConstants.APPLICATION_ID) applicationId: String,
        @Body dto: UpdateApplicationCompleteRequest
    ): Response<ApplicationResponse>

    @DELETE(Applications.BY_ID)
    suspend fun withdrawApplication(
        @Path(PathConstants.APPLICATION_ID) applicationId: String
    ): Response<Unit>

    @PUT(Applications.MARK_RECEIVED)
    suspend fun markCopyReceived(
        @Path(PathConstants.APPLICATION_ID) applicationId: String
    ): Response<ApplicationResponse>

    @PUT(Applications.READING_STATUS)
    suspend fun updateReadingStatus(
        @Path(PathConstants.APPLICATION_ID) applicationId: String,
        @Body status: UpdateReadingStatusRequest
    ): Response<ApplicationResponse>

    @GET(Applications.READING_PROGRESS)
    suspend fun getReadingProgress(): Response<List<ApplicationResponse>>

    @GET(Applications.BOOK_APPLICATIONS)
    suspend fun getBookApplications(
        @Path(PathConstants.BOOK_ID) bookId: String
    ): Response<PaginatedResponse<ApplicationResponse>>

    @POST(Applications.BULK_ACTION)
    suspend fun bulkActionApplications(
        @Path(PathConstants.BOOK_ID) bookId: String,
        @Body action: BulkActionRequest
    ): Response<BulkActionResponse>

    @PUT(Applications.MARK_SENT)
    suspend fun markCopySent(
        @Path(PathConstants.APPLICATION_ID) applicationId: String
    ): Response<ApplicationResponse>

    @GET(Applications.OVERDUE_REVIEWS)
    suspend fun getOverdueReviews(): Response<List<ApplicationResponse>>
}

