package com.example.booknest.domain.usecase.author

import com.example.booknest.domain.repository.BooksRepository
import com.example.booknest.testutil.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthorUseCasesTest {

    private val repository = mockk<BooksRepository>()

    @Test
    fun getMyBooksUseCase_delegatesToRepository() = runTest {
        val books = listOf(TestFixtures.bookDetails())
        coEvery { repository.getMyBooks() } returns Result.success(books)

        assertEquals(books, GetMyBooksUseCase(repository)().getOrNull())
    }

    @Test
    fun publishBookUseCase_delegatesToRepository() = runTest {
        val book = TestFixtures.bookDetails(status = "active")
        coEvery { repository.publishBook("book-1") } returns Result.success(book)

        assertEquals(book, PublishBookUseCase(repository)("book-1").getOrNull())
        coVerify { repository.publishBook("book-1") }
    }

    @Test
    fun deleteBookUseCase_delegatesToRepository() = runTest {
        coEvery { repository.deleteBook("book-1") } returns Result.success(Unit)

        val result = DeleteBookUseCase(repository)("book-1")
        assertEquals(Unit, result.getOrNull())
    }
}
