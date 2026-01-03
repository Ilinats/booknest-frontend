package com.example.booknest.data.session

import android.content.Context
import com.example.booknest.data.constants.Auth
import com.example.booknest.data.constants.Header
import com.example.booknest.data.constants.RetrofitConstants
import com.example.booknest.dataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(private val context: Context) : Interceptor {

    private val refreshTokenURL = "${RetrofitConstants.BASE_URL}${Auth.REFRESH_STRING_CONCAT}"

    override fun intercept(chain: Interceptor.Chain): Response {
        return runBlocking {
            val requestBuilder = chain.request().newBuilder()

            val token = if (chain.request().url.toString() == refreshTokenURL) {
                SessionManager.getInstance(dataStore = context.dataStore).getRefreshToken()
            } else {
                SessionManager.getInstance(dataStore = context.dataStore).getToken()
            }

            if (token.isNotEmpty()) {
                requestBuilder.addHeader(Header.AUTHORIZATION, "${Header.BEARER} $token")
            }
            chain.proceed(requestBuilder.build())
        }
    }
}

