@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.booknest.ui.books.components.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.ui.books.components.filters.AgeRatingFilter
import com.example.booknest.ui.books.components.filters.ApplicationStatusFilter
import com.example.booknest.ui.books.components.filters.DeadlineFilter
import com.example.booknest.ui.books.components.filters.DistributionTypeFilter
import com.example.booknest.ui.books.components.filters.SortByFilter
import com.example.booknest.viewmodel.books.BookListBrowseUiState
import com.example.booknest.viewmodel.books.BookViewModel

@Composable
fun BookListBrowseFiltersPanel(
    browseUi: BookListBrowseUiState,
    browseFilterGenres: List<GenreResponse>,
    browseGenresLoading: Boolean,
    bookViewModel: BookViewModel,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = {
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
                    }
                ) {
                    Icon(
                        Icons.Filled.Clear,
                        contentDescription = "Clear",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Clear All")
                }
            }

            Column {
                Text(
                    text = "Genres",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (browseGenresLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(browseFilterGenres) { genre ->
                            FilterChip(
                                selected = browseUi.selectedGenres.contains(genre.id),
                                onClick = {
                                    bookViewModel.updateBookListBrowseUi { s ->
                                        val id = genre.id
                                        val next =
                                            if (s.selectedGenres.contains(id)) {
                                                s.selectedGenres - id
                                            } else {
                                                s.selectedGenres + id
                                            }
                                        s.copy(selectedGenres = next)
                                    }
                                },
                                label = { Text(genre.name) }
                            )
                        }
                    }
                }
            }

            AgeRatingFilter(
                selectedAgeRating = browseUi.selectedAgeRating,
                onAgeRatingSelected = { v ->
                    bookViewModel.updateBookListBrowseUi { it.copy(selectedAgeRating = v) }
                }
            )

            DistributionTypeFilter(
                selectedDistributionType = browseUi.selectedDistributionType,
                onDistributionTypeSelected = { v ->
                    bookViewModel.updateBookListBrowseUi {
                        it.copy(selectedDistributionType = v)
                    }
                }
            )

            Column {
                Text(
                    text = "Average Rating",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${
                        String.format(
                            "%.1f",
                            browseUi.minRating
                        )
                    } - ${String.format("%.1f", browseUi.maxRating)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format("%.1f", browseUi.minRating),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(40.dp)
                    )
                    Slider(
                        value = browseUi.minRating,
                        onValueChange = { v ->
                            bookViewModel.updateBookListBrowseUi {
                                it.copy(minRating = v.coerceAtMost(it.maxRating))
                            }
                        },
                        valueRange = 0f..5f,
                        steps = 49,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format("%.1f", browseUi.maxRating),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(40.dp)
                    )
                    Slider(
                        value = browseUi.maxRating,
                        onValueChange = { v ->
                            bookViewModel.updateBookListBrowseUi {
                                it.copy(maxRating = v.coerceAtLeast(it.minRating))
                            }
                        },
                        valueRange = 0f..5f,
                        steps = 49,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            ApplicationStatusFilter(
                selectedApplicationStatus = browseUi.selectedApplicationStatus,
                onApplicationStatusSelected = { v ->
                    bookViewModel.updateBookListBrowseUi {
                        it.copy(selectedApplicationStatus = v)
                    }
                }
            )

            DeadlineFilter(
                selectedDeadlineFilter = browseUi.selectedDeadlineFilter,
                onDeadlineFilterSelected = { v ->
                    bookViewModel.updateBookListBrowseUi {
                        it.copy(selectedDeadlineFilter = v)
                    }
                }
            )

            SortByFilter(
                selectedSortBy = browseUi.selectedSortBy,
                onSortBySelected = { v ->
                    bookViewModel.updateBookListBrowseUi { it.copy(selectedSortBy = v) }
                }
            )
        }
    }
}
