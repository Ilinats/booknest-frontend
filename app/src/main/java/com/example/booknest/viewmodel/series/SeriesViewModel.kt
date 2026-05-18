package com.example.booknest.viewmodel.series

import androidx.lifecycle.ViewModel
import com.example.booknest.viewmodel.common.UserFeedback
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.request.CreateSeriesRequest
import com.example.booknest.domain.model.request.UpdateSeriesRequest
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.SeriesResponse
import com.example.booknest.domain.usecase.author.GetMySeriesUseCase
import com.example.booknest.domain.usecase.series.CreateSeriesUseCase
import com.example.booknest.domain.usecase.series.DeleteSeriesUseCase
import com.example.booknest.domain.usecase.series.UpdateSeriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SeriesViewModel(
    private val feedback: UserFeedback,
    private val getMySeriesUseCase: GetMySeriesUseCase,
    private val createSeriesUseCase: CreateSeriesUseCase,
    private val updateSeriesUseCase: UpdateSeriesUseCase,
    private val deleteSeriesUseCase: DeleteSeriesUseCase
) : ViewModel() {

    private val _series = MutableStateFlow<List<SeriesResponse>>(emptyList())
    val series: StateFlow<List<SeriesResponse>> = _series.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _seriesBooks = MutableStateFlow<Map<String, List<BookResponse>>>(emptyMap())
    val seriesBooks: StateFlow<Map<String, List<BookResponse>>> = _seriesBooks.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun clearError() { _error.value = null }
    fun clearSuccessMessage() { _successMessage.value = null }

    private fun notifyError(message: String) = feedback.error(message, _error)
    private fun notifySuccess(message: String) = feedback.success(message, _successMessage)

    fun loadMySeries() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = getMySeriesUseCase()
                result
                    .onSuccess { seriesList -> _series.value = seriesList }
                    .onFailure { e -> notifyError(e.message ?: "Failed to load series") }
            } catch (e: Exception) {
                notifyError(e.message ?: "Error loading series")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createSeries(name: String, description: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = createSeriesUseCase(CreateSeriesRequest(name = name, description = description))
                result
                    .onSuccess {
                        notifySuccess("Series created successfully!")
                        loadMySeries()
                    }
                    .onFailure { e -> notifyError(e.message ?: "Failed to create series") }
            } catch (e: Exception) {
                notifyError(e.message ?: "Error creating series")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSeries(seriesId: String, name: String, description: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = updateSeriesUseCase(seriesId, UpdateSeriesRequest(name = name, description = description))
                result
                    .onSuccess {
                        notifySuccess("Series updated successfully!")
                        loadMySeries()
                    }
                    .onFailure { e -> notifyError(e.message ?: "Failed to update series") }
            } catch (e: Exception) {
                notifyError(e.message ?: "Error updating series")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSeries(seriesId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = deleteSeriesUseCase(seriesId)
                result
                    .onSuccess {
                        notifySuccess("Series deleted successfully!")
                        _seriesBooks.value = _seriesBooks.value - seriesId
                        loadMySeries()
                    }
                    .onFailure { e -> notifyError(e.message ?: "Failed to delete series") }
            } catch (e: Exception) {
                notifyError(e.message ?: "Error deleting series")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
