package com.example.booknest.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateApplicationRequest(
    val bookId: String,
    val applicationMessage: String? = null
)

@Serializable
data class UpdateApplicationRequest(
    val applicationMessage: String? = null
)

@Serializable
data class UpdateApplicationCompleteRequest(
    val applicationMessage: String? = null,
    val status: String? = null,
    val authorNotes: String? = null,
    val readingStatus: String? = null,
    val markCopySent: Boolean? = null,
    val markCopyReceived: Boolean? = null
)

@Serializable
data class UpdateReadingStatusRequest(
    val readingStatus: String
)

@Serializable
data class ApproveApplicationRequest(
    val authorNotes: String? = null
)

@Serializable
data class RejectApplicationRequest(
    val authorNotes: String? = null
)

@Serializable
data class BulkActionRequest(
    val applicationIds: List<String>,
    val action: String,
    val authorNotes: String? = null
)

