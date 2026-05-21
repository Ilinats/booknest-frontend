package com.example.booknest.ui.books

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.ui.components.BackButton
import com.example.booknest.ui.books.components.list.BookItem
import com.example.booknest.ui.components.BackgroundDecoration
import com.example.booknest.viewmodel.books.BrowseBooksViewModel
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesBooksScreen(
    navController: NavController,
    seriesId: String,
    seriesName: String? = null,
    browseBooksViewModel: BrowseBooksViewModel = getViewModel()
) {
    val seriesBooksMap by browseBooksViewModel.seriesBooksBySeriesId.collectAsState()
    val loadingIds by browseBooksViewModel.seriesBooksLoadingIds.collectAsState()
    val seriesErrors by browseBooksViewModel.seriesBooksLoadError.collectAsState()

    LaunchedEffect(seriesId) {
        browseBooksViewModel.clearSeriesBooksLoadError(seriesId)
        browseBooksViewModel.ensureSeriesBooksLoaded(
            seriesId = seriesId,
            forceRefresh = false,
            treatFailureAsEmptyCatalog = false
        )
    }

    val books = seriesBooksMap[seriesId].orEmpty()
    val seriesError = seriesErrors[seriesId]
    val isLoading = loadingIds.contains(seriesId)
    val hasLoaded = seriesBooksMap.containsKey(seriesId)

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
            BackgroundDecoration(modifier = Modifier.fillMaxSize())

            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                seriesError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = seriesError,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            browseBooksViewModel.clearSeriesBooksLoadError(seriesId)
                            browseBooksViewModel.ensureSeriesBooksLoaded(
                                seriesId = seriesId,
                                forceRefresh = true,
                                treatFailureAsEmptyCatalog = false
                            )
                        }) {
                            Text("Retry")
                        }
                    }
                }

                !hasLoaded && seriesError == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
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
