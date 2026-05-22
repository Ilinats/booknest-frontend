package com.example.booknest.data.datasource

import com.example.booknest.data.error.BNError
import com.example.booknest.data.service.BooksService
import com.example.booknest.testutil.DataSourceJsonFixtures
import com.example.booknest.testutil.MockWebServerDataSourceTest
import com.example.booknest.testutil.RetrofitTestSupport
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BNBooksDataSourceTest : MockWebServerDataSourceTest() {

    private val dataSource: BNBooksDataSource
        get() = BNBooksDataSource(RetrofitTestSupport.service<BooksService>(mockWebServer))

    @Test
    fun getRecommendedBooks_successReturnsPaginatedList() = runTest {
        enqueueJson(200, DataSourceJsonFixtures.paginatedBooks())

        val result = dataSource.getRecommendedBooks(limit = 10, page = 1)

        assertTrue(result.isSuccess)
        assertEquals("book-1", result.getOrNull()?.first()?.id)
        assertEquals("/api/books/recommended", mockWebServer.takeRequest().path?.substringBefore("?"))
    }

    @Test
    fun getBookDetails_successReturnsBook() = runTest {
        enqueueJson(200, DataSourceJsonFixtures.BOOK_DETAILS)

        val result = dataSource.getBookDetails("book-1")

        assertTrue(result.isSuccess)
        assertEquals("Test Book", result.getOrNull()?.title)
        assertEquals("/api/books/book-1", mockWebServer.takeRequest().path)
    }

    @Test
    fun getTrendingBooks_successReturnsList() = runTest {
        enqueueJson(200, "[${DataSourceJsonFixtures.trendingBook}]")

        val result = dataSource.getTrendingBooks(limit = 5)

        assertTrue(result.isSuccess)
        assertEquals(12, result.getOrNull()?.first()?.applicationCount)
        assertTrue(mockWebServer.takeRequest().path!!.startsWith("/api/books/trending"))
    }

    @Test
    fun getBookDetails_failureMapsBnError() = runTest {
        enqueueJson(404, DataSourceJsonFixtures.errorBody("Book not found", 404))

        val result = dataSource.getBookDetails("missing")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as BNError.Generic
        assertEquals("Book not found", error.messageString)
        assertEquals(404, error.statusCode)
    }
}
