package com.example.booknest.data.datasource

import com.example.booknest.data.error.BNError
import com.example.booknest.data.service.GenresService
import com.example.booknest.testutil.DataSourceJsonFixtures
import com.example.booknest.testutil.MockWebServerDataSourceTest
import com.example.booknest.testutil.RetrofitTestSupport
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BNGenresDataSourceTest : MockWebServerDataSourceTest() {

    private val dataSource: BNGenresDataSource
        get() = BNGenresDataSource(RetrofitTestSupport.service<GenresService>(mockWebServer))

    @Test
    fun getGenres_successReturnsList() = runTest {
        enqueueJson(200, """[{"id":1,"name":"Fantasy"},{"id":2,"name":"Sci-Fi"}]""")

        val result = dataSource.getGenres()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("Fantasy", result.getOrNull()?.first()?.name)
        assertEquals("/api/genres", mockWebServer.takeRequest().path)
    }

    @Test
    fun getGenres_failureMapsError() = runTest {
        enqueueJson(500, DataSourceJsonFixtures.errorBody("Server error", 500))

        val result = dataSource.getGenres()

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull() as BNError.Generic
        assertEquals("Server error", error.messageString)
        assertEquals(500, error.statusCode)
    }
}
