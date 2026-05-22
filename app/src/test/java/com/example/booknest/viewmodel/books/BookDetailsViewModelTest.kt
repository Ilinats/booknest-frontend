package com.example.booknest.viewmodel.books

import com.example.booknest.domain.usecase.books.GetBookDetailsUseCase
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.testutil.TestFixtures
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getBookDetailsUseCase = mockk<GetBookDetailsUseCase>()
    private val bookCatalogCache = BookCatalogCache()

    private fun createViewModel() = BookDetailsViewModel(
        getBookDetailsUseCase = getBookDetailsUseCase,
        bookCatalogCache = bookCatalogCache,
    )

    @Test
    fun calculateRating_usesBookRatingWhenPresent() {
        val viewModel = createViewModel()
        val rating = viewModel.calculateRating(
            bookRating = 4.5,
            reviews = listOf(TestFixtures.review(rating = 2.0)),
        )
        assertEquals(4.5, rating, 0.001)
    }

    @Test
    fun calculateRating_averagesReviewsWhenBookRatingMissing() {
        val viewModel = createViewModel()
        val rating = viewModel.calculateRating(
            bookRating = null,
            reviews = listOf(
                TestFixtures.review(rating = 4.0),
                TestFixtures.review(id = "r-2", rating = 2.0),
            ),
        )
        assertEquals(3.0, rating, 0.001)
    }

    @Test
    fun calculateRating_returnsZeroWhenNoData() {
        val viewModel = createViewModel()
        assertEquals(0.0, viewModel.calculateRating(null, emptyList()), 0.001)
    }

    @Test
    fun beginBookDetailsScreen_showsCachedBookImmediately() = runTest(testDispatcher) {
        val cached = TestFixtures.book(id = "book-1", title = "Cached Title")
        bookCatalogCache.register(listOf(cached))
        coEvery { getBookDetailsUseCase("book-1") } returns Result.success(
            TestFixtures.bookDetails(id = "book-1", title = "Full Title", fullDescription = "Desc"),
        )

        val viewModel = createViewModel()
        viewModel.beginBookDetailsScreen("book-1")

        assertEquals("Cached Title", viewModel.bookDetailsScreenBook.value?.title)
        assertFalse(viewModel.bookDetailsScreenLoading.value)

        advanceUntilIdle()
        assertEquals("Full Title", viewModel.bookDetailsScreenBook.value?.title)
    }

    @Test
    fun beginBookDetailsScreen_loadsFullDetailsWhenNotCached() = runTest(testDispatcher) {
        val full = TestFixtures.bookDetails(id = "book-2", title = "Loaded", fullDescription = "Long")
        coEvery { getBookDetailsUseCase("book-2") } returns Result.success(full)

        val viewModel = createViewModel()
        viewModel.beginBookDetailsScreen("book-2")
        advanceUntilIdle()

        assertEquals("Loaded", viewModel.bookDetailsScreenBook.first()?.title)
        assertEquals("Long", viewModel.bookDetailsScreenBook.first()?.fullDescription)
        assertFalse(viewModel.bookDetailsScreenLoading.first())
    }

    @Test
    fun beginBookDetailsScreen_blankIdIsIgnored() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.beginBookDetailsScreen("  ")
        advanceUntilIdle()

        assertNull(viewModel.bookDetailsScreenBook.first())
    }
}
