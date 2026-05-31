package com.example.booknest.testutil

import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule

fun ComposeContentTestRule.waitUntilTestTagExists(
    tag: String,
    timeoutMillis: Long = 15_000,
) {
    waitUntil(timeoutMillis) {
        runCatching {
            onAllNodesWithTag(tag)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }.getOrDefault(false)
    }
}

fun ComposeContentTestRule.waitUntilNodesExist(
    timeoutMillis: Long = 15_000,
    matcher: ComposeContentTestRule.() -> SemanticsNodeInteractionCollection,
) {
    waitUntil(timeoutMillis) {
        runCatching {
            matcher()
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }.getOrDefault(false)
    }
}
