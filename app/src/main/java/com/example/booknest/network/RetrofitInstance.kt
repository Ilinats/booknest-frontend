package com.example.booknest.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object TokenCache {
    @Volatile
    var accessToken: String? = null
}

object RetrofitInstance {

    private const val BASE_URL = "http://192.168.1.23:3000/"

    @Volatile
    var tokenRefreshCallback: (suspend () -> Boolean)? = null
        private set

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun createAuthInterceptor(): AuthInterceptor {
        return AuthInterceptor()
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
            .addInterceptor(createAuthInterceptor())
        .build()
    }

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Volatile
    private var _api: ApiService? = null

    val api: ApiService
        get() {
            if (_api == null) {
                synchronized(this) {
                    if (_api == null) {
                        _api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
                    }
                }
            }
            return _api!!
        }

    fun setTokenRefreshCallback(callback: suspend () -> Boolean) {
        synchronized(this) {
            tokenRefreshCallback = callback
        }
    }
}