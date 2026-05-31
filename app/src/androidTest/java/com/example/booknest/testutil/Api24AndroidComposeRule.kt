package com.example.booknest.testutil

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class Api24AndroidComposeRule<A : ComponentActivity>(
    private val activityClass: Class<A>,
) : TestRule {

    private lateinit var delegate: AndroidComposeTestRule<*, A>

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                requireApi24OrSkip()
                delegate = createAndroidComposeRule(activityClass)
                delegate.apply(base, description).evaluate()
            }
        }
    }

    val rule: AndroidComposeTestRule<*, A>
        get() = delegate
}
