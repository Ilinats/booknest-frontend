package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.AuthorFollowResponse
import com.example.booknest.domain.model.response.AuthorFollowWithStatsResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.repository.AuthorFollowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthorFollowViewModel(
    private val authorFollowRepository: AuthorFollowRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _followedAuthors = MutableStateFlow<List<AuthorFollowResponse>>(emptyList())
    val followedAuthors: StateFlow<List<AuthorFollowResponse>> = _followedAuthors.asStateFlow()

    private val _followedAuthorsWithStats =
        MutableStateFlow<List<AuthorFollowWithStatsResponse>>(emptyList())
    val followedAuthorsWithStats: StateFlow<List<AuthorFollowWithStatsResponse>> =
        _followedAuthorsWithStats.asStateFlow()

    private val _authorFollowers = MutableStateFlow<List<AuthorFollowResponse>>(emptyList())
    val authorFollowers: StateFlow<List<AuthorFollowResponse>> = _authorFollowers.asStateFlow()

    private val _booksFromFollowedAuthors =
        MutableStateFlow<List<RecommendedBookResponse>>(emptyList())
    val booksFromFollowedAuthors: StateFlow<List<RecommendedBookResponse>> =
        _booksFromFollowedAuthors.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingAuthors = MutableStateFlow<Set<String>>(emptySet())
    val loadingAuthors: StateFlow<Set<String>> = _loadingAuthors.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun isAuthorLoading(authorId: String): Boolean {
        return _loadingAuthors.value.contains(authorId)
    }

    fun loadFollowedAuthors() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val result = authorFollowRepository.getFollowedAuthors()
                result
                    .onSuccess { authors ->
                        _followedAuthors.value = authors
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load followed authors"
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
                val result = authorFollowRepository.getFollowedAuthorsWithStats()
                result
                    .onSuccess { authors ->
                        _followedAuthorsWithStats.value = authors
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load followed authors with stats"
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
                val result = authorFollowRepository.getAuthorFollowers(authorId)
                result
                    .onSuccess { followers ->
                        _authorFollowers.value = followers
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load author followers"
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
                val result = authorFollowRepository.getBooksFromFollowedAuthors()
                result
                    .onSuccess { books ->
                        _booksFromFollowedAuthors.value = books
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load books from followed authors"
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                println("Books from followed authors exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun followAuthor(authorId: String) {
        viewModelScope.launch {
            try {
                _loadingAuthors.value = _loadingAuthors.value + authorId
                _isLoading.value = true
                _error.value = null

                val optimisticFollow = AuthorFollowResponse(
                    id = "temp_$authorId",
                    authorId = authorId,
                    followerId = sessionManager.currentUser.value?.id ?: "",
                    createdAt = System.currentTimeMillis().toString()
                )
                _followedAuthors.value = _followedAuthors.value + optimisticFollow

                val result = authorFollowRepository.followAuthor(authorId)
                result
                    .onSuccess {
                        loadFollowedAuthors()
                    }
                    .onFailure { e ->
                        _followedAuthors.value =
                            _followedAuthors.value.filter { it.authorId != authorId }
                        _error.value = e.message ?: "Failed to follow author"
                    }
            } catch (e: Exception) {
                _followedAuthors.value = _followedAuthors.value.filter { it.authorId != authorId }
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _loadingAuthors.value = _loadingAuthors.value - authorId
                _isLoading.value = false
            }
        }
    }

    fun unfollowAuthor(authorId: String) {
        viewModelScope.launch {
            try {
                _loadingAuthors.value = _loadingAuthors.value + authorId
                _isLoading.value = true
                _error.value = null

                val itemToRemove = _followedAuthors.value.find { it.authorId == authorId }

                _followedAuthors.value = _followedAuthors.value.filter { it.authorId != authorId }

                val result = authorFollowRepository.unfollowAuthor(authorId)
                result
                    .onSuccess {
                        loadFollowedAuthors()
                    }
                    .onFailure { e ->
                        itemToRemove?.let {
                            _followedAuthors.value = _followedAuthors.value + it
                        }
                        _error.value = e.message ?: "Failed to unfollow author"
                    }
            } catch (e: Exception) {
                val itemToRestore = _followedAuthors.value.find { it.authorId == authorId }
                if (itemToRestore == null) {
                    loadFollowedAuthors()
                }
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _loadingAuthors.value = _loadingAuthors.value - authorId
                _isLoading.value = false
            }
        }
    }

    fun checkIfFollowingAuthor(authorId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val result = authorFollowRepository.checkIfFollowingAuthor(authorId)
                result.onSuccess { followMap ->
                    onResult(followMap["isFollowing"] == true)
                }.onFailure {
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
