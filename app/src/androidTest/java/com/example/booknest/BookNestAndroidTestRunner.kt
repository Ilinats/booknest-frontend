package com.example.booknest

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.test.runner.AndroidJUnitRunner

class BookNestAndroidTestRunner : AndroidJUnitRunner() {

    override fun onCreate(arguments: Bundle?) {
        val args = arguments ?: Bundle()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.w(
                TAG,
                "Device API ${Build.VERSION.SDK_INT} is below 24. " +
                    "Compose UI tests will be skipped. Use an API 24+ emulator (e.g. API 34–37).",
            )
        }
        super.onCreate(args)
    }

    override fun onStart() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            finish(
                Activity.RESULT_OK,
                Bundle().apply {
                    putString(
                        "stream",
                        "\nSkipped instrumented tests: require API 24+ AVD (device is API " +
                            "${Build.VERSION.SDK_INT}).\n",
                    )
                },
            )
            return
        }
        super.onStart()
    }

    private companion object {
        const val TAG = "BookNestAndroidTestRunner"
    }
}
