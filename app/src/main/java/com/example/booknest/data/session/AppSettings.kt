package com.example.booknest.data.session

import kotlinx.serialization.Serializable

/** Non-sensitive session metadata. Auth tokens live in [SecureTokenStore]. */
@Serializable
data class AppSettings(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val userType: String = "",
)
