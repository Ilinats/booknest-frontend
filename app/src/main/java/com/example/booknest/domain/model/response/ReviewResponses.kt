package com.example.booknest.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ReviewResponse(
    val id: String,
    val applicationId: String,
    val rating: Double,
    val reviewType: String? = null,
    val reviewContent: String? = null,
    val reviewUrls: List<String>? = null,
    val isPublic: Boolean,
    val wordCount: Int? = null,
    val createdAt: String,
    val updatedAt: String,
    val application: ApplicationResponse? = null
)

