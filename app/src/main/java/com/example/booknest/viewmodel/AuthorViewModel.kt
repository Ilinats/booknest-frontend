package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.request.CreateBookRequest
import com.example.booknest.domain.model.request.CreateSeriesRequest
import com.example.booknest.domain.model.request.UpdateBookRequest
import com.example.booknest.domain.model.request.UpdateSeriesRequest
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.BookStatsResponse
import com.example.booknest.domain.model.response.SeriesResponse
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.model.response.UserStatsResponse
import com.example.booknest.domain.repository.ApplicationsRepository
import com.example.booknest.domain.repository.BooksRepository
import com.example.booknest.domain.repository.ProfileRepository
import com.example.booknest.domain.repository.ReviewsRepository
import com.example.booknest.domain.repository.SeriesRepository
import com.example.booknest.domain.usecase.author.GetBookStatsUseCase
import com.example.booknest.domain.usecase.author.GetMyBooksUseCase
import com.example.booknest.domain.usecase.author.GetMySeriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import com.example.booknest.ui.toast.GlobalToastHandler
import com.example.booknest.ui.state.UiState

class AuthorViewModel(
    private val booksRepository: BooksRepository,
    private val seriesRepository: SeriesRepository,
    private val getMyBooksUseCase: GetMyBooksUseCase,
    private val getMySeriesUseCase: GetMySeriesUseCase,
    private val getBookStatsUseCase: GetBookStatsUseCase,
    private val profileRepository: ProfileRepository,
    private val reviewsRepository: ReviewsRepository,
    private val applicationsRepository: ApplicationsRepository
) : ViewModel() {

    private val _myBooks = MutableStateFlow<List<BookResponse>>(emptyList())
    val myBooks: StateFlow<List<BookResponse>> = _myBooks.asStateFlow()

    private val _isLoadingBooks = MutableStateFlow(false)
    val isLoadingBooks: StateFlow<Boolean> = _isLoadingBooks.asStateFlow()

    private val _mySeries = MutableStateFlow<List<SeriesResponse>>(emptyList())
    val mySeries: StateFlow<List<SeriesResponse>> = _mySeries.asStateFlow()

    private val _isLoadingSeries = MutableStateFlow(false)
    val isLoadingSeries: StateFlow<Boolean> = _isLoadingSeries.asStateFlow()

    private val _bookStats = MutableStateFlow<Map<String, BookStatsResponse>>(emptyMap())
    val bookStats: StateFlow<Map<String, BookStatsResponse>> = _bookStats.asStateFlow()

    private val _quickStats = MutableStateFlow(QuickStats())
    val quickStats: StateFlow<QuickStats> = _quickStats.asStateFlow()

    private val _authorStats = MutableStateFlow<UserStatsResponse?>(null)
    val authorStats: StateFlow<UserStatsResponse?> = _authorStats.asStateFlow()

    private val _recentReviews = MutableStateFlow<List<ReviewResponse>>(emptyList())
    val recentReviews: StateFlow<List<ReviewResponse>> = _recentReviews.asStateFlow()

    private val _overdueReviews = MutableStateFlow<List<ApplicationResponse>>(emptyList())
    val overdueReviews: StateFlow<List<ApplicationResponse>> = _overdueReviews.asStateFlow()

    private val _isLoadingStats = MutableStateFlow(false)
    val isLoadingStats: StateFlow<Boolean> = _isLoadingStats.asStateFlow()

    private val _isLoadingReviews = MutableStateFlow(false)
    val isLoadingReviews: StateFlow<Boolean> = _isLoadingReviews.asStateFlow()

    private val _coverImageRemovalState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val coverImageRemovalState: StateFlow<UiState<Unit>> = _coverImageRemovalState.asStateFlow()

    private val _coverImageUploadState = MutableStateFlow<UiState<Pair<String, String>>>(UiState.Idle)
    val coverImageUploadState: StateFlow<UiState<Pair<String, String>>> = _coverImageUploadState.asStateFlow()

    private val _bookFileUploadState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val bookFileUploadState: StateFlow<UiState<String>> = _bookFileUploadState.asStateFlow()

    data class QuickStats(
        val totalBooks: Int = 0,
        val activeBooks: Int = 0,
        val totalApplications: Int = 0,
        val pendingApplications: Int = 0,
        val applicationsThisMonth: Int = 0,
        val avgResponseTime: String = "0 days",
        val totalReviews: Int = 0,
        val averageRating: Double = 0.0,
        val approvalRate: Int = 0
    )

    init {
        loadMyBooks()
        loadMySeries()
        loadAuthorStats()
        loadRecentReviews()
        loadOverdueReviews()
    }

    fun loadMyBooks() {
        viewModelScope.launch {
            try {
                _isLoadingBooks.value = true
                val result = getMyBooksUseCase()
                result
                    .onSuccess { books ->
                        _myBooks.value = books
                        updateQuickStats()
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e)
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError(e)
            } finally {
                _isLoadingBooks.value = false
            }
        }
    }

    fun loadMySeries() {
        viewModelScope.launch {
            try {
                _isLoadingSeries.value = true
                val result = getMySeriesUseCase()
                result
                    .onSuccess { series ->
                        _mySeries.value = series
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e)
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError(e)
            } finally {
                _isLoadingSeries.value = false
            }
        }
    }

    private val _bookCreationState = MutableStateFlow<UiState<BookResponse>>(UiState.Idle)
    val bookCreationState: StateFlow<UiState<BookResponse>> = _bookCreationState.asStateFlow()


    fun createBook(
        book: CreateBookRequest,
        fileUri: android.net.Uri? = null,
        coverImageUri: android.net.Uri? = null,
        context: android.content.Context? = null
    ) {
        viewModelScope.launch {
            try {
                _bookCreationState.value = UiState.Loading

                if (context == null && (fileUri != null || coverImageUri != null)) {
                    _bookCreationState.value = UiState.Error("Context required for file upload")
                    return@launch
                }

                val filePart = fileUri?.let { uri ->
                    val contextNonNull = context ?: run {
                        _bookCreationState.value =
                            UiState.Error("Context required for file upload")
                        return@launch
                    }

                    val mimeType = withContext(kotlinx.coroutines.Dispatchers.IO) {
                        contextNonNull.contentResolver.getType(uri)
                    }

                    val file = withContext(kotlinx.coroutines.Dispatchers.IO) {
                        uriToFileForBook(contextNonNull, uri, mimeType)
                    } ?: run {
                        _bookCreationState.value = UiState.Error("File type not allowed. Allowed types: pdf, epub")
                        return@launch
                    }

                    val uploadManager = com.example.booknest.utils.FileUploadManager(contextNonNull)
                    val validationResult = uploadManager.validateBookFile(file)
                    if (validationResult is com.example.booknest.utils.FileUploadManager.ValidationResult.Error) {
                        _bookCreationState.value = UiState.Error(validationResult.message)
                        return@launch
                    }

                    uploadManager.createMultipartBody(file)
                }

                val bookWithoutCover = book.copy(coverImageUrl = null)
                val result = booksRepository.createBook(bookWithoutCover, filePart)
                result
                    .onSuccess { createdBook ->
                        coverImageUri?.let { uri ->
                            // Upload cover image (non-blocking)
                            uploadBookCoverImage(createdBook.id, uri, context!!)
                            // Set success state immediately, cover will be updated via state flow
                            GlobalToastHandler.showSuccess("Book created successfully!")
                            _bookCreationState.value = UiState.Success(createdBook)
                            reloadHomeScreenData()
                        } ?: run {
                            GlobalToastHandler.showSuccess("Book created successfully!")
                            _bookCreationState.value = UiState.Success(createdBook)
                            reloadHomeScreenData()
                        }
                    }
                    .onFailure { e ->
                        val errorMsg = e.message ?: "Failed to create book"
                        GlobalToastHandler.showError(errorMsg)
                        _bookCreationState.value = UiState.Error(errorMsg)
                    }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Error creating book"
                GlobalToastHandler.showError(errorMsg)
                _bookCreationState.value = UiState.Error(errorMsg)
            }
        }
    }

    fun uploadBookFile(
        bookId: String,
        fileUri: android.net.Uri,
        context: android.content.Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(kotlinx.coroutines.NonCancellable) {
            try {
                _bookFileUploadState.value = UiState.Loading
                val mimeType = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    context.contentResolver.getType(fileUri)
                }

                val file = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    uriToFileForBook(context, fileUri, mimeType)
                } ?: run {
                    val errorMsg = "File type not allowed. Allowed types: pdf, epub"
                    _bookFileUploadState.value = UiState.Error(errorMsg)
                    onError(errorMsg)
                    return@launch
                }

                val uploadManager = com.example.booknest.utils.FileUploadManager(context)
                val validationResult = uploadManager.validateBookFile(file)
                if (validationResult is com.example.booknest.utils.FileUploadManager.ValidationResult.Error) {
                    _bookFileUploadState.value = UiState.Error(validationResult.message)
                    onError(validationResult.message)
                    return@launch
                }

                val multipartBody = uploadManager.createMultipartBody(file)
                val result = booksRepository.uploadBookFile(bookId, multipartBody)

                result
                    .onSuccess {
                        _bookFileUploadState.value = UiState.Success(bookId)
                        onSuccess()
                        withContext(kotlinx.coroutines.Dispatchers.IO) {
                            try {
                                if (file.exists()) file.delete()
                            } catch (e: Exception) {
                            }
                        }
                    }
                    .onFailure { e ->
                        if (e !is kotlinx.coroutines.CancellationException) {
                            val errorMsg = e.message ?: "Failed to upload file"
                            _bookFileUploadState.value = UiState.Error(errorMsg)
                            onError(errorMsg)
                        }
                    }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    val errorMsg = e.message ?: "Error uploading file"
                    _bookFileUploadState.value = UiState.Error(errorMsg)
                    onError(errorMsg)
                }
            }
        }
    }

    fun uploadBookCoverImage(
        bookId: String,
        imageUri: android.net.Uri,
        context: android.content.Context
    ) {
        viewModelScope.launch(kotlinx.coroutines.NonCancellable) {
            try {
                _coverImageUploadState.value = UiState.Loading
                val mimeType = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    context.contentResolver.getType(imageUri) ?: "image/png"
                }

                val file = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    uriToFile(context, imageUri, mimeType)
                } ?: run {
                    val errorMsg = "Failed to process image file"
                    _coverImageUploadState.value = UiState.Error(errorMsg)
                        GlobalToastHandler.showError(errorMsg)
                    return@launch
                }

                val finalMimeType = when {
                    mimeType.isNotEmpty() && mimeType.startsWith("image/") -> mimeType
                    else -> {
                        val extension = file.name.substringAfterLast('.', "").lowercase()
                        when (extension) {
                            "jpg", "jpeg" -> "image/jpeg"
                            "png" -> "image/png"
                            "gif" -> "image/gif"
                            "webp" -> "image/webp"
                            else -> "image/png"
                        }
                    }
                }

                val uploadManager = com.example.booknest.utils.FileUploadManager(context)
                val requestFile = file.asRequestBody(finalMimeType.toMediaType())
                val multipartBody =
                    MultipartBody.Part.createFormData("cover", file.name, requestFile)

                val result = booksRepository.uploadBookCoverImage(bookId, multipartBody)

                result
                    .onSuccess { bookResponse ->
                        val coverUrl = bookResponse.coverImageUrl ?: ""
                        _coverImageUploadState.value = UiState.Success(bookId to coverUrl)
                        GlobalToastHandler.showSuccess("Cover image uploaded successfully")
                        // Reload books to reflect the change
                        loadMyBooks()
                        withContext(kotlinx.coroutines.Dispatchers.IO) {
                            try {
                                if (file.exists()) file.delete()
                            } catch (e: Exception) {
                            }
                        }
                    }
                    .onFailure { e ->
                        if (e !is kotlinx.coroutines.CancellationException) {
                            val errorMsg = e.message ?: "Failed to upload cover image"
                            _coverImageUploadState.value = UiState.Error(errorMsg)
                            GlobalToastHandler.showError(e)
                        }
                    }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    val errorMsg = e.message ?: "Error uploading cover image"
                    _coverImageUploadState.value = UiState.Error(errorMsg)
                    GlobalToastHandler.showError(e)
                }
            }
        }
    }

    fun removeBookCoverImage(bookId: String) {
        viewModelScope.launch(kotlinx.coroutines.NonCancellable) {
            try {
                _coverImageRemovalState.value = UiState.Loading
                val result = booksRepository.removeBookCoverImage(bookId)
                result
                    .onSuccess {
                        _coverImageRemovalState.value = UiState.Success(Unit)
                        GlobalToastHandler.showSuccess("Cover image removed successfully")
                        // Reload books to reflect the change
                        loadMyBooks()
                    }
                    .onFailure { e ->
                        val errorMsg = e.message ?: "Failed to remove cover image"
                        _coverImageRemovalState.value = UiState.Error(errorMsg)
                        GlobalToastHandler.showError(e)
                    }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Error removing cover image"
                _coverImageRemovalState.value = UiState.Error(errorMsg)
                GlobalToastHandler.showError(e)
            }
        }
    }

    private suspend fun uriToFile(
        context: android.content.Context,
        uri: android.net.Uri,
        mimeType: String? = null
    ): java.io.File? =
        withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val inputStream =
                    context.contentResolver.openInputStream(uri) ?: return@withContext null

                val extension = when {
                    mimeType != null -> {
                        when {
                            mimeType.contains("jpeg") || mimeType.contains("jpg") -> "jpg"
                            mimeType.contains("png") -> "png"
                            mimeType.contains("gif") -> "gif"
                            mimeType.contains("webp") -> "webp"
                            else -> "png"
                        }
                    }

                    else -> {
                        val uriPath = uri.toString()
                        val uriExt =
                            uriPath.substringAfterLast('.', "").substringBefore('?', "").lowercase()
                        if (uriExt in listOf("jpg", "jpeg", "png", "gif", "webp")) {
                            uriExt
                        } else {
                            "png"
                        }
                    }
                }

                val tempFile = java.io.File(
                    context.cacheDir,
                    "temp_book_cover_${System.currentTimeMillis()}.$extension"
                )
                tempFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                tempFile
            } catch (e: Exception) {
                GlobalToastHandler.showError(e)
                null
            }
        }

    private suspend fun uriToFileForBook(
        context: android.content.Context,
        uri: android.net.Uri,
        mimeType: String? = null
    ): java.io.File? =
        withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val inputStream =
                    context.contentResolver.openInputStream(uri) ?: return@withContext null

                val fileName = try {
                    var displayName: String? = null
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex =
                            cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0 && cursor.moveToFirst()) {
                            displayName = cursor.getString(nameIndex)
                        }
                    }
                    displayName
                } catch (e: Exception) {
                    null
                }

                val extension = when {
                    fileName != null -> {
                        val ext = fileName.substringAfterLast('.', "").substringBefore('?', "")
                            .substringBefore('(', "").trim().lowercase()
                        if (ext.isNotEmpty() && ext in com.example.booknest.utils.FileUploadManager.BOOK_FILE_EXTENSIONS) {
                            ext
                        } else {
                            null
                        }
                    }

                    else -> null
                } ?: when {
                    mimeType != null -> {
                        when {
                            mimeType.contains("pdf", ignoreCase = true) -> "pdf"
                            mimeType.contains(
                                "epub",
                                ignoreCase = true
                            ) || mimeType.contains("application/epub", ignoreCase = true) -> "epub"

                            else -> null
                        }
                    }

                    else -> null
                } ?: run {
                    val uriPath = uri.toString()
                    val uriExt = uriPath.substringAfterLast('.', "").substringBefore('?', "")
                        .substringBefore('(', "").trim().lowercase()
                    if (uriExt in com.example.booknest.utils.FileUploadManager.BOOK_FILE_EXTENSIONS) {
                        uriExt
                    } else {
                        null
                    }
                }

                if (extension == null || extension !in com.example.booknest.utils.FileUploadManager.BOOK_FILE_EXTENSIONS) {
                    return@withContext null
                }

                val sanitizedFileName = fileName?.let { origName ->
                    val ext = origName.substringAfterLast('.', "").substringBefore('?', "")
                        .substringBefore('(', "").trim().lowercase()
                    if (ext in com.example.booknest.utils.FileUploadManager.BOOK_FILE_EXTENSIONS) {
                        val nameWithoutExt = origName.substringBeforeLast('.', origName)
                            .replace(" ", "_")
                            .replace("[^a-zA-Z0-9._-]".toRegex(), "_")
                        "$nameWithoutExt.$ext"
                    } else {
                        "book_file_${System.currentTimeMillis()}.$extension"
                    }
                } ?: "book_file_${System.currentTimeMillis()}.$extension"

                val tempFile = java.io.File(context.cacheDir, "temp_$sanitizedFileName")
                tempFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                tempFile
            } catch (e: Exception) {
                GlobalToastHandler.showError(e)
                null
            }
        }

    fun clearBookCreationState() {
        _bookCreationState.value = UiState.Idle
        _bookFileUploadState.value = UiState.Idle
    }

    fun reloadHomeScreenData() {
        viewModelScope.launch {
            loadMyBooks()
            loadAuthorStats()
            loadRecentReviews()
            loadOverdueReviews()
        }
    }

    fun updateBook(bookId: String, book: UpdateBookRequest) {
        viewModelScope.launch {
            try {
                val result = booksRepository.updateBook(bookId, book)
                result
                    .onSuccess {
                        GlobalToastHandler.showSuccess("Book updated successfully!")
                        reloadHomeScreenData()
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e)
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError(e)
            }
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            try {
                val result = booksRepository.deleteBook(bookId)
                result
                    .onSuccess {
                        GlobalToastHandler.showSuccess("Book deleted successfully!")
                        reloadHomeScreenData()
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e)
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError(e)
            }
        }
    }

    fun publishBook(bookId: String) {
        viewModelScope.launch {
            try {
                val result = booksRepository.publishBook(bookId)
                result
                    .onSuccess {
                        reloadHomeScreenData()
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e)
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError(e)
            }
        }
    }

    fun getBookStats(bookId: String) {
        viewModelScope.launch {
            try {
                val result = getBookStatsUseCase(bookId)
                result
                    .onSuccess { stats ->
                        _bookStats.value = _bookStats.value + (bookId to stats)
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e)
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError(e)
            }
        }
    }

    fun createSeries(series: CreateSeriesRequest) {
        viewModelScope.launch {
            try {
                val result = seriesRepository.createSeries(series)
                result
                    .onSuccess {
                        loadMySeries()
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e)
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError(e)
            }
        }
    }

    fun updateSeries(seriesId: String, series: UpdateSeriesRequest) {
        viewModelScope.launch {
            try {
                val result = seriesRepository.updateSeries(seriesId, series)
                result
                    .onSuccess {
                        loadMySeries()
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e)
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError(e)
            }
        }
    }

    fun loadAuthorStats() {
        viewModelScope.launch {
            try {
                _isLoadingStats.value = true
                val result = profileRepository.getMyStats()
                result
                    .onSuccess { stats ->
                        _authorStats.value = stats
                        updateQuickStatsFromStats(stats)
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e)
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError(e)
            } finally {
                _isLoadingStats.value = false
            }
        }
    }

    fun loadRecentReviews() {
        viewModelScope.launch {
            try {
                _isLoadingReviews.value = true
                val result = reviewsRepository.getAuthorLatestReviews(limit = 3)
                result
                    .onSuccess { reviews ->
                        _recentReviews.value = reviews
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e)
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError(e)
            } finally {
                _isLoadingReviews.value = false
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
                        GlobalToastHandler.showError(e)
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError(e)
            }
        }
    }

    private fun updateQuickStats() {
        val books = _myBooks.value
        val totalBooks = books.size
        val activeBooks = books.count { it.status == "active" }
        val totalApplications = _bookStats.value.values.sumOf { it.effectiveTotalApplications }

        _quickStats.value = _quickStats.value.copy(
            totalBooks = totalBooks,
            activeBooks = activeBooks,
            totalApplications = totalApplications
        )
    }

    private fun updateQuickStatsFromStats(stats: UserStatsResponse) {
        val statsData = stats.stats
        val avgResponseTime = statsData.averageResponseTime?.let {
            if (it < 1) "${(it * 24).toInt()} hours"
            else "${it.toInt()} days"
        } ?: "0 days"

        _quickStats.value = QuickStats(
            totalBooks = statsData.totalBooks ?: 0,
            activeBooks = statsData.publishedBooks ?: 0,
            totalApplications = statsData.totalApplications,
            pendingApplications = statsData.pendingApplications,
            applicationsThisMonth = statsData.applicationsThisMonth ?: 0,
            avgResponseTime = avgResponseTime,
            totalReviews = statsData.totalReviews ?: 0,
            averageRating = statsData.averageRating ?: 0.0,
            approvalRate = statsData.approvalRate ?: (if (statsData.totalApplications > 0) {
                ((statsData.approvedApplications.toDouble() / statsData.totalApplications) * 100).toInt()
            } else 0)
        )
    }
}

