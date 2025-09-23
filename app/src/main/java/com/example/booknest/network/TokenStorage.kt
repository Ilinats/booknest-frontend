package com.example.booknest.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.preferencesDataStore

private const val DATASTORE_NAME = "auth_prefs"
private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

object TokenStorage {
    private val TOKEN_KEY = stringPreferencesKey("access_token")
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    suspend fun saveToken(token: String) {
        appContext.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    fun getTokenFlow(): Flow<String?> {
        return appContext.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]
        }
    }

    suspend fun clearToken() {
        appContext.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }
}