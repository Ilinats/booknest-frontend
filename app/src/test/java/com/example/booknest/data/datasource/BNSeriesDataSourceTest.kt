package com.example.booknest.data.datasource

import com.example.booknest.data.error.BNError
import com.example.booknest.data.service.SeriesService
import com.example.booknest.domain.model.request.CreateSeriesRequest
import com.example.booknest.domain.model.request.UpdateSeriesRequest
import com.example.booknest.testutil.DataSourceJsonFixtures
import com.example.booknest.testutil.MockWebServerDataSourceTest
import com.example.booknest.testutil.RetrofitTestSupport
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BNSeriesDataSourceTest : MockWebServerDataSourceTest() {

    private val dataSource: BNSeriesDataSource
        get() = BNSeriesDataSource(RetrofitTestSupport.service<SeriesService>(mockWebServer))

    @Test
    fun getMySeries_successReturnsList() = runTest {
        enqueueJson(200, "[${DataSourceJsonFixtures.series}]")

        val result = dataSource.getMySeries()

        assertTrue(result.isSuccess)
        assertEquals("My Series", result.getOrNull()?.first()?.name)
        assertEquals("/api/series/my", mockWebServer.takeRequest().path)
    }

    @Test
    fun createSeries_successReturnsSeries() = runTest {
        enqueueJson(200, DataSourceJsonFixtures.series)

        val result = dataSource.createSeries(CreateSeriesRequest(name = "My Series", description = "Saga"))

        assertTrue(result.isSuccess)
        assertEquals("series-1", result.getOrNull()?.id)
        assertEquals("POST", mockWebServer.takeRequest().method)
    }

    @Test
    fun updateSeries_successReturnsSeries() = runTest {
        enqueueJson(200, DataSourceJsonFixtures.series)

        val result = dataSource.updateSeries(
            "series-1",
            UpdateSeriesRequest(name = "Renamed Series"),
        )

        assertTrue(result.isSuccess)
        assertEquals("series-1", result.getOrNull()?.id)
        assertEquals("/api/series/series-1", mockWebServer.takeRequest().path)
    }

    @Test
    fun deleteSeries_failureMapsBnError() = runTest {
        enqueueJson(409, DataSourceJsonFixtures.errorBody("Series has books", 409))

        val result = dataSource.deleteSeries("series-1")

        assertTrue(result.isFailure)
        assertEquals("Series has books", (result.exceptionOrNull() as BNError.Generic).messageString)
    }
}
