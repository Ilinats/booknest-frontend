package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.request.ApproveApplicationRequest
import com.example.booknest.domain.model.request.UpdateApplicationCompleteRequest
import com.example.booknest.domain.model.request.BulkActionRequest
import com.example.booknest.domain.model.request.CreateApplicationRequest
import com.example.booknest.domain.model.request.RejectApplicationRequest
import com.example.booknest.domain.model.request.UpdateApplicationRequest
import com.example.booknest.domain.model.request.UpdateReadingStatusRequest
import com.example.booknest.domain.model.response.ApplicationCheckResponse
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.repository.ApplicationsRepository
import com.example.booknest.domain.usecase.applications.GetMyApplicationsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ReadingStatus(val value: String) {
    NOT_STARTED("not_started"),
    CURRENTLY_READING("currently_reading"),
    FOR_REVIEW("for_review")
}

enum class ApplicationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    WITHDRAWN
}

class ApplicationViewModel(
    private val getMyApplicationsUseCase: GetMyApplicationsUseCase,
    private val applicationsRepository: ApplicationsRepository
) : ViewModel() {

    private val _myApplications = MutableStateFlow<List<ApplicationResponse>>(emptyList())
    val myApplications: StateFlow<List<ApplicationResponse>> = _myApplications

    private val _readingProgress = MutableStateFlow<List<ApplicationResponse>>(emptyList())
    val readingProgress: StateFlow<List<ApplicationResponse>> = _readingProgress

    private val _applicationCheck = MutableStateFlow<ApplicationCheckResponse?>(null)
    val applicationCheck: StateFlow<ApplicationCheckResponse?> = _applicationCheck

    private val _bookApplications = MutableStateFlow<List<ApplicationResponse>>(emptyList())
    val bookApplications: StateFlow<List<ApplicationResponse>> = _bookApplications

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent

    fun loadMyApplications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = getMyApplicationsUseCase()
                result
                    .onSuccess { applications ->
                        _myApplications.value = applications
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to load applications")
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
                val result = applicationsRepository.checkApplication(bookId)
                result
                    .onSuccess { check ->
                        _applicationCheck.value = check
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to check application status")
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error checking application status: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getApplication(applicationId: String) =
        applicationsRepository.getApplication(applicationId)

    fun loadReadingProgress() {
        viewModelScope.launch {
            try {
                val result = applicationsRepository.getReadingProgress()
                result.onSuccess { progress ->
                    _readingProgress.value = progress
                }
            } catch (e: Exception) {
                _readingProgress.value = emptyList()
            }
        }
    }

    fun createApplication(bookId: String, message: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = CreateApplicationRequest(
                    bookId = bookId,
                    applicationMessage = message
                )
                val result = applicationsRepository.createApplication(request)
                result
                    .onSuccess {
                        _snackbarEvent.emit("Application submitted successfully!")
                        checkApplication(bookId)
                        loadMyApplications()
                    }
                    .onFailure { e ->
                        val errorMessage = when (e) {
                            is com.example.booknest.data.error.BNError.Generic -> {
                                when {
                                    e.messageString?.contains(
                                        "already applied",
                                        ignoreCase = true
                                    ) == true ||
                                            e.messageString?.contains(
                                                "APPLICATION_ALREADY_EXISTS",
                                                ignoreCase = true
                                            ) == true -> {
                                        "You have already applied for this book"
                                    }

                                    e.messageString?.contains(
                                        "email verification",
                                        ignoreCase = true
                                    ) == true -> {
                                        "Please verify your email address before applying"
                                    }

                                    e.messageString?.contains(
                                        "address",
                                        ignoreCase = true
                                    ) == true -> {
                                        "Please add your address in your profile to apply for physical copies"
                                    }

                                    else -> e.messageString ?: e.message
                                    ?: "Failed to submit application"
                                }
                            }

                            else -> {
                                val msg = e.message ?: "Failed to submit application"
                                if (msg.contains("\"message\"") && msg.contains("already applied")) {
                                    "You have already applied for this book"
                                } else {
                                    msg
                                }
                            }
                        }
                        _snackbarEvent.emit(errorMessage)
                    }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is com.example.booknest.data.error.BNError.Generic -> {
                        e.messageString ?: e.message ?: "Error submitting application"
                    }

                    else -> {
                        val msg = e.message ?: "Error submitting application"
                        if (msg.contains("\"message\"") && msg.contains("already applied")) {
                            "You have already applied for this book"
                        } else {
                            msg
                        }
                    }
                }
                _snackbarEvent.emit(errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateApplication(applicationId: String, message: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = UpdateApplicationRequest(applicationMessage = message)
                val result = applicationsRepository.updateApplication(applicationId, request)
                result
                    .onSuccess {
                        _snackbarEvent.emit("Application updated successfully!")
                        loadMyApplications()
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to update application")
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
                val result = applicationsRepository.withdrawApplication(applicationId)
                result
                    .onSuccess {
                        _snackbarEvent.emit("Application withdrawn successfully!")
                        loadMyApplications()
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to withdraw application")
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
                val result = applicationsRepository.markCopyReceived(applicationId)
                result
                    .onSuccess {
                        _snackbarEvent.emit("Copy marked as received!")
                        loadMyApplications()
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to mark copy as received")
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
                val request = UpdateReadingStatusRequest(readingStatus = status.value)
                val result = applicationsRepository.updateReadingStatus(applicationId, request)
                result
                    .onSuccess {
                        _snackbarEvent.emit("Reading status updated!")
                        loadMyApplications()
                        loadReadingProgress()
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to update reading status")
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error updating reading status: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadBookApplications(bookId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = applicationsRepository.getBookApplications(bookId)
                result
                    .onSuccess { apps ->
                        _bookApplications.value = apps
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to load book applications")
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
                val request = UpdateApplicationCompleteRequest(
                    status = "approved",
                    authorNotes = authorNotes
                )
                val result =
                    applicationsRepository.updateApplicationComplete(applicationId, request)
                result
                    .onSuccess {
                        _snackbarEvent.emit("Application approved!")
                        val currentBookId = _bookApplications.value.firstOrNull()?.bookId
                        if (currentBookId != null) {
                            loadBookApplications(currentBookId)
                        }
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to approve application")
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
                val request = UpdateApplicationCompleteRequest(
                    status = "rejected",
                    authorNotes = authorNotes
                )
                val result =
                    applicationsRepository.updateApplicationComplete(applicationId, request)
                result
                    .onSuccess {
                        _snackbarEvent.emit("Application rejected!")
                        val currentBookId = _bookApplications.value.firstOrNull()?.bookId
                        if (currentBookId != null) {
                            loadBookApplications(currentBookId)
                        }
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to reject application")
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error rejecting application: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun bulkActionApplications(
        applicationIds: List<String>,
        action: String,
        authorNotes: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentBookId = _bookApplications.value.firstOrNull()?.bookId
                if (currentBookId == null) {
                    _snackbarEvent.emit("Error: Book ID not found")
                    _isLoading.value = false
                    return@launch
                }

                val request = BulkActionRequest(
                    applicationIds = applicationIds,
                    action = action,
                    authorNotes = authorNotes
                )
                val result = applicationsRepository.bulkActionApplications(currentBookId, request)
                result
                    .onSuccess {
                        _snackbarEvent.emit("Bulk action completed!")
                        loadBookApplications(currentBookId)
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to perform bulk action")
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
                val result = applicationsRepository.markCopySent(applicationId)
                result
                    .onSuccess {
                        _snackbarEvent.emit("Copy marked as sent!")
                        val currentBookId = _bookApplications.value.firstOrNull()?.bookId
                        if (currentBookId != null) {
                            loadBookApplications(currentBookId)
                        }
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to mark copy as sent")
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error marking copy as sent: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearApplicationCheck() {
        _applicationCheck.value = null
    }

    fun mapStringToApplicationStatus(status: String?): ApplicationStatus {
        return when (status?.lowercase()) {
            "pending" -> ApplicationStatus.PENDING
            "approved" -> ApplicationStatus.APPROVED
            "rejected" -> ApplicationStatus.REJECTED
            "withdrawn" -> ApplicationStatus.WITHDRAWN
            else -> ApplicationStatus.PENDING
        }
    }

    fun mapStringToReadingStatus(status: String?): ReadingStatus {
        return when (status?.lowercase()) {
            "not_started" -> ReadingStatus.NOT_STARTED
            "currently_reading" -> ReadingStatus.CURRENTLY_READING
            "for_review" -> ReadingStatus.FOR_REVIEW
            "reviewed" -> ReadingStatus.FOR_REVIEW
            else -> ReadingStatus.NOT_STARTED
        }
    }
}
