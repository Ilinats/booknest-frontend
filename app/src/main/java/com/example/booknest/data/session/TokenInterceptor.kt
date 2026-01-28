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
    private val loginURL = "${RetrofitConstants.BASE_URL}${Auth.LOGIN}"
    private val logoutURL = "${RetrofitConstants.BASE_URL}${Auth.LOGOUT}"
    private val registerURL = "${RetrofitConstants.BASE_URL}${Auth.REGISTER}"

    override fun intercept(chain: Interceptor.Chain): Response {
        return runBlocking {
            val request = chain.request()
            val requestUrl = request.url.toString()
            val host = request.url.host

            val isS3Url = host.contains("s3") || host.contains("amazonaws.com")
            
            if (isS3Url) {
                return@runBlocking chain.proceed(request)
            }

            if (requestUrl == loginURL || requestUrl == registerURL || requestUrl == logoutURL) {
                return@runBlocking chain.proceed(request)
            }
            
            val requestBuilder = request.newBuilder()

            val token = if (requestUrl == refreshTokenURL) {
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

