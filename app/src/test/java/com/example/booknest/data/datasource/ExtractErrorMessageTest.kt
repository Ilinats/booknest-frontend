package com.example.booknest.data.datasource

import com.example.booknest.data.error.BNError
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ExtractErrorMessageTest {

    @Test
    fun extractErrorMessage_parsesJsonMessage() {
        val body = """{"message":"Invalid credentials","error":null,"statusCode":401}"""
        assertEquals("Invalid credentials", extractErrorMessage(body))
    }

    @Test
    fun extractErrorMessage_joinsArrayMessages() {
        val body = """{"message":["Title is required","Genres required"],"statusCode":400}"""
        assertEquals("Title is required, Genres required", extractErrorMessage(body))
    }

    @Test
    fun extractErrorMessage_mapsAlreadyAppliedFromRawBody() {
        val body = """{"error":"APPLICATION_ALREADY_EXISTS"}"""
        assertEquals(
            "You have already applied for this book",
            extractErrorMessage(body),
        )
    }

    @Test
    fun extractErrorMessage_returnsDefaultForBlank() {
        assertEquals("An error occurred", extractErrorMessage(null))
        assertEquals("An error occurred", extractErrorMessage("  "))
    }

    @Test
    fun requestBody_successReturnsBody() {
        val result = requestBody(Response.success("ok"))
        assertEquals("ok", result.getOrNull())
    }

    @Test
    fun requestBody_failureWrapsBnError() {
        val errorBody = """{"message":"Not found","statusCode":404}"""
        val response = Response.error<String>(
            404,
            errorBody.toResponseBody(null),
        )

        val failure = requestBody(response).exceptionOrNull()

        assertTrue(failure is BNError.Generic)
        assertEquals("Not found", (failure as BNError.Generic).messageString)
        assertEquals(404, failure.statusCode)
    }

    @Test
    fun mapNetworkOrUnknown_mapsHttpException() {
        val response = Response.error<String>(
            500,
            """{"message":"Server error"}""".toResponseBody(null),
        )
        val mapped = mapNetworkOrUnknown(HttpException(response))

        assertTrue(mapped is BNError.Generic)
        assertEquals(500, (mapped as BNError.Generic).statusCode)
    }

    @Test
    fun mapNetworkOrUnknown_preservesExistingBnError() {
        val original = BNError.Unauthorized(messageString = "Unauthorized")
        assertEquals(original, mapNetworkOrUnknown(original))
    }
}
