package com.example.booknest.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class UploadFileResponse(
    val url: String,
    val key: String,
    val size: Long,
    val type: String,
    val originalName: String
)

@Serializable
data class DownloadFileResponse(
    val downloadUrl: String,
    val expiresIn: Int,
    val fileName: String,
    val fileSize: String,
    val fileType: String? = null
)

@Serializable
data class FileMetadataResponse(
    val key: String,
    val url: String,
    val size: Long,
    val type: String,
    val originalName: String,
    val uploadedAt: String,
    val lastModified: String
)

