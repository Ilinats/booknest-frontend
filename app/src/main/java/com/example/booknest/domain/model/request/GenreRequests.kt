package com.example.booknest.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateGenreRequest(
    val name: String,
    val description: String? = null,
    val colorCode: String? = null,
    val icon: String? = null,
    val isActive: Boolean? = null
)

@Serializable
data class UpsertPreferenceRequest(
    val genreId: Int
)

@Serializable
data class DeleteGenrePreferenceRequest(
    val genreId: Int
)

