package com.example.booknest.ui.myapplications

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.example.booknest.ui.Api24ComposeTest
import com.example.booknest.ui.myapplications.components.stats.QuickStatsSummary
import com.example.booknest.ui.theme.BookNestTheme
import com.example.booknest.viewmodel.applications.ApplicationStats
import org.junit.Test

class QuickStatsSummaryTest : Api24ComposeTest() {

    @Test
    fun displaysQuickStatsValues() {
        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                QuickStatsSummary(
                    stats = ApplicationStats(
                        total = 12,
                        approvalRate = 75.0,
                        reviewsThisMonth = 4,
                        pendingReviews = 2,
                    ),
                )
            }
        }

        composeTestRule.onNodeWithText("Quick Stats").assertIsDisplayed()
        composeTestRule.onNodeWithText("12").assertIsDisplayed()
        composeTestRule.onNodeWithText("75%").assertIsDisplayed()
        composeTestRule.onNodeWithText("4").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
    }
}
