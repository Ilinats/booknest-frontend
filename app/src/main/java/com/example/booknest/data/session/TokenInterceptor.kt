package com.example.booknest.data.session

import com.example.booknest.data.constants.Auth
import com.example.booknest.data.constants.Header
import com.example.booknest.data.constants.RetrofitConstants
import com.example.booknest.port.AuthTokenAccessor
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(
    private val authTokens: AuthTokenAccessor,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestUrl = request.url.toString()
        val host = request.url.host

        if (host.contains("s3") || host.contains("amazonaws.com")) {
            return chain.proceed(request)
        }

        if (requestUrl == loginUrl || requestUrl == registerUrl || requestUrl == logoutUrl) {
            return chain.proceed(request)
        }

        val requestBuilder = request.newBuilder()

        val token = if (requestUrl == refreshTokenUrl) {
            authTokens.getRefreshToken()
        } else {
            authTokens.getToken()
        }

        if (token.isNotEmpty()) {
            requestBuilder.addHeader(Header.AUTHORIZATION, "${Header.BEARER} $token")
        }
        return chain.proceed(requestBuilder.build())
    }

    companion object {
        private val refreshTokenUrl = "${RetrofitConstants.BASE_URL}${Auth.REFRESH_STRING_CONCAT}"
        private val loginUrl = "${RetrofitConstants.BASE_URL}${Auth.LOGIN}"
        private val logoutUrl = "${RetrofitConstants.BASE_URL}${Auth.LOGOUT}"
        private val registerUrl = "${RetrofitConstants.BASE_URL}${Auth.REGISTER}"
    }
}
