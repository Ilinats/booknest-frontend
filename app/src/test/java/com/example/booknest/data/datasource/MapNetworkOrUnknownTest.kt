package com.example.booknest.data.datasource

import com.example.booknest.data.error.BNError
import kotlinx.serialization.SerializationException
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MapNetworkOrUnknownTest {

    @Test
    fun mapNetworkOrUnknown_mapsSocketTimeout() {
        val mapped = mapNetworkOrUnknown(SocketTimeoutException("timeout"))

        assertTrue(mapped is BNError.Network)
        assertEquals(
            "The server took too long to respond. Try again.",
            (mapped as BNError.Network).messageString,
        )
    }

    @Test
    fun mapNetworkOrUnknown_mapsUnknownHost() {
        val mapped = mapNetworkOrUnknown(UnknownHostException("offline"))

        assertTrue(mapped is BNError.Network)
        assertEquals(
            "Unable to reach the server. Check your connection and try again.",
            (mapped as BNError.Network).messageString,
        )
    }

    @Test
    fun mapNetworkOrUnknown_mapsSerializationException() {
        val mapped = mapNetworkOrUnknown(SerializationException("bad json"))

        assertTrue(mapped is BNError.Generic)
        assertEquals(
            "Could not read the server response. Try again.",
            (mapped as BNError.Generic).messageString,
        )
    }

    @Test
    fun mapNetworkOrUnknown_mapsIOExceptionAsNetwork() {
        val mapped = mapNetworkOrUnknown(IOException("broken pipe"))

        assertTrue(mapped is BNError.Network)
    }

    @Test
    fun mapNetworkOrUnknown_httpExceptionUsesExtractedMessage() {
        val response = Response.error<String>(
            422,
            """{"message":"Validation failed"}""".toResponseBody(null),
        )
        val httpException = HttpException(response)

        val mapped = mapNetworkOrUnknown(httpException) as BNError.Generic

        assertEquals("Validation failed", mapped.messageString)
        assertEquals(422, mapped.statusCode)
    }

    @Test
    fun mapNetworkOrUnknown_httpExceptionFallsBackToStatusCode() {
        val response = Response.error<String>(
            503,
            "".toResponseBody(null),
        )
        val mapped = mapNetworkOrUnknown(HttpException(response)) as BNError.Generic

        assertEquals("Request failed (503)", mapped.messageString)
        assertEquals(503, mapped.statusCode)
    }
}
