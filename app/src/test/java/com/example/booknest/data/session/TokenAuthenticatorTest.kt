package com.example.booknest.data.session

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TokenAuthenticatorTest {

    private val coordinator = mockk<TokenRefreshCoordinator>()
    private val authenticator = TokenAuthenticator(coordinator)

    @Test
    fun authenticate_delegatesToRefreshCoordinator() {
        val request = Request.Builder().url("https://api.example.com/api/books").build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body("".toResponseBody(null))
            .build()
        val retried = request.newBuilder().header("Authorization", "Bearer new-token").build()
        every { coordinator.refreshAccessToken(response) } returns retried

        val result = authenticator.authenticate(null, response)

        assertEquals(retried, result)
        verify { coordinator.refreshAccessToken(response) }
    }

    @Test
    fun authenticate_returnsNullWhenCoordinatorCannotRefresh() {
        val request = Request.Builder().url("https://api.example.com/api/books").build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body("".toResponseBody(null))
            .build()
        every { coordinator.refreshAccessToken(response) } returns null

        assertNull(authenticator.authenticate(null, response))
    }
}
