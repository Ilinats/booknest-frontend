package com.example.booknest.viewmodel.author

import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.usecase.applications.GetBookApplicationsUseCase
import com.example.booknest.domain.usecase.applications.GetOverdueReviewsUseCase
import com.example.booknest.domain.usecase.author.GetMyBooksUseCase
import com.example.booknest.domain.usecase.profile.GetMyStatsUseCase
import com.example.booknest.domain.usecase.reviews.GetAuthorLatestReviewsUseCase
import com.example.booknest.port.ToastNotifier
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.testutil.TestFixtures
import com.example.booknest.viewmodel.common.UserFeedback
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthorDashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getMyStatsUseCase = mockk<GetMyStatsUseCase>()
    private val getMyBooksUseCase = mockk<GetMyBooksUseCase>()
    private val getBookApplicationsUseCase = mockk<GetBookApplicationsUseCase>()
    private val getAuthorLatestReviewsUseCase = mockk<GetAuthorLatestReviewsUseCase>()
    private val getOverdueReviewsUseCase = mockk<GetOverdueReviewsUseCase>()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel() = AuthorDashboardViewModel(
        feedback = feedback,
        getMyStatsUseCase = getMyStatsUseCase,
        getMyBooksUseCase = getMyBooksUseCase,
        getBookApplicationsUseCase = getBookApplicationsUseCase,
        getAuthorLatestReviewsUseCase = getAuthorLatestReviewsUseCase,
        getOverdueReviewsUseCase = getOverdueReviewsUseCase,
    )

    @Test
    fun loadAuthorStats_updatesQuickStats() = runTest(testDispatcher) {
        val stats = TestFixtures.userStats(totalBooks = 8, publishedBooks = 5)
        coEvery { getMyStatsUseCase() } returns Result.success(stats)
        coEvery { getMyBooksUseCase() } returns Result.success(emptyList())

        val viewModel = createViewModel()
        viewModel.loadAuthorStats()
        advanceUntilIdle()

        assertEquals(8, viewModel.quickStats.value.totalBooks)
        assertEquals(5, viewModel.quickStats.value.activeBooks)
        assertEquals(stats, viewModel.authorStats.value)
    }

    @Test
    fun loadRecentReviews_populatesList() = runTest(testDispatcher) {
        val reviews = listOf(TestFixtures.review(id = "r-1"))
        coEvery { getAuthorLatestReviewsUseCase(limit = 3) } returns Result.success(reviews)

        val viewModel = createViewModel()
        viewModel.loadRecentReviews()
        advanceUntilIdle()

        assertEquals(reviews, viewModel.recentReviews.value)
    }

    @Test
    fun loadOverdueReviews_clearsOnFailure() = runTest(testDispatcher) {
        coEvery { getOverdueReviewsUseCase() } returns Result.failure(
            IllegalStateException("Unavailable"),
        )

        val viewModel = createViewModel()
        viewModel.loadOverdueReviews()
        advanceUntilIdle()

        assertEquals(emptyList<ApplicationResponse>(), viewModel.overdueReviews.value)
    }
}
