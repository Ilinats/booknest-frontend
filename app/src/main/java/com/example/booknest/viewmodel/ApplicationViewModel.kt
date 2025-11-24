package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.AuthManager
import com.example.booknest.network.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ApplicationViewModel(private val authManager: AuthManager) : ViewModel() {

    // Reader state
    private val _myApplications = MutableStateFlow<List<Application>>(emptyList())
    val myApplications: StateFlow<List<Application>> = _myApplications

    private val _readingProgress = MutableStateFlow<List<Application>>(emptyList())
    val readingProgress: StateFlow<List<Application>> = _readingProgress

    private val _applicationCheck = MutableStateFlow<ApplicationCheckResponse?>(null)
    val applicationCheck: StateFlow<ApplicationCheckResponse?> = _applicationCheck

    // Author state
    private val _bookApplications = MutableStateFlow<List<Application>>(emptyList())
    val bookApplications: StateFlow<List<Application>> = _bookApplications

    // Common state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent

    // Reader operations
    fun loadMyApplications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getMyApplications()
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    println("DEBUG: Applications API response - success: ${apiResponse.success}, data size: ${apiResponse.data?.size ?: 0}")
                    if (apiResponse.success) {
                        // Applications are already properly mapped by the serializers
                        val applications = apiResponse.data ?: emptyList()
                        _myApplications.value = applications
                        println("DEBUG: Applications loaded: ${applications.size} applications")
                        applications.forEach { app ->
                            println("DEBUG: Application ${app.id} - status: ${app.status}, book: ${app.bookTitle}, author: ${app.authorName}")
                        }
                    } else {
                        println("DEBUG: Applications API error: ${apiResponse.message}")
                        _snackbarEvent.emit(apiResponse.message ?: "Failed to load applications")
                    }
                } else {
                    println("DEBUG: Applications API error: ${response.code()} - ${response.message()}")
                    _snackbarEvent.emit("Failed to load applications: ${response.message()}")
                }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error loading applications: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkApplication(bookId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.checkApplication(bookId)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _applicationCheck.value = apiResponse.data
                    } else {
                        _snackbarEvent.emit(apiResponse.message ?: "Failed to check application status")
                    }
                } else {
                    _snackbarEvent.emit("Failed to check application status: ${response.message()}")
                }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error checking application status: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadReadingProgress() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getReadingProgress()
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _readingProgress.value = apiResponse.data ?: emptyList()
                    }
                    // Silently fail - reading progress is not critical
                }
                // Silently fail on error - reading progress is not critical
            } catch (e: Exception) {
                // Silently fail - reading progress is not critical for the main flow
                // Just set empty list to avoid errors
                _readingProgress.value = emptyList()
            }
        }
    }

    fun createApplication(bookId: String, message: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.createApplication(
                    CreateApplicationDto(
                        bookId = bookId,
                        applicationMessage = message
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _snackbarEvent.emit(apiResponse.message ?: "Application submitted successfully!")
                        loadMyApplications() // Refresh the list
                    } else {
                        // Backend returned success: false - this could be email verification requirement
                        val errorMessage = apiResponse.message ?: "Failed to submit application"
                        _snackbarEvent.emit(errorMessage)
                    }
                } else {
                    // Handle HTTP errors
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            "Failed to submit application: ${response.message()}"
                        } catch (e: Exception) {
                            "Failed to submit application: ${response.message()}"
                        }
                    } else {
                        "Failed to submit application: ${response.message()}"
                    }
                    _snackbarEvent.emit(errorMessage)
                }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error submitting application: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateApplication(applicationId: String, message: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.updateApplication(
                    applicationId,
                    UpdateApplicationDto(applicationMessage = message)
                )
                if (response.isSuccessful) {
                    _snackbarEvent.emit("Application updated successfully!")
                    loadMyApplications()
                } else {
                    _snackbarEvent.emit("Failed to update application: ${response.message()}")
                }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error updating application: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun withdrawApplication(applicationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.withdrawApplication(applicationId)
                if (response.isSuccessful) {
                    _snackbarEvent.emit("Application withdrawn successfully!")
                    loadMyApplications()
                } else {
                    _snackbarEvent.emit("Failed to withdraw application: ${response.message()}")
                }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error withdrawing application: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markCopyReceived(applicationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.markCopyReceived(applicationId)
                if (response.isSuccessful) {
                    _snackbarEvent.emit("Copy marked as received!")
                    loadMyApplications()
                } else {
                    _snackbarEvent.emit("Failed to mark copy as received: ${response.message()}")
                }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error marking copy as received: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateReadingStatus(applicationId: String, status: ReadingStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.updateReadingStatus(
                    applicationId,
                    UpdateReadingStatusDto(readingStatus = status.value)
                )
                if (response.isSuccessful) {
                    _snackbarEvent.emit("Reading status updated!")
                    loadMyApplications()
                    // Load reading progress in background (non-blocking)
                    loadReadingProgress()
                } else {
                    _snackbarEvent.emit("Failed to update reading status: ${response.message()}")
                }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error updating reading status: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Author operations
    fun loadBookApplications(bookId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getBookApplications(bookId)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _bookApplications.value = apiResponse.data ?: emptyList()
                    } else {
                        _snackbarEvent.emit(apiResponse.message ?: "Failed to load book applications")
                    }
                } else {
                    _snackbarEvent.emit("Failed to load book applications: ${response.message()}")
                }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error loading book applications: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveApplication(applicationId: String, authorNotes: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.approveApplication(
                    applicationId,
                    ApproveApplicationDto(authorNotes = authorNotes)
                )
                if (response.isSuccessful) {
                    _snackbarEvent.emit("Application approved!")
                    // Refresh the current book's applications
                    val currentBookId = _bookApplications.value.firstOrNull()?.bookId
                    if (currentBookId != null) {
                        loadBookApplications(currentBookId)
                    }
                } else {
                    _snackbarEvent.emit("Failed to approve application: ${response.message()}")
                }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error approving application: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rejectApplication(applicationId: String, authorNotes: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.rejectApplication(
                    applicationId,
                    RejectApplicationDto(authorNotes = authorNotes)
                )
                if (response.isSuccessful) {
                    _snackbarEvent.emit("Application rejected!")
                    // Refresh the current book's applications
                    val currentBookId = _bookApplications.value.firstOrNull()?.bookId
                    if (currentBookId != null) {
                        loadBookApplications(currentBookId)
                    }
                } else {
                    _snackbarEvent.emit("Failed to reject application: ${response.message()}")
                }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error rejecting application: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun bulkActionApplications(applicationIds: List<String>, action: String, authorNotes: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.bulkActionApplications(
                    BulkActionDto(
                        applicationIds = applicationIds,
                        action = action,
                        authorNotes = authorNotes
                    )
                )
                if (response.isSuccessful) {
                    _snackbarEvent.emit("Bulk action completed!")
                    // Refresh the current book's applications
                    val currentBookId = _bookApplications.value.firstOrNull()?.bookId
                    if (currentBookId != null) {
                        loadBookApplications(currentBookId)
                    }
                } else {
                    _snackbarEvent.emit("Failed to perform bulk action: ${response.message()}")
                }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error performing bulk action: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markCopySent(applicationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.markCopySent(applicationId)
                if (response.isSuccessful) {
                    _snackbarEvent.emit("Copy marked as sent!")
                    // Refresh the current book's applications
                    val currentBookId = _bookApplications.value.firstOrNull()?.bookId
                    if (currentBookId != null) {
                        loadBookApplications(currentBookId)
                    }
                } else {
                    _snackbarEvent.emit("Failed to mark copy as sent: ${response.message()}")
                }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error marking copy as sent: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Helper functions for mapping string status to enums
    private fun mapStringToApplicationStatus(status: String?): ApplicationStatus {
        return when (status?.lowercase()) {
            "pending" -> ApplicationStatus.PENDING
            "approved" -> ApplicationStatus.APPROVED
            "rejected" -> ApplicationStatus.REJECTED
            "withdrawn" -> ApplicationStatus.WITHDRAWN
            else -> ApplicationStatus.PENDING
        }
    }
    
    private fun mapStringToReadingStatus(status: String?): ReadingStatus {
        return when (status?.lowercase()) {
            "not_started" -> ReadingStatus.NOT_STARTED
            "currently_reading" -> ReadingStatus.CURRENTLY_READING
            "for_review" -> ReadingStatus.FOR_REVIEW
            "reviewed" -> ReadingStatus.REVIEWED
            else -> ReadingStatus.NOT_STARTED
        }
    }
}

class ApplicationViewModelFactory(private val authManager: AuthManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ApplicationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ApplicationViewModel(authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
