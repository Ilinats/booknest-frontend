package com.example.booknest.domain.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenreResponse(
    val id: Int,
    val name: String,
    val description: String? = null,
    val colorCode: String? = null,
    val icon: String? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null
)

@Serializable
data class GenrePreferenceResponse(
    val id: String,
    val userId: String? = null,
    @SerialName("genreId")
    val genreId: Int? = null,
    val preferenceLevel: Int? = null,
    val genre: GenreResponse? = null,
    val createdAt: String? = null
) {
    val resolvedGenreId: Int
        get() = genreId ?: genre?.id ?: 0
}

