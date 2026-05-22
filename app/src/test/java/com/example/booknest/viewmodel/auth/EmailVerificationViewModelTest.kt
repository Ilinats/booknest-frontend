package com.example.booknest.viewmodel.auth

import com.example.booknest.domain.usecase.auth.ResendVerificationCodeUseCase
import com.example.booknest.domain.usecase.auth.VerifyEmailUseCase
import com.example.booknest.domain.usecase.profile.GetCurrentUserUseCase
import com.example.booknest.port.ToastNotifier
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.testutil.TestFixtures
import com.example.booknest.testutil.mockLoggedInSessionManager
import com.example.booknest.viewmodel.common.UserFeedback
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmailVerificationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val verifyEmailUseCase = mockk<VerifyEmailUseCase>()
    private val resendVerificationCodeUseCase = mockk<ResendVerificationCodeUseCase>()
    private val getCurrentUserUseCase = mockk<GetCurrentUserUseCase>()
    private val sessionManager = mockLoggedInSessionManager()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel(email: String? = "user@example.com") = EmailVerificationViewModel(
        feedback = feedback,
        verifyEmailUseCase = verifyEmailUseCase,
        resendVerificationCodeUseCase = resendVerificationCodeUseCase,
        getCurrentUserUseCase = getCurrentUserUseCase,
        sessionManager = sessionManager,
        userEmail = email,
    )

    @Test
    fun verifyEmail_successMarksVerified() = runTest(testDispatcher) {
        val user = TestFixtures.user()
        coEvery { verifyEmailUseCase("123456") } returns Result.success(user)
        coEvery { getCurrentUserUseCase() } returns Result.success(user)

        val viewModel = createViewModel()
        viewModel.verifyEmail("123456")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isVerificationSuccessful)
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify { sessionManager.updateUser(user) }
    }

    @Test
    fun verifyEmail_mapsExpiredCodeError() = runTest(testDispatcher) {
        coEvery { verifyEmailUseCase(any()) } returns Result.failure(
            IllegalStateException("Invalid or expired verification code"),
        )

        val viewModel = createViewModel()
        viewModel.verifyEmail("000000")
        advanceUntilIdle()

        assertEquals(
            "Code is invalid or expired. Please try again.",
            viewModel.uiState.value.error,
        )
    }

    @Test
    fun resendVerificationCode_requiresEmail() = runTest(testDispatcher) {
        val viewModel = createViewModel(email = null)
        viewModel.resendVerificationCode()
        advanceUntilIdle()

        assertEquals("Email is required", viewModel.uiState.value.error)
    }

    @Test
    fun resendVerificationCode_successClearsError() = runTest(testDispatcher) {
        coEvery { resendVerificationCodeUseCase("user@example.com") } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.resendVerificationCode()
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
