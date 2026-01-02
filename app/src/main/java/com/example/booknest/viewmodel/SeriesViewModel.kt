package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.request.CreateSeriesRequest
import com.example.booknest.domain.model.request.UpdateSeriesRequest
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.SeriesResponse
import com.example.booknest.domain.repository.SeriesRepository
import com.example.booknest.domain.usecase.author.GetMySeriesUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SeriesViewModel(
    private val getMySeriesUseCase: GetMySeriesUseCase,
    private val seriesRepository: SeriesRepository
) : ViewModel() {

    private val _series = MutableStateFlow<List<SeriesResponse>>(emptyList())
    val series: StateFlow<List<SeriesResponse>> = _series.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private val _seriesBooks = MutableStateFlow<Map<String, List<BookResponse>>>(emptyMap())
    val seriesBooks: StateFlow<Map<String, List<BookResponse>>> = _seriesBooks.asStateFlow()

    fun loadMySeries() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = getMySeriesUseCase()
                result
                    .onSuccess { seriesList ->
                        _series.value = seriesList
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to load series")
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error loading series: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createSeries(name: String, description: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = CreateSeriesRequest(name = name, description = description)
                val result = seriesRepository.createSeries(request)
                result
                    .onSuccess { series ->
                        _snackbarEvent.emit("Series created successfully!")
                        loadMySeries()
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to create series")
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error creating series: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSeries(seriesId: String, name: String, description: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = UpdateSeriesRequest(name = name, description = description)
                val result = seriesRepository.updateSeries(seriesId, request)
                result
                    .onSuccess { updatedSeries ->
                        _snackbarEvent.emit("Series updated successfully!")
                        loadMySeries()
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to update series")
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error updating series: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSeries(seriesId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = seriesRepository.deleteSeries(seriesId)
                result
                    .onSuccess {
                        _snackbarEvent.emit("Series deleted successfully!")
                        loadMySeries()
                        _seriesBooks.value = _seriesBooks.value - seriesId
                    }
                    .onFailure { e ->
                        _snackbarEvent.emit(e.message ?: "Failed to delete series")
                    }
            } catch (e: Exception) {
                _snackbarEvent.emit("Error deleting series: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getBookCountForSeries(seriesId: String): Int {
        return _seriesBooks.value[seriesId]?.size ?: 0
    }

    fun hasBooks(seriesId: String): Boolean {
        return getBookCountForSeries(seriesId) > 0
    }
}

