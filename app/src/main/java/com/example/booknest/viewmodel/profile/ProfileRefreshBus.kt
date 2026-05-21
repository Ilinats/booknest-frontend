package com.example.booknest.viewmodel.profile

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Signals [ProfileViewModel] to reload the current user's profile after edits. */
class ProfileRefreshBus {
    private val _refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshRequests: SharedFlow<Unit> = _refreshRequests.asSharedFlow()

    fun requestRefresh() {
        _refreshRequests.tryEmit(Unit)
    }
}
