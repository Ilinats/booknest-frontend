package com.example.booknest.viewmodel.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.request.CreateReviewRequest
import com.example.booknest.domain.model.request.UpdateReviewRequest
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.usecase.reviews.CreateReviewUseCase
import com.example.booknest.domain.usecase.reviews.GetBookAllReviewsUseCase
import com.example.booknest.domain.usecase.reviews.GetBookReviewsUseCase
import com.example.booknest.domain.usecase.reviews.GetReviewUseCase
import com.example.booknest.domain.usecase.reviews.GetUserReviewsUseCase
import com.example.booknest.domain.usecase.reviews.UpdateReviewUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ReviewType(val value: String) {
    TEXT("text"),
    LINK("link")
}

class ReviewViewModel(
    private val getBookReviewsUseCase: GetBookReviewsUseCase,
    private val getUserReviewsUseCase: GetUserReviewsUseCase,
    private val getReviewUseCase: GetReviewUseCase,
    private val createReviewUseCase: CreateReviewUseCase,
    private val updateReviewUseCase: UpdateReviewUseCase,
    private val getBookAllReviewsUseCase: GetBookAllReviewsUseCase
) : ViewModel() {

    private val _bookReviews = MutableStateFlow<List<ReviewResponse>>(emptyList())
    val bookReviews: StateFlow<List<ReviewResponse>> = _bookReviews.asStateFlow()

    private val _userReviews = MutableStateFlow<List<ReviewResponse>>(emptyList())
    val userReviews: StateFlow<List<ReviewResponse>> = _userReviews.asStateFlow()

    private val _currentReview = MutableStateFlow<ReviewResponse?>(null)
    val currentReview: StateFlow<ReviewResponse?> = _currentReview.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun clearError() { _error.value = null }
    fun clearSuccessMessage() { _successMessage.value = null }

    fun loadBookReviews(bookId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = getBookReviewsUseCase(bookId)
                result
                    .onSuccess { reviews ->
                        _bookReviews.value = reviews
                    }
                    .onFailure { _ ->
                        val fallbackResult = getBookAllReviewsUseCase(bookId)
                        fallbackResult
                            .onSuccess { reviews ->
                                _bookReviews.value = reviews
                            }
                            .onFailure { fallbackError ->
                                _error.value = fallbackError.message ?: "Failed to load book reviews"
                            }
                    }
            } catch (e: Exception) {
                try {
                    val fallbackResult = getBookAllReviewsUseCase(bookId)
                    fallbackResult
                        .onSuccess { reviews ->
                            _bookReviews.value = reviews
                        }
                        .onFailure { fallbackError ->
                            _error.value = "Error loading book reviews: ${fallbackError.message}"
                        }
                } catch (fallbackException: Exception) {
                    _error.value = "Error loading book reviews: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserReviews(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = getUserReviewsUseCase(userId)
                result
                    .onSuccess { reviews ->
                        _userReviews.value = reviews
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load user reviews"
                    }
            } catch (e: Exception) {
                _error.value = "Error loading user reviews: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadReview(reviewId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = getReviewUseCase(reviewId)
                result
                    .onSuccess { review ->
                        _currentReview.value = review
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to load review"
                    }
            } catch (e: Exception) {
                _error.value = "Error loading review: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createReview(
        applicationId: String,
        rating: Double,
        reviewType: ReviewType,
        reviewContent: String? = null,
        reviewUrls: List<String>? = null,
        isPublic: Boolean = true
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = CreateReviewRequest(
                    applicationId = applicationId,
                    rating = rating,
                    reviewType = reviewType.value,
                    reviewContent = reviewContent,
                    reviewUrls = reviewUrls,
                    isPublic = isPublic
                )
                val result = createReviewUseCase(request)
                result
                    .onSuccess { review ->
                        _successMessage.value = "Review submitted successfully!"
                        if (review.application?.bookId != null) {
                            loadBookReviews(review.application.bookId)
                        }
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to submit review"
                    }
            } catch (e: Exception) {
                _error.value = "Error submitting review: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateReview(
        reviewId: String,
        rating: Double? = null,
        reviewType: ReviewType? = null,
        reviewContent: String? = null,
        reviewUrls: List<String>? = null,
        isPublic: Boolean? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = UpdateReviewRequest(
                    rating = rating,
                    reviewType = reviewType?.value,
                    reviewContent = reviewContent,
                    reviewUrls = reviewUrls,
                    isPublic = isPublic
                )
                val result = updateReviewUseCase(reviewId, request)
                result
                    .onSuccess { review ->
                        _successMessage.value = "Review updated successfully!"
                        _currentReview.value = review
                        if (review.application?.bookId != null) {
                            loadBookReviews(review.application.bookId)
                        }
                    }
                    .onFailure { e ->
                        _error.value = e.message ?: "Failed to update review"
                    }
            } catch (e: Exception) {
                _error.value = "Error updating review: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
