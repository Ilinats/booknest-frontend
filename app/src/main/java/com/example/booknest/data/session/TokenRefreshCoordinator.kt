package com.example.booknest.data.session

import com.example.booknest.data.constants.Auth
import com.example.booknest.data.constants.RetrofitConstants
import com.example.booknest.domain.usecase.auth.RefreshTokenUseCase
import com.example.booknest.port.AuthTokenAccessor
import com.example.booknest.port.SessionWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Request
import okhttp3.Response

class TokenRefreshCoordinator(
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val sessionWriter: SessionWriter,
    private val authTokens: AuthTokenAccessor,
) {
    private val refreshMutex = Mutex()

    fun refreshAccessToken(response: Response): Request? = runBlocking(Dispatchers.Default) {
        if (response.code != 401) return@runBlocking null
        if (responseCount(response) >= 2) return@runBlocking null

        val requestUrl = response.request.url.toString()
        if (requestUrl == refreshTokenUrl) {
            sessionWriter.logout()
            return@runBlocking null
        }
        if (requestUrl == loginUrl || requestUrl == logoutUrl) {
            return@runBlocking null
        }

        refreshMutex.withLock {
            refreshLocked(response.request)
        }
    }

    private suspend fun refreshLocked(failedRequest: Request): Request? {
        val failedToken = failedRequest.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()
            .orEmpty()

        val currentToken = authTokens.getToken()
        if (currentToken.isNotEmpty() && currentToken != failedToken) {
            return failedRequest.newBuilder()
                .header("Authorization", "Bearer $currentToken")
                .build()
        }

        return refreshTokenUseCase().fold(
            onSuccess = { tokenResponse ->
                sessionWriter.updateTokens(
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken,
                )
                failedRequest.newBuilder()
                    .header("Authorization", "Bearer ${tokenResponse.accessToken}")
                    .build()
            },
            onFailure = {
                sessionWriter.logout()
                null
            },
        )
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    companion object {
        private val refreshTokenUrl = "${RetrofitConstants.BASE_URL}${Auth.REFRESH_STRING_CONCAT}"
        private val loginUrl = "${RetrofitConstants.BASE_URL}${Auth.LOGIN}"
        private val logoutUrl = "${RetrofitConstants.BASE_URL}${Auth.LOGOUT}"
    }
}
