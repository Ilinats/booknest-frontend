package com.example.booknest.viewmodel.applications

import androidx.lifecycle.viewModelScope
import com.example.booknest.data.error.BNError
import com.example.booknest.domain.usecase.applications.CheckApplicationUseCase
import com.example.booknest.domain.usecase.applications.CreateApplicationUseCase
import com.example.booknest.domain.usecase.applications.GetApplicationUseCase
import com.example.booknest.domain.usecase.applications.GetMyApplicationsUseCase
import com.example.booknest.domain.usecase.applications.GetReadingProgressUseCase
import com.example.booknest.domain.usecase.applications.MarkCopyReceivedUseCase
import com.example.booknest.domain.usecase.applications.UpdateReadingStatusUseCase
import com.example.booknest.domain.usecase.applications.WithdrawApplicationUseCase
import com.example.booknest.port.ToastNotifier
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.testutil.TestFixtures
import com.example.booknest.viewmodel.common.UserFeedback
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ApplicationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getMyApplicationsUseCase = mockk<GetMyApplicationsUseCase>()
    private val checkApplicationUseCase = mockk<CheckApplicationUseCase>()
    private val createApplicationUseCase = mockk<CreateApplicationUseCase>()
    private val getApplicationUseCase = mockk<GetApplicationUseCase>()
    private val getReadingProgressUseCase = mockk<GetReadingProgressUseCase>()
    private val withdrawApplicationUseCase = mockk<WithdrawApplicationUseCase>()
    private val markCopyReceivedUseCase = mockk<MarkCopyReceivedUseCase>()
    private val updateReadingStatusUseCase = mockk<UpdateReadingStatusUseCase>()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun ApplicationViewModel.close() {
        viewModelScope.cancel()
    }

    private fun createViewModel() = ApplicationViewModel(
        feedback = feedback,
        getMyApplicationsUseCase = getMyApplicationsUseCase,
        checkApplicationUseCase = checkApplicationUseCase,
        createApplicationUseCase = createApplicationUseCase,
        getApplicationUseCase = getApplicationUseCase,
        getReadingProgressUseCase = getReadingProgressUseCase,
        withdrawApplicationUseCase = withdrawApplicationUseCase,
        markCopyReceivedUseCase = markCopyReceivedUseCase,
        updateReadingStatusUseCase = updateReadingStatusUseCase,
    )

    @Test
    fun loadMyApplications_populatesList() = runTest(testDispatcher) {
        val apps = listOf(
            TestFixtures.application(id = "1", status = "pending"),
            TestFixtures.application(id = "2", status = "approved"),
        )
        coEvery { getMyApplicationsUseCase() } returns Result.success(apps)

        val viewModel = createViewModel()
        viewModel.loadMyApplications()
        advanceUntilIdle()

        assertEquals(apps, viewModel.myApplications.first())
        assertFalse(viewModel.isLoading.first())
    }

    @Test
    fun updateSearchQuery_updatesSearchQueryState() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.updateSearchQuery("dragon")
        assertEquals("dragon", viewModel.searchQuery.value)
    }

    @Test
    fun updateSelectedTab_updatesTabState() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.updateSelectedTab(2)
        assertEquals(2, viewModel.selectedTab.value)
    }

    @Test
    fun loadMyApplications_setsErrorOnFailure() = runTest(testDispatcher) {
        coEvery { getMyApplicationsUseCase() } returns Result.failure(
            IllegalStateException("Network down"),
        )

        val viewModel = createViewModel()
        viewModel.loadMyApplications()
        advanceUntilIdle()

        assertEquals("Network down", viewModel.error.value)
    }

    @Test
    fun checkApplication_populatesApplicationCheck() = runTest(testDispatcher) {
        val check = TestFixtures.applicationCheck(hasApplied = true, applicationId = "app-1")
        coEvery { checkApplicationUseCase("book-1") } returns Result.success(check)

        val viewModel = createViewModel()
        viewModel.checkApplication("book-1")
        advanceUntilIdle()

        assertEquals(check, viewModel.applicationCheck.value)
    }

    @Test
    fun createApplication_mapsEmailVerificationError() = runTest(testDispatcher) {
        coEvery { createApplicationUseCase(any()) } returns Result.failure(
            BNError.Generic(
                messageString = "Please complete email verification first",
                error = null,
                statusCode = 403,
            ),
        )

        val viewModel = createViewModel()
        viewModel.createApplication(bookId = "book-1")
        advanceUntilIdle()

        assertEquals(
            "Please verify your email address before applying",
            viewModel.error.value,
        )
    }

    @Test
    fun createApplication_mapsAlreadyAppliedError() = runTest(testDispatcher) {
        coEvery { createApplicationUseCase(any()) } returns Result.failure(
            BNError.Generic(
                messageString = "APPLICATION_ALREADY_EXISTS",
                error = null,
                statusCode = 409,
            ),
        )
        coEvery { checkApplicationUseCase(any()) } returns Result.success(TestFixtures.applicationCheck())
        coEvery { getMyApplicationsUseCase() } returns Result.success(emptyList())

        val viewModel = createViewModel()
        viewModel.createApplication(bookId = "book-1")
        advanceUntilIdle()

        assertEquals(
            "You have already applied for this book",
            viewModel.error.first(),
        )
    }

    @Test
    fun withdrawApplication_refreshesListOnSuccess() = runTest(testDispatcher) {
        val withdrawn = TestFixtures.application(id = "app-1", status = "withdrawn")
        coEvery { withdrawApplicationUseCase("app-1") } returns Result.success(Unit)
        coEvery { getMyApplicationsUseCase() } returnsMany listOf(
            Result.success(listOf(TestFixtures.application(id = "app-1", status = "pending"))),
            Result.success(listOf(withdrawn)),
        )

        val viewModel = createViewModel()
        viewModel.withdrawApplication(applicationId = "app-1")
        advanceUntilIdle()

        assertEquals("Application withdrawn successfully!", viewModel.successMessage.value)
        coVerify(atLeast = 1) { getMyApplicationsUseCase() }
    }

    @Test
    fun markCopyReceived_trimsApplicationId() = runTest(testDispatcher) {
        coEvery { markCopyReceivedUseCase("app-1") } returns Result.success(
            TestFixtures.application(id = "app-1", status = "approved", copyReceivedAt = "now"),
        )
        coEvery { getMyApplicationsUseCase() } returns Result.success(emptyList())

        val viewModel = createViewModel()
        viewModel.markCopyReceived("  app-1  ")
        advanceUntilIdle()

        coVerify { markCopyReceivedUseCase("app-1") }
        assertEquals("Copy marked as received!", viewModel.successMessage.first())
    }

    @Test
    fun updateReadingStatus_reloadsApplicationsOnSuccess() = runTest(testDispatcher) {
        coEvery {
            updateReadingStatusUseCase(any(), any())
        } returns Result.success(TestFixtures.application(status = "approved", readingStatus = "currently_reading"))
        coEvery { getMyApplicationsUseCase() } returns Result.success(emptyList())
        coEvery { getReadingProgressUseCase() } returns Result.success(emptyList())

        val viewModel = createViewModel()
        viewModel.updateReadingStatus("app-1", ReadingStatus.CURRENTLY_READING)
        advanceUntilIdle()

        assertEquals("Reading status updated!", viewModel.successMessage.first())
        coVerify { getReadingProgressUseCase() }
    }

    @Test
    fun filteredApplications_filtersBySearchQuery() = runTest(testDispatcher) {
        val apps = listOf(
            TestFixtures.application(id = "1", bookTitle = "Dragon Keep"),
            TestFixtures.application(id = "2", bookTitle = "Quiet Lake"),
        )
        coEvery { getMyApplicationsUseCase() } returns Result.success(apps)

        val viewModel = createViewModel()
        viewModel.loadMyApplications()
        advanceUntilIdle()

        try {
            viewModel.updateSearchQuery("dragon")
            advanceUntilIdle()

            val filtered = viewModel.filteredApplications.first { it.isNotEmpty() }
            assertEquals(1, filtered.size)
            assertEquals("Dragon Keep", filtered.first().bookTitle)
        } finally {
            viewModel.close()
            advanceUntilIdle()
        }
    }

    @Test
    fun pendingApplications_filtersPendingOnly() = runTest(testDispatcher) {
        val apps = listOf(
            TestFixtures.application(id = "1", status = "pending"),
            TestFixtures.application(id = "2", status = "approved"),
        )
        coEvery { getMyApplicationsUseCase() } returns Result.success(apps)

        val viewModel = createViewModel()
        viewModel.loadMyApplications()
        advanceUntilIdle()

        try {
            val pending = viewModel.pendingApplications.first { it.isNotEmpty() }
            assertEquals(1, pending.size)
            assertEquals("pending", pending.first().status)
        } finally {
            viewModel.close()
            advanceUntilIdle()
        }
    }

    @Test
    fun applicationStats_reflectsLoadedApplications() = runTest(testDispatcher) {
        val apps = listOf(
            TestFixtures.application(id = "1", status = "approved"),
            TestFixtures.application(id = "2", status = "pending"),
            TestFixtures.application(id = "3", status = "rejected"),
        )
        coEvery { getMyApplicationsUseCase() } returns Result.success(apps)

        val viewModel = createViewModel()
        viewModel.loadMyApplications()
        advanceUntilIdle()

        try {
            val stats = viewModel.applicationStats.first { it.total > 0 }
            assertEquals(3, stats.total)
            assertEquals(33.333333333333336, stats.approvalRate, 0.001)
        } finally {
            viewModel.close()
            advanceUntilIdle()
        }
    }

    @Test
    fun loadReadingProgress_populatesList() = runTest(testDispatcher) {
        val progress = listOf(
            TestFixtures.application(id = "app-1", status = "approved", readingStatus = "currently_reading"),
        )
        coEvery { getReadingProgressUseCase() } returns Result.success(progress)

        val viewModel = createViewModel()
        viewModel.loadReadingProgress()
        advanceUntilIdle()

        assertEquals(progress, viewModel.readingProgress.value)
    }
}
