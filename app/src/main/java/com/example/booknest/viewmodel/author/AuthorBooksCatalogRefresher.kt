package com.example.booknest.viewmodel.author

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Signals [AuthorBooksViewModel] to reload the author's book list after editor mutations. */
class AuthorBooksCatalogRefresher {
    private val _refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshRequests: SharedFlow<Unit> = _refreshRequests.asSharedFlow()

    fun requestRefresh() {
        _refreshRequests.tryEmit(Unit)
    }
}
