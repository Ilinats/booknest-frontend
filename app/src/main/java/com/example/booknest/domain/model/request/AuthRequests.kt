package com.example.booknest.domain.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val identifier: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val userType: String,
    val firstName: String,
    val lastName: String,
    val birthDate: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val address: AddressDto? = null
)

@Serializable
data class AddressDto(
    @SerialName("streetAddress")
    val streetAddress: String,
    val city: String,
    @SerialName("postalCode")
    val postalCode: String,
    val country: String? = null,
    @SerialName("isPrimary")
    val isPrimary: Boolean? = true
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class VerifyEmailRequest(
    val code: String
)

@Serializable
data class ResendVerificationRequest(
    val email: String
)

@Serializable
data class RequestPasswordResetRequest(
    val email: String
)

@Serializable
data class ResetPasswordRequest(
    val code: String,
    val newPassword: String
)

