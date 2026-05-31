package com.example.booknest.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class PaginateMeta(
    val itemsPerPage: Int? = null,
    val totalItems: Int? = null,
    val currentPage: Int? = null,
    val totalPages: Int? = null,
)

@Serializable
data class PaginatedResponse<T>(
    val data: List<T> = emptyList(),
    val meta: PaginateMeta? = null,
)
