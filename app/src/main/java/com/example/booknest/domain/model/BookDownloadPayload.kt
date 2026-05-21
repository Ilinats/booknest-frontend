package com.example.booknest.domain.model

import com.example.booknest.domain.model.response.DownloadBookResponse
import okhttp3.ResponseBody

/**
 * Result of [GET /api/books/{bookId}/download]: JSON with a presigned URL for some formats,
 * or a raw PDF/EPUB body fingerprinted per reader (see booknest-backend books controller).
 */
sealed class BookDownloadPayload {
    data class PresignedUrl(val data: DownloadBookResponse) : BookDownloadPayload()

    data class DirectStream(
        val body: ResponseBody,
        val displayFileName: String,
        val extension: String
    ) : BookDownloadPayload()
}
