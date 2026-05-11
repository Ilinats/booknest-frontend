package com.example.booknest.viewmodel.author

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.request.CreateSeriesRequest
import com.example.booknest.domain.model.request.UpdateSeriesRequest
import com.example.booknest.domain.model.response.SeriesResponse
import com.example.booknest.domain.usecase.author.GetMySeriesUseCase
import com.example.booknest.domain.usecase.series.CreateSeriesUseCase
import com.example.booknest.domain.usecase.series.UpdateSeriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthorSeriesViewModel(
    private val getMySeriesUseCase: GetMySeriesUseCase,
    private val createSeriesUseCase: CreateSeriesUseCase,
    private val updateSeriesUseCase: UpdateSeriesUseCase
) : ViewModel() {

    private val _mySeries = MutableStateFlow<List<SeriesResponse>>(emptyList())
    val mySeries: StateFlow<List<SeriesResponse>> = _mySeries.asStateFlow()

    private val _isLoadingSeries = MutableStateFlow(false)
    val isLoadingSeries: StateFlow<Boolean> = _isLoadingSeries.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() { _error.value = null }

    fun loadMySeries() {
        viewModelScope.launch {
            try {
                _isLoadingSeries.value = true
                val result = getMySeriesUseCase()
                result
                    .onSuccess { series -> _mySeries.value = series }
                    .onFailure { e -> _error.value = e.message ?: "Failed to load series" }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading series"
            } finally {
                _isLoadingSeries.value = false
            }
        }
    }

    fun createSeries(series: CreateSeriesRequest) {
        viewModelScope.launch {
            try {
                val result = createSeriesUseCase(series)
                result
                    .onSuccess { loadMySeries() }
                    .onFailure { e -> _error.value = e.message ?: "Failed to create series" }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error creating series"
            }
        }
    }

    fun updateSeries(seriesId: String, series: UpdateSeriesRequest) {
        viewModelScope.launch {
            try {
                val result = updateSeriesUseCase(seriesId, series)
                result
                    .onSuccess { loadMySeries() }
                    .onFailure { e -> _error.value = e.message ?: "Failed to update series" }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error updating series"
            }
        }
    }
}
