package com.example.booknest.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "http://10.0.2.2:3000/"
    // ⚠️ Use 10.0.2.2 for Android emulator if backend runs on your local machine.
    // Replace with your server’s IP/domain in production.

    // Logging for debugging network requests
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttpClient with logging (can also add authentication headers here later)
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Retrofit instance
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ApiService exposed for use in ViewModel/Repository
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}