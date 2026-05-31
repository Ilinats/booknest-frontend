package com.example.booknest.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.booknest.ui.Api24ComposeTest
import com.example.booknest.ui.components.stats.StatCard
import com.example.booknest.ui.testing.UiTestTags
import com.example.booknest.ui.theme.BookNestTheme
import org.junit.Test

class StatCardTest : Api24ComposeTest() {

    @Test
    fun displaysTitleAndValue() {
        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                StatCard(title = "Total Books", value = "42")
            }
        }

        composeTestRule.onNodeWithText("Total Books").assertIsDisplayed()
        composeTestRule.onNodeWithText("42").assertIsDisplayed()
        composeTestRule.onNodeWithTag(UiTestTags.STAT_CARD_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(UiTestTags.STAT_CARD_VALUE).assertIsDisplayed()
    }
}
