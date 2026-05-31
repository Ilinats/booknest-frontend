package com.example.booknest.viewmodel.genres

import androidx.lifecycle.ViewModel
import com.example.booknest.viewmodel.common.UserFeedback
import androidx.lifecycle.viewModelScope
import com.example.booknest.domain.model.request.DeleteGenrePreferenceRequest
import com.example.booknest.domain.model.request.UpsertPreferenceRequest
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.usecase.genres.DeleteUserGenrePreferenceUseCase
import com.example.booknest.domain.usecase.genres.GetGenrePreferencesUseCase
import com.example.booknest.domain.usecase.genres.GetGenresUseCase
import com.example.booknest.domain.usecase.genres.SaveUserGenrePreferenceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteGenresViewModel(
    private val feedback: UserFeedback,
    private val getGenresUseCase: GetGenresUseCase,
    private val getGenrePreferencesUseCase: GetGenrePreferencesUseCase,
    private val saveUserGenrePreferenceUseCase: SaveUserGenrePreferenceUseCase,
    private val deleteUserGenrePreferenceUseCase: DeleteUserGenrePreferenceUseCase,
) : ViewModel() {

    private val _genres = MutableStateFlow<List<GenreResponse>>(emptyList())
    val genres: StateFlow<List<GenreResponse>> = _genres.asStateFlow()

    private val _selectedGenreIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedGenreIds: StateFlow<Set<Int>> = _selectedGenreIds.asStateFlow()

    /** Genre ids last loaded from or successfully synced with the server. */
    private val _persistedGenreIds = MutableStateFlow<Set<Int>>(emptySet())

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

    private fun notifyError(message: String) = feedback.error(message, _error)
    private fun notifySuccess(message: String) = feedback.success(message, _successMessage)

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
                        notifyError(errorMsg)
                    }

                val preferencesResult = getGenrePreferencesUseCase()
                preferencesResult
                    .onSuccess { preferences ->
                        val ids = preferences
                            .mapNotNull { it.resolvedGenreId.takeIf { id -> id > 0 } }
                            .toSet()
                        _selectedGenreIds.value = ids
                        _persistedGenreIds.value = ids
                    }
                    .onFailure { _ -> }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Failed to load genres"
                _message.value = errorMsg
                notifyError(errorMsg)
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
            val selected = _selectedGenreIds.value
            if (selected.isEmpty()) {
                val errorMsg = "Select at least one genre."
                _message.value = errorMsg
                notifyError(errorMsg)
                return@launch
            }

            val persisted = _persistedGenreIds.value
            val toDelete = persisted - selected
            val toSave = selected - persisted

            if (toDelete.isEmpty() && toSave.isEmpty()) {
                val msg = "Favorite genres saved."
                _message.value = msg
                notifySuccess(msg)
                return@launch
            }

            _isLoading.value = true
            try {
                val deleteResults = toDelete.map { genreId ->
                    deleteUserGenrePreferenceUseCase(DeleteGenrePreferenceRequest(genreId = genreId))
                }
                val saveResults = toSave.map { genreId ->
                    saveUserGenrePreferenceUseCase(UpsertPreferenceRequest(genreId = genreId))
                }

                val allSucceeded = (deleteResults + saveResults).all { it.isSuccess }
                val msg = if (allSucceeded) {
                    _persistedGenreIds.value = selected
                    "Favorite genres saved."
                } else {
                    "Some preferences could not be saved."
                }
                _message.value = msg
                if (allSucceeded) {
                    notifySuccess(msg)
                } else {
                    notifyError(msg)
                }
            } catch (e: Exception) {
                val errorMsg = "Failed to save preferences: ${e.localizedMessage}"
                _message.value = errorMsg
                notifyError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
