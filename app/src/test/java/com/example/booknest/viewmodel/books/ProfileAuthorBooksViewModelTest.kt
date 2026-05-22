package com.example.booknest.viewmodel.books

import com.example.booknest.domain.usecase.books.BrowseBooksUseCase
import com.example.booknest.testutil.MainDispatcherRule
import com.example.booknest.testutil.TestFixtures
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
class ProfileAuthorBooksViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val browseBooksUseCase = mockk<BrowseBooksUseCase>()
    private val bookCatalogCache = BookCatalogCache()

    private fun createViewModel() = ProfileAuthorBooksViewModel(
        browseBooksUseCase = browseBooksUseCase,
        bookCatalogCache = bookCatalogCache,
    )

    @Test
    fun loadAuthorBooks_populatesListAndCache() = runTest(testDispatcher) {
        val books = listOf(TestFixtures.book(id = "book-9"))
        coEvery {
            browseBooksUseCase(authorId = "author-1", page = 1, limit = 100)
        } returns Result.success(books)

        val viewModel = createViewModel()
        viewModel.loadAuthorBooks("author-1", authorName = "Author")
        advanceUntilIdle()

        assertEquals(books, viewModel.authorBooks.value)
        assertFalse(viewModel.authorBooksLoading.value)
        assertEquals("book-9", bookCatalogCache.findBook("book-9")?.id)
        coVerify { browseBooksUseCase(authorId = "author-1", page = 1, limit = 100) }
    }
}
