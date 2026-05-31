package com.example.booknest.data.datasource

import com.example.booknest.data.error.BNError
import com.example.booknest.domain.model.response.PaginatedResponse
import com.example.booknest.domain.model.response.PaginateMeta
import com.example.booknest.testutil.TestFixtures
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class RequestPaginatedBodyTest {

    @Test
    fun requestPaginatedBody_successExtractsDataList() {
        val books = listOf(TestFixtures.book())
        val response = Response.success(
            PaginatedResponse(data = books, meta = PaginateMeta(totalItems = 1)),
        )

        val result = requestPaginatedBody(response)

        assertTrue(result.isSuccess)
        assertEquals(books, result.getOrNull())
    }

    @Test
    fun requestPaginatedBody_failureMapsBnError() {
        val response = Response.error<PaginatedResponse<Nothing>>(
            404,
            """{"message":"Not found"}""".toResponseBody(null),
        )

        val result = requestPaginatedBody(response)

        assertTrue(result.isFailure)
        assertEquals("Not found", (result.exceptionOrNull() as BNError.Generic).messageString)
        assertEquals(404, (result.exceptionOrNull() as BNError.Generic).statusCode)
    }
}
