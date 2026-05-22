package com.example.booknest.testutil

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit

object RetrofitTestSupport {

    val json: Json = Json { ignoreUnknownKeys = true }

    fun retrofit(server: MockWebServer): Retrofit =
        Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    inline fun <reified T> service(server: MockWebServer): T =
        retrofit(server).create(T::class.java)
}
