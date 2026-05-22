package com.example.booknest.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.booknest.ui.Api24ComposeTest
import com.example.booknest.ui.home.components.cards.QuickActionCard
import com.example.booknest.ui.testing.UiTestTags
import com.example.booknest.ui.theme.BookNestTheme
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickActionCardTest : Api24ComposeTest() {

    @Test
    fun displaysContentAndHandlesClick() {
        var clicked = false

        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                QuickActionCard(
                    title = "Reading",
                    subtitle = "2 book(s)",
                    icon = Icons.Filled.Book,
                    testTagSuffix = "reading",
                    onClick = { clicked = true },
                )
            }
        }

        composeTestRule.onNodeWithTag("${UiTestTags.QUICK_ACTION_CARD_PREFIX}reading").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reading").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 book(s)").assertIsDisplayed()
        composeTestRule.onNodeWithTag("${UiTestTags.QUICK_ACTION_CARD_PREFIX}reading").performClick()
        assertTrue(clicked)
    }
}
