package com.example.booknest.data.datasource

import com.example.booknest.data.error.BNError
import com.example.booknest.data.service.ReviewsService
import com.example.booknest.domain.model.request.CreateReviewRequest
import com.example.booknest.testutil.DataSourceJsonFixtures
import com.example.booknest.testutil.MockWebServerDataSourceTest
import com.example.booknest.testutil.RetrofitTestSupport
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BNReviewsDataSourceTest : MockWebServerDataSourceTest() {

    private val dataSource: BNReviewsDataSource
        get() = BNReviewsDataSource(RetrofitTestSupport.service<ReviewsService>(mockWebServer))

    @Test
    fun getReview_successReturnsReview() = runTest {
        enqueueJson(200, DataSourceJsonFixtures.review)

        val result = dataSource.getReview("review-1")

        assertTrue(result.isSuccess)
        assertEquals("review-1", result.getOrNull()?.id)
        assertEquals("/api/reviews/review-1", mockWebServer.takeRequest().path)
    }

    @Test
    fun createReview_successReturnsReview() = runTest {
        enqueueJson(200, DataSourceJsonFixtures.review)

        val result = dataSource.createReview(
            CreateReviewRequest(
                applicationId = "app-1",
                rating = 4.5,
                reviewType = "written",
                reviewContent = "Great book",
                isPublic = true,
            ),
        )

        assertTrue(result.isSuccess)
        assertEquals(4.5, result.getOrNull()?.rating!!, 0.001)
        assertEquals("POST", mockWebServer.takeRequest().method)
    }

    @Test
    fun getAuthorLatestReviews_successReturnsList() = runTest {
        enqueueJson(200, "[${DataSourceJsonFixtures.review}]")

        val result = dataSource.getAuthorLatestReviews(limit = 3)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertTrue(mockWebServer.takeRequest().path!!.startsWith("/api/users/me/reviews/latest"))
    }

    @Test
    fun deleteReview_failureMapsBnError() = runTest {
        enqueueJson(403, DataSourceJsonFixtures.errorBody("Forbidden", 403))

        val result = dataSource.deleteReview("review-1")

        assertTrue(result.isFailure)
        assertEquals("Forbidden", (result.exceptionOrNull() as BNError.Generic).messageString)
    }
}
