package com.example.booknest.ui.books.components.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.booknest.ui.testing.UiTestTags
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.viewmodel.books.BookListBrowseUiState
import com.example.booknest.viewmodel.books.BrowseBooksViewModel

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
    if (browseUi.minRating > 0 || browseUi.maxRating < 5) {
        add(
            "Rating: ${String.format("%.1f", browseUi.minRating)}-${
                String.format(
                    "%.1f",
                    browseUi.maxRating
                )
            }"
        )
    }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookListActiveFiltersBanner(
    activeFilters: List<String>,
    browseFilterGenres: List<GenreResponse>,
    browseBooksViewModel: BrowseBooksViewModel,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Active filters",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick = {
                    browseBooksViewModel.clearBookListSearchImmediate()
                    browseBooksViewModel.updateBookListBrowseUi {
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
                },
                modifier = Modifier
                    .testTag(UiTestTags.BROWSE_ACTIVE_FILTERS_CLEAR)
                    .padding(end = 0.dp),
            ) {
                Text("Clear all")
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            activeFilters.forEach { filter ->
                InputChip(
                    selected = true,
                    onClick = {
                        removeBookListActiveFilter(filter, browseFilterGenres, browseBooksViewModel)
                    },
                    label = {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
            }
        }
    }
}

private fun removeBookListActiveFilter(
    filter: String,
    browseFilterGenres: List<GenreResponse>,
    browseBooksViewModel: BrowseBooksViewModel,
) {
    when {
        filter.startsWith("Search:") -> browseBooksViewModel.clearBookListSearchImmediate()
        filter.startsWith("Genre:") -> {
            val genreName = filter.removePrefix("Genre: ")
            browseFilterGenres.find { it.name == genreName }?.let { g ->
                browseBooksViewModel.updateBookListBrowseUi { s ->
                    s.copy(selectedGenres = s.selectedGenres - g.id)
                }
            }
        }
        filter.startsWith("Age:") -> browseBooksViewModel.updateBookListBrowseUi {
            it.copy(selectedAgeRating = null)
        }
        filter.startsWith("Type:") -> browseBooksViewModel.updateBookListBrowseUi {
            it.copy(selectedDistributionType = null)
        }
        filter.startsWith("Rating:") -> browseBooksViewModel.updateBookListBrowseUi {
            it.copy(minRating = 0f, maxRating = 5f)
        }
        filter.startsWith("Status:") -> browseBooksViewModel.updateBookListBrowseUi {
            it.copy(selectedApplicationStatus = null)
        }
        filter.startsWith("Deadline:") -> browseBooksViewModel.updateBookListBrowseUi {
            it.copy(selectedDeadlineFilter = null)
        }
        filter.startsWith("Sort:") -> browseBooksViewModel.updateBookListBrowseUi {
            it.copy(selectedSortBy = null)
        }
    }
}
