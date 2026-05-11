package com.example.booknest.viewmodel.genres

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.request.UpsertPreferenceRequest
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.usecase.genres.GetGenrePreferencesUseCase
import com.example.booknest.domain.usecase.genres.GetGenresUseCase
import com.example.booknest.domain.usecase.genres.SaveUserGenrePreferenceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteGenresViewModel(
    private val getGenresUseCase: GetGenresUseCase,
    private val getGenrePreferencesUseCase: GetGenrePreferencesUseCase,
    private val saveUserGenrePreferenceUseCase: SaveUserGenrePreferenceUseCase
) : ViewModel() {

    private val _genres = MutableStateFlow<List<GenreResponse>>(emptyList())
    val genres: StateFlow<List<GenreResponse>> = _genres.asStateFlow()

    private val _selectedGenreIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedGenreIds: StateFlow<Set<Int>> = _selectedGenreIds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun clearError() { _error.value = null }
    fun clearSuccessMessage() { _successMessage.value = null }

    fun loadGenres() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val genresResult = getGenresUseCase()
                genresResult
                    .onSuccess { genres ->
                        _genres.value = genres
                    }
                    .onFailure { e ->
                        val errorMsg = e.localizedMessage ?: "Failed to load genres"
                        _message.value = errorMsg
                        _error.value = errorMsg
                    }

                val preferencesResult = getGenrePreferencesUseCase()
                preferencesResult
                    .onSuccess { preferences ->
                        _selectedGenreIds.value = preferences
                            .mapNotNull { it.resolvedGenreId.takeIf { id -> id > 0 } }
                            .toSet()
                    }
                    .onFailure { _ -> }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Failed to load genres"
                _message.value = errorMsg
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleGenre(genreId: Int) {
        val updated = _selectedGenreIds.value.toMutableSet()
        if (!updated.add(genreId)) {
            updated.remove(genreId)
        }
        _selectedGenreIds.value = updated
    }

    fun savePreferences() {
        viewModelScope.launch {
            if (_selectedGenreIds.value.isEmpty()) {
                val errorMsg = "Select at least one genre."
                _message.value = errorMsg
                _error.value = errorMsg
                return@launch
            }
            _isLoading.value = true
            try {
                val results = _selectedGenreIds.value.map { genreId ->
                    saveUserGenrePreferenceUseCase(
                        UpsertPreferenceRequest(
                            genreId = genreId
                        )
                    )
                }

                val allSucceeded = results.all { it.isSuccess }
                val msg = if (allSucceeded) {
                    "Favorite genres saved."
                } else {
                    "Some preferences could not be saved."
                }
                _message.value = msg
                if (allSucceeded) {
                    _successMessage.value = msg
                } else {
                    _error.value = msg
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to save preferences: ${e.localizedMessage}"
                _message.value = errorMsg
                _error.value = errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
