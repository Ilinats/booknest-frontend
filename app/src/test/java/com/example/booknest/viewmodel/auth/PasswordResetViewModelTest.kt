package com.example.booknest.viewmodel.auth

import com.example.booknest.domain.usecase.auth.RequestPasswordResetUseCase
import com.example.booknest.domain.usecase.auth.ResetPasswordUseCase
import com.example.booknest.port.ToastNotifier
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.viewmodel.common.UserFeedback
import io.mockk.coEvery
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
class PasswordResetViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val resetPasswordUseCase = mockk<ResetPasswordUseCase>()
    private val requestPasswordResetUseCase = mockk<RequestPasswordResetUseCase>()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel() = PasswordResetViewModel(
        feedback = feedback,
        resetPasswordUseCase = resetPasswordUseCase,
        requestPasswordResetUseCase = requestPasswordResetUseCase,
    )

    @Test
    fun resetPassword_successUpdatesState() = runTest(testDispatcher) {
        coEvery { resetPasswordUseCase("123456", "newpass") } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.resetPassword("123456", "newpass")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isPasswordResetSuccessful)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun resetPassword_mapsExpiredCodeMessage() = runTest(testDispatcher) {
        coEvery { resetPasswordUseCase(any(), any()) } returns Result.failure(
            IllegalStateException("Invalid or expired verification code"),
        )

        val viewModel = createViewModel()
        viewModel.resetPassword("000000", "newpass")
        advanceUntilIdle()

        assertEquals(
            "Code is invalid or expired. Please try again.",
            viewModel.uiState.value.error,
        )
    }

    @Test
    fun resendResetCode_successSetsCodeSent() = runTest(testDispatcher) {
        coEvery { requestPasswordResetUseCase("user@example.com") } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.resendResetCode("user@example.com")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isCodeSent)
    }

    @Test
    fun clearCodeSentState_resetsFlag() = runTest(testDispatcher) {
        coEvery { requestPasswordResetUseCase(any()) } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.resendResetCode("user@example.com")
        advanceUntilIdle()
        viewModel.clearCodeSentState()

        assertFalse(viewModel.uiState.value.isCodeSent)
    }
}
