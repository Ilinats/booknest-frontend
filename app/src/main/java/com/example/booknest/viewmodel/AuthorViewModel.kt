package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.AuthManager
import com.example.booknest.network.Book
import com.example.booknest.network.BookAnalytics
import com.example.booknest.network.BookStats
import com.example.booknest.network.CreateBookDto
import com.example.booknest.network.CreateSeriesDto
import com.example.booknest.network.RetrofitInstance
import com.example.booknest.network.Series
import com.example.booknest.network.UpdateBookDto
import com.example.booknest.network.UpdateSeriesDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthorViewModel(private val authManager: AuthManager) : ViewModel() {

    // Books state
    private val _myBooks = MutableStateFlow<List<Book>>(emptyList())
    val myBooks: StateFlow<List<Book>> = _myBooks.asStateFlow()

    private val _isLoadingBooks = MutableStateFlow(false)
    val isLoadingBooks: StateFlow<Boolean> = _isLoadingBooks.asStateFlow()

    // Series state
    private val _mySeries = MutableStateFlow<List<Series>>(emptyList())
    val mySeries: StateFlow<List<Series>> = _mySeries.asStateFlow()

    private val _isLoadingSeries = MutableStateFlow(false)
    val isLoadingSeries: StateFlow<Boolean> = _isLoadingSeries.asStateFlow()

    // Book stats state
    private val _bookStats = MutableStateFlow<Map<String, BookStats>>(emptyMap())
    val bookStats: StateFlow<Map<String, BookStats>> = _bookStats.asStateFlow()

    // Quick stats
    private val _quickStats = MutableStateFlow(QuickStats())
    val quickStats: StateFlow<QuickStats> = _quickStats.asStateFlow()

    data class QuickStats(
        val totalBooks: Int = 0,
        val activeBooks: Int = 0,
        val totalApplications: Int = 0,
        val avgResponseTime: String = "0 days"
    )

    init {
        loadMyBooks()
        loadMySeries()
    }

    fun loadMyBooks() {
        viewModelScope.launch {
            try {
                _isLoadingBooks.value = true
                val response = RetrofitInstance.api.getMyBooks()
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _myBooks.value = apiResponse.data ?: emptyList()
                        updateQuickStats()
                    } else {
                        println("Failed to load books: ${apiResponse.message}")
                    }
                } else {
                    println("Failed to load books: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                println("Error loading books: ${e.message}")
            } finally {
                _isLoadingBooks.value = false
            }
        }
    }

    fun loadMySeries() {
        viewModelScope.launch {
            try {
                _isLoadingSeries.value = true
                val response = RetrofitInstance.api.getMySeries()
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _mySeries.value = apiResponse.data ?: emptyList()
                    } else {
                        println("Failed to load series: ${apiResponse.message}")
                    }
                } else {
                    println("Failed to load series: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                println("Error loading series: ${e.message}")
            } finally {
                _isLoadingSeries.value = false
            }
        }
    }

    fun createBook(book: CreateBookDto) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.createBook(book)
                if (response.isSuccessful) {
                    loadMyBooks() // Refresh the list
                } else {
                    println("Failed to create book: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                println("Error creating book: ${e.message}")
            }
        }
    }

    fun updateBook(bookId: String, book: UpdateBookDto) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.updateBook(bookId, book)
                if (response.isSuccessful) {
                    loadMyBooks() // Refresh the list
                } else {
                    println("Failed to update book: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                println("Error updating book: ${e.message}")
            }
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.deleteBook(bookId)
                if (response.isSuccessful) {
                    loadMyBooks() // Refresh the list
                } else {
                    println("Failed to delete book: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                println("Error deleting book: ${e.message}")
            }
        }
    }

    fun publishBook(bookId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.publishBook(bookId)
                if (response.isSuccessful) {
                    loadMyBooks() // Refresh the list
                } else {
                    println("Failed to publish book: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                println("Error publishing book: ${e.message}")
            }
        }
    }

    fun getBookStats(bookId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getBookStats(bookId)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        val stats = apiResponse.data
                        if (stats != null) {
                            _bookStats.value = _bookStats.value + (bookId to stats)
                        }
                    } else {
                        println("Failed to get book stats: ${apiResponse.message}")
                    }
                } else {
                    println("Failed to get book stats: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                println("Error getting book stats: ${e.message}")
            }
        }
    }

    fun createSeries(series: CreateSeriesDto) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.createSeries(series)
                if (response.isSuccessful) {
                    loadMySeries() // Refresh the list
                } else {
                    println("Failed to create series: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                println("Error creating series: ${e.message}")
            }
        }
    }

    fun updateSeries(seriesId: String, series: UpdateSeriesDto) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.updateSeries(seriesId, series)
                if (response.isSuccessful) {
                    loadMySeries() // Refresh the list
                } else {
                    println("Failed to update series: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                println("Error updating series: ${e.message}")
            }
        }
    }

    fun deleteSeries(seriesId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.deleteSeries(seriesId)
                if (response.isSuccessful) {
                    loadMySeries() // Refresh the list
                } else {
                    println("Failed to delete series: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                println("Error deleting series: ${e.message}")
            }
        }
    }

    private fun updateQuickStats() {
        val books = _myBooks.value
        val totalBooks = books.size
        val activeBooks = books.count { it.status.name == "ACTIVE" }
        val totalApplications = _bookStats.value.values.sumOf { it.totalApplications }
        
        _quickStats.value = QuickStats(
            totalBooks = totalBooks,
            activeBooks = activeBooks,
            totalApplications = totalApplications,
            avgResponseTime = "2 days" // This would be calculated from actual data
        )
    }
}

class AuthorViewModelFactory(private val authManager: AuthManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthorViewModel(authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
