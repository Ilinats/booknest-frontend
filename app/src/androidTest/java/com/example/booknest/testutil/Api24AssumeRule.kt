package com.example.booknest.testutil

import org.junit.Assume.assumeTrue
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class Api24AssumeRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                requireApi24OrSkip()
                base.evaluate()
            }
        }
    }
}

fun requireApi24OrSkip() {
    assumeTrue(
        "Instrumented UI tests require API 24+ (device API ${android.os.Build.VERSION.SDK_INT}). " +
            "Create an API 24+ AVD in Device Manager (e.g. Medium Phone API 34).",
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N,
    )
}
