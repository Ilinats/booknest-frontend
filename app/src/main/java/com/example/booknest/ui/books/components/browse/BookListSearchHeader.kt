package com.example.booknest.ui.books.components.browse

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.shadow
import com.example.booknest.ui.testing.UiTestTags
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.booknest.R
import com.example.booknest.viewmodel.books.BookListBrowseUiState
import com.example.booknest.viewmodel.books.BrowseBooksViewModel

@Composable
fun BookListSearchHeader(
    browseUi: BookListBrowseUiState,
    recentSearches: List<String>,
    showFiltersForBrowse: Boolean,
    browseBooksViewModel: BrowseBooksViewModel,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isFocused by interactionSource.collectIsFocusedAsState()

        LaunchedEffect(isFocused) {
            if (isFocused && browseUi.searchQuery.isBlank() && recentSearches.isNotEmpty()) {
                browseBooksViewModel.setBookListShowRecentSearches(true)
            }
        }

        OutlinedTextField(
            value = browseUi.searchQuery,
            onValueChange = { newValue ->
                browseBooksViewModel.updateBookListSearchInput(newValue)
                browseBooksViewModel.setBookListShowRecentSearches(
                    newValue.isBlank() && recentSearches.isNotEmpty() && isFocused
                )
            },
            placeholder = {
                Text(
                    "Search by title, author, or series",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .weight(1f)
                .testTag(UiTestTags.BROWSE_SEARCH_FIELD)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(28.dp)
                ),
            interactionSource = interactionSource,
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            trailingIcon = {
                if (browseUi.searchQuery.isNotBlank()) {
                    IconButton(onClick = {
                        browseBooksViewModel.clearBookListSearchImmediate()
                        browseBooksViewModel.setBookListShowRecentSearches(
                            recentSearches.isNotEmpty() && isFocused
                        )
                    }) {
                        Icon(
                            Icons.Filled.Clear,
                            contentDescription = "Clear"
                        )
                    }
                } else if (recentSearches.isNotEmpty()) {
                    IconButton(onClick = {
                        browseBooksViewModel.setBookListShowRecentSearches(!browseUi.showRecentSearches)
                    }) {
                        Icon(
                            if (browseUi.showRecentSearches) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Recent Searches"
                        )
                    }
                }
            }
        )

        if (showFiltersForBrowse) {
            IconButton(
                onClick = { browseBooksViewModel.setBookListShowFilters(!browseUi.showFilters) },
                modifier = Modifier
                    .size(48.dp)
                    .testTag(UiTestTags.BROWSE_FILTER_BUTTON),
            ) {
                Icon(
                    Icons.Filled.FilterList,
                    contentDescription = stringResource(R.string.cd_book_list_filters),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun BookListRecentSearchesCard(
    recentSearches: List<String>,
    browseBooksViewModel: BrowseBooksViewModel,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(UiTestTags.BROWSE_RECENT_SEARCHES)
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
                    text = "Recent Searches",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { browseBooksViewModel.clearBookListSearchHistory() }) {
                    Text(
                        "Clear",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentSearches) { search ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            browseBooksViewModel.applyBookListRecentSearch(search)
                        },
                        label = { Text(search, maxLines = 1) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.History,
                                contentDescription = "Recent",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}
