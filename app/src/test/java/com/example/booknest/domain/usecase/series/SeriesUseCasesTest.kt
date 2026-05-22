package com.example.booknest.domain.usecase.series

import com.example.booknest.domain.model.request.CreateSeriesRequest
import com.example.booknest.domain.repository.SeriesRepository
import com.example.booknest.testutil.TestFixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SeriesUseCasesTest {

    private val repository = mockk<SeriesRepository>()

    @Test
    fun createSeriesUseCase_delegatesToRepository() = runTest {
        val series = TestFixtures.series()
        val request = CreateSeriesRequest(name = "Trilogy", description = "Desc")
        coEvery { repository.createSeries(request) } returns Result.success(series)

        assertEquals(series, CreateSeriesUseCase(repository)(request).getOrNull())
        coVerify { repository.createSeries(request) }
    }

    @Test
    fun deleteSeriesUseCase_delegatesToRepository() = runTest {
        coEvery { repository.deleteSeries("series-1") } returns Result.success(Unit)

        val result = DeleteSeriesUseCase(repository)("series-1")
        assertEquals(Unit, result.getOrNull())
    }
}
