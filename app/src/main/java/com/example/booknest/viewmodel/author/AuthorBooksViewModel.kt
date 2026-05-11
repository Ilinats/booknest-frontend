package com.example.booknest.viewmodel.author

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.request.CreateBookRequest
import com.example.booknest.domain.model.request.UpdateBookRequest
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.BookStatsResponse
import com.example.booknest.domain.usecase.author.CreateBookUseCase
import com.example.booknest.domain.usecase.author.DeleteBookUseCase
import com.example.booknest.domain.usecase.author.GetBookStatsUseCase
import com.example.booknest.domain.usecase.author.GetMyBooksUseCase
import com.example.booknest.domain.usecase.author.PublishBookUseCase
import com.example.booknest.domain.usecase.author.UpdateBookUseCase
import com.example.booknest.domain.usecase.files.RemoveBookCoverImageUseCase
import com.example.booknest.domain.usecase.files.UploadBookCoverImageUseCase
import com.example.booknest.domain.usecase.files.UploadBookFileUseCase
import com.example.booknest.ui.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

enum class BookStatus(val value: String) {
    DRAFT("draft"),
    ACTIVE("active"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    ARCHIVED("archived")
}

enum class BookSortOption {
    DATE_CREATED, TITLE, STATUS, APPLICATION_COUNT
}

private data class BooksFilterState(
    val searchQuery: String = "",
    val selectedTab: Int = 0,
    val sortOption: BookSortOption = BookSortOption.DATE_CREATED
)

class AuthorBooksViewModel(
    private val getMyBooksUseCase: GetMyBooksUseCase,
    private val getBookStatsUseCase: GetBookStatsUseCase,
    private val createBookUseCase: CreateBookUseCase,
    private val updateBookUseCase: UpdateBookUseCase,
    private val deleteBookUseCase: DeleteBookUseCase,
    private val publishBookUseCase: PublishBookUseCase,
    private val uploadBookFileUseCase: UploadBookFileUseCase,
    private val uploadBookCoverImageUseCase: UploadBookCoverImageUseCase,
    private val removeBookCoverImageUseCase: RemoveBookCoverImageUseCase
) : ViewModel() {

    private val _myBooks = MutableStateFlow<List<BookResponse>>(emptyList())
    val myBooks: StateFlow<List<BookResponse>> = _myBooks.asStateFlow()

    private val _isLoadingBooks = MutableStateFlow(false)
    val isLoadingBooks: StateFlow<Boolean> = _isLoadingBooks.asStateFlow()

    private val _bookStats = MutableStateFlow<Map<String, BookStatsResponse>>(emptyMap())
    val bookStats: StateFlow<Map<String, BookStatsResponse>> = _bookStats.asStateFlow()

    private val _bookCreationState = MutableStateFlow<UiState<BookResponse>>(UiState.Idle)
    val bookCreationState: StateFlow<UiState<BookResponse>> = _bookCreationState.asStateFlow()

    private val _coverImageUploadState = MutableStateFlow<UiState<Pair<String, String>>>(UiState.Idle)
    val coverImageUploadState: StateFlow<UiState<Pair<String, String>>> = _coverImageUploadState.asStateFlow()

    private val _coverImageRemovalState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val coverImageRemovalState: StateFlow<UiState<Unit>> = _coverImageRemovalState.asStateFlow()

    private val _bookFileUploadState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val bookFileUploadState: StateFlow<UiState<String>> = _bookFileUploadState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun clearError() { _error.value = null }
    fun clearSuccessMessage() { _successMessage.value = null }

    // Filter / sort state owned by ViewModel so config changes don't reset it
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _sortOption = MutableStateFlow(BookSortOption.DATE_CREATED)
    val sortOption: StateFlow<BookSortOption> = _sortOption.asStateFlow()

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun updateSelectedTab(tab: Int) { _selectedTab.value = tab }
    fun updateSortOption(option: BookSortOption) { _sortOption.value = option }

    val tabCounts: StateFlow<Map<Int, Int>> = _myBooks.map { books ->
        val nonArchived = books.filter { it.status != BookStatus.ARCHIVED.value }
        mapOf(
            0 to nonArchived.size,
            1 to nonArchived.count { it.status == BookStatus.DRAFT.value },
            2 to nonArchived.count { it.status == BookStatus.ACTIVE.value },
            3 to nonArchived.count { it.status == BookStatus.IN_PROGRESS.value },
            4 to nonArchived.count { it.status == BookStatus.COMPLETED.value }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    private val _filterState: StateFlow<BooksFilterState> = combine(
        _searchQuery, _selectedTab, _sortOption
    ) { query, tab, sort -> BooksFilterState(query, tab, sort) }
        .stateIn(viewModelScope, SharingStarted.Lazily, BooksFilterState())

    val filteredBooks: StateFlow<List<BookResponse>> = combine(
        _myBooks, _filterState, _bookStats
    ) { books, filter, stats ->
        val nonArchived = books.filter { it.status != BookStatus.ARCHIVED.value }
        val byTab = when (filter.selectedTab) {
            1 -> nonArchived.filter { it.status == BookStatus.DRAFT.value }
            2 -> nonArchived.filter { it.status == BookStatus.ACTIVE.value }
            3 -> nonArchived.filter { it.status == BookStatus.IN_PROGRESS.value }
            4 -> nonArchived.filter { it.status == BookStatus.COMPLETED.value }
            else -> nonArchived
        }
        val searched = if (filter.searchQuery.isBlank()) byTab
        else byTab.filter { book ->
            book.title.contains(filter.searchQuery, ignoreCase = true) ||
                    book.shortDescription?.contains(filter.searchQuery, ignoreCase = true) == true
        }
        when (filter.sortOption) {
            BookSortOption.DATE_CREATED -> searched.sortedByDescending { it.createdAt ?: "" }
            BookSortOption.TITLE -> searched.sortedBy { it.title }
            BookSortOption.STATUS -> searched.sortedBy { it.status }
            BookSortOption.APPLICATION_COUNT -> searched.sortedByDescending { book ->
                stats[book.id]?.effectiveTotalApplications ?: 0
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun loadMyBooks() {
        viewModelScope.launch {
            try {
                _isLoadingBooks.value = true
                val result = getMyBooksUseCase()
                result
                    .onSuccess { books ->
                        _myBooks.value = books
                        books.forEach { book -> getBookStats(book.id) }
                    }
                    .onFailure { e -> _error.value = e.message ?: "Failed to load books" }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading books"
            } finally {
                _isLoadingBooks.value = false
            }
        }
    }

    fun getBookStats(bookId: String) {
        viewModelScope.launch {
            try {
                val result = getBookStatsUseCase(bookId)
                result
                    .onSuccess { stats -> _bookStats.value = _bookStats.value + (bookId to stats) }
                    .onFailure { e -> _error.value = e.message ?: "Failed to load book stats" }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading book stats"
            }
        }
    }

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
                        _bookCreationState.value = UiState.Error("Context required for file upload")
                        return@launch
                    }
                    val mimeType = withContext(Dispatchers.IO) { contextNonNull.contentResolver.getType(uri) }
                    val file = withContext(Dispatchers.IO) { uriToFileForBook(contextNonNull, uri, mimeType) } ?: run {
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
                val result = createBookUseCase(bookWithoutCover, filePart)
                result
                    .onSuccess { createdBook ->
                        coverImageUri?.let { uri ->
                            uploadBookCoverImage(createdBook.id, uri, context!!)
                        }
                        _successMessage.value = "Book created successfully!"
                        _bookCreationState.value = UiState.Success(createdBook)
                        loadMyBooks()
                    }
                    .onFailure { e ->
                        _bookCreationState.value = UiState.Error(e.message ?: "Failed to create book")
                    }
            } catch (e: Exception) {
                _bookCreationState.value = UiState.Error(e.message ?: "Error creating book")
            }
        }
    }

    fun updateBook(bookId: String, book: UpdateBookRequest) {
        viewModelScope.launch {
            try {
                val result = updateBookUseCase(bookId, book)
                result
                    .onSuccess {
                        _successMessage.value = "Book updated successfully!"
                        loadMyBooks()
                    }
                    .onFailure { e -> _error.value = e.message ?: "Failed to update book" }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error updating book"
            }
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            try {
                val result = deleteBookUseCase(bookId)
                result
                    .onSuccess {
                        _successMessage.value = "Book deleted successfully!"
                        loadMyBooks()
                    }
                    .onFailure { e -> _error.value = e.message ?: "Failed to delete book" }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error deleting book"
            }
        }
    }

    fun publishBook(bookId: String) {
        viewModelScope.launch {
            try {
                val result = publishBookUseCase(bookId)
                result
                    .onSuccess { loadMyBooks() }
                    .onFailure { e -> _error.value = e.message ?: "Failed to publish book" }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error publishing book"
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
        viewModelScope.launch(NonCancellable) {
            try {
                _bookFileUploadState.value = UiState.Loading
                val mimeType = withContext(Dispatchers.IO) { context.contentResolver.getType(fileUri) }
                val file = withContext(Dispatchers.IO) { uriToFileForBook(context, fileUri, mimeType) } ?: run {
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
                val result = uploadBookFileUseCase(bookId, multipartBody)
                result
                    .onSuccess {
                        _bookFileUploadState.value = UiState.Success(bookId)
                        onSuccess()
                        withContext(Dispatchers.IO) { try { if (file.exists()) file.delete() } catch (e: Exception) { } }
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
        viewModelScope.launch(NonCancellable) {
            try {
                _coverImageUploadState.value = UiState.Loading
                val mimeType = withContext(Dispatchers.IO) { context.contentResolver.getType(imageUri) ?: "image/png" }
                val file = withContext(Dispatchers.IO) { uriToFile(context, imageUri, mimeType) } ?: run {
                    _coverImageUploadState.value = UiState.Error("Failed to process image file")
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
                val multipartBody = MultipartBody.Part.createFormData("cover", file.name, requestFile)
                val result = uploadBookCoverImageUseCase(bookId, multipartBody)
                result
                    .onSuccess { bookResponse ->
                        val coverUrl = bookResponse.coverImageUrl ?: ""
                        _coverImageUploadState.value = UiState.Success(bookId to coverUrl)
                        _successMessage.value = "Cover image uploaded successfully"
                        loadMyBooks()
                        withContext(Dispatchers.IO) { try { if (file.exists()) file.delete() } catch (e: Exception) { } }
                    }
                    .onFailure { e ->
                        if (e !is kotlinx.coroutines.CancellationException) {
                            _coverImageUploadState.value = UiState.Error(e.message ?: "Failed to upload cover image")
                        }
                    }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _coverImageUploadState.value = UiState.Error(e.message ?: "Error uploading cover image")
                }
            }
        }
    }

    fun removeBookCoverImage(bookId: String) {
        viewModelScope.launch(NonCancellable) {
            try {
                _coverImageRemovalState.value = UiState.Loading
                val result = removeBookCoverImageUseCase(bookId)
                result
                    .onSuccess {
                        _coverImageRemovalState.value = UiState.Success(Unit)
                        _successMessage.value = "Cover image removed successfully"
                        loadMyBooks()
                    }
                    .onFailure { e ->
                        _coverImageRemovalState.value = UiState.Error(e.message ?: "Failed to remove cover image")
                    }
            } catch (e: Exception) {
                _coverImageRemovalState.value = UiState.Error(e.message ?: "Error removing cover image")
            }
        }
    }

    fun clearBookCreationState() {
        _bookCreationState.value = UiState.Idle
        _bookFileUploadState.value = UiState.Idle
    }

    private suspend fun uriToFile(
        context: android.content.Context,
        uri: android.net.Uri,
        mimeType: String? = null
    ): java.io.File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val extension = when {
                mimeType != null -> when {
                    mimeType.contains("jpeg") || mimeType.contains("jpg") -> "jpg"
                    mimeType.contains("png") -> "png"
                    mimeType.contains("gif") -> "gif"
                    mimeType.contains("webp") -> "webp"
                    else -> "png"
                }
                else -> {
                    val uriPath = uri.toString()
                    val uriExt = uriPath.substringAfterLast('.', "").substringBefore('?', "").lowercase()
                    if (uriExt in listOf("jpg", "jpeg", "png", "gif", "webp")) uriExt else "png"
                }
            }
            val tempFile = java.io.File(context.cacheDir, "temp_book_cover_${System.currentTimeMillis()}.$extension")
            tempFile.outputStream().use { output -> inputStream.copyTo(output) }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun uriToFileForBook(
        context: android.content.Context,
        uri: android.net.Uri,
        mimeType: String? = null
    ): java.io.File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val fileName = try {
                var displayName: String? = null
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) {
                        displayName = cursor.getString(nameIndex)
                    }
                }
                displayName
            } catch (e: Exception) { null }

            val extension = when {
                fileName != null -> {
                    val ext = fileName.substringAfterLast('.', "").substringBefore('?', "")
                        .substringBefore('(', "").trim().lowercase()
                    if (ext.isNotEmpty() && ext in com.example.booknest.utils.FileUploadManager.BOOK_FILE_EXTENSIONS) ext else null
                }
                else -> null
            } ?: when {
                mimeType != null -> when {
                    mimeType.contains("pdf", ignoreCase = true) -> "pdf"
                    mimeType.contains("epub", ignoreCase = true) || mimeType.contains("application/epub", ignoreCase = true) -> "epub"
                    else -> null
                }
                else -> null
            } ?: run {
                val uriPath = uri.toString()
                val uriExt = uriPath.substringAfterLast('.', "").substringBefore('?', "")
                    .substringBefore('(', "").trim().lowercase()
                if (uriExt in com.example.booknest.utils.FileUploadManager.BOOK_FILE_EXTENSIONS) uriExt else null
            }

            if (extension == null || extension !in com.example.booknest.utils.FileUploadManager.BOOK_FILE_EXTENSIONS) {
                return@withContext null
            }

            val sanitizedFileName = fileName?.let { origName ->
                val ext = origName.substringAfterLast('.', "").substringBefore('?', "")
                    .substringBefore('(', "").trim().lowercase()
                if (ext in com.example.booknest.utils.FileUploadManager.BOOK_FILE_EXTENSIONS) {
                    val nameWithoutExt = origName.substringBeforeLast('.', origName)
                        .replace(" ", "_").replace("[^a-zA-Z0-9._-]".toRegex(), "_")
                    "$nameWithoutExt.$ext"
                } else {
                    "book_file_${System.currentTimeMillis()}.$extension"
                }
            } ?: "book_file_${System.currentTimeMillis()}.$extension"

            val tempFile = java.io.File(context.cacheDir, "temp_$sanitizedFileName")
            tempFile.outputStream().use { output -> inputStream.copyTo(output) }
            tempFile
        } catch (e: Exception) {
            null
        }
    }
}
