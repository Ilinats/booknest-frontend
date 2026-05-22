package com.example.booknest.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.booknest.data.session.SessionManager
import com.example.booknest.ui.Api24ComposeTest
import com.example.booknest.ui.testing.UiTestTags
import com.example.booknest.ui.theme.BookNestTheme
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test

class LandingScreenTest : Api24ComposeTest() {

    @Test
    fun displaysWelcomeAndAuthButtons() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val navController = TestNavHostController(context)
        val sessionManager = mockk<SessionManager>(relaxed = true)
        every { sessionManager.isLoggedIn } returns MutableStateFlow(false)

        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                LandingScreen(navController = navController, sessionManager = sessionManager)
            }
        }

        composeTestRule.onNodeWithText("Welcome").assertIsDisplayed()
        composeTestRule.onNodeWithTag(UiTestTags.LANDING_LOGIN_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(UiTestTags.LANDING_SIGNUP_BUTTON).assertIsDisplayed()
    }
}
