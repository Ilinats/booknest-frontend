package com.example.booknest

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.example.booknest.testutil.Api24AssumeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
class ExampleInstrumentedTest {

    @JvmField
    @Rule(order = 0)
    val api24AssumeRule = Api24AssumeRule()

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assert(appContext.packageName == "com.example.booknest")
    }
}
