package com.example.booknest.domain.repository

import com.example.booknest.domain.model.response.DownloadFileResponse
import com.example.booknest.domain.model.response.FileMetadataResponse
import com.example.booknest.domain.model.response.UploadFileResponse
import okhttp3.MultipartBody

interface FilesRepository {
    suspend fun uploadFile(file: MultipartBody.Part): Result<UploadFileResponse>
    suspend fun getFileDownloadUrl(key: String): Result<DownloadFileResponse>
    suspend fun deleteFile(key: String): Result<Unit>
    suspend fun getFileMetadata(key: String): Result<FileMetadataResponse>
}
