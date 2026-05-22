package com.example.booknest.viewmodel.books

import com.example.booknest.data.session.SearchHistoryManager
import com.example.booknest.domain.usecase.books.BrowseBooksUseCase
import com.example.booknest.domain.usecase.genres.GetGenresUseCase
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
class BrowseBooksViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val browseBooksUseCase = mockk<BrowseBooksUseCase>()
    private val getGenresUseCase = mockk<GetGenresUseCase>()
    private val searchHistoryManager = mockk<SearchHistoryManager>(relaxed = true)
    private val bookCatalogCache = BookCatalogCache()

    private fun createViewModel(): BrowseBooksViewModel =
        BrowseBooksViewModel(
            browseBooksUseCase = browseBooksUseCase,
            getGenresUseCase = getGenresUseCase,
            searchHistoryManager = searchHistoryManager,
            bookCatalogCache = bookCatalogCache,
        )

    @Test
    fun clearBookListSearchImmediate_clearsSearchFields() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.updateBookListBrowseUi {
            it.copy(searchQuery = "dragon", debouncedSearchQuery = "dragon")
        }

        viewModel.clearBookListSearchImmediate()
        advanceUntilIdle()

        val ui = viewModel.bookListBrowseUi.first()
        assertEquals("", ui.searchQuery)
        assertEquals("", ui.debouncedSearchQuery)
    }

    @Test
    fun onBrowseListRouteChanged_searchCategory_loadsBooks() = runTest(testDispatcher) {
        val books = listOf(TestFixtures.book(id = "1"), TestFixtures.book(id = "2"))
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
                status = any(),
                applicationStatus = any(),
                deadlineFilter = any(),
                sortBy = any(),
            )
        } returns Result.success(books)

        val viewModel = createViewModel()
        viewModel.onBrowseListRouteChanged(category = "search", searchQuery = "magic", pageSize = 20)
        advanceUntilIdle()

        assertEquals(books, viewModel.books.first())
        assertFalse(viewModel.isLoading.first())
        assertTrue(viewModel.browseListFiltersReady.first())
    }

    @Test
    fun onBrowseListRouteChanged_searchCategory_setsHasMoreWhenPageFull() = runTest(testDispatcher) {
        val books = (1..20).map { TestFixtures.book(id = "book-$it") }
        coEvery {
            browseBooksUseCase(
                query = any(),
                page = any(),
                limit = any(),
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
                status = any(),
                applicationStatus = any(),
                deadlineFilter = any(),
                sortBy = any(),
            )
        } returns Result.success(books)

        val viewModel = createViewModel()
        viewModel.onBrowseListRouteChanged(category = "search", searchQuery = "q", pageSize = 20)
        advanceUntilIdle()

        assertTrue(viewModel.browseListHasMore.first())
        assertEquals(20, viewModel.lastFetchCount.first())
    }

    @Test
    fun updateBookListSearchInput_debouncesDebouncedQuery() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.updateBookListSearchInput("hello")
        assertEquals("hello", viewModel.bookListBrowseUi.first().searchQuery)
        assertEquals("", viewModel.bookListBrowseUi.first().debouncedSearchQuery)

        advanceTimeBy(300)
        advanceUntilIdle()
        assertEquals("hello", viewModel.bookListBrowseUi.first().debouncedSearchQuery)
    }

    @Test
    fun loadMoreBrowseList_appendsNextPage() = runTest(testDispatcher) {
        val page1 = (1..20).map { TestFixtures.book(id = "p1-$it") }
        val page2 = listOf(TestFixtures.book(id = "p2-1"))
        coEvery {
            browseBooksUseCase(
                query = any(),
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
                status = any(),
                applicationStatus = any(),
                deadlineFilter = any(),
                sortBy = any(),
            )
        } returns Result.success(page1)
        coEvery {
            browseBooksUseCase(
                query = any(),
                page = 2,
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
                status = any(),
                applicationStatus = any(),
                deadlineFilter = any(),
                sortBy = any(),
            )
        } returns Result.success(page2)

        val viewModel = createViewModel()
        viewModel.onBrowseListRouteChanged(category = null, searchQuery = null, pageSize = 20)
        advanceUntilIdle()
        viewModel.loadMoreBrowseList(category = null)
        advanceUntilIdle()

        assertEquals(21, viewModel.books.first().size)
        assertEquals("p2-1", viewModel.books.first().last().id)
    }
}
