package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.request.CreateSeriesRequest
import com.example.booknest.domain.model.request.UpdateSeriesRequest
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.SeriesResponse
import com.example.booknest.domain.repository.SeriesRepository
import com.example.booknest.domain.usecase.author.GetMySeriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.booknest.ui.toast.GlobalToastHandler

class SeriesViewModel(
    private val getMySeriesUseCase: GetMySeriesUseCase,
    private val seriesRepository: SeriesRepository
) : ViewModel() {

    private val _series = MutableStateFlow<List<SeriesResponse>>(emptyList())
    val series: StateFlow<List<SeriesResponse>> = _series.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


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
                        GlobalToastHandler.showError(e.message ?: "Failed to load series")
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError("Error loading series: ${e.message}")
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
                        GlobalToastHandler.showSuccess("Series created successfully!")
                        loadMySeries()
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e.message ?: "Failed to create series")
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError("Error creating series: ${e.message}")
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
                        GlobalToastHandler.showSuccess("Series updated successfully!")
                        loadMySeries()
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e.message ?: "Failed to update series")
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError("Error updating series: ${e.message}")
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
                        GlobalToastHandler.showSuccess("Series deleted successfully!")
                        loadMySeries()
                        _seriesBooks.value = _seriesBooks.value - seriesId
                    }
                    .onFailure { e ->
                        GlobalToastHandler.showError(e.message ?: "Failed to delete series")
                    }
            } catch (e: Exception) {
                GlobalToastHandler.showError("Error deleting series: ${e.message}")
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

