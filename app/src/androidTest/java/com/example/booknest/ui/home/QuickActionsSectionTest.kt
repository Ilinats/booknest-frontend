package com.example.booknest.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.ui.Api24ComposeTest
import com.example.booknest.ui.home.components.sections.QuickActionsSection
import com.example.booknest.ui.testing.UiTestTags
import com.example.booknest.ui.theme.BookNestTheme
import org.junit.Test

class QuickActionsSectionTest : Api24ComposeTest() {

    @Test
    fun showsQuickActionsWhenDataPresent() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val navController = TestNavHostController(context)

        val activeApp = ApplicationResponse(
            id = "a1",
            bookId = "b1",
            status = "approved",
            appliedAt = "2024-01-01T00:00:00.000Z",
            readingStatus = "reading",
        )
        val pendingApp = activeApp.copy(id = "a2", status = "pending")

        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                QuickActionsSection(
                    activeReadingApplications = listOf(activeApp),
                    pendingApplications = listOf(pendingApp),
                    unreadCount = 3,
                    navController = navController,
                )
            }
        }

        composeTestRule.onNodeWithTag("${UiTestTags.QUICK_ACTION_CARD_PREFIX}reading").assertIsDisplayed()
        composeTestRule.onNodeWithTag("${UiTestTags.QUICK_ACTION_CARD_PREFIX}pending").assertIsDisplayed()
        composeTestRule.onNodeWithTag("${UiTestTags.QUICK_ACTION_CARD_PREFIX}alerts").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 new").assertIsDisplayed()
    }
}
