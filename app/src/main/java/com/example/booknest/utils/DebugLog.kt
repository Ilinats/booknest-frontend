package com.example.booknest.utils

import android.util.Log
import com.example.booknest.BuildConfig

/** Debug-only logging; no-ops in release so `println` is not shipped as behavior. */
object DebugLog {
    private const val TAG = "BookNest"

    fun d(subtag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("$TAG.$subtag", message)
        }
    }

    fun w(subtag: String, message: String, t: Throwable? = null) {
        if (t != null) Log.w("$TAG.$subtag", message, t) else Log.w("$TAG.$subtag", message)
    }
}
