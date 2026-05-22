package com.example.booknest.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.booknest.data.session.SessionManager
import com.example.booknest.ui.Api24ComposeTest
import com.example.booknest.presentation.common.UiState
import com.example.booknest.presentation.effects.AuthUiEffect
import com.example.booknest.ui.testing.UiTestTags
import com.example.booknest.ui.theme.BookNestTheme
import com.example.booknest.viewmodel.auth.LoginResult
import com.example.booknest.viewmodel.auth.LoginViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

class LoginScreenTest : Api24ComposeTest() {

    private val loginState = MutableStateFlow<UiState<LoginResult>>(UiState.Idle)
    private val authUiEffect = MutableSharedFlow<AuthUiEffect>()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = mockk(relaxed = true)
        every { viewModel.loginState } returns loginState
        every { viewModel.authUiEffect } returns authUiEffect
    }

    @Test
    fun signInDisabledUntilFormValid() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val navController = TestNavHostController(context)
        val sessionManager = mockk<SessionManager>(relaxed = true)

        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                LoginScreen(
                    navController = navController,
                    viewModel = viewModel,
                    sessionManager = sessionManager,
                )
            }
        }

        composeTestRule.onNodeWithText("Log In").assertIsDisplayed()
        composeTestRule.onNodeWithTag(UiTestTags.LOGIN_SUBMIT_BUTTON).assertIsNotEnabled()

        composeTestRule.onNodeWithTag(UiTestTags.LOGIN_IDENTIFIER_FIELD).performTextInput("reader")
        composeTestRule.onNodeWithTag(UiTestTags.LOGIN_PASSWORD_FIELD).performTextInput("password12")

        composeTestRule.onNodeWithTag(UiTestTags.LOGIN_SUBMIT_BUTTON).assertIsEnabled()
        composeTestRule.onNodeWithTag(UiTestTags.LOGIN_SUBMIT_BUTTON).performClick()

        verify { viewModel.loginUser("reader", "password12") }
    }

    @Test
    fun forgotPasswordButtonIsDisplayed() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val navController = TestNavHostController(context)
        val sessionManager = mockk<SessionManager>(relaxed = true)

        composeTestRule.setContent {
            BookNestTheme(dynamicColor = false) {
                LoginScreen(
                    navController = navController,
                    viewModel = viewModel,
                    sessionManager = sessionManager,
                )
            }
        }

        composeTestRule.onNodeWithTag(UiTestTags.LOGIN_FORGOT_PASSWORD).assertIsDisplayed()
    }
}
