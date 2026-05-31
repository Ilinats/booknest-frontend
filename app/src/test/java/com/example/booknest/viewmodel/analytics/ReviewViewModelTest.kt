package com.example.booknest.viewmodel.analytics

import com.example.booknest.domain.usecase.reviews.CreateReviewUseCase
import com.example.booknest.domain.usecase.reviews.GetBookReviewsUseCase
import com.example.booknest.domain.usecase.reviews.GetReviewUseCase
import com.example.booknest.domain.usecase.reviews.GetUserReviewsUseCase
import com.example.booknest.domain.usecase.reviews.UpdateReviewUseCase
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
class ReviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getBookReviewsUseCase = mockk<GetBookReviewsUseCase>()
    private val getUserReviewsUseCase = mockk<GetUserReviewsUseCase>()
    private val getReviewUseCase = mockk<GetReviewUseCase>()
    private val createReviewUseCase = mockk<CreateReviewUseCase>()
    private val updateReviewUseCase = mockk<UpdateReviewUseCase>()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel() = ReviewViewModel(
        feedback = feedback,
        getBookReviewsUseCase = getBookReviewsUseCase,
        getUserReviewsUseCase = getUserReviewsUseCase,
        getReviewUseCase = getReviewUseCase,
        createReviewUseCase = createReviewUseCase,
        updateReviewUseCase = updateReviewUseCase,
    )

    @Test
    fun loadBookReviews_populatesList() = runTest(testDispatcher) {
        val reviews = listOf(TestFixtures.review(id = "r-1"))
        coEvery { getBookReviewsUseCase("book-1") } returns Result.success(reviews)

        val viewModel = createViewModel()
        viewModel.loadBookReviews("book-1")
        advanceUntilIdle()

        assertEquals(reviews, viewModel.bookReviews.value)
    }

    @Test
    fun loadUserReviews_populatesList() = runTest(testDispatcher) {
        val reviews = listOf(TestFixtures.review(id = "r-2"))
        coEvery { getUserReviewsUseCase("user-1") } returns Result.success(reviews)

        val viewModel = createViewModel()
        viewModel.loadUserReviews("user-1")
        advanceUntilIdle()

        assertEquals(reviews, viewModel.userReviews.value)
    }
}
