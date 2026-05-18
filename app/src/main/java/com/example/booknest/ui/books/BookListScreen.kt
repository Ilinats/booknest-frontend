package com.example.booknest.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.booknest.R
import com.example.booknest.data.session.SearchHistoryManager
import com.example.booknest.data.session.SessionManager
import com.example.booknest.ui.books.components.browse.BookListActiveFiltersBanner
import com.example.booknest.ui.books.components.browse.BookListBrowseFiltersPanel
import com.example.booknest.ui.books.components.browse.BookListRecentSearchesCard
import com.example.booknest.ui.books.components.browse.BookListSearchHeader
import com.example.booknest.ui.books.components.browse.bookListActiveFilterLabels
import com.example.booknest.ui.books.components.list.BookItem
import com.example.booknest.viewmodel.books.BookViewModel
import com.example.booknest.ui.components.BackgroundDecoration
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    navController: NavController? = null,
    sessionManager: SessionManager,
    bookViewModel: BookViewModel = getViewModel(),
    searchHistoryManager: SearchHistoryManager = koinInject(),
    searchQuery: String? = null,
    category: String? = null
) {
    val books by bookViewModel.books.collectAsState()
    val isLoading by bookViewModel.isLoading.collectAsState()
    val hasMore by bookViewModel.browseListHasMore.collectAsState()
    val browseUi by bookViewModel.bookListBrowseUi.collectAsState()
    val browseListLoadingMore by bookViewModel.browseListLoadingMore.collectAsState()

    val showFiltersForBrowse = category == null

    val browseFilterGenres by bookViewModel.browseFilterGenres.collectAsState()
    val browseGenresLoading by bookViewModel.browseGenresLoading.collectAsState()

    val screenTitle = when (category) {
        "search" -> "Search Results"
        else -> "Books"
    }

    val currentBooks = books
    val currentIsLoading = isLoading

    LaunchedEffect(category, searchQuery) {
        bookViewModel.onBookListRouteArgs(searchQuery, category)
    }

    val recentSearches by searchHistoryManager.recentSearches.collectAsState(initial = emptyList())

    val listState = rememberLazyListState()

    LaunchedEffect(listState, hasMore, category, browseListLoadingMore, isLoading) {
        if (category != null || !hasMore) return@LaunchedEffect
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val visible = layoutInfo.visibleItemsInfo
            if (visible.isEmpty()) return@snapshotFlow false
            val lastVisible = visible.last().index
            val total = layoutInfo.totalItemsCount
            lastVisible >= total - 3
        }.collect { nearEnd ->
            if (nearEnd && !browseListLoadingMore && !isLoading) {
                bookViewModel.loadMoreBrowseList(category)
            }
        }
    }

    val listBottomPadding = 12.dp
    val listTopPadding = if (category == null) 16.dp else 8.dp

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (category != null) {
                TopAppBar(
                    title = {
                        Text(
                            screenTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    actions = {
                        if (showFiltersForBrowse) {
                            IconButton(onClick = { bookViewModel.setBookListShowFilters(!browseUi.showFilters) }) {
                                Icon(
                                    Icons.Filled.FilterList,
                                    contentDescription = stringResource(R.string.cd_book_list_filters),
                                    tint = if (browseUi.showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BackgroundDecoration(modifier = Modifier.fillMaxSize())

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = listTopPadding, bottom = listBottomPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (category == null || category == "search") {
                    item {
                        BookListSearchHeader(
                            browseUi = browseUi,
                            recentSearches = recentSearches,
                            showFiltersForBrowse = showFiltersForBrowse,
                            bookViewModel = bookViewModel,
                        )
                    }

                    if (browseUi.showRecentSearches && recentSearches.isNotEmpty()) {
                        item {
                            BookListRecentSearchesCard(
                                recentSearches = recentSearches,
                                bookViewModel = bookViewModel,
                            )
                        }
                    }
                }

                if (browseUi.showFilters && showFiltersForBrowse) {
                    item {
                        BookListBrowseFiltersPanel(
                            browseUi = browseUi,
                            browseFilterGenres = browseFilterGenres,
                            browseGenresLoading = browseGenresLoading,
                            bookViewModel = bookViewModel,
                        )
                    }
                }

                if (showFiltersForBrowse && category == null) {
                    val activeFilterLabels = bookListActiveFilterLabels(browseUi, browseFilterGenres)
                    if (activeFilterLabels.isNotEmpty()) {
                        item {
                            BookListActiveFiltersBanner(
                                activeFilters = activeFilterLabels,
                                browseFilterGenres = browseFilterGenres,
                                bookViewModel = bookViewModel,
                            )
                        }
                    }

                    if (currentIsLoading && currentBooks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    if (currentBooks.isEmpty() && !currentIsLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when {
                                        category == "recommended" -> "No recommended books available"
                                        category == "new_releases" -> "No new releases available"
                                        category == "followed_authors" -> "No books from followed authors"
                                        category == "search" -> "No books found for \"${browseUi.debouncedSearchQuery}\""
                                        browseUi.debouncedSearchQuery.isBlank() && browseUi.selectedGenres.isEmpty() && browseUi.selectedAgeRating == null && browseUi.selectedDistributionType == null -> "No books available"
                                        else -> "No books found matching your filters"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (currentBooks.isNotEmpty() && showFiltersForBrowse) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Found ${currentBooks.size} book${if (currentBooks.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (currentBooks.isNotEmpty()) {
                        items(currentBooks.size) { index ->
                            val book = currentBooks[index]
                            BookItem(
                                book = book,
                                navController = navController,
                                isFullWidth = true
                            )
                        }

                        if (browseListLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }

                if (category == "search") {
                    if (currentIsLoading && currentBooks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    if (currentBooks.isEmpty() && !currentIsLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No books found for \"${browseUi.debouncedSearchQuery}\"",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (currentBooks.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Found ${currentBooks.size} book${if (currentBooks.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (currentBooks.isNotEmpty()) {
                        items(currentBooks.size) { index ->
                            val book = currentBooks[index]
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
