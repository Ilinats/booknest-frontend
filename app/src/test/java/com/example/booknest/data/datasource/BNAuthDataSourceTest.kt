package com.example.booknest.data.datasource

import com.example.booknest.data.error.BNError
import com.example.booknest.data.service.AuthService
import com.example.booknest.data.service.ProfilesService
import com.example.booknest.domain.model.request.LoginRequest
import com.example.booknest.domain.model.response.LoginResponse
import okhttp3.ResponseBody.Companion.toResponseBody
import com.example.booknest.port.SessionWriter
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.Response

class BNAuthDataSourceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var authService: AuthService
    private val profilesService = mockk<ProfilesService>()
    private val sessionWriter = mockk<SessionWriter>(relaxed = true)

    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        authService = retrofit.create(AuthService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun createDataSource() = BNAuthDataSource(
        authService = authService,
        profilesService = profilesService,
        sessionWriter = sessionWriter,
    )

    @Test
    fun refresh_successReturnsTokens() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"accessToken":"new-access","refreshToken":"new-refresh"}""")
                .addHeader("Content-Type", "application/json"),
        )

        val result = createDataSource().refresh("old-refresh")

        assertTrue(result.isSuccess)
        assertEquals("new-access", result.getOrNull()?.accessToken)
        assertEquals("new-refresh", result.getOrNull()?.refreshToken)
        assertEquals("/api/auth/refresh-token", mockWebServer.takeRequest().path)
    }

    @Test
    fun refresh_failureMapsErrorBody() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"message":"Invalid refresh token","statusCode":401}""")
                .addHeader("Content-Type", "application/json"),
        )

        val result = createDataSource().refresh("bad-refresh")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is BNError.Generic)
        assertEquals("Invalid refresh token", (error as BNError.Generic).messageString)
        assertEquals(401, error.statusCode)
    }

    @Test
    fun login_successMapsTokens() = runTest {
        val dataSource = BNAuthDataSource(
            authService = mockk {
                coEvery { login(LoginRequest("a@b.com", "secret")) } returns Response.success(
                    LoginResponse(accessToken = "access", refreshToken = "refresh"),
                )
            },
            profilesService = profilesService,
            sessionWriter = sessionWriter,
        )

        val result = dataSource.login(LoginRequest("a@b.com", "secret"))

        assertTrue(result.isSuccess)
        assertEquals("access", result.getOrNull()?.accessToken)
        assertEquals("refresh", result.getOrNull()?.refreshToken)
    }

    @Test
    fun login_failureExtractsMessage() = runTest {
        val dataSource = BNAuthDataSource(
            authService = mockk {
                coEvery { login(any()) } returns Response.error(
                    401,
                    """{"message":"Invalid credentials"}""".toResponseBody(null),
                )
            },
            profilesService = profilesService,
            sessionWriter = sessionWriter,
        )

        val result = dataSource.login(LoginRequest("a@b.com", "wrong"))

        assertTrue(result.isFailure)
        assertEquals(
            "Invalid credentials",
            (result.exceptionOrNull() as BNError.Generic).messageString,
        )
    }
}
