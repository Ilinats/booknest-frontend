package com.example.booknest.domain.usecase.analytics

import com.example.booknest.domain.repository.BooksRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsUseCasesTest {

    private val repository = mockk<BooksRepository>()

    @Test
    fun getDetailedBookAnalyticsUseCase_delegatesToRepository() = runTest {
        val analytics = mockk<com.example.booknest.domain.model.response.DetailedBookAnalyticsResponse>()
        coEvery { repository.getDetailedBookAnalytics("book-1") } returns Result.success(analytics)

        assertEquals(analytics, GetDetailedBookAnalyticsUseCase(repository)("book-1").getOrNull())
        coVerify { repository.getDetailedBookAnalytics("book-1") }
    }
}
