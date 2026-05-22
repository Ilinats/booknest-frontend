package com.example.booknest.viewmodel.profile

import com.example.booknest.domain.usecase.author.GetMyBooksUseCase
import com.example.booknest.domain.usecase.profile.GetAuthorStatsUseCase
import com.example.booknest.domain.usecase.profile.GetMyStatsUseCase
import com.example.booknest.presentation.common.UiState
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.testutil.TestFixtures
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
class ProfileStatsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getMyStatsUseCase = mockk<GetMyStatsUseCase>()
    private val getAuthorStatsUseCase = mockk<GetAuthorStatsUseCase>()
    private val getMyBooksUseCase = mockk<GetMyBooksUseCase>()

    private fun createViewModel() = ProfileStatsViewModel(
        getMyStatsUseCase = getMyStatsUseCase,
        getAuthorStatsUseCase = getAuthorStatsUseCase,
        getMyBooksUseCase = getMyBooksUseCase,
    )

    @Test
    fun loadMyStats_successUpdatesState() = runTest(testDispatcher) {
        val stats = TestFixtures.userStats()
        coEvery { getMyStatsUseCase() } returns Result.success(stats)
        coEvery { getMyBooksUseCase() } returns Result.success(emptyList())

        val viewModel = createViewModel()
        viewModel.loadMyStats()
        advanceUntilIdle()

        assertEquals(UiState.Success(stats), viewModel.statsState.value)
        assertEquals(stats, viewModel.currentStats.value)
    }

    @Test
    fun loadAuthorStats_mapsAuthorResponse() = runTest(testDispatcher) {
        val authorStats = TestFixtures.authorStatsResponse()
        coEvery { getAuthorStatsUseCase("author-1") } returns Result.success(authorStats)

        val viewModel = createViewModel()
        viewModel.loadAuthorStats("author-1")
        advanceUntilIdle()

        assertEquals(authorStats.author, viewModel.currentStats.value?.user)
        assertEquals(authorStats.stats, viewModel.currentStats.value?.stats)
    }
}
