package com.example.booknest.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.datasource.extractErrorMessage
import com.example.booknest.data.error.BNError
import com.example.booknest.domain.usecase.files.GetBookDownloadUrlUseCase
import com.example.booknest.domain.usecase.files.UploadBookFileUseCase
import com.example.booknest.utils.FileDownloadManager
import com.example.booknest.utils.FileUploadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class FileUiState(
    val isLoading: Boolean = false,
    val uploadProgress: Float = 0f,
    val downloadProgress: Float = 0f,
    val error: String? = null,
    val successMessage: String? = null,
    val downloadingMessage: String? = null,
    val downloadedBooks: List<File> = emptyList()
)

class FileViewModel(
    private val context: Context,
    private val uploadBookFileUseCase: UploadBookFileUseCase,
    private val getBookDownloadUrlUseCase: GetBookDownloadUrlUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FileUiState())
    val uiState: StateFlow<FileUiState> = _uiState.asStateFlow()
    private val uploadManager = FileUploadManager(context)
    private val downloadManager = FileDownloadManager(context)

    fun uploadBookFile(bookId: String, file: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val validationResult = uploadManager.validateFile(file)
                if (validationResult is FileUploadManager.ValidationResult.Error) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = validationResult.message
                    )
                    return@launch
                }

                val multipartBody = uploadManager.createMultipartBody(file)

                val result = uploadBookFileUseCase(bookId, multipartBody)
                result
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "File uploaded successfully"
                        )
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = e.message ?: "Upload failed"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Upload failed: ${e.message}"
                )
            }
        }
    }

    fun downloadBook(bookId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                successMessage = null,
                downloadingMessage = "Downloading book...",
                downloadProgress = 0f
            )

            try {
                val downloadResult = getBookDownloadUrlUseCase(bookId)

                downloadResult
                    .onSuccess { downloadData ->
                        val fileType = downloadData.fileType ?: run {
                            val url = downloadData.downloadUrl
                            val fileName = downloadData.fileName
                            when {
                                url.contains(".epub") || fileName.contains(".epub") -> "epub"
                                url.contains(".pdf") || fileName.contains(".pdf") -> "pdf"
                                url.contains(".mobi") || fileName.contains(".mobi") -> "mobi"
                                else -> {
                                    url.substringAfterLast(".").substringBefore("?")
                                        .takeIf { it.length <= 5 } ?: "epub"
                                }
                            }
                        }

                        val result = downloadManager.downloadBook(
                            bookId = bookId,
                            downloadUrl = downloadData.downloadUrl,
                            fileName = downloadData.fileName,
                            fileType = fileType
                        )

                        result.fold(
                            onSuccess = { _ ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    downloadingMessage = null,
                                    successMessage = "Book downloaded successfully",
                                    downloadedBooks = downloadManager.getDownloadedBooks()
                                )
                            },
                            onFailure = { exception ->
                                val friendlyMessage = when (exception) {
                                    is BNError.Generic -> {
                                        exception.messageString?.takeIf { it.isNotBlank() }
                                            ?: "Download failed. Please try again later."
                                    }

                                    else -> {
                                        val extracted = extractErrorMessage(exception.message)
                                        if (extracted.isNotBlank() && !extracted.contains("{") && !extracted.contains(
                                                "statusCode"
                                            )
                                        ) {
                                            extracted
                                        } else {
                                            "Download failed. Please try again later."
                                        }
                                    }
                                }
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    downloadingMessage = null,
                                    error = friendlyMessage
                                )
                            }
                        )
                    }
                    .onFailure { e ->
                        val friendlyMessage = when (e) {
                            is BNError.Generic -> {
                                e.messageString?.takeIf { it.isNotBlank() }
                                    ?: "No file available for this book"
                            }

                            else -> {
                                val extracted = extractErrorMessage(e.message)
                                if (extracted.isNotBlank() && !extracted.contains("{") && !extracted.contains(
                                        "statusCode"
                                    )
                                ) {
                                    extracted
                                } else {
                                    "No file available for this book"
                                }
                            }
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            downloadingMessage = null,
                            error = friendlyMessage
                        )
                    }
            } catch (e: Exception) {
                val friendlyMessage = when (e) {
                    is BNError.Generic -> {
                        e.messageString?.takeIf { it.isNotBlank() }
                            ?: "Download failed. Please try again later."
                    }

                    else -> {
                        val extracted = extractErrorMessage(e.message)
                        if (extracted.isNotBlank() && !extracted.contains("{") && !extracted.contains(
                                "statusCode"
                            )
                        ) {
                            extracted
                        } else {
                            "Download failed. Please try again later."
                        }
                    }
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    downloadingMessage = null,
                    error = friendlyMessage
                )
            }
        }
    }

    fun deleteDownloadedBook(file: File) {
        viewModelScope.launch {
            val success = downloadManager.deleteDownloadedBook(file)
            if (success) {
                _uiState.value = _uiState.value.copy(
                    downloadedBooks = downloadManager.getDownloadedBooks(),
                    successMessage = "Book deleted successfully"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete book"
                )
            }
        }
    }

    fun loadDownloadedBooks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                downloadedBooks = downloadManager.getDownloadedBooks()
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun clearDownloadingMessage() {
        _uiState.value = _uiState.value.copy(downloadingMessage = null)
    }

    fun isBookDownloaded(bookId: String): Boolean {
        return downloadManager.isBookDownloaded(bookId)
    }

    fun getDownloadedBook(bookId: String): File? {
        return downloadManager.getDownloadedBook(bookId)
    }
}
