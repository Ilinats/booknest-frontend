package com.example.booknest.domain.usecase.auth

import com.example.booknest.domain.model.request.LoginRequest
import com.example.booknest.domain.model.response.LoginDataResponse
import com.example.booknest.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginUseCaseTest {

    private val authRepository = mockk<AuthRepository>()
    private val useCase = LoginUseCase(authRepository)

    @Test
    fun invoke_delegatesToRepositoryWithBuiltRequest() = runTest {
        val expected = LoginDataResponse(accessToken = "access", refreshToken = "refresh")
        coEvery { authRepository.login(any()) } returns Result.success(expected)

        val result = useCase(identifier = "user@example.com", password = "secret")

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        coVerify {
            authRepository.login(
                LoginRequest(identifier = "user@example.com", password = "secret"),
            )
        }
    }

    @Test
    fun invoke_propagatesRepositoryFailure() = runTest {
        val error = IllegalStateException("Invalid credentials")
        coEvery { authRepository.login(any()) } returns Result.failure(error)

        val result = useCase(identifier = "user@example.com", password = "wrong")

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
