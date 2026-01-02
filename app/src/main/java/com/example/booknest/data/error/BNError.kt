package com.example.booknest.data.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class BNError : Exception() {
    @Serializable
    data class Generic(
        @SerialName("message")
        val messageString: String?,
        @SerialName("error")
        val error: String?,
        @SerialName("statusCode")
        val statusCode: Int?,
        val stringRes: Int? = null
    ) : BNError() {
        override val message: String
            get() = messageString ?: error ?: "Unknown error"
    }
    
    @Serializable
    data class Network(
        @SerialName("message")
        val messageString: String?
    ) : BNError() {
        override val message: String
            get() = messageString ?: "Network error"
    }
    
    @Serializable
    data class Unauthorized(
        @SerialName("message")
        val messageString: String?
    ) : BNError() {
        override val message: String
            get() = messageString ?: "Unauthorized"
    }
}

