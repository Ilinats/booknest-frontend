package com.example.booknest.domain.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateBookRequest(
    val title: String,
    val shortDescription: String? = null,
    val fullDescription: String? = null,
    val coverImageUrl: String? = null,
    val pageCount: Int? = null,
    val ageRating: String,
    val distributionType: String,
    val fileUrl: String? = null,
    val fileSize: Int? = null,
    val fileType: String? = null,
    val totalCopies: Int = 1,
    val availableCopies: Int? = null,
    val applicationDeadline: String,
    val reviewDeadline: String? = null,
    val selectionCriteria: String? = null,
    val selectionMethod: String = "author_selects",
    @SerialName("genres")
    val genreIds: List<Int>? = null,
    val seriesId: String? = null,
    val seriesOrder: Int? = null
)

@Serializable
data class UpdateBookRequest(
    val title: String? = null,
    val shortDescription: String? = null,
    val fullDescription: String? = null,
    val coverImageUrl: String? = null,
    val pageCount: Int? = null,
    val ageRating: String? = null,
    val distributionType: String? = null,
    val fileUrl: String? = null,
    val fileSize: Int? = null,
    val fileType: String? = null,
    val totalCopies: Int? = null,
    val availableCopies: Int? = null,
    val applicationDeadline: String? = null,
    val reviewDeadline: String? = null,
    val selectionCriteria: String? = null,
    val selectionMethod: String? = null,
    @SerialName("genres")
    val genreIds: List<Int>? = null,
    val seriesId: String? = null,
    val seriesOrder: Int? = null
)

@Serializable
data class CreateSeriesRequest(
    val name: String,
    val description: String? = null
)

@Serializable
data class UpdateSeriesRequest(
    val name: String? = null,
    val description: String? = null
)

