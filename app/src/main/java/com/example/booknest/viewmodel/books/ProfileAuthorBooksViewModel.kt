package com.example.booknest.viewmodel.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.usecase.books.BrowseBooksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileAuthorBooksViewModel(
    private val browseBooksUseCase: BrowseBooksUseCase,
    private val bookCatalogCache: BookCatalogCache,
) : ViewModel() {

    private val _authorBooks = MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val authorBooks: StateFlow<List<RecommendedBookResponse>> = _authorBooks.asStateFlow()

    private val _authorBooksLoading = MutableStateFlow(false)
    val authorBooksLoading: StateFlow<Boolean> = _authorBooksLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadAuthorBooks(authorId: String, @Suppress("UNUSED_PARAMETER") authorName: String?) {
        viewModelScope.launch {
            try {
                _authorBooksLoading.value = true
                browseBooksUseCase(
                    authorId = authorId,
                    page = 1,
                    limit = 100,
                )
                    .onSuccess { books ->
                        _authorBooks.value = books
                        bookCatalogCache.register(books)
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load author books"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _authorBooksLoading.value = false
            }
        }
    }
}
