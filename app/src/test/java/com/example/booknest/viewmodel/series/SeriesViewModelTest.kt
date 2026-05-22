package com.example.booknest.viewmodel.series

import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.usecase.author.GetMySeriesUseCase
import com.example.booknest.domain.usecase.series.CreateSeriesUseCase
import com.example.booknest.domain.usecase.series.DeleteSeriesUseCase
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
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeriesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getMySeriesUseCase = mockk<GetMySeriesUseCase>()
    private val createSeriesUseCase = mockk<CreateSeriesUseCase>()
    private val updateSeriesUseCase = mockk<UpdateSeriesUseCase>()
    private val deleteSeriesUseCase = mockk<DeleteSeriesUseCase>()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel() = SeriesViewModel(
        feedback = feedback,
        getMySeriesUseCase = getMySeriesUseCase,
        createSeriesUseCase = createSeriesUseCase,
        updateSeriesUseCase = updateSeriesUseCase,
        deleteSeriesUseCase = deleteSeriesUseCase,
    )

    @Test
    fun loadMySeries_populatesList() = runTest(testDispatcher) {
        val series = listOf(TestFixtures.series())
        coEvery { getMySeriesUseCase() } returns Result.success(series)

        val viewModel = createViewModel()
        viewModel.loadMySeries()
        advanceUntilIdle()

        assertEquals(series, viewModel.series.value)
    }

    @Test
    fun createSeries_reloadsOnSuccess() = runTest(testDispatcher) {
        coEvery { createSeriesUseCase(any()) } returns Result.success(TestFixtures.series())
        coEvery { getMySeriesUseCase() } returns Result.success(listOf(TestFixtures.series()))

        val viewModel = createViewModel()
        viewModel.createSeries("New Series", "Description")
        advanceUntilIdle()

        assertEquals("Series created successfully!", viewModel.successMessage.value)
        coVerify(atLeast = 1) { getMySeriesUseCase() }
    }

    @Test
    fun deleteSeries_removesFromSeriesBooksMap() = runTest(testDispatcher) {
        coEvery { deleteSeriesUseCase("series-1") } returns Result.success(Unit)
        coEvery { getMySeriesUseCase() } returns Result.success(emptyList())

        val viewModel = createViewModel()
        viewModel.loadMySeries()
        advanceUntilIdle()

        viewModel.deleteSeries("series-1")
        advanceUntilIdle()

        assertEquals(emptyMap<String, List<BookResponse>>(), viewModel.seriesBooks.value)
    }
}
