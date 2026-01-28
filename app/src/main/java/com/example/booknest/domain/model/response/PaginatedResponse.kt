package com.example.booknest.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val total: Int? = null,
    val skip: Int? = null,
    val take: Int? = null,
    val hasMore: Boolean? = null
)
