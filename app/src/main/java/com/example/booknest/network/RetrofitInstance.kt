package com.example.booknest.network

import com.example.booknest.data.AuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TokenCache {
    @Volatile
    var accessToken: String? = null
}

object RetrofitInstance {

    private const val BASE_URL = "http://192.168.1.23:3000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private var authInterceptor: AuthInterceptor? = null

    private fun createClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .apply {
                authInterceptor?.let { addInterceptor(it) }
            }
            .build()
    }

    private var retrofit: Retrofit? = null

    fun initialize(authManager: AuthManager?) {
        authInterceptor = authManager?.let { AuthInterceptor(it) }
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService
        get() {
            if (retrofit == null) {
                throw IllegalStateException("RetrofitInstance not initialized. Call initialize() first.")
            }
            return retrofit!!.create(ApiService::class.java)
        }
}