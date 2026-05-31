package com.example.booknest.viewmodel.profile

import com.example.booknest.domain.usecase.profile.GetMyRecentActivityUseCase
import com.example.booknest.domain.usecase.profile.GetUserRecentActivityUseCase
import com.example.booknest.port.ToastNotifier
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.testutil.TestFixtures
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
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileActivityViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getMyRecentActivityUseCase = mockk<GetMyRecentActivityUseCase>()
    private val getUserRecentActivityUseCase = mockk<GetUserRecentActivityUseCase>()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel() = ProfileActivityViewModel(
        feedback = feedback,
        getMyRecentActivityUseCase = getMyRecentActivityUseCase,
        getUserRecentActivityUseCase = getUserRecentActivityUseCase,
    )

    @Test
    fun loadMyRecentActivity_populatesList() = runTest(testDispatcher) {
        val activities = listOf(TestFixtures.userActivity())
        coEvery { getMyRecentActivityUseCase(days = 14, limit = 50) } returns Result.success(activities)

        val viewModel = createViewModel()
        viewModel.loadMyRecentActivity(days = 14)
        advanceUntilIdle()

        assertEquals(activities, viewModel.myRecentActivity.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun loadUserRecentActivity_delegatesToUseCase() = runTest(testDispatcher) {
        val activities = listOf(TestFixtures.userActivity(id = "activity-2"))
        coEvery {
            getUserRecentActivityUseCase("reader1", days = 7, limit = 50)
        } returns Result.success(activities)

        val viewModel = createViewModel()
        viewModel.loadUserRecentActivity("reader1")
        advanceUntilIdle()

        coVerify { getUserRecentActivityUseCase("reader1", days = 7, limit = 50) }
        assertEquals(activities, viewModel.myRecentActivity.value)
    }

    @Test
    fun clearError_resetsErrorState() {
        val viewModel = createViewModel()
        viewModel.clearError()
        assertNull(viewModel.error.value)
    }
}
