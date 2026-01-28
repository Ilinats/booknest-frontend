package com.example.booknest.data.service

import com.example.booknest.data.constants.Auth
import com.example.booknest.domain.model.request.LoginRequest
import com.example.booknest.domain.model.request.RefreshTokenRequest
import com.example.booknest.domain.model.request.RegisterRequest
import com.example.booknest.domain.model.request.RequestPasswordResetRequest
import com.example.booknest.domain.model.request.ResendVerificationRequest
import com.example.booknest.domain.model.request.ResetPasswordRequest
import com.example.booknest.domain.model.request.VerifyEmailRequest
import com.example.booknest.domain.model.response.AuthTokenResponse
import com.example.booknest.domain.model.response.LoginResponse
import com.example.booknest.domain.model.response.MessageResponse
import com.example.booknest.domain.model.response.VerifyEmailDataResponse
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

@Serializable
data class AvailabilityResponse(
    val available: Boolean,
    val message: String
)

interface AuthService {
    @POST(Auth.LOGIN)
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST(Auth.REGISTER)
    suspend fun register(@Body body: RegisterRequest): Response<AuthTokenResponse>

    @POST(Auth.REFRESH)
    suspend fun refresh(@Body body: RefreshTokenRequest): Response<AuthTokenResponse>

    @POST(Auth.LOGOUT)
    suspend fun logout(@Body body: RefreshTokenRequest): Response<MessageResponse>

    @POST(Auth.VERIFY_EMAIL)
    suspend fun verifyEmail(@Body body: VerifyEmailRequest): Response<VerifyEmailDataResponse>

    @POST(Auth.RESEND_VERIFICATION)
    suspend fun resendVerification(@Body body: ResendVerificationRequest): Response<MessageResponse>

    @POST(Auth.REQUEST_PASSWORD_RESET)
    suspend fun requestPasswordReset(@Body body: RequestPasswordResetRequest): Response<MessageResponse>

    @POST(Auth.RESET_PASSWORD)
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<MessageResponse>

    @GET(Auth.CHECK_USERNAME)
    suspend fun checkUsernameAvailability(@Query("username") username: String): Response<AvailabilityResponse>

    @GET(Auth.CHECK_EMAIL)
    suspend fun checkEmailAvailability(@Query("email") email: String): Response<AvailabilityResponse>
}

