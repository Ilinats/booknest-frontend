package com.example.booknest.data.service

import com.example.booknest.data.constants.Files
import com.example.booknest.data.constants.PathConstants
import com.example.booknest.domain.model.response.DownloadFileResponse
import com.example.booknest.domain.model.response.FileMetadataResponse
import com.example.booknest.domain.model.response.UploadFileResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface FilesService {
    @Multipart
    @POST(Files.UPLOAD)
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<UploadFileResponse>

    @GET(Files.DOWNLOAD)
    suspend fun getFileDownloadUrl(
        @Path(PathConstants.KEY) key: String
    ): Response<DownloadFileResponse>

    @DELETE(Files.DELETE)
    suspend fun deleteFile(
        @Path(PathConstants.KEY) key: String
    ): Response<Unit>

    @GET(Files.METADATA)
    suspend fun getFileMetadata(
        @Path(PathConstants.KEY) key: String
    ): Response<FileMetadataResponse>
}

