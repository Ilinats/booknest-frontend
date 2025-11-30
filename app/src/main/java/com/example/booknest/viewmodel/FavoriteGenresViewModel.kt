package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.AuthManager
import com.example.booknest.network.GenreDto
import com.example.booknest.network.GenrePreference
import com.example.booknest.network.UpsertPreferenceRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteGenresViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    private val apiService = authManager.apiService

    private val _genres = MutableStateFlow<List<GenreDto>>(emptyList())
    val genres: StateFlow<List<GenreDto>> = _genres.asStateFlow()

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
                // Load all available genres
                val genresResponse = apiService.getGenres()
                if (genresResponse.isSuccessful) {
                    _genres.value = genresResponse.body()?.data ?: emptyList()
                } else {
                    _message.value = genresResponse.body()?.message ?: "Failed to load genres"
                }
                
                // Load user's current genre preferences
                val preferencesResponse = apiService.getGenrePreferences()
                if (preferencesResponse.isSuccessful) {
                    val preferences = preferencesResponse.body()?.data ?: emptyList()
                    _selectedGenreIds.value = preferences.map { it.resolvedGenreId }.toSet()
                }
            } catch (e: Exception) {
                _message.value = e.localizedMessage ?: "Failed to load genres"
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
                _message.value = "Select at least one genre."
                return@launch
            }
            _isLoading.value = true
            try {
                val results = _selectedGenreIds.value.map { genreId ->
                    apiService.saveUserGenres(
                        UpsertPreferenceRequest(
                            genreId = genreId,
                            preferenceLevel = 5
                        )
                    )
                }
                if (results.all { it.isSuccessful }) {
                    _message.value = "Favorite genres saved."
                } else {
                    _message.value = "Some preferences could not be saved."
                }
            } catch (e: Exception) {
                _message.value = "Failed to save preferences: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

class FavoriteGenresViewModelFactory(
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteGenresViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoriteGenresViewModel(authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

