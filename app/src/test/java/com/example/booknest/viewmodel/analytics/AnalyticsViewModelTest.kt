package com.example.booknest.viewmodel.analytics

import com.example.booknest.domain.model.response.AuthorAnalyticsResponse
import com.example.booknest.domain.model.response.DetailedBookAnalyticsResponse
import com.example.booknest.domain.usecase.analytics.GetAuthorAnalyticsUseCase
import com.example.booknest.domain.usecase.analytics.GetBookPerformanceComparisonUseCase
import com.example.booknest.domain.usecase.analytics.GetDetailedBookAnalyticsUseCase
import com.example.booknest.port.ToastNotifier
import com.example.booknest.presentation.common.UiState
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.viewmodel.common.UserFeedback
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getDetailedBookAnalyticsUseCase = mockk<GetDetailedBookAnalyticsUseCase>()
    private val getAuthorAnalyticsUseCase = mockk<GetAuthorAnalyticsUseCase>()
    private val getBookPerformanceComparisonUseCase = mockk<GetBookPerformanceComparisonUseCase>()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel() = AnalyticsViewModel(
        feedback = feedback,
        getDetailedBookAnalyticsUseCase = getDetailedBookAnalyticsUseCase,
        getAuthorAnalyticsUseCase = getAuthorAnalyticsUseCase,
        getBookPerformanceComparisonUseCase = getBookPerformanceComparisonUseCase,
    )

    @Test
    fun loadDetailedBookAnalytics_successUpdatesState() = runTest(testDispatcher) {
        val analytics = mockk<DetailedBookAnalyticsResponse>(relaxed = true)
        coEvery { getDetailedBookAnalyticsUseCase("book-1") } returns Result.success(analytics)

        val viewModel = createViewModel()
        viewModel.loadDetailedBookAnalytics("book-1")
        advanceUntilIdle()

        assertTrue(viewModel.bookAnalyticsState.value is UiState.Success)
        assertEquals(analytics, viewModel.currentBookAnalytics.value)
    }

    @Test
    fun loadDetailedBookAnalytics_failureSetsErrorState() = runTest(testDispatcher) {
        coEvery { getDetailedBookAnalyticsUseCase("book-1") } returns Result.failure(
            IllegalStateException("Analytics unavailable"),
        )

        val viewModel = createViewModel()
        viewModel.loadDetailedBookAnalytics("book-1")
        advanceUntilIdle()

        assertTrue(viewModel.bookAnalyticsState.value is UiState.Error)
        assertEquals("Analytics unavailable", viewModel.error.value)
    }

    @Test
    fun loadAuthorAnalytics_successUpdatesState() = runTest(testDispatcher) {
        val analytics = mockk<AuthorAnalyticsResponse>(relaxed = true)
        coEvery { getAuthorAnalyticsUseCase(any()) } returns Result.success(analytics)

        val viewModel = createViewModel()
        viewModel.loadAuthorAnalytics()
        advanceUntilIdle()

        assertTrue(viewModel.authorAnalyticsState.value is UiState.Success)
        assertEquals(analytics, viewModel.currentAuthorAnalytics.value)
    }
}
