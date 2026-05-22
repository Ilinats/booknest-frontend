package com.example.booknest.domain.usecase.books

import com.example.booknest.domain.repository.BooksRepository
import com.example.booknest.testutil.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BooksUseCasesTest {

    private val repository = mockk<BooksRepository>()

    @Test
    fun browseBooksUseCase_forwardsAllFiltersToRepository() = runTest {
        val books = listOf(TestFixtures.book())
        coEvery {
            repository.browseBooks(
                search = "fantasy",
                genres = listOf(1, 2),
                title = null,
                authorName = null,
                authorId = null,
                seriesName = null,
                seriesId = null,
                ageRating = "16+",
                distributionType = "digital",
                publishedFrom = null,
                publishedTo = null,
                createdFrom = null,
                createdTo = null,
                minAvgRating = 3.0,
                maxAvgRating = 5.0,
                page = 1,
                limit = 20,
                status = "active",
                applicationStatus = null,
                deadlineFilter = null,
                sortBy = "rating",
            )
        } returns Result.success(books)

        val result = BrowseBooksUseCase(repository)(
            query = "fantasy",
            genres = listOf(1, 2),
            ageRating = "16+",
            distributionType = "digital",
            minAvgRating = 3.0,
            maxAvgRating = 5.0,
            page = 1,
            limit = 20,
            status = "active",
            sortBy = "rating",
        )

        assertEquals(books, result.getOrNull())
        coVerify {
            repository.browseBooks(
                search = "fantasy",
                genres = listOf(1, 2),
                title = null,
                authorName = null,
                authorId = null,
                seriesName = null,
                seriesId = null,
                ageRating = "16+",
                distributionType = "digital",
                publishedFrom = null,
                publishedTo = null,
                createdFrom = null,
                createdTo = null,
                minAvgRating = 3.0,
                maxAvgRating = 5.0,
                page = 1,
                limit = 20,
                status = "active",
                applicationStatus = null,
                deadlineFilter = null,
                sortBy = "rating",
            )
        }
    }

    @Test
    fun getRecommendedBooksUseCase_delegatesToRepository() = runTest {
        val books = listOf(TestFixtures.book(id = "rec-1"))
        coEvery { repository.getRecommendedBooks(limit = 10, page = 1) } returns Result.success(books)

        val result = GetRecommendedBooksUseCase(repository)(limit = 10)

        assertEquals(books, result.getOrNull())
    }

    @Test
    fun searchBooksUseCase_delegatesToRepository() = runTest {
        val books = listOf(TestFixtures.book())
        coEvery { repository.searchBooks("dragon", 1, 20) } returns Result.success(books)

        val result = SearchBooksUseCase(repository)("dragon", page = 1, limit = 20)

        assertEquals(books, result.getOrNull())
        coVerify { repository.searchBooks("dragon", 1, 20) }
    }
}
