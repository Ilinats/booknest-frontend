package com.example.booknest.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.booknest.ui.Api24ComposeTest
import com.example.booknest.ui.myapplications.components.common.EmptyApplicationsState
import com.example.booknest.ui.testing.UiTestTags
import com.example.booknest.ui.theme.BookNestTheme
import org.junit.Test

class EmptyApplicationsStateTest : Api24ComposeTest() {

    @Test
    fun displaysCustomMessage() {
        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                EmptyApplicationsState(
                    message = "No applications yet",
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        composeTestRule.onNodeWithText("No applications yet").assertIsDisplayed()
        composeTestRule.onNodeWithTag(UiTestTags.EMPTY_APPLICATIONS_MESSAGE).assertIsDisplayed()
    }
}
