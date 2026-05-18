package com.example.booknest.viewmodel.author

import androidx.lifecycle.ViewModel
import com.example.booknest.viewmodel.common.UserFeedback
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.AuthorFollowResponse
import com.example.booknest.domain.model.response.AuthorFollowWithStatsResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.repository.AuthorFollowRepository
import com.example.booknest.domain.usecase.author.CheckIfFollowingAuthorUseCase
import com.example.booknest.domain.usecase.author.FollowAuthorUseCase
import com.example.booknest.domain.usecase.author.GetAuthorFollowersUseCase
import com.example.booknest.domain.usecase.author.GetBooksFromFollowedAuthorsUseCase
import com.example.booknest.domain.usecase.author.GetFollowedAuthorsUseCase
import com.example.booknest.domain.usecase.author.UnfollowAuthorUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthorFollowViewModel(
    private val feedback: UserFeedback,
    private val getFollowedAuthorsUseCase: GetFollowedAuthorsUseCase,
    private val getAuthorFollowersUseCase: GetAuthorFollowersUseCase,
    private val getBooksFromFollowedAuthorsUseCase: GetBooksFromFollowedAuthorsUseCase,
    private val followAuthorUseCase: FollowAuthorUseCase,
    private val unfollowAuthorUseCase: UnfollowAuthorUseCase,
    private val checkIfFollowingAuthorUseCase: CheckIfFollowingAuthorUseCase,
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

    private fun notifyError(message: String) = feedback.error(message, _error)
    private fun notifySuccess(message: String) = feedback.success(message)

    private val _followingStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val followingStatus: StateFlow<Map<String, Boolean>> = _followingStatus.asStateFlow()

    fun isAuthorLoading(authorId: String): Boolean {
        return _loadingAuthors.value.contains(authorId)
    }

    fun loadFollowedAuthors() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val result = getFollowedAuthorsUseCase()
                result
                    .onSuccess { authors ->
                        _followedAuthors.value = authors
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to load followed authors")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
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
                val result = getAuthorFollowersUseCase(authorId)
                result
                    .onSuccess { followers ->
                        _authorFollowers.value = followers
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to load author followers")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
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
                val result = getBooksFromFollowedAuthorsUseCase()
                result
                    .onSuccess { books ->
                        _booksFromFollowedAuthors.value = books
                    }
                    .onFailure { e ->
                        notifyError(e.message ?: "Failed to load books from followed authors")
                    }
            } catch (e: Exception) {
                notifyError(e.message ?: "Unknown error occurred")
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
                _followingStatus.value = _followingStatus.value + (authorId to true)

                val result = followAuthorUseCase(authorId)
                result
                    .onSuccess {
                        notifySuccess("Now following author")
                        loadFollowedAuthors()
                    }
                    .onFailure { e ->
                        _followedAuthors.value =
                            _followedAuthors.value.filter { it.authorId != authorId }
                        _followingStatus.value = _followingStatus.value + (authorId to false)
                        notifyError(e.message ?: "Failed to follow author")
                    }
            } catch (e: Exception) {
                _followedAuthors.value = _followedAuthors.value.filter { it.authorId != authorId }
                _followingStatus.value = _followingStatus.value + (authorId to false)
                notifyError(e.message ?: "Unknown error occurred")
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
                _followingStatus.value = _followingStatus.value + (authorId to false)

                val result = unfollowAuthorUseCase(authorId)
                result
                    .onSuccess {
                        notifySuccess("Unfollowed author")
                        loadFollowedAuthors()
                    }
                    .onFailure { e ->
                        itemToRemove?.let {
                            _followedAuthors.value = _followedAuthors.value + it
                        }
                        _followingStatus.value = _followingStatus.value + (authorId to true)
                        notifyError(e.message ?: "Failed to unfollow author")
                    }
            } catch (e: Exception) {
                val itemToRestore = _followedAuthors.value.find { it.authorId == authorId }
                if (itemToRestore == null) {
                    loadFollowedAuthors()
                }
                notifyError(e.message ?: "Unknown error occurred")
            } finally {
                _loadingAuthors.value = _loadingAuthors.value - authorId
                _isLoading.value = false
            }
        }
    }

    fun checkIfFollowingAuthor(authorId: String) {
        viewModelScope.launch {
            try {
                val result = checkIfFollowingAuthorUseCase(authorId)
                result.onSuccess { followMap ->
                    val isFollowing = followMap["isFollowing"] == true
                    _followingStatus.value = _followingStatus.value + (authorId to isFollowing)
                }.onFailure {
                    _followingStatus.value = _followingStatus.value + (authorId to false)
                }
            } catch (e: Exception) {
                _followingStatus.value = _followingStatus.value + (authorId to false)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
