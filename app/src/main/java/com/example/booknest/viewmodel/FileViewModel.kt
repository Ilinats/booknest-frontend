package com.example.booknest.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.network.ApiService
import com.example.booknest.network.RetrofitInstance
import com.example.booknest.utils.FileDownloadManager
import com.example.booknest.utils.FileUploadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File

data class FileUiState(
    val isLoading: Boolean = false,
    val uploadProgress: Float = 0f,
    val downloadProgress: Float = 0f,
    val error: String? = null,
    val successMessage: String? = null,
    val downloadedBooks: List<File> = emptyList()
)

class FileViewModel(
    private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FileUiState())
    val uiState: StateFlow<FileUiState> = _uiState.asStateFlow()
    
    private val apiService: ApiService = RetrofitInstance.api
    private val uploadManager = FileUploadManager(context)
    private val downloadManager = FileDownloadManager(context)
    
    fun uploadBookFile(bookId: String, file: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Validate file
                val validationResult = uploadManager.validateFile(file)
                if (validationResult is FileUploadManager.ValidationResult.Error) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = validationResult.message
                    )
                    return@launch
                }
                
                // Create multipart body
                val multipartBody = uploadManager.createMultipartBody(file)
                
                // Upload file
                val response = apiService.uploadBookFile(bookId, multipartBody)
                
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "File uploaded successfully"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = apiResponse.message ?: "Upload failed"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Upload failed: ${response.code()}"
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
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Get download URL
                val response = apiService.getBookDownloadUrl(bookId)
                
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success && apiResponse.data != null) {
                        val downloadData = apiResponse.data!!
                        
                        // Download file
                        val result = downloadManager.downloadBook(
                            bookId = bookId,
                            downloadUrl = downloadData.downloadUrl,
                            fileName = downloadData.fileName,
                            fileType = downloadData.fileType
                        )
                        
                        result.fold(
                            onSuccess = { file ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    successMessage = "Book downloaded successfully",
                                    downloadedBooks = downloadManager.getDownloadedBooks()
                                )
                            },
                            onFailure = { exception ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = "Download failed: ${exception.message}"
                                )
                            }
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = apiResponse.message ?: "Download failed"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Download failed: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Download failed: ${e.message}"
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
    
    fun isBookDownloaded(bookId: String): Boolean {
        return downloadManager.isBookDownloaded(bookId)
    }
    
    fun getDownloadedBook(bookId: String): File? {
        return downloadManager.getDownloadedBook(bookId)
    }
}
