package com.example.booknest.data.datasource

import com.example.booknest.data.service.FilesService
import com.example.booknest.domain.model.response.DownloadFileResponse
import com.example.booknest.domain.model.response.FileMetadataResponse
import com.example.booknest.domain.model.response.UploadFileResponse
import okhttp3.MultipartBody

class BNFilesDataSource(private val filesService: FilesService) : FilesDataSource {

    override suspend fun uploadFile(file: MultipartBody.Part): Result<UploadFileResponse> {
        return requestBody(filesService.uploadFile(file))
    }

    override suspend fun getFileDownloadUrl(key: String): Result<DownloadFileResponse> {
        return requestBody(filesService.getFileDownloadUrl(key))
    }

    override suspend fun deleteFile(key: String): Result<Unit> {
        return requestBodyUnit(filesService.deleteFile(key))
    }

    override suspend fun getFileMetadata(key: String): Result<FileMetadataResponse> {
        return requestBody(filesService.getFileMetadata(key))
    }
}

