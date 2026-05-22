package com.example.booknest.data.session

import com.example.booknest.data.constants.Auth
import com.example.booknest.data.constants.Header
import com.example.booknest.data.constants.RetrofitConstants
import com.example.booknest.port.AuthTokenAccessor
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TokenInterceptorTest {

    private val authTokens = mockk<AuthTokenAccessor>()
    private val interceptor = TokenInterceptor(authTokens)

    @Test
    fun intercept_addsBearerTokenForApiRequests() {
        every { authTokens.getToken() } returns "access-token"

        val request = intercept(url = "${RetrofitConstants.BASE_URL}api/books")

        verify { authTokens.getToken() }
        assertEquals("${Header.BEARER} access-token", request.header(Header.AUTHORIZATION))
    }

    @Test
    fun intercept_usesRefreshTokenOnRefreshEndpoint() {
        every { authTokens.getRefreshToken() } returns "refresh-token"

        val request = intercept(url = "${RetrofitConstants.BASE_URL}${Auth.REFRESH_STRING_CONCAT}")

        verify { authTokens.getRefreshToken() }
        assertEquals("${Header.BEARER} refresh-token", request.header(Header.AUTHORIZATION))
    }

    @Test
    fun intercept_skipsAuthorizationForLoginAndRegister() {
        every { authTokens.getToken() } returns "access-token"

        val loginRequest = intercept(url = "${RetrofitConstants.BASE_URL}${Auth.LOGIN}")
        val registerRequest = intercept(url = "${RetrofitConstants.BASE_URL}${Auth.REGISTER}")

        assertNull(loginRequest.header(Header.AUTHORIZATION))
        assertNull(registerRequest.header(Header.AUTHORIZATION))
    }

    @Test
    fun intercept_skipsAuthorizationForS3Hosts() {
        every { authTokens.getToken() } returns "access-token"

        val request = intercept(url = "https://my-bucket.s3.amazonaws.com/cover.jpg")

        assertNull(request.header(Header.AUTHORIZATION))
    }

    @Test
    fun intercept_doesNotAddHeaderWhenTokenEmpty() {
        every { authTokens.getToken() } returns ""

        val request = intercept(url = "${RetrofitConstants.BASE_URL}api/books")

        assertNull(request.header(Header.AUTHORIZATION))
    }

    private fun intercept(url: String): Request {
        val chain = mockk<Interceptor.Chain>()
        val original = Request.Builder().url(url).build()
        val requestSlot = slot<Request>()
        val response = Response.Builder()
            .request(original)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("".toResponseBody(null))
            .build()

        every { chain.request() } returns original
        every { chain.proceed(capture(requestSlot)) } returns response

        interceptor.intercept(chain)
        return requestSlot.captured
    }
}
