package com.example.booknest.utils

import android.content.Context
import android.provider.Settings
import com.google.firebase.messaging.FirebaseMessaging
import com.example.booknest.utils.DebugLog
import kotlinx.coroutines.tasks.await

object FCMTokenManager {
    suspend fun getToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            DebugLog.w("FCM", "Error getting FCM token: ${e.message}", e)
            null
        }
    }

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
    }

    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }
}

