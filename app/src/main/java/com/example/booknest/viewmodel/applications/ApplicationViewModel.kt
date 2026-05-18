package com.example.booknest.viewmodel.applications

import androidx.lifecycle.ViewModel
import com.example.booknest.viewmodel.common.UserFeedback
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.request.CreateApplicationRequest
import com.example.booknest.domain.model.request.UpdateReadingStatusRequest
import com.example.booknest.domain.model.response.ApplicationCheckResponse
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.usecase.applications.CheckApplicationUseCase
import com.example.booknest.domain.usecase.applications.CreateApplicationUseCase
import com.example.booknest.domain.usecase.applications.GetApplicationUseCase
import com.example.booknest.domain.usecase.applications.GetMyApplicationsUseCase
import com.example.booknest.domain.usecase.applications.GetReadingProgressUseCase
import com.example.booknest.domain.usecase.applications.MarkCopyReceivedUseCase
import com.example.booknest.domain.usecase.applications.UpdateReadingStatusUseCase
import com.example.booknest.domain.usecase.applications.WithdrawApplicationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

enum class ApplicationSortOption {
    APPLICATION_DATE, DEADLINE, STATUS
}

data class ApplicationStats(
    val total: Int,
    val approvalRate: Double,
    val reviewsThisMonth: Int,
    val pendingReviews: Int
)

class ApplicationViewModel(
    private val feedback: UserFeedback,
    private val getMyApplicationsUseCase: GetMyApplicationsUseCase,
    private val checkApplicationUseCase: CheckApplicationUseCase,
    private val createApplicationUseCase: CreateApplicationUseCase,
    private val getApplicationUseCase: GetApplicationUseCase,
    private val getReadingProgressUseCase: GetReadingProgressUseCase,
    private val withdrawApplicationUseCase: WithdrawApplicationUseCase,
    private val markCopyReceivedUseCase: MarkCopyReceivedUseCase,
    private val updateReadingStatusUseCase: UpdateReadingStatusUseCase
) : ViewModel() {

    private val _myApplications = MutableStateFlow<List<ApplicationResponse>>(emptyList())
    val myApplications: StateFlow<List<ApplicationResponse>> = _myApplications.asStateFlow()

    val activeReadingApplications: StateFlow<List<ApplicationResponse>> = _myApplications
        .map { list -> list.filter { it.status == "approved" && it.readingStatus != "reviewed" } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val pendingApplications: StateFlow<List<ApplicationResponse>> = _myApplications
        .map { list -> list.filter { it.status == "pending" } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _readingProgress = MutableStateFlow<List<ApplicationResponse>>(emptyList())
    val readingProgress: StateFlow<List<ApplicationResponse>> = _readingProgress.asStateFlow()

    private val _applicationCheck = MutableStateFlow<ApplicationCheckResponse?>(null)
    val applicationCheck: StateFlow<ApplicationCheckResponse?> = _applicationCheck.asStateFlow()

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

    // Filter / sort state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _sortOption = MutableStateFlow(ApplicationSortOption.APPLICATION_DATE)
    val sortOption: StateFlow<ApplicationSortOption> = _sortOption.asStateFlow()

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun updateSelectedTab(tab: Int) { _selectedTab.value = tab }
    fun updateSortOption(option: ApplicationSortOption) { _sortOption.value = option }

    val applicationStats: StateFlow<ApplicationStats> = _myApplications.map { apps ->
        val total = apps.size
        val approved = apps.count { it.status == "approved" }
        val approvalRate = if (total > 0) approved.toDouble() / total * 100 else 0.0
        val now = Calendar.getInstance()
        val thisMonth = now.get(Calendar.MONTH)
        val thisYear = now.get(Calendar.YEAR)
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val reviewsThisMonth = apps.count { app ->
            app.reviewSubmittedAt?.let { dateStr ->
                try {
                    val date = fmt.parse(dateStr)
                    if (date != null) {
                        val cal = Calendar.getInstance().apply { time = date }
                        cal.get(Calendar.MONTH) == thisMonth && cal.get(Calendar.YEAR) == thisYear
                    } else false
                } catch (_: Exception) { false }
            } ?: false
        }
        val pendingReviews = apps.count {
            it.status == "approved" && it.reviewSubmittedAt == null && it.readingStatus != "reviewed"
        }
        ApplicationStats(total, approvalRate, reviewsThisMonth, pendingReviews)
    }.stateIn(viewModelScope, SharingStarted.Lazily, ApplicationStats(0, 0.0, 0, 0))

    val tabCounts: StateFlow<Map<Int, Int>> = _myApplications.map { apps ->
        mapOf(
            0 to apps.size,
            1 to apps.count { it.status == "pending" },
            2 to apps.count { it.status == "approved" && it.reviewSubmittedAt == null },
            3 to apps.count { it.status == "approved" && (it.readingStatus == "reviewed" || it.reviewSubmittedAt != null) },
            4 to apps.count { it.status == "rejected" || it.status == "withdrawn" }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    val filteredApplications: StateFlow<List<ApplicationResponse>> = combine(
        _myApplications, _searchQuery, _selectedTab, _sortOption
    ) { apps, query, tab, sort ->
        val byTab = when (tab) {
            1 -> apps.filter { it.status == "pending" }
            2 -> apps.filter { it.status == "approved" && it.reviewSubmittedAt == null }
            3 -> apps.filter { it.status == "approved" && (it.readingStatus == "reviewed" || it.reviewSubmittedAt != null) }
            4 -> apps.filter { it.status == "rejected" || it.status == "withdrawn" }
            else -> apps
        }
        val searched = if (query.isBlank()) byTab
        else byTab.filter { app ->
            app.bookTitle?.contains(query, ignoreCase = true) == true ||
                    app.book?.title?.contains(query, ignoreCase = true) == true ||
                    app.authorName?.contains(query, ignoreCase = true) == true ||
                    app.book?.author?.firstName?.contains(query, ignoreCase = true) == true ||
                    app.book?.author?.lastName?.contains(query, ignoreCase = true) == true ||
                    app.book?.author?.username?.contains(query, ignoreCase = true) == true ||
                    app.applicationMessage?.contains(query, ignoreCase = true) == true
        }
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        when (sort) {
            ApplicationSortOption.APPLICATION_DATE -> searched.sortedByDescending {
                try { fmt.parse(it.appliedAt ?: "")?.time } catch (_: Exception) { null } ?: 0L
            }
            ApplicationSortOption.DEADLINE -> searched.sortedBy { app ->
                val deadline = app.book?.reviewDeadline ?: app.book?.applicationDeadline
                deadline?.let { try { fmt.parse(it)?.time } catch (_: Exception) { null } } ?: Long.MAX_VALUE
            }
            ApplicationSortOption.STATUS -> searched.sortedBy { it.status }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val approvedApplicationsBySub: StateFlow<Pair<List<ApplicationResponse>, List<ApplicationResponse>>> =
        combine(_myApplications, _selectedTab) { apps, tab ->
            if (tab == 2) {
                val approved = apps.filter { it.status == "approved" && it.reviewSubmittedAt == null }
                val awaitingCopy = approved.filter { it.copyReceivedAt == null }
                val reading = approved.filter { it.copyReceivedAt != null }
                Pair(awaitingCopy, reading)
            } else {
                Pair(emptyList(), emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, Pair(emptyList(), emptyList()))

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
                        notifyError(e.message ?: "Failed to load applications")
                    }
            } catch (e: Exception) {
                notifyError("Error loading applications: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkApplication(bookId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = checkApplicationUseCase(bookId)
                result
                    .onSuccess { check ->
                        _applicationCheck.value = check
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to check application status")
                    }
            } catch (e: Exception) {
                notifyError("Error checking application status: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getApplication(applicationId: String) =
        getApplicationUseCase(applicationId)

    fun loadReadingProgress() {
        viewModelScope.launch {
            try {
                val result = getReadingProgressUseCase()
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
                val result = createApplicationUseCase(request)
                result
                    .onSuccess {
                        notifySuccess("Application submitted successfully!")
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
                        notifyError(errorMessage)
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
                notifyError(errorMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun withdrawApplication(applicationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = withdrawApplicationUseCase(applicationId)
                result
                    .onSuccess {
                        notifySuccess("Application withdrawn successfully!")
                        loadMyApplications()
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to withdraw application")
                    }
            } catch (e: Exception) {
                notifyError("Error withdrawing application: ${e.message}")
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
                val result = markCopyReceivedUseCase(trimmedId)
                result
                    .onSuccess {
                        notifySuccess("Copy marked as received!")
                        loadMyApplications()
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to mark copy as received")
                    }
            } catch (e: Exception) {
                notifyError("Error marking copy as received: ${e.message}")
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
                val result = updateReadingStatusUseCase(applicationId, request)
                result
                    .onSuccess {
                        notifySuccess("Reading status updated!")
                        loadMyApplications()
                        loadReadingProgress()
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to update reading status")
                    }
            } catch (e: Exception) {
                notifyError("Error updating reading status: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

}
