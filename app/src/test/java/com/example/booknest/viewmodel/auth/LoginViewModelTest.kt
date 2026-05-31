package com.example.booknest.viewmodel.auth

import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.usecase.auth.LoginUseCase
import com.example.booknest.domain.usecase.profile.GetCurrentUserUseCase
import com.example.booknest.port.ToastNotifier
import com.example.booknest.presentation.common.UiState
import com.example.booknest.presentation.effects.AuthUiEffect
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.testutil.TestFixtures
import com.example.booknest.viewmodel.common.UserFeedback
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val loginUseCase = mockk<LoginUseCase>()
    private val getCurrentUserUseCase = mockk<GetCurrentUserUseCase>()
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel() = LoginViewModel(
        feedback = feedback,
        loginUseCase = loginUseCase,
        getCurrentUserUseCase = getCurrentUserUseCase,
        sessionManager = sessionManager,
    )

    @Test
    fun loginUser_successUpdatesSessionAndNavigates() = runTest(testDispatcher) {
        val loginData = TestFixtures.loginData()
        val user = TestFixtures.user()
        coEvery { loginUseCase(any(), any()) } returns Result.success(loginData)
        coEvery { getCurrentUserUseCase() } returns Result.success(user)

        val viewModel = createViewModel()
        viewModel.loginUser("user@example.com", "password")
        advanceUntilIdle()

        val state = viewModel.loginState.first()
        assertTrue(state is UiState.Success)
        coVerify {
            sessionManager.updateTokens(
                accessToken = loginData.accessToken,
                refreshToken = loginData.refreshToken,
            )
            sessionManager.updateUser(user)
        }
    }

    @Test
    fun loginUser_emptyAccessToken_setsError() = runTest(testDispatcher) {
        coEvery { loginUseCase(any(), any()) } returns Result.success(
            TestFixtures.loginData(accessToken = ""),
        )

        val viewModel = createViewModel()
        viewModel.loginUser("user@example.com", "password")
        advanceUntilIdle()

        val state = viewModel.loginState.first()
        assertTrue(state is UiState.Error)
        assertEquals("Login failed: empty access token", (state as UiState.Error).message)
    }

    @Test
    fun loginUser_loginFailure_setsErrorState() = runTest(testDispatcher) {
        coEvery { loginUseCase(any(), any()) } returns Result.failure(
            IllegalStateException("Invalid credentials"),
        )

        val viewModel = createViewModel()
        viewModel.loginUser("user@example.com", "wrong")
        advanceUntilIdle()

        val state = viewModel.loginState.first()
        assertTrue(state is UiState.Error)
        assertEquals("Invalid credentials", (state as UiState.Error).message)
    }

    @Test
    fun loginUser_userFetchFailure_setsErrorWithoutNavigation() = runTest(testDispatcher) {
        coEvery { loginUseCase(any(), any()) } returns Result.success(TestFixtures.loginData())
        coEvery { getCurrentUserUseCase() } returns Result.failure(
            IllegalStateException("Profile unavailable"),
        )

        val viewModel = createViewModel()
        viewModel.loginUser("user@example.com", "password")
        advanceUntilIdle()

        val state = viewModel.loginState.first()
        assertTrue(state is UiState.Error)
        assertEquals("Profile unavailable", (state as UiState.Error).message)
        coVerify(exactly = 0) { sessionManager.updateUser(any()) }
    }
}
