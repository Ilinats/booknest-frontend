package com.example.booknest.viewmodel.author

import com.example.booknest.domain.usecase.applications.GetBookApplicationsUseCase
import com.example.booknest.domain.usecase.author.DeleteBookUseCase
import com.example.booknest.domain.usecase.author.GetBookStatsUseCase
import com.example.booknest.domain.usecase.author.GetMyBooksUseCase
import com.example.booknest.domain.usecase.author.PublishBookUseCase
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
class AuthorBooksViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private val getMyBooksUseCase = mockk<GetMyBooksUseCase>()
    private val getBookStatsUseCase = mockk<GetBookStatsUseCase>()
    private val getBookApplicationsUseCase = mockk<GetBookApplicationsUseCase>()
    private val deleteBookUseCase = mockk<DeleteBookUseCase>()
    private val publishBookUseCase = mockk<PublishBookUseCase>()
    private val catalogRefresher = AuthorBooksCatalogRefresher()
    private val feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true))

    private fun createViewModel() = AuthorBooksViewModel(
        feedback = feedback,
        getMyBooksUseCase = getMyBooksUseCase,
        getBookStatsUseCase = getBookStatsUseCase,
        getBookApplicationsUseCase = getBookApplicationsUseCase,
        deleteBookUseCase = deleteBookUseCase,
        publishBookUseCase = publishBookUseCase,
        catalogRefresher = catalogRefresher,
    )

    @Test
    fun loadMyBooks_populatesList() = runTest(testDispatcher) {
        val books = listOf(TestFixtures.bookDetails(id = "b-1"))
        coEvery { getMyBooksUseCase() } returns Result.success(books)
        coEvery { getBookStatsUseCase(any()) } returns Result.success(TestFixtures.bookStats())
        coEvery { getBookApplicationsUseCase(any()) } returns Result.success(emptyList())

        val viewModel = createViewModel()
        viewModel.loadMyBooks()
        advanceUntilIdle()

        assertEquals(books, viewModel.myBooks.value)
    }

    @Test
    fun deleteBook_reloadsOnSuccess() = runTest(testDispatcher) {
        coEvery { deleteBookUseCase("b-1") } returns Result.success(Unit)
        coEvery { getMyBooksUseCase() } returns Result.success(emptyList())

        val viewModel = createViewModel()
        viewModel.deleteBook("b-1")
        advanceUntilIdle()

        assertEquals("Book deleted successfully!", viewModel.successMessage.value)
        coVerify(atLeast = 1) { getMyBooksUseCase() }
    }

    @Test
    fun updateSearchQuery_updatesState() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.updateSearchQuery("dragon")
        assertEquals("dragon", viewModel.searchQuery.value)
    }
}
