package com.example.booknest

import android.os.Build
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.example.booknest.testutil.Api24AndroidComposeRule
import com.example.booknest.testutil.Api24AssumeRule
import com.example.booknest.testutil.LoggedOutSessionRule
import com.example.booknest.testutil.waitUntilTestTagExists
import com.example.booknest.ui.testing.UiTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
class MainActivityNavigationTest {

    @JvmField
    @Rule(order = 0)
    val api24AssumeRule = Api24AssumeRule()

    @JvmField
    @Rule(order = 1)
    val loggedOutSessionRule = LoggedOutSessionRule()

    @JvmField
    @Rule(order = 2)
    val composeRuleHolder = Api24AndroidComposeRule(MainActivity::class.java)

    private val composeTestRule
        get() = composeRuleHolder.rule

    @Test
    fun loggedOutUserReachesLandingAfterSplash() {
        composeTestRule.waitUntilTestTagExists(
            tag = UiTestTags.LANDING_LOGIN_BUTTON,
            timeoutMillis = 15_000,
        )
        composeTestRule.onNodeWithTag(UiTestTags.LANDING_LOGIN_BUTTON).assertExists()
        composeTestRule.onNodeWithText("Welcome").assertExists()
    }
}
