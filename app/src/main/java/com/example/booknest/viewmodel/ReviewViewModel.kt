package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.AuthManager
import com.example.booknest.network.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(private val authManager: AuthManager) : ViewModel() {

    // Review state
    private val _bookReviews = MutableStateFlow<List<Review>>(emptyList())
    val bookReviews: StateFlow<List<Review>> = _bookReviews

    private val _userReviews = MutableStateFlow<List<Review>>(emptyList())
    val userReviews: StateFlow<List<Review>> = _userReviews

    private val _featuredReviews = MutableStateFlow<List<Review>>(emptyList())
    val featuredReviews: StateFlow<List<Review>> = _featuredReviews

    private val _currentReview = MutableStateFlow<Review?>(null)
    val currentReview: StateFlow<Review?> = _currentReview

    // Common state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent

    // Review operations
    fun loadBookReviews(bookId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getBookReviews(bookId)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _bookReviews.value = apiResponse.data ?: emptyList()
                    } else {
                        _snackbarEvent.emit(apiResponse.message ?: "Failed to load book reviews")
                    }
                } else {
                    _snackbarEvent.emit("Failed to load book reviews: ${response.message()}")
                }
            } catch (e: Exception) {
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
                val response = RetrofitInstance.api.getUserReviews(userId)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _userReviews.value = apiResponse.data ?: emptyList()
                    } else {
                        _snackbarEvent.emit(apiResponse.message ?: "Failed to load user reviews")
                    }
                } else {
                    _snackbarEvent.emit("Failed to load user reviews: ${response.message()}")
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
                val response = RetrofitInstance.api.getFeaturedReviews()
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _featuredReviews.value = apiResponse.data ?: emptyList()
                    } else {
                        _snackbarEvent.emit(apiResponse.message ?: "Failed to load featured reviews")
                    }
                } else {
                    _snackbarEvent.emit("Failed to load featured reviews: ${response.message()}")
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
                val response = RetrofitInstance.api.getReview(reviewId)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _currentReview.value = apiResponse.data
                    } else {
                        _snackbarEvent.emit(apiResponse.message ?: "Failed to load review")
                    }
                } else {
                    _snackbarEvent.emit("Failed to load review: ${response.message()}")
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
                val response = RetrofitInstance.api.createReview(
                    CreateReviewDto(
                        applicationId = applicationId,
                        rating = rating,
                        reviewType = reviewType,
                        reviewContent = reviewContent,
                        reviewUrls = reviewUrls,
                        isPublic = isPublic
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _snackbarEvent.emit("Review submitted successfully!")
                        // Refresh relevant lists
                        val review = apiResponse.data
                        if (review?.application?.bookId != null) {
                            loadBookReviews(review.application.bookId)
                        }
                    } else {
                        _snackbarEvent.emit(apiResponse.message ?: "Failed to submit review")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    _snackbarEvent.emit("Failed to submit review: ${response.message()}")
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
                val response = RetrofitInstance.api.updateReview(
                    reviewId,
                    UpdateReviewDto(
                        rating = rating,
                        reviewType = reviewType,
                        reviewContent = reviewContent,
                        reviewUrls = reviewUrls,
                        isPublic = isPublic
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _snackbarEvent.emit("Review updated successfully!")
                        _currentReview.value = apiResponse.data
                        // Refresh relevant lists
                        val review = apiResponse.data
                        if (review?.application?.bookId != null) {
                            loadBookReviews(review.application.bookId)
                        }
                    } else {
                        _snackbarEvent.emit(apiResponse.message ?: "Failed to update review")
                    }
                } else {
                    _snackbarEvent.emit("Failed to update review: ${response.message()}")
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
                val response = RetrofitInstance.api.deleteReview(reviewId)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _snackbarEvent.emit("Review deleted successfully!")
                        _currentReview.value = null
                        // Refresh relevant lists
                        val currentReview = _currentReview.value
                        if (currentReview?.application?.bookId != null) {
                            loadBookReviews(currentReview.application.bookId)
                        }
                    } else {
                        _snackbarEvent.emit(apiResponse.message ?: "Failed to delete review")
                    }
                } else {
                    _snackbarEvent.emit("Failed to delete review: ${response.message()}")
                }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error deleting review: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Author operations
    fun featureReview(reviewId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.featureReview(reviewId)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _snackbarEvent.emit("Review featured!")
                        _currentReview.value = apiResponse.data
                        // Refresh relevant lists
                        val review = apiResponse.data
                        if (review?.application?.bookId != null) {
                            loadBookReviews(review.application.bookId)
                        }
                        loadFeaturedReviews()
                    } else {
                        _snackbarEvent.emit(apiResponse.message ?: "Failed to feature review")
                    }
                } else {
                    _snackbarEvent.emit("Failed to feature review: ${response.message()}")
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
                val response = RetrofitInstance.api.unfeatureReview(reviewId)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _snackbarEvent.emit("Review unfeatured!")
                        _currentReview.value = apiResponse.data
                        // Refresh relevant lists
                        val review = apiResponse.data
                        if (review?.application?.bookId != null) {
                            loadBookReviews(review.application.bookId)
                        }
                        loadFeaturedReviews()
                    } else {
                        _snackbarEvent.emit(apiResponse.message ?: "Failed to unfeature review")
                    }
                } else {
                    _snackbarEvent.emit("Failed to unfeature review: ${response.message()}")
                }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error unfeaturing review: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Helper functions
    fun getAverageRating(reviews: List<Review>): Double {
        if (reviews.isEmpty()) return 0.0
        return reviews.map { it.rating }.average()
    }

    fun getRatingDistribution(reviews: List<Review>): Map<Int, Int> {
        return reviews.groupingBy { it.rating }.eachCount()
    }

    fun canSubmitReview(application: Application): Boolean {
        return application.status == ApplicationStatus.APPROVED && 
               application.copyReceivedAt != null &&
               application.reviewSubmittedAt == null
    }

    fun canUpdateReadingStatus(application: Application, newStatus: ReadingStatus): Boolean {
        return when (application.status) {
            ApplicationStatus.APPROVED -> true
            else -> false
        }
    }
}

class ReviewViewModelFactory(private val authManager: AuthManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReviewViewModel(authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
