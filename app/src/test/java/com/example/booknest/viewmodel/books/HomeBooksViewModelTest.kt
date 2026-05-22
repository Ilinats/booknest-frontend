package com.example.booknest.viewmodel.books

import com.example.booknest.domain.usecase.books.BrowseBooksUseCase
import com.example.booknest.domain.usecase.books.GetNewReleasesUseCase
import com.example.booknest.domain.usecase.books.GetRecommendedBooksUseCase
import com.example.booknest.domain.usecase.books.GetTrendingBooksUseCase
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.testutil.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeBooksViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getRecommendedBooksUseCase = mockk<GetRecommendedBooksUseCase>()
    private val getNewReleasesUseCase = mockk<GetNewReleasesUseCase>()
    private val getTrendingBooksUseCase = mockk<GetTrendingBooksUseCase>()
    private val browseBooksUseCase = mockk<BrowseBooksUseCase>()

    private fun createViewModel() = HomeBooksViewModel(
        getRecommendedBooksUseCase = getRecommendedBooksUseCase,
        getNewReleasesUseCase = getNewReleasesUseCase,
        getTrendingBooksUseCase = getTrendingBooksUseCase,
        browseBooksUseCase = browseBooksUseCase,
        bookCatalogCache = BookCatalogCache(),
    )

    @Test
    fun getRecommendedBooks_populatesList() = runTest(testDispatcher) {
        val books = listOf(TestFixtures.book(id = "r-1"))
        coEvery { getRecommendedBooksUseCase(10) } returns Result.success(books)

        val viewModel = createViewModel()
        viewModel.getRecommendedBooks()
        advanceUntilIdle()

        assertEquals(books, viewModel.recommendedBooks.first())
        assertFalse(viewModel.recommendedLoading.first())
    }

    @Test
    fun getNewReleases_setsErrorOnFailure() = runTest(testDispatcher) {
        coEvery { getNewReleasesUseCase(daysBack = 30, limit = 10) } returns Result.failure(
            IllegalStateException("Unavailable"),
        )

        val viewModel = createViewModel()
        viewModel.getNewReleases()
        advanceUntilIdle()

        assertEquals("Unavailable", viewModel.error.first())
    }

    @Test
    fun updateSearchQuery_debouncedSearchLoadsResults() = runTest(testDispatcher) {
        val results = listOf(TestFixtures.book(id = "s-1", title = "Search Hit"))
        coEvery {
            browseBooksUseCase(
                query = "magic",
                page = 1,
                limit = 20,
                genres = any(),
                title = any(),
                authorName = any(),
                authorId = any(),
                seriesName = any(),
                seriesId = any(),
                ageRating = any(),
                distributionType = any(),
                publishedFrom = any(),
                publishedTo = any(),
                createdFrom = any(),
                createdTo = any(),
                minAvgRating = any(),
                maxAvgRating = any(),
                status = "active",
                applicationStatus = any(),
                deadlineFilter = any(),
                sortBy = any(),
            )
        } returns Result.success(results)

        val viewModel = createViewModel()
        viewModel.updateSearchQuery("magic")
        advanceTimeBy(500)
        advanceUntilIdle()

        assertEquals(results, viewModel.homeSearchResults.first())
        assertFalse(viewModel.homeSearchLoading.first())
    }

    @Test
    fun updateSearchQuery_blankQueryClearsResults() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.updateSearchQuery("temp")
        advanceTimeBy(500)
        advanceUntilIdle()
        viewModel.updateSearchQuery("")
        advanceTimeBy(500)
        advanceUntilIdle()

        assertTrue(viewModel.homeSearchResults.first().isEmpty())
    }
}
