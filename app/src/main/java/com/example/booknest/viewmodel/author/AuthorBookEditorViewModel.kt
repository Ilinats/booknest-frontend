package com.example.booknest.viewmodel.author

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.request.CreateBookRequest
import com.example.booknest.domain.model.request.UpdateBookRequest
import com.example.booknest.domain.model.response.BookLeakFingerprintResponse
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.usecase.author.CreateBookUseCase
import com.example.booknest.domain.usecase.author.DecodeBookLeakFingerprintUseCase
import com.example.booknest.domain.usecase.author.UpdateBookUseCase
import com.example.booknest.domain.usecase.files.RemoveBookCoverImageUseCase
import com.example.booknest.domain.usecase.files.UploadBookCoverImageUseCase
import com.example.booknest.domain.usecase.files.UploadBookFileUseCase
import com.example.booknest.presentation.common.UiState
import com.example.booknest.utils.DebugLog
import com.example.booknest.viewmodel.common.UserFeedback
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class AuthorBookEditorViewModel(
    private val feedback: UserFeedback,
    private val createBookUseCase: CreateBookUseCase,
    private val updateBookUseCase: UpdateBookUseCase,
    private val uploadBookFileUseCase: UploadBookFileUseCase,
    private val uploadBookCoverImageUseCase: UploadBookCoverImageUseCase,
    private val removeBookCoverImageUseCase: RemoveBookCoverImageUseCase,
    private val decodeBookLeakFingerprintUseCase: DecodeBookLeakFingerprintUseCase,
    private val catalogRefresher: AuthorBooksCatalogRefresher,
) : ViewModel() {

    private val _bookCreationState = MutableStateFlow<UiState<BookResponse>>(UiState.Idle)
    val bookCreationState: StateFlow<UiState<BookResponse>> = _bookCreationState.asStateFlow()

    private val _coverImageUploadState = MutableStateFlow<UiState<Pair<String, String>>>(UiState.Idle)
    val coverImageUploadState: StateFlow<UiState<Pair<String, String>>> = _coverImageUploadState.asStateFlow()

    private val _coverImageRemovalState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val coverImageRemovalState: StateFlow<UiState<Unit>> = _coverImageRemovalState.asStateFlow()

    private val _bookFileUploadState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val bookFileUploadState: StateFlow<UiState<String>> = _bookFileUploadState.asStateFlow()

    private val _leakFingerprintState =
        MutableStateFlow<UiState<BookLeakFingerprintResponse>>(UiState.Idle)
    val leakFingerprintState: StateFlow<UiState<BookLeakFingerprintResponse>> =
        _leakFingerprintState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    private fun notifyError(message: String) = feedback.error(message, _error)
    private fun notifySuccess(message: String) = feedback.success(message, _successMessage)

    fun clearLeakFingerprintState() {
        _leakFingerprintState.value = UiState.Idle
    }

    fun clearBookCreationState() {
        _bookCreationState.value = UiState.Idle
        _bookFileUploadState.value = UiState.Idle
    }

    fun decodeLeakFingerprint(bookId: String, fileUri: Uri, context: Context) {
        viewModelScope.launch(NonCancellable) {
            try {
                _leakFingerprintState.value = UiState.Loading
                val mimeType = withContext(Dispatchers.IO) { context.contentResolver.getType(fileUri) }
                val file = withContext(Dispatchers.IO) { uriToFileForBook(context, fileUri, mimeType) }
                    ?: run {
                        _leakFingerprintState.value =
                            UiState.Error("Only PDF and EPUB files are supported")
                        return@launch
                    }
                val uploadManager = com.example.booknest.utils.FileUploadManager(context)
                val validationResult = uploadManager.validateBookFile(file)
                if (validationResult is com.example.booknest.utils.FileUploadManager.ValidationResult.Error) {
                    _leakFingerprintState.value = UiState.Error(validationResult.message)
                    withContext(Dispatchers.IO) {
                        try {
                            if (file.exists()) file.delete()
                        } catch (e: Exception) {
                            DebugLog.w("AuthorBookEditorVM", "Temp file delete failed", e)
                        }
                    }
                    return@launch
                }
                val multipartBody = uploadManager.createMultipartBody(file)
                decodeBookLeakFingerprintUseCase(bookId, multipartBody)
                    .onSuccess { data ->
                        _leakFingerprintState.value = UiState.Success(data)
                    }
                    .onFailure { e ->
                        if (e !is CancellationException) {
                            _leakFingerprintState.value =
                                UiState.Error(e.message ?: "Could not read fingerprint from file", e)
                        }
                    }
                withContext(Dispatchers.IO) {
                    try {
                        if (file.exists()) file.delete()
                    } catch (e: Exception) {
                        DebugLog.w("AuthorBookEditorVM", "Temp file delete failed after leak decode", e)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _leakFingerprintState.value =
                        UiState.Error(e.message ?: "Could not read fingerprint from file", e)
                }
            }
        }
    }

    fun createBook(
        book: CreateBookRequest,
        fileUri: Uri? = null,
        coverImageUri: Uri? = null,
        context: Context? = null,
    ) {
        viewModelScope.launch(NonCancellable) {
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
                    val file = withContext(Dispatchers.IO) { uriToFileForBook(contextNonNull, uri, mimeType) }
                        ?: run {
                            _bookCreationState.value =
                                UiState.Error("File type not allowed. Allowed types: pdf, epub")
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
                createBookUseCase(bookWithoutCover, filePart)
                    .onSuccess { createdBook ->
                        val bookWithCover = if (coverImageUri != null) {
                            uploadCoverImageInternal(createdBook.id, coverImageUri, context!!)
                                .getOrElse { e ->
                                    _bookCreationState.value =
                                        UiState.Error(
                                            e.message ?: "Book created but cover image upload failed",
                                        )
                                    return@launch
                                }
                        } else {
                            createdBook
                        }
                        notifySuccess("Book created successfully!")
                        _bookCreationState.value = UiState.Success(bookWithCover)
                        catalogRefresher.requestRefresh()
                    }
                    .onFailure { e ->
                        if (e !is CancellationException) {
                            _bookCreationState.value = UiState.Error(e.message ?: "Failed to create book")
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _bookCreationState.value = UiState.Error(e.message ?: "Error creating book")
                }
            }
        }
    }

    fun updateBook(bookId: String, book: UpdateBookRequest) {
        viewModelScope.launch {
            try {
                updateBookUseCase(bookId, book)
                    .onSuccess {
                        notifySuccess("Book updated successfully!")
                        catalogRefresher.requestRefresh()
                    }
                    .onFailure { e -> notifyError(e.message ?: "Failed to update book") }
            } catch (e: Exception) {
                notifyError(e.message ?: "Error updating book")
            }
        }
    }

    fun uploadBookFile(
        bookId: String,
        fileUri: Uri,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch(NonCancellable) {
            try {
                _bookFileUploadState.value = UiState.Loading
                val mimeType = withContext(Dispatchers.IO) { context.contentResolver.getType(fileUri) }
                val file = withContext(Dispatchers.IO) { uriToFileForBook(context, fileUri, mimeType) }
                    ?: run {
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
                uploadBookFileUseCase(bookId, multipartBody)
                    .onSuccess {
                        _bookFileUploadState.value = UiState.Success(bookId)
                        onSuccess()
                        withContext(Dispatchers.IO) {
                            try {
                                if (file.exists()) file.delete()
                            } catch (e: Exception) {
                                DebugLog.w("AuthorBookEditorVM", "Temp file delete failed after book upload", e)
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

    fun uploadBookCoverImage(bookId: String, imageUri: Uri, context: Context) {
        viewModelScope.launch(NonCancellable) {
            try {
                _coverImageUploadState.value = UiState.Loading
                uploadCoverImageInternal(bookId, imageUri, context)
                    .onSuccess { bookResponse ->
                        val coverUrl = bookResponse.coverImageUrl.orEmpty()
                        _coverImageUploadState.value = UiState.Success(bookId to coverUrl)
                        notifySuccess("Cover image uploaded successfully")
                        catalogRefresher.requestRefresh()
                    }
                    .onFailure { e ->
                        if (e !is CancellationException) {
                            _coverImageUploadState.value =
                                UiState.Error(e.message ?: "Failed to upload cover image")
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _coverImageUploadState.value = UiState.Error(e.message ?: "Error uploading cover image")
                }
            }
        }
    }

    private suspend fun uploadCoverImageInternal(
        bookId: String,
        imageUri: Uri,
        context: Context,
    ): Result<BookResponse> {
        val mimeType = withContext(Dispatchers.IO) {
            context.contentResolver.getType(imageUri) ?: "image/png"
        }
        val file = withContext(Dispatchers.IO) { uriToFile(context, imageUri, mimeType) }
            ?: return Result.failure(Exception("Failed to process image file"))
        return try {
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
            val requestFile = file.asRequestBody(finalMimeType.toMediaType())
            val multipartBody = MultipartBody.Part.createFormData("cover", file.name, requestFile)
            uploadBookCoverImageUseCase(bookId, multipartBody)
        } finally {
            withContext(Dispatchers.IO) {
                try {
                    if (file.exists()) file.delete()
                } catch (e: Exception) {
                    DebugLog.w("AuthorBookEditorVM", "Temp file delete failed after cover upload", e)
                }
            }
        }
    }

    fun removeBookCoverImage(bookId: String) {
        viewModelScope.launch(NonCancellable) {
            try {
                _coverImageRemovalState.value = UiState.Loading
                removeBookCoverImageUseCase(bookId)
                    .onSuccess {
                        _coverImageRemovalState.value = UiState.Success(Unit)
                        notifySuccess("Cover image removed successfully")
                        catalogRefresher.requestRefresh()
                    }
                    .onFailure { e ->
                        _coverImageRemovalState.value =
                            UiState.Error(e.message ?: "Failed to remove cover image")
                    }
            } catch (e: Exception) {
                _coverImageRemovalState.value = UiState.Error(e.message ?: "Error removing cover image")
            }
        }
    }

    private suspend fun uriToFile(context: Context, uri: Uri, mimeType: String? = null): java.io.File? =
        withContext(Dispatchers.IO) {
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
        context: Context,
        uri: Uri,
        mimeType: String? = null,
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
                mimeType != null -> when {
                    mimeType.contains("pdf", ignoreCase = true) -> "pdf"
                    mimeType.contains("epub", ignoreCase = true) ||
                        mimeType.contains("application/epub", ignoreCase = true) -> "epub"
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
