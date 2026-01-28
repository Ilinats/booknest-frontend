package com.example.booknest.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class FriendRequestResponse(
    val id: String,
    val requesterId: String,
    val addresseeId: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val requester: UserResponse? = null,
    val addressee: UserResponse? = null
)

@Serializable
data class FriendshipStatusResponse(
    val status: String?,
    val isRequester: Boolean
)

@Serializable
data class UserSearchResultItemResponse(
    val user: UserResponse,
    val friendshipStatus: String? = null,
    val isRequester: Boolean = false
)

