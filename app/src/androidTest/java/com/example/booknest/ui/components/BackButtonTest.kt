package com.example.booknest.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.booknest.ui.Api24ComposeTest
import com.example.booknest.ui.testing.UiTestTags
import com.example.booknest.ui.theme.BookNestTheme
import org.junit.Assert.assertTrue
import org.junit.Test

class BackButtonTest : Api24ComposeTest() {

    @Test
    fun clickInvokesCallback() {
        var clicked = false

        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                BackButton(onClick = { clicked = true })
            }
        }

        composeTestRule.onNodeWithTag(UiTestTags.BACK_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(UiTestTags.BACK_BUTTON).performClick()

        assertTrue(clicked)
    }
}
