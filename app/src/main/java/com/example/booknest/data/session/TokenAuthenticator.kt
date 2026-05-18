package com.example.booknest.data.session

import com.example.booknest.data.constants.Auth
import com.example.booknest.data.constants.RetrofitConstants
import com.example.booknest.domain.usecase.auth.RefreshTokenUseCase
import com.example.booknest.port.SessionWriter
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val sessionWriter: SessionWriter,
) : Authenticator {

    private val loginURL = "${RetrofitConstants.BASE_URL}${Auth.LOGIN}"
    private val refreshTokenURL = "${RetrofitConstants.BASE_URL}${Auth.REFRESH_STRING_CONCAT}"
    private val logoutURL = "${RetrofitConstants.BASE_URL}${Auth.LOGOUT}"

    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking {
            getRequest(response)
        }
    }

    private suspend fun getRequest(response: Response): Request? {
        val requestUrl = response.request.url.toString()

        if (requestUrl.endsWith("/login") || requestUrl == loginURL) {
            return null
        }

        if (requestUrl == refreshTokenURL) {
            sessionWriter.logout()
            return null
        }

        if (requestUrl == logoutURL) {
            return null
        }

        return refreshTokenUseCase().fold(
            onSuccess = { tokenResponse ->
                val requestBuilder = response.request.newBuilder()
                val token = tokenResponse.accessToken

                sessionWriter.updateTokens(
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken
                )

                requestBuilder.header("Authorization", "Bearer $token")
                requestBuilder.build()
            },
            onFailure = {
                sessionWriter.logout()
                null
            }
        )
    }
}
