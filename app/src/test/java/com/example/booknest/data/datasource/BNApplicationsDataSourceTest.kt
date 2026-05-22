package com.example.booknest.data.datasource

import com.example.booknest.data.error.BNError
import com.example.booknest.data.service.ApplicationsService
import com.example.booknest.testutil.DataSourceJsonFixtures
import com.example.booknest.testutil.MockWebServerDataSourceTest
import com.example.booknest.testutil.RetrofitTestSupport
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BNApplicationsDataSourceTest : MockWebServerDataSourceTest() {

    private val dataSource: BNApplicationsDataSource
        get() = BNApplicationsDataSource(RetrofitTestSupport.service<ApplicationsService>(mockWebServer))

    @Test
    fun checkApplication_successReturnsPayload() = runTest {
        enqueueJson(200, """{"hasApplied":false}""")

        val result = dataSource.checkApplication("book-42")

        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull()!!.hasApplied)
        assertEquals("/api/applications/check/book-42", mockWebServer.takeRequest().path)
    }

    @Test
    fun getMyApplications_successReturnsPaginatedData() = runTest {
        enqueueJson(
            200,
            """
            {
              "data": [
                {
                  "id": "app-1",
                  "status": "pending",
                  "appliedAt": "2024-06-01T00:00:00.000Z",
                  "bookId": "book-1",
                  "readingStatus": "not_started"
                }
              ]
            }
            """.trimIndent(),
        )

        val result = dataSource.getMyApplications()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("app-1", result.getOrNull()?.first()?.id)
    }

    @Test
    fun getApplication_failureMapsBnError() = runTest {
        enqueueJson(404, DataSourceJsonFixtures.errorBody("Application not found", 404))

        val result = dataSource.getApplication("app-missing")

        assertTrue(result.isFailure)
        assertEquals(
            "Application not found",
            (result.exceptionOrNull() as BNError.Generic).messageString,
        )
    }
}
