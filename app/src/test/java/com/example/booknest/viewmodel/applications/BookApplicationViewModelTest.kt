package com.example.booknest.viewmodel.applications

import com.example.booknest.domain.model.response.BulkActionResponse
import com.example.booknest.domain.usecase.applications.BulkActionApplicationsUseCase
import com.example.booknest.domain.usecase.applications.GetBookApplicationsUseCase
import com.example.booknest.domain.usecase.applications.GetOverdueReviewsUseCase
import com.example.booknest.domain.usecase.applications.MarkCopySentUseCase
import com.example.booknest.domain.usecase.applications.RunLotterySelectionUseCase
import com.example.booknest.domain.usecase.applications.UpdateApplicationCompleteUseCase
import com.example.booknest.port.ToastNotifier
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.testutil.TestFixtures
import com.example.booknest.viewmodel.common.UserFeedback
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
class BookApplicationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getBookApplicationsUseCase = mockk<GetBookApplicationsUseCase>()
    private val updateApplicationCompleteUseCase = mockk<UpdateApplicationCompleteUseCase>()
    private val bulkActionApplicationsUseCase = mockk<BulkActionApplicationsUseCase>()
    private val markCopySentUseCase = mockk<MarkCopySentUseCase>()
    private val runLotterySelectionUseCase = mockk<RunLotterySelectionUseCase>()
    private val getOverdueReviewsUseCase = mockk<GetOverdueReviewsUseCase>()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel() = BookApplicationViewModel(
        feedback = feedback,
        getBookApplicationsUseCase = getBookApplicationsUseCase,
        updateApplicationCompleteUseCase = updateApplicationCompleteUseCase,
        bulkActionApplicationsUseCase = bulkActionApplicationsUseCase,
        markCopySentUseCase = markCopySentUseCase,
        runLotterySelectionUseCase = runLotterySelectionUseCase,
        getOverdueReviewsUseCase = getOverdueReviewsUseCase,
    )

    @Test
    fun loadBookApplications_populatesState() = runTest(testDispatcher) {
        val apps = listOf(TestFixtures.application(id = "a-1", bookId = "book-1"))
        coEvery { getBookApplicationsUseCase("book-1") } returns Result.success(apps)

        val viewModel = createViewModel()
        viewModel.loadBookApplications("book-1")
        advanceUntilIdle()

        assertEquals(apps, viewModel.bookApplications.first())
    }

    @Test
    fun approveApplication_reloadsBookApplications() = runTest(testDispatcher) {
        val apps = listOf(TestFixtures.application(id = "a-1", bookId = "book-1", status = "approved"))
        coEvery { updateApplicationCompleteUseCase(any(), any()) } returns Result.success(apps.first())
        coEvery { getBookApplicationsUseCase("book-1") } returns Result.success(apps)

        val viewModel = createViewModel()
        viewModel.loadBookApplications("book-1")
        advanceUntilIdle()
        viewModel.approveApplication("book-1", "a-1")
        advanceUntilIdle()

        assertEquals("Application approved!", viewModel.successMessage.first())
        assertEquals("approved", viewModel.bookApplications.first().first().status)
        coVerify(atLeast = 2) { getBookApplicationsUseCase("book-1") }
    }

    @Test
    fun runLottery_mapsDeadlineErrorMessage() = runTest(testDispatcher) {
        coEvery { runLotterySelectionUseCase("book-1") } returns Result.failure(
            IllegalStateException("Application deadline has not passed"),
        )

        val viewModel = createViewModel()
        viewModel.runLottery("book-1")
        advanceUntilIdle()

        assertTrue(
            viewModel.error.first()!!.contains("deadline has not passed"),
        )
    }

    @Test
    fun runLottery_successShowsCounts() = runTest(testDispatcher) {
        coEvery { runLotterySelectionUseCase("book-1") } returns Result.success(TestFixtures.lotteryResult())
        coEvery { getBookApplicationsUseCase("book-1") } returns Result.success(emptyList())

        val viewModel = createViewModel()
        viewModel.runLottery("book-1")
        advanceUntilIdle()

        assertEquals(
            "Lottery completed: 3 approved, 2 rejected",
            viewModel.successMessage.first(),
        )
    }

    @Test
    fun bulkActionApplications_reloadsBookApplications() = runTest(testDispatcher) {
        coEvery {
            bulkActionApplicationsUseCase("book-1", any())
        } returns Result.success(BulkActionResponse(updated = 1))
        coEvery { getBookApplicationsUseCase("book-1") } returns Result.success(emptyList())

        val viewModel = createViewModel()
        viewModel.bulkActionApplications("book-1", listOf("a-1"), action = "approved")
        advanceUntilIdle()

        assertEquals("Bulk action completed!", viewModel.successMessage.first())
        coVerify { bulkActionApplicationsUseCase("book-1", any()) }
    }

    @Test
    fun loadOverdueReviews_skipsWhenAlreadyLoaded() = runTest(testDispatcher) {
        val overdue = listOf(TestFixtures.application(id = "o-1"))
        coEvery { getOverdueReviewsUseCase() } returns Result.success(overdue)

        val viewModel = createViewModel()
        viewModel.loadOverdueReviews()
        advanceUntilIdle()
        viewModel.loadOverdueReviews()
        advanceUntilIdle()

        coVerify(exactly = 1) { getOverdueReviewsUseCase() }
        assertEquals(overdue, viewModel.overdueReviews.first())
    }
}
