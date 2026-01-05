package com.example.booknest.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class LoginDataResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class RegisterResponse(
    val user: UserResponse,
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class AuthTokenResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class VerifyEmailDataResponse(
    val message: String? = null,
    val user: UserResponse? = null
)

