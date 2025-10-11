package com.example.booknest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.booknest.data.AuthManager
import com.example.booknest.network.Book
import com.example.booknest.network.RetrofitInstance
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(FlowPreview::class)
class BookViewModel(private val authManager: AuthManager) : ViewModel() {

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books

    private val _featuredBooks = MutableStateFlow<List<Book>>(emptyList())
    val featuredBooks: StateFlow<List<Book>> = _featuredBooks

    private val _bookDetails = MutableStateFlow<Book?>(null)
    val bookDetails: StateFlow<Book?> = _bookDetails

    private val _recommendedBooks = MutableStateFlow<List<Book>>(emptyList())
    val recommendedBooks: StateFlow<List<Book>> = _recommendedBooks

    private val _newReleases = MutableStateFlow<List<Book>>(emptyList())
    val newReleases: StateFlow<List<Book>> = _newReleases

    private val _searchQuery = MutableStateFlow("")
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .collect { query ->
                    if (query.isNotBlank()) {
                        searchBooks(query)
                    } else {
                        _books.value = emptyList()
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun getRecommendedBooks() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = RetrofitInstance.api.getRecommendedBooks(10)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _recommendedBooks.value = apiResponse.data ?: emptyList()
                        println("Recommended books loaded: ${apiResponse.data?.size ?: 0} books")
                    } else {
                        println("Recommended books API error: ${apiResponse.message}")
                    }
                } else {
                    println("Recommended books API error: ${response.code()} - ${response.message()}")
                    val errorBody = response.errorBody()?.string()
                    println("Error body: $errorBody")
                }
            } catch (e: Exception) {
                println("Recommended books exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getNewReleases() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Get books from the last 30 days
                val thirtyDaysAgo = LocalDate.now().minusDays(30)
                val formatter = DateTimeFormatter.ISO_LOCAL_DATE
                val formattedDate = thirtyDaysAgo.format(formatter)
                
                val response = RetrofitInstance.api.browseBooks(
                    query = null,
                    genreId = null,
                    ageRating = null,
                    distributionType = null,
                    publishedFrom = formattedDate,
                    publishedTo = null,
                    skip = null,
                    take = 10,
                    status = "active"
                )
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _newReleases.value = apiResponse.data ?: emptyList()
                    }
                } else {
                    println("New releases API error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun browseBooks(
        query: String? = null,
        genreId: Int? = null,
        ageRating: String? = null,
        distributionType: String? = null,
        publishedFrom: String? = null,
        publishedTo: String? = null,
        skip: Int? = null,
        take: Int? = null
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.browseBooks(
                    query,
                    genreId,
                    ageRating,
                    distributionType,
                    publishedFrom,
                    publishedTo,
                    skip,
                    take,
                    status = "active"
                )
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _books.value = apiResponse.data ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getFeaturedBooks() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getFeaturedBooks()
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _featuredBooks.value = apiResponse.data ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun searchBooks(query: String, skip: Int? = null, take: Int? = null) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.searchBooks(query, skip, take)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _books.value = apiResponse.data ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun getBookDetails(bookId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getBookDetails(bookId)
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    if (apiResponse.success) {
                        _bookDetails.value = apiResponse.data
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

class BookViewModelFactory(private val authManager: AuthManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookViewModel(authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}