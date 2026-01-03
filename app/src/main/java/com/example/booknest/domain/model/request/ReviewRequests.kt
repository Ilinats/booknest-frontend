package com.example.booknest.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateReviewRequest(
    val applicationId: String,
    val rating: Int,
    val reviewType: String,
    val reviewContent: String? = null,
    val reviewUrls: List<String>? = null,
    val isPublic: Boolean = true
)

@Serializable
data class UpdateReviewRequest(
    val rating: Int? = null,
    val reviewType: String? = null,
    val reviewContent: String? = null,
    val reviewUrls: List<String>? = null,
    val isPublic: Boolean? = null
)

