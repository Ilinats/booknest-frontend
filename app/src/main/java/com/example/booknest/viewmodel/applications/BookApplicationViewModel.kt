package com.example.booknest.viewmodel.applications

import androidx.lifecycle.ViewModel
import com.example.booknest.viewmodel.common.UserFeedback
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.request.BulkActionRequest
import com.example.booknest.domain.model.request.UpdateApplicationCompleteRequest
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.usecase.applications.BulkActionApplicationsUseCase
import com.example.booknest.domain.usecase.applications.GetBookApplicationsUseCase
import com.example.booknest.domain.usecase.applications.GetOverdueReviewsUseCase
import com.example.booknest.domain.usecase.applications.MarkCopySentUseCase
import com.example.booknest.domain.usecase.applications.RunLotterySelectionUseCase
import com.example.booknest.domain.usecase.applications.UpdateApplicationCompleteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookApplicationViewModel(
    private val feedback: UserFeedback,
    private val getBookApplicationsUseCase: GetBookApplicationsUseCase,
    private val updateApplicationCompleteUseCase: UpdateApplicationCompleteUseCase,
    private val bulkActionApplicationsUseCase: BulkActionApplicationsUseCase,
    private val markCopySentUseCase: MarkCopySentUseCase,
    private val runLotterySelectionUseCase: RunLotterySelectionUseCase,
    private val getOverdueReviewsUseCase: GetOverdueReviewsUseCase
) : ViewModel() {

    private val _bookApplications = MutableStateFlow<List<ApplicationResponse>>(emptyList())
    val bookApplications: StateFlow<List<ApplicationResponse>> = _bookApplications.asStateFlow()

    private val _overdueReviews = MutableStateFlow<List<ApplicationResponse>>(emptyList())
    val overdueReviews: StateFlow<List<ApplicationResponse>> = _overdueReviews.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun clearError() { _error.value = null }
    fun clearSuccessMessage() { _successMessage.value = null }

    private fun notifyError(message: String) = feedback.error(message, _error)
    private fun notifySuccess(message: String) = feedback.success(message, _successMessage)

    fun loadBookApplications(bookId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = getBookApplicationsUseCase(bookId)
                result
                    .onSuccess { apps -> _bookApplications.value = apps }
                    .onFailure { e -> notifyError(e.message ?: "Failed to load book applications") }
            } catch (e: Exception) {
                notifyError("Error loading book applications: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveApplication(applicationId: String, authorNotes: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = UpdateApplicationCompleteRequest(status = "approved", authorNotes = authorNotes)
                val result = updateApplicationCompleteUseCase(applicationId, request)
                result
                    .onSuccess {
                        notifySuccess("Application approved!")
                        val currentBookId = _bookApplications.value.firstOrNull()?.bookId
                        if (currentBookId != null) loadBookApplications(currentBookId)
                    }
                    .onFailure { e -> notifyError(e.message ?: "Failed to approve application") }
            } catch (e: Exception) {
                notifyError("Error approving application: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rejectApplication(applicationId: String, authorNotes: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = UpdateApplicationCompleteRequest(status = "rejected", authorNotes = authorNotes)
                val result = updateApplicationCompleteUseCase(applicationId, request)
                result
                    .onSuccess {
                        notifySuccess("Application rejected!")
                        val currentBookId = _bookApplications.value.firstOrNull()?.bookId
                        if (currentBookId != null) loadBookApplications(currentBookId)
                    }
                    .onFailure { e -> notifyError(e.message ?: "Failed to reject application") }
            } catch (e: Exception) {
                notifyError("Error rejecting application: ${e.message}")
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
                    notifyError("Error: Book ID not found")
                    _isLoading.value = false
                    return@launch
                }
                val request = BulkActionRequest(
                    applicationIds = applicationIds,
                    action = action,
                    authorNotes = authorNotes
                )
                val result = bulkActionApplicationsUseCase(currentBookId, request)
                result
                    .onSuccess {
                        notifySuccess("Bulk action completed!")
                        loadBookApplications(currentBookId)
                    }
                    .onFailure { e -> notifyError(e.message ?: "Failed to perform bulk action") }
            } catch (e: Exception) {
                notifyError("Error performing bulk action: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markCopySent(applicationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = markCopySentUseCase(applicationId)
                result
                    .onSuccess {
                        notifySuccess("Copy marked as sent!")
                        val currentBookId = _bookApplications.value.firstOrNull()?.bookId
                        if (currentBookId != null) loadBookApplications(currentBookId)
                    }
                    .onFailure { e -> notifyError(e.message ?: "Failed to mark copy as sent") }
            } catch (e: Exception) {
                notifyError("Error marking copy as sent: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun runLottery(bookId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = runLotterySelectionUseCase(bookId)
                result
                    .onSuccess { lotteryResult ->
                        notifySuccess("Lottery completed: ${lotteryResult.approved} approved, ${lotteryResult.rejected} rejected")
                        loadBookApplications(bookId)
                    }
                    .onFailure { e ->
                        notifyError(when {
                            e.message?.contains("deadline", ignoreCase = true) == true ->
                                "Application deadline has not passed yet. Lottery can only be run after the deadline."
                            e.message?.contains("already been run", ignoreCase = true) == true ->
                                "Lottery has already been run for this book."
                            e.message?.contains("lottery selection", ignoreCase = true) == true ->
                                "This book does not use lottery selection method."
                            else -> e.message ?: "Failed to run lottery"
                        })
                    }
            } catch (e: Exception) {
                notifyError("Error running lottery: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadOverdueReviews(force: Boolean = false) {
        if (!force && _overdueReviews.value.isNotEmpty()) return
        viewModelScope.launch {
            try {
                val result = getOverdueReviewsUseCase()
                result
                    .onSuccess { applications -> _overdueReviews.value = applications }
                    .onFailure { _overdueReviews.value = emptyList() }
            } catch (e: Exception) {
                _overdueReviews.value = emptyList()
            }
        }
    }
}
