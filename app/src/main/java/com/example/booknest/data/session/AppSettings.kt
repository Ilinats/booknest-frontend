package com.example.booknest.data.session

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val token: String = "",
    val refreshToken: String = "",
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val userType: String = ""
)

