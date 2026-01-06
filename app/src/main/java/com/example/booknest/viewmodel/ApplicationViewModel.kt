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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.booknest.ui.toast.GlobalToastHandler

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

    private val _overdueReviews = MutableStateFlow<List<ApplicationResponse>>(emptyList())
    val overdueReviews: StateFlow<List<ApplicationResponse>> = _overdueReviews.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading


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
                        GlobalToastHandler.showError(e.message ?: "Failed to load applications")
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError("Error loading applications: ${e.message}")
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
                        GlobalToastHandler.showError(e.message ?: "Failed to check application status")
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError("Error checking application status: ${e.message}")
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
                        GlobalToastHandler.showSuccess("Application submitted successfully!")
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
                        GlobalToastHandler.showError(errorMessage)
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
                GlobalToastHandler.showError(errorMessage)
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
                        GlobalToastHandler.showSuccess("Application withdrawn successfully!")
                        loadMyApplications()
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e.message ?: "Failed to withdraw application")
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError("Error withdrawing application: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markCopyReceived(applicationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val trimmedId = applicationId.trim()
                val result = applicationsRepository.markCopyReceived(trimmedId)
                result
                    .onSuccess {
                        GlobalToastHandler.showSuccess("Copy marked as received!")
                        loadMyApplications()
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e.message ?: "Failed to mark copy as received")
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError("Error marking copy as received: ${e.message}")
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
                        GlobalToastHandler.showSuccess("Reading status updated!")
                        loadMyApplications()
                        loadReadingProgress()
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e.message ?: "Failed to update reading status")
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError("Error updating reading status: ${e.message}")
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
                        GlobalToastHandler.showError(e.message ?: "Failed to load book applications")
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError("Error loading book applications: ${e.message}")
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
                        GlobalToastHandler.showSuccess("Application approved!")
                        val currentBookId = _bookApplications.value.firstOrNull()?.bookId
                        if (currentBookId != null) {
                            loadBookApplications(currentBookId)
                        }
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e.message ?: "Failed to approve application")
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError("Error approving application: ${e.message}")
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
                        GlobalToastHandler.showSuccess("Application rejected!")
                        val currentBookId = _bookApplications.value.firstOrNull()?.bookId
                        if (currentBookId != null) {
                            loadBookApplications(currentBookId)
                        }
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e.message ?: "Failed to reject application")
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError("Error rejecting application: ${e.message}")
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
                    GlobalToastHandler.showError("Error: Book ID not found")
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
                        GlobalToastHandler.showSuccess("Bulk action completed!")
                        loadBookApplications(currentBookId)
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e.message ?: "Failed to perform bulk action")
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError("Error performing bulk action: ${e.message}")
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
                        GlobalToastHandler.showSuccess("Copy marked as sent!")
                        val currentBookId = _bookApplications.value.firstOrNull()?.bookId
                        if (currentBookId != null) {
                            loadBookApplications(currentBookId)
                        }
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e.message ?: "Failed to mark copy as sent")
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError("Error marking copy as sent: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun runLottery(bookId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = applicationsRepository.runLotterySelection(bookId)
                result
                    .onSuccess { lotteryResult ->
                        val message = "Lottery completed: ${lotteryResult.approved} approved, ${lotteryResult.rejected} rejected"
                        GlobalToastHandler.showSuccess(message)
                        loadBookApplications(bookId)
                    }
                    .onFailure { e ->
                        val errorMessage = when {
                            e.message?.contains("deadline", ignoreCase = true) == true -> {
                                "Application deadline has not passed yet. Lottery can only be run after the deadline."
                            }
                            e.message?.contains("already been run", ignoreCase = true) == true -> {
                                "Lottery has already been run for this book."
                            }
                            e.message?.contains("lottery selection", ignoreCase = true) == true -> {
                                "This book does not use lottery selection method."
                            }
                            else -> e.message ?: "Failed to run lottery"
                        }
                        GlobalToastHandler.showError(errorMessage)
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError("Error running lottery: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadOverdueReviews() {
        viewModelScope.launch {
            try {
                val result = applicationsRepository.getOverdueReviews()
                result
                    .onSuccess { applications ->
                        _overdueReviews.value = applications
                    }
                    .onFailure { e ->
                        // Silently fail - don't show error toast for this
                        _overdueReviews.value = emptyList()
                    }
            } catch (e: Exception) {
                _overdueReviews.value = emptyList()
            }
        }
    }
}
