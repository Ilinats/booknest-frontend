package com.example.booknest.data.datasource

import com.example.booknest.data.error.BNError
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class RunSuspendRequestTest {

    @Test
    fun runSuspendRequestUnit_successReturnsUnit() = runTest {
        val block = mockk<suspend () -> Response<Unit>>()
        coEvery { block() } returns Response.success(Unit)

        val result = runSuspendRequestUnit(block)

        assertTrue(result.isSuccess)
    }

    @Test
    fun runSuspendRequestUnit_failureMapsBnError() = runTest {
        val block = mockk<suspend () -> Response<Unit>>()
        coEvery { block() } returns Response.error(
            400,
            """{"message":"Bad request"}""".toResponseBody(null),
        )

        val result = runSuspendRequestUnit(block)

        assertTrue(result.isFailure)
        assertEquals(
            "Bad request",
            (result.exceptionOrNull() as BNError.Generic).messageString,
        )
    }
}
