package com.example.booknest.viewmodel.analytics

import com.example.booknest.domain.model.response.RatingDistributionResponse
import com.example.booknest.port.ToastNotifier
import com.example.booknest.viewmodel.common.UserFeedback
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsViewModelHelpersTest {

    private val viewModel = AnalyticsViewModel(
        feedback = UserFeedback(mockk<ToastNotifier>(relaxed = true)),
        getDetailedBookAnalyticsUseCase = mockk(),
        getAuthorAnalyticsUseCase = mockk(),
        getBookPerformanceComparisonUseCase = mockk(),
    )

    @Test
    fun getRatingDistributionList_mapsAllBuckets() {
        val distribution = RatingDistributionResponse(`1` = 2, `2` = 0, `3` = 1, `4` = 4, `5` = 7)

        assertEquals(
            listOf(1 to 2, 2 to 0, 3 to 1, 4 to 4, 5 to 7),
            viewModel.getRatingDistributionList(distribution),
        )
    }

    @Test
    fun getTopRatingCount_returnsMaximum() {
        val distribution = RatingDistributionResponse(`1` = 2, `5` = 9, `3` = 4)
        assertEquals(9, viewModel.getTopRatingCount(distribution))
    }

    @Test
    fun formatDate_parsesIsoTimestamp() {
        assertEquals("Jan 10, 2024", viewModel.formatDate("2024-01-10T12:00:00.000Z"))
    }
}
