package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.request.CreateReviewRequest
import com.example.booknest.domain.model.request.UpdateReviewRequest
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.repository.ReviewsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ReviewType(val value: String) {
    TEXT("text"),
    LINK("link")
}

class ReviewViewModel(
    private val reviewsRepository: ReviewsRepository,
    private val booksRepository: com.example.booknest.domain.repository.BooksRepository
) : ViewModel() {

    private val _bookReviews = MutableStateFlow<List<ReviewResponse>>(emptyList())
    val bookReviews: StateFlow<List<ReviewResponse>> = _bookReviews

    private val _userReviews = MutableStateFlow<List<ReviewResponse>>(emptyList())
    val userReviews: StateFlow<List<ReviewResponse>> = _userReviews

    private val _featuredReviews = MutableStateFlow<List<ReviewResponse>>(emptyList())
    val featuredReviews: StateFlow<List<ReviewResponse>> = _featuredReviews

    private val _currentReview = MutableStateFlow<ReviewResponse?>(null)
    val currentReview: StateFlow<ReviewResponse?> = _currentReview

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent

    fun loadBookReviews(bookId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = booksRepository.getBookAllReviews(bookId)
                result
                    .onSuccess { reviews ->
                        _bookReviews.value = reviews
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to load book reviews")
                    }
            } catch (e: Exception) {
                println("DEBUG: Exception loading book reviews: ${e.message}")
                e.printStackTrace()
                _snackbarEvent.emit("Error loading book reviews: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserReviews(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = reviewsRepository.getUserReviews(userId)
                result
                    .onSuccess { reviews ->
                        _userReviews.value = reviews
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to load user reviews")
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error loading user reviews: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFeaturedReviews() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = reviewsRepository.getFeaturedReviews()
                result
                    .onSuccess { reviews ->
                        _featuredReviews.value = reviews
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to load featured reviews")
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error loading featured reviews: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadReview(reviewId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = reviewsRepository.getReview(reviewId)
                result
                    .onSuccess { review ->
                        _currentReview.value = review
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to load review")
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error loading review: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createReview(
        applicationId: String,
        rating: Int,
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
                val result = reviewsRepository.createReview(request)
                result
                    .onSuccess { review ->
                        _snackbarEvent.emit("Review submitted successfully!")
                        if (review.application?.bookId != null) {
                            loadBookReviews(review.application.bookId)
                        }
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to submit review")
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error submitting review: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateReview(
        reviewId: String,
        rating: Int? = null,
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
                val result = reviewsRepository.updateReview(reviewId, request)
                result
                    .onSuccess { review ->
                        _snackbarEvent.emit("Review updated successfully!")
                        _currentReview.value = review
                        if (review.application?.bookId != null) {
                            loadBookReviews(review.application.bookId)
                        }
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to update review")
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error updating review: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = reviewsRepository.deleteReview(reviewId)
                result
                    .onSuccess {
                        _snackbarEvent.emit("Review deleted successfully!")
                        val currentReview = _currentReview.value
                        _currentReview.value = null
                        if (currentReview?.application?.bookId != null) {
                            loadBookReviews(currentReview.application.bookId)
                        }
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to delete review")
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error deleting review: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun featureReview(reviewId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = reviewsRepository.featureReview(reviewId)
                result
                    .onSuccess { review ->
                        _snackbarEvent.emit("Review featured!")
                        _currentReview.value = review
                        if (review.application?.bookId != null) {
                            loadBookReviews(review.application.bookId)
                        }
                        loadFeaturedReviews()
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to feature review")
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error featuring review: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun unfeatureReview(reviewId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = reviewsRepository.unfeatureReview(reviewId)
                result
                    .onSuccess { review ->
                        _snackbarEvent.emit("Review unfeatured!")
                        _currentReview.value = review
                        if (review.application?.bookId != null) {
                            loadBookReviews(review.application.bookId)
                        }
                        loadFeaturedReviews()
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to unfeature review")
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error unfeaturing review: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAverageRating(reviews: List<ReviewResponse>): Double {
        if (reviews.isEmpty()) return 0.0
        return reviews.map { it.rating }.average()
    }

    fun getRatingDistribution(reviews: List<ReviewResponse>): Map<Int, Int> {
        return reviews.groupingBy { it.rating }.eachCount()
    }

    fun canSubmitReview(application: ApplicationResponse): Boolean {
        return application.status == "approved" &&
                application.copyReceivedAt != null &&
                application.reviewSubmittedAt == null
    }

    fun canUpdateReadingStatus(
        application: ApplicationResponse,
        newStatus: ReadingStatus
    ): Boolean {
        return application.status == "approved"
    }

    fun clearCurrentReview() {
        _currentReview.value = null
    }
}
