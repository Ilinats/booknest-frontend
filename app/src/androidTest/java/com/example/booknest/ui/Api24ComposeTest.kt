package com.example.booknest.ui

import android.os.Build
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.example.booknest.testutil.Api24AssumeRule
import com.example.booknest.testutil.requireApi24OrSkip
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
abstract class Api24ComposeTest {

    @JvmField
    @Rule(order = 0)
    val api24AssumeRule = Api24AssumeRule()

    private lateinit var composeRule: ComposeContentTestRule

    @JvmField
    @Rule(order = 1)
    val composeTestRuleWrapper: TestRule = TestRule { base, description ->
        object : Statement() {
            override fun evaluate() {
                requireApi24OrSkip()
                composeRule = createComposeRule()
                composeRule.apply(base, description).evaluate()
            }
        }
    }

    protected val composeTestRule: ComposeContentTestRule
        get() = composeRule
}
