package com.example.booknest.data.session

import com.example.booknest.data.constants.Auth
import com.example.booknest.data.constants.RetrofitConstants
import com.example.booknest.domain.model.response.AuthTokenResponse
import com.example.booknest.domain.usecase.auth.RefreshTokenUseCase
import com.example.booknest.port.AuthTokenAccessor
import com.example.booknest.port.SessionWriter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TokenRefreshCoordinatorTest {

    private val refreshTokenUseCase = mockk<RefreshTokenUseCase>()
    private val sessionWriter = mockk<SessionWriter>(relaxed = true)
    private val authTokens = mockk<AuthTokenAccessor>()
    private val coordinator = TokenRefreshCoordinator(
        refreshTokenUseCase = refreshTokenUseCase,
        sessionWriter = sessionWriter,
        authTokens = authTokens,
    )

    @Test
    fun refreshAccessToken_returnsNullForNon401() = runTest {
        val response = buildResponse(code = 403, url = apiUrl("/api/books"))

        assertNull(coordinator.refreshAccessToken(response))
    }

    @Test
    fun refreshAccessToken_returnsNullWhenRetryChainTooLong() = runTest {
        val first = buildResponse(code = 401, url = apiUrl("/api/books"))
        val second = first.newBuilder()
            .request(first.request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body("".toResponseBody(null))
            .build()
        val chained = second.newBuilder()
            .request(second.request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .priorResponse(first)
            .body("".toResponseBody(null))
            .build()

        assertNull(coordinator.refreshAccessToken(chained))
    }

    @Test
    fun refreshAccessToken_logsOutWhenRefreshEndpointReturns401() = runTest {
        val response = buildResponse(
            code = 401,
            url = "${RetrofitConstants.BASE_URL}${Auth.REFRESH_STRING_CONCAT}",
        )

        assertNull(coordinator.refreshAccessToken(response))
        coVerify { sessionWriter.logout() }
    }

    @Test
    fun refreshAccessToken_retriesWithUpdatedTokenFromRefreshUseCase() = runTest {
        val failedRequest = Request.Builder()
            .url(apiUrl("/api/books"))
            .header("Authorization", "Bearer old-token")
            .build()
        val response = buildResponse(code = 401, request = failedRequest)

        every { authTokens.getToken() } returns ""
        coEvery { refreshTokenUseCase() } returns Result.success(
            AuthTokenResponse(accessToken = "new-access", refreshToken = "new-refresh"),
        )

        val retryRequest = coordinator.refreshAccessToken(response)

        assertEquals("Bearer new-access", retryRequest?.header("Authorization"))
        coVerify {
            sessionWriter.updateTokens(accessToken = "new-access", refreshToken = "new-refresh")
        }
    }

    @Test
    fun refreshAccessToken_reusesConcurrentlyRefreshedTokenWithoutCallingRefresh() = runTest {
        val failedRequest = Request.Builder()
            .url(apiUrl("/api/books"))
            .header("Authorization", "Bearer stale-token")
            .build()
        val response = buildResponse(code = 401, request = failedRequest)

        every { authTokens.getToken() } returns "fresh-token"
        val retryRequest = coordinator.refreshAccessToken(response)

        assertEquals("Bearer fresh-token", retryRequest?.header("Authorization"))
        coVerify(exactly = 0) { refreshTokenUseCase() }
    }

    @Test
    fun refreshAccessToken_logsOutWhenRefreshFails() = runTest {
        val failedRequest = Request.Builder()
            .url(apiUrl("/api/books"))
            .header("Authorization", "Bearer old-token")
            .build()
        val response = buildResponse(code = 401, request = failedRequest)

        every { authTokens.getToken() } returns ""
        coEvery { refreshTokenUseCase() } returns Result.failure(IllegalStateException("expired"))

        assertNull(coordinator.refreshAccessToken(response))
        coVerify { sessionWriter.logout() }
    }

    private fun apiUrl(path: String): String = "${RetrofitConstants.BASE_URL}$path"

    private fun buildResponse(
        code: Int,
        url: String = apiUrl("/api/books"),
        request: Request = Request.Builder().url(url).build(),
    ): Response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message("HTTP $code")
        .body("".toResponseBody(null))
        .build()
}
