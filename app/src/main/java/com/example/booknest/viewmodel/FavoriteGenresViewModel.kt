package com.example.booknest.viewmodel

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
import com.example.booknest.ui.toast.GlobalToastHandler

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
                        GlobalToastHandler.showError(errorMsg)
                    }

                val preferencesResult = getGenrePreferencesUseCase()
                preferencesResult
                    .onSuccess { preferences ->
                        _selectedGenreIds.value = preferences
                            .mapNotNull { it.resolvedGenreId.takeIf { id -> id > 0 } }
                            .toSet()
                    }
                    .onFailure { e ->
                        println("DEBUG: Failed to load genre preferences: ${e.message}")
                    }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Failed to load genres"
                _message.value = errorMsg
                GlobalToastHandler.showError(errorMsg)
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
                GlobalToastHandler.showError(errorMsg)
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
                    GlobalToastHandler.showSuccess(msg)
                } else {
                    GlobalToastHandler.showError(msg)
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to save preferences: ${e.localizedMessage}"
                _message.value = errorMsg
                GlobalToastHandler.showError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
