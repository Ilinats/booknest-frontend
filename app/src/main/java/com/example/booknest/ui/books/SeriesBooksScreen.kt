package com.example.booknest.ui.books

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.domain.usecase.books.BrowseBooksUseCase
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.components.BackButton
import org.koin.compose.koinInject
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesBooksScreen(
    navController: NavController,
    seriesId: String,
    seriesName: String? = null,
    browseBooksUseCase: BrowseBooksUseCase = koinInject()
) {
    var books by remember {
        mutableStateOf<List<com.example.booknest.domain.model.response.RecommendedBookResponse>>(
            emptyList()
        )
    }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(seriesId) {
        scope.launch {
            isLoading = true
            error = null
            browseBooksUseCase(
                seriesId = seriesId,
                status = "active",
                take = 100
            ).onSuccess { bookList ->
                books = bookList.sortedBy { it.seriesOrder ?: Int.MAX_VALUE }
                isLoading = false
            }.onFailure { e ->
                error = e.message ?: "Failed to load series books"
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = seriesName ?: "Series Books",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error ?: "Error loading books",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            scope.launch {
                                isLoading = true
                                error = null
                                browseBooksUseCase(
                                    seriesId = seriesId,
                                    status = "active",
                                    take = 100
                                ).onSuccess { bookList ->
                                    books = bookList.sortedBy { it.seriesOrder ?: Int.MAX_VALUE }
                                    isLoading = false
                                }.onFailure { e ->
                                    error = e.message ?: "Failed to load series books"
                                    isLoading = false
                                }
                            }
                        }) {
                            Text("Retry")
                        }
                    }
                }

                books.isEmpty() -> {
                    Text(
                        text = "No books in this series yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "${books.size} book${if (books.size != 1) "s" else ""} in series",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(books) { book ->
                            BookItem(
                                book = book,
                                navController = navController,
                                isFullWidth = true
                            )
                        }
                    }
                }
            }
        }
    }
}

