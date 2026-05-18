package com.example.booknest.ui.books.components.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.viewmodel.books.BookListBrowseUiState
import com.example.booknest.viewmodel.books.BookViewModel

internal fun bookListActiveFilterLabels(
    browseUi: BookListBrowseUiState,
    browseFilterGenres: List<GenreResponse>,
): List<String> = buildList {
    if (browseUi.debouncedSearchQuery.isNotBlank()) add("Search: ${browseUi.debouncedSearchQuery}")
    browseUi.selectedGenres.forEach { genreId ->
        browseFilterGenres.find { it.id == genreId }?.let { add("Genre: ${it.name}") }
    }
    browseUi.selectedAgeRating?.let { add("Age: ${it.replaceFirstChar { c -> c.uppercase() }}") }
    browseUi.selectedDistributionType?.let {
        add("Type: ${it.replaceFirstChar { c -> c.uppercase() }}")
    }
    if (browseUi.minRating > 0 || browseUi.maxRating < 5) add(
        "Rating: ${
            String.format(
                "%.1f",
                browseUi.minRating
            )
        }-${String.format("%.1f", browseUi.maxRating)}"
    )
    browseUi.selectedApplicationStatus?.let {
        add("Status: ${if (it == "accepting_applications") "Accepting Applications" else "All Books"}")
    }
    browseUi.selectedDeadlineFilter?.let {
        add("Deadline: ${if (it == "ending_soon") "Ending Soon" else "Still Time"}")
    }
    browseUi.selectedSortBy?.let {
        val sortNames = mapOf(
            "newest" to "Newest First",
            "most_popular" to "Most Popular",
            "highest_rated" to "Highest Rated",
            "deadline_soonest" to "Deadline Soonest",
            "most_available" to "Most Available"
        )
        add("Sort: ${sortNames[it] ?: it}")
    }
}

@Composable
fun BookListActiveFiltersBanner(
    activeFilters: List<String>,
    browseFilterGenres: List<GenreResponse>,
    bookViewModel: BookViewModel,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Filters",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = {
                    bookViewModel.clearBookListSearchImmediate()
                    bookViewModel.updateBookListBrowseUi {
                        it.copy(
                            selectedGenres = emptySet(),
                            selectedAgeRating = null,
                            selectedDistributionType = null,
                            minRating = 0f,
                            maxRating = 5f,
                            selectedApplicationStatus = null,
                            selectedDeadlineFilter = null,
                            selectedSortBy = null,
                        )
                    }
                }) {
                    Text("Clear All")
                }
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activeFilters.size) { index ->
                    FilterChip(
                        selected = true,
                        onClick = { },
                        label = {
                            Text(
                                activeFilters[index],
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                val filter = activeFilters[index]
                                when {
                                    filter.startsWith("Search:") -> {
                                        bookViewModel.clearBookListSearchImmediate()
                                    }

                                    filter.startsWith("Genre:") -> {
                                        val genreName =
                                            filter.removePrefix("Genre: ")
                                        browseFilterGenres.find { it.name == genreName }
                                            ?.let { g ->
                                                bookViewModel.updateBookListBrowseUi { s ->
                                                    s.copy(selectedGenres = s.selectedGenres - g.id)
                                                }
                                            }
                                    }

                                    filter.startsWith("Age:") -> bookViewModel.updateBookListBrowseUi {
                                        it.copy(selectedAgeRating = null)
                                    }

                                    filter.startsWith("Type:") -> bookViewModel.updateBookListBrowseUi {
                                        it.copy(selectedDistributionType = null)
                                    }

                                    filter.startsWith("Rating:") -> bookViewModel.updateBookListBrowseUi {
                                        it.copy(minRating = 0f, maxRating = 5f)
                                    }

                                    filter.startsWith("Status:") -> bookViewModel.updateBookListBrowseUi {
                                        it.copy(selectedApplicationStatus = null)
                                    }

                                    filter.startsWith("Deadline:") -> bookViewModel.updateBookListBrowseUi {
                                        it.copy(selectedDeadlineFilter = null)
                                    }

                                    filter.startsWith("Sort:") -> bookViewModel.updateBookListBrowseUi {
                                        it.copy(selectedSortBy = null)
                                    }
                                }
                            }) {
                                Icon(
                                    Icons.Filled.Clear,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
