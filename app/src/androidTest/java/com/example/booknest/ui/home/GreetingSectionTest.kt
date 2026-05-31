package com.example.booknest.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.ui.Api24ComposeTest
import com.example.booknest.ui.home.components.sections.GreetingSection
import com.example.booknest.ui.testing.UiTestTags
import com.example.booknest.ui.theme.BookNestTheme
import org.junit.Test

class GreetingSectionTest : Api24ComposeTest() {

    @Test
    fun showsPersonalizedGreetingWhenFirstNamePresent() {
        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                GreetingSection(
                    currentUser = UserResponse(
                        id = "u1",
                        username = "reader",
                        firstName = "Alex",
                    ),
                )
            }
        }

        composeTestRule.onNodeWithTag(UiTestTags.GREETING_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome back, Alex!").assertIsDisplayed()
        composeTestRule.onNodeWithText("What would you like to read today?").assertIsDisplayed()
    }

    @Test
    fun showsGenericGreetingWhenUserMissing() {
        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                GreetingSection(currentUser = null)
            }
        }

        composeTestRule.onNodeWithText("Welcome back!").assertIsDisplayed()
    }
}
