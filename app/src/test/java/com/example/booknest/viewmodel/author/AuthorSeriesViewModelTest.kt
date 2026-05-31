package com.example.booknest.viewmodel.author

import com.example.booknest.domain.model.request.CreateSeriesRequest
import com.example.booknest.domain.usecase.author.GetMySeriesUseCase
import com.example.booknest.domain.usecase.series.CreateSeriesUseCase
import com.example.booknest.domain.usecase.series.UpdateSeriesUseCase
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
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthorSeriesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getMySeriesUseCase = mockk<GetMySeriesUseCase>()
    private val createSeriesUseCase = mockk<CreateSeriesUseCase>()
    private val updateSeriesUseCase = mockk<UpdateSeriesUseCase>()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel() = AuthorSeriesViewModel(
        feedback = feedback,
        getMySeriesUseCase = getMySeriesUseCase,
        createSeriesUseCase = createSeriesUseCase,
        updateSeriesUseCase = updateSeriesUseCase,
    )

    @Test
    fun loadMySeries_populatesList() = runTest(testDispatcher) {
        val series = listOf(TestFixtures.series())
        coEvery { getMySeriesUseCase() } returns Result.success(series)

        val viewModel = createViewModel()
        viewModel.loadMySeries()
        advanceUntilIdle()

        assertEquals(series, viewModel.mySeries.value)
        assertFalse(viewModel.isLoadingSeries.value)
    }

    @Test
    fun createSeries_reloadsSeries() = runTest(testDispatcher) {
        val series = listOf(TestFixtures.series(id = "series-2"))
        val request = CreateSeriesRequest(name = "New Saga", description = "Desc")
        coEvery { createSeriesUseCase(request) } returns Result.success(TestFixtures.series())
        coEvery { getMySeriesUseCase() } returns Result.success(series)

        val viewModel = createViewModel()
        viewModel.createSeries(request)
        advanceUntilIdle()

        coVerify { createSeriesUseCase(request) }
        coVerify { getMySeriesUseCase() }
        assertEquals(series, viewModel.mySeries.value)
    }
}
