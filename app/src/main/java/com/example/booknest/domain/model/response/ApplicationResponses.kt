package com.example.booknest.domain.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApplicationResponse(
    val id: String,
    val status: String,
    val appliedAt: String,
    val respondedAt: String? = null,
    val applicationMessage: String? = null,
    val authorNotes: String? = null,
    val bookId: String,
    val bookTitle: String? = null,
    val bookCoverImageUrl: String? = null,
    val authorName: String? = null,
    val readingStatus: String,
    val readingStartedAt: String? = null,
    val readingCompletedAt: String? = null,
    val copySentAt: String? = null,
    val copyReceivedAt: String? = null,
    val reviewSubmittedAt: String? = null,
    val reader: ApplicationReaderResponse? = null,
    val book: BookResponse? = null,
    val review: ReviewResponse? = null
)

@Serializable
data class ApplicationReaderResponse(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    @SerialName("avatarUrl")
    val profilePictureUrl: String? = null,
    val addresses: List<ReaderAddressResponse>? = null
)

@Serializable
data class ApplicationCheckResponse(
    val hasApplied: Boolean,
    val application: ApplicationCheckApplicationResponse? = null
)

@Serializable
data class ApplicationCheckApplicationResponse(
    val id: String,
    val status: String,
    val appliedAt: String,
    val applicationMessage: String? = null,
    val authorNotes: String? = null,
    val respondedAt: String? = null,
    val book: ApplicationCheckBookResponse? = null
)

@Serializable
data class ApplicationCheckBookResponse(
    val id: String,
    val title: String,
    val authorId: String
)

@Serializable
data class BulkActionResponse(
    val updated: Int
)

