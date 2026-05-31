package com.example.booknest.domain.usecase.auth

import com.example.booknest.domain.model.request.LoginRequest
import com.example.booknest.domain.model.request.RegisterRequest
import com.example.booknest.domain.repository.AuthRepository
import com.example.booknest.testutil.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthUseCasesTest {

    private val repository = mockk<AuthRepository>()

    @Test
    fun registerUseCase_buildsRequestAndDelegates() = runTest {
        val response = mockk<com.example.booknest.domain.model.response.RegisterResponse>()
        coEvery { repository.register(any()) } returns Result.success(response)

        val result = RegisterUseCase(repository)(
            username = "newuser",
            email = "new@example.com",
            password = "secret",
            userType = "reader",
            firstName = "New",
            lastName = "User",
        )

        assertEquals(response, result.getOrNull())
        coVerify {
            repository.register(
                RegisterRequest(
                    username = "newuser",
                    email = "new@example.com",
                    password = "secret",
                    userType = "reader",
                    firstName = "New",
                    lastName = "User",
                    birthDate = null,
                    bio = null,
                    avatarUrl = null,
                    address = null,
                ),
            )
        }
    }

    @Test
    fun refreshTokenUseCase_delegatesToRepository() = runTest {
        val tokens = com.example.booknest.domain.model.response.AuthTokenResponse(
            accessToken = "new-access",
            refreshToken = "new-refresh",
        )
        val lazyRepo = lazy { repository }
        coEvery { repository.refresh() } returns Result.success(tokens)

        val result = RefreshTokenUseCase(lazyRepo)()

        assertEquals(tokens, result.getOrNull())
    }

    @Test
    fun loginUseCase_usesIdentifierAndPassword() = runTest {
        val loginData = TestFixtures.loginData()
        coEvery { repository.login(any()) } returns Result.success(loginData)

        val result = LoginUseCase(repository)("user@example.com", "pass")

        assertEquals(loginData, result.getOrNull())
        coVerify {
            repository.login(LoginRequest(identifier = "user@example.com", password = "pass"))
        }
    }
}
