package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.AuthManager
import com.example.booknest.network.ApiService
import com.example.booknest.network.AuthorFollow
import com.example.booknest.network.AuthorFollowWithStats
import com.example.booknest.network.Book
import com.example.booknest.network.RecommendedBook
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthorFollowViewModel(
    private val apiService: ApiService,
    private val authManager: AuthManager
) : ViewModel() {
    
    private val _followedAuthors = MutableStateFlow<List<AuthorFollow>>(emptyList())
    val followedAuthors: StateFlow<List<AuthorFollow>> = _followedAuthors.asStateFlow()
    
    private val _followedAuthorsWithStats = MutableStateFlow<List<AuthorFollowWithStats>>(emptyList())
    val followedAuthorsWithStats: StateFlow<List<AuthorFollowWithStats>> = _followedAuthorsWithStats.asStateFlow()
    
    private val _authorFollowers = MutableStateFlow<List<AuthorFollow>>(emptyList())
    val authorFollowers: StateFlow<List<AuthorFollow>> = _authorFollowers.asStateFlow()
    
    private val _booksFromFollowedAuthors = MutableStateFlow<List<Book>>(emptyList())
    val booksFromFollowedAuthors: StateFlow<List<Book>> = _booksFromFollowedAuthors.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadFollowedAuthors() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.getFollowedAuthors()
                if (response.isSuccessful) {
                    _followedAuthors.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "Failed to load followed authors"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadFollowedAuthorsWithStats() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.getFollowedAuthorsWithStats()
                if (response.isSuccessful) {
                    _followedAuthorsWithStats.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "Failed to load followed authors with stats"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadAuthorFollowers(authorId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.getAuthorFollowers(authorId)
                if (response.isSuccessful) {
                    _authorFollowers.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "Failed to load author followers"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadBooksFromFollowedAuthors() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.getBooksFromFollowedAuthors()
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        // Convert RecommendedBook to Book for UI compatibility
                        val books = (apiResponse.data ?: emptyList()).map { recommendedBook ->
                            recommendedBook.toBook()
                        }
                        _booksFromFollowedAuthors.value = books
                    } else {
                        _error.value = "Failed to load books from followed authors: ${apiResponse.message}"
                    }
                } else {
                    _error.value = "Failed to load books from followed authors"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                println("Books from followed authors exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Extension function to convert RecommendedBook to Book
    private fun RecommendedBook.toBook(): Book {
        return Book(
            id = this.id,
            authorId = "", // Required but not available in RecommendedBook
            title = this.title,
            shortDescription = null,
            fullDescription = null,
            coverImageUrl = this.coverImageUrl,
            pageCount = null,
            ageRating = null,
            distributionType = null,
            fileUrl = null,
            fileSize = null,
            fileType = null,
            totalCopies = null,
            availableCopies = null,
            applicationDeadline = null,
            reviewDeadlineDays = null,
            selectionCriteria = null,
            selectionMethod = null,
            status = null, // Required but not available in RecommendedBook
            createdAt = null,
            updatedAt = null,
            publishedAt = this.publishedAt, // Nullable in Book
            seriesId = null, // Required but not available in RecommendedBook
            seriesOrder = this.seriesOrder,
            seriesName = this.seriesName,
            authorName = this.authorName,
            author = null,
            rating = this.rating,
            genres = null
        )
    }
    
    fun followAuthor(authorId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.followAuthor(authorId)
                if (response.isSuccessful) {
                    // Refresh followed authors
                    loadFollowedAuthors()
                } else {
                    _error.value = "Failed to follow author"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun unfollowAuthor(authorId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = apiService.unfollowAuthor(authorId)
                if (response.isSuccessful) {
                    // Refresh followed authors
                    loadFollowedAuthors()
                } else {
                    _error.value = "Failed to unfollow author"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun checkIfFollowingAuthor(authorId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.checkIfFollowingAuthor(authorId)
                if (response.isSuccessful) {
                    val isFollowing = response.body()?.data?.get("isFollowing") ?: false
                    onResult(isFollowing)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

class AuthorFollowViewModelFactory(
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthorFollowViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthorFollowViewModel(
                apiService = authManager.apiService,
                authManager = authManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
