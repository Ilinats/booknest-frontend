package com.example.booknest.data.datasource

import com.example.booknest.data.error.BNError
import com.example.booknest.data.service.AuthorsService
import com.example.booknest.testutil.DataSourceJsonFixtures
import com.example.booknest.testutil.MockWebServerDataSourceTest
import com.example.booknest.testutil.RetrofitTestSupport
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BNAuthorsDataSourceTest : MockWebServerDataSourceTest() {

    private val dataSource: BNAuthorsDataSource
        get() = BNAuthorsDataSource(RetrofitTestSupport.service<AuthorsService>(mockWebServer))

    @Test
    fun followAuthor_successReturnsFollow() = runTest {
        enqueueJson(200, DataSourceJsonFixtures.authorFollow)

        val result = dataSource.followAuthor("author-1")

        assertTrue(result.isSuccess)
        assertEquals("author-1", result.getOrNull()?.authorId)
        assertEquals("/api/authors/follow/author-1", mockWebServer.takeRequest().path)
    }

    @Test
    fun checkIfFollowingAuthor_successReturnsMap() = runTest {
        enqueueJson(200, """{"isFollowing":true}""")

        val result = dataSource.checkIfFollowingAuthor("author-1")

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull()?.get("isFollowing"))
    }

    @Test
    fun getFollowedAuthors_successReturnsList() = runTest {
        enqueueJson(200, "[${DataSourceJsonFixtures.authorFollow}]")

        val result = dataSource.getFollowedAuthors()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("/api/authors/following", mockWebServer.takeRequest().path)
    }

    @Test
    fun unfollowAuthor_failureMapsBnError() = runTest {
        enqueueJson(404, DataSourceJsonFixtures.errorBody("Author not found", 404))

        val result = dataSource.unfollowAuthor("missing")

        assertTrue(result.isFailure)
        assertEquals("Author not found", (result.exceptionOrNull() as BNError.Generic).messageString)
    }
}
