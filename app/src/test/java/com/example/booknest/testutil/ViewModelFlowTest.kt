package com.example.booknest.testutil

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Collects [StateFlow]s backed by [SharingStarted.Lazily] for the duration of [block],
 * then cancels collectors when the block completes (avoids UncompletedCoroutinesError).
 */
suspend fun <R> withActiveStateFlows(
    vararg flows: StateFlow<*>,
    block: suspend () -> R,
): R = coroutineScope {
    flows.forEach { flow ->
        launch { flow.collect { } }
    }
    block()
}
