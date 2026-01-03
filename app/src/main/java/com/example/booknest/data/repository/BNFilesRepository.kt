package com.example.booknest.data.repository

import com.example.booknest.data.datasource.FilesDataSource
import com.example.booknest.data.datasource.resultBody
import com.example.booknest.domain.model.response.DownloadFileResponse
import com.example.booknest.domain.model.response.FileMetadataResponse
import com.example.booknest.domain.model.response.UploadFileResponse
import com.example.booknest.domain.repository.FilesRepository
import okhttp3.MultipartBody

class BNFilesRepository(private val filesDataSource: FilesDataSource) : FilesRepository {

    override suspend fun uploadFile(file: MultipartBody.Part): Result<UploadFileResponse> {
        return resultBody(filesDataSource.uploadFile(file))
    }

    override suspend fun getFileDownloadUrl(key: String): Result<DownloadFileResponse> {
        return resultBody(filesDataSource.getFileDownloadUrl(key))
    }

    override suspend fun deleteFile(key: String): Result<Unit> {
        return resultBody(filesDataSource.deleteFile(key))
    }

    override suspend fun getFileMetadata(key: String): Result<FileMetadataResponse> {
        return resultBody(filesDataSource.getFileMetadata(key))
    }
}

