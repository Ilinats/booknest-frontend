package com.example.booknest.viewmodel.common

import com.example.booknest.data.error.BNError
import com.example.booknest.port.ToastNotifier
import com.example.booknest.testutil.MainDispatcherRule
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class TestViewModel(feedback: UserFeedback) : BaseViewModel(feedback) {
        fun exposeErrorMessage(throwable: Throwable): String = throwable.toErrorMessage()

        fun run(block: suspend () -> Result<String>) {
            launchWithLoading(onSuccess = {}, block = block)
        }
    }

    private val toastNotifier = mockk<ToastNotifier>(relaxed = true)
    private val feedback = UserFeedback(toastNotifier)
    private val viewModel = TestViewModel(feedback)

    @Test
    fun toErrorMessage_parsesBracketedGenericValidationList() {
        val message = viewModel.exposeErrorMessage(
            BNError.Generic(
                messageString = "[\"Title is required\", \"Genres are required\"]",
                error = null,
                statusCode = 400,
            ),
        )

        assertEquals("Title is required, Genres are required", message)
    }

    @Test
    fun toErrorMessage_usesNetworkFallback() {
        val message = viewModel.exposeErrorMessage(
            BNError.Network(messageString = null),
        )

        assertEquals("Network error. Please check your connection.", message)
    }

    @Test
    fun toErrorMessage_usesUnauthorizedFallback() {
        val message = viewModel.exposeErrorMessage(
            BNError.Unauthorized(messageString = "Session expired"),
        )

        assertEquals("Session expired", message)
    }

    @Test
    fun launchWithLoading_setsAndClearsLoadingFlag() = runTest {
        viewModel.run { Result.success("ok") }
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.first())
    }

    @Test
    fun launchWithLoading_surfacesMappedError() = runTest {
        viewModel.run {
            Result.failure(
                BNError.Generic(messageString = "Something went wrong", error = null, statusCode = 500),
            )
        }
        advanceUntilIdle()

        assertEquals("Something went wrong", viewModel.error.first())
    }
}
