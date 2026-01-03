package com.example.booknest.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceTokenRequest(
    val token: String,
    val deviceType: String? = "android",
    val deviceId: String? = null,
    val appVersion: String? = null
)

@Serializable
data class UpdateDeviceTokenRequest(
    val isActive: Boolean? = null
)

