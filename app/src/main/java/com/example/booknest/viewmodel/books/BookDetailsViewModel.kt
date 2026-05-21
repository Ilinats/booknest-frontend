package com.example.booknest.viewmodel.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.usecase.books.GetBookDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookDetailsViewModel(
    private val getBookDetailsUseCase: GetBookDetailsUseCase,
    private val bookCatalogCache: BookCatalogCache,
) : ViewModel() {

    private val _bookDetails = MutableStateFlow<BookResponse?>(null)
    val bookDetails: StateFlow<BookResponse?> = _bookDetails.asStateFlow()

    private val _bookDetailsScreenId = MutableStateFlow<String?>(null)
    private val _bookDetailsScreenMerged = MutableStateFlow<BookResponse?>(null)
    private val _bookDetailsScreenLoading = MutableStateFlow(false)
    val bookDetailsScreenBook: StateFlow<BookResponse?> = _bookDetailsScreenMerged.asStateFlow()
    val bookDetailsScreenLoading: StateFlow<Boolean> = _bookDetailsScreenLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    private fun mergeDetailsScreenBook(partial: BookResponse?, full: BookResponse): BookResponse {
        if (partial == null) return full
        if (full.fullDescription != null && partial.fullDescription == null) return full
        return partial
    }

    fun beginBookDetailsScreen(bookId: String) {
        if (bookId.isBlank()) return
        _bookDetailsScreenId.value = bookId
        val cached = bookCatalogCache.findBook(bookId)
        _bookDetailsScreenMerged.value = cached
        _bookDetailsScreenLoading.value = cached == null
        getBookDetails(bookId, syncDetailsScreen = true)
    }

    fun refreshBookDetailsScreenFromCache() {
        val id = _bookDetailsScreenId.value ?: return
        if (_bookDetailsScreenMerged.value == null) {
            val fromCache = bookCatalogCache.findBook(id)
            if (fromCache != null) {
                _bookDetailsScreenMerged.value = fromCache
                _bookDetailsScreenLoading.value = false
            }
        }
    }

    fun applyBookDetailsFromApplicationCheck(stub: BookResponse?) {
        val id = _bookDetailsScreenId.value ?: return
        if (stub == null || stub.id != id) return
        if (_bookDetailsScreenMerged.value == null) {
            _bookDetailsScreenMerged.value = stub
            _bookDetailsScreenLoading.value = false
        }
    }

    fun getBookDetails(bookId: String, syncDetailsScreen: Boolean = false) {
        viewModelScope.launch {
            try {
                getBookDetailsUseCase(bookId)
                    .onSuccess { book ->
                        bookCatalogCache.registerFull(book)
                        _bookDetails.value = book
                        if (syncDetailsScreen && _bookDetailsScreenId.value == bookId) {
                            _bookDetailsScreenMerged.value =
                                mergeDetailsScreenBook(_bookDetailsScreenMerged.value, book)
                            _bookDetailsScreenLoading.value = false
                        }
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load book details"
                        if (syncDetailsScreen && _bookDetailsScreenId.value == bookId) {
                            _bookDetailsScreenLoading.value = false
                        }
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load book details"
                if (syncDetailsScreen && _bookDetailsScreenId.value == bookId) {
                    _bookDetailsScreenLoading.value = false
                }
            }
        }
    }

    fun calculateRating(bookRating: Double?, reviews: List<ReviewResponse>): Double {
        return if (bookRating == null || bookRating == 0.0) {
            if (reviews.isNotEmpty()) {
                reviews.map { it.rating.toDouble() }.average()
            } else {
                0.0
            }
        } else {
            bookRating
        }
    }
}
