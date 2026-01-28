package com.example.booknest.ui.books

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.booknest.data.session.SearchHistoryManager
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.usecase.genres.GetGenresUseCase
import com.example.booknest.ui.books.components.list.BookItem
import com.example.booknest.ui.books.components.filters.AgeRatingFilter
import com.example.booknest.ui.books.components.filters.ApplicationStatusFilter
import com.example.booknest.ui.books.components.filters.DeadlineFilter
import com.example.booknest.ui.books.components.filters.DistributionTypeFilter
import com.example.booknest.ui.books.components.filters.SortByFilter
import com.example.booknest.viewmodel.BookViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    navController: NavController? = null,
    sessionManager: SessionManager,
    bookViewModel: BookViewModel = getViewModel(),
    getGenresUseCase: GetGenresUseCase = koinInject(),
    searchHistoryManager: SearchHistoryManager = koinInject(),
    searchQuery: String? = null,
    category: String? = null
) {
    val books by bookViewModel.books.collectAsState()
    val isLoading by bookViewModel.isLoading.collectAsState()
    val lastFetchCount by bookViewModel.lastFetchCount.collectAsState()
    var currentSearchQuery by remember { mutableStateOf(searchQuery ?: "") }

    var debouncedSearchQuery by remember { mutableStateOf(searchQuery ?: "") }

    val initialSearchQuery = remember { searchQuery }

    var initialLoadCompleted by remember { mutableStateOf(false) }

    var filterEffectHasRun by remember { mutableStateOf(false) }

    var previousSearchQuery by remember { mutableStateOf(initialSearchQuery ?: "") }
    var previousGenres by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var previousAgeRating by remember { mutableStateOf<String?>(null) }
    var previousDistributionType by remember { mutableStateOf<String?>(null) }
    var previousMinRating by remember { mutableStateOf(0f) }
    var previousMaxRating by remember { mutableStateOf(5f) }
    var previousApplicationStatus by remember { mutableStateOf<String?>(null) }
    var previousDeadlineFilter by remember { mutableStateOf<String?>(null) }
    var previousSortBy by remember { mutableStateOf<String?>(null) }

    var currentSkip by remember { mutableStateOf(0) }
    val pageSize = 20
    var hasMore by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }

    LaunchedEffect(lastFetchCount, isLoading) {
        if (category == null && !isLoading && initialLoadCompleted) {
            hasMore = lastFetchCount >= pageSize
        }
    }

    val showFiltersForBrowse = category == null
    var showFilters by remember { mutableStateOf(false) }
    var selectedGenres by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var selectedAgeRating by remember { mutableStateOf<String?>(null) }
    var selectedDistributionType by remember { mutableStateOf<String?>(null) }
    var minRating by remember { mutableStateOf(0f) }
    var maxRating by remember { mutableStateOf(5f) }
    var selectedApplicationStatus by remember { mutableStateOf<String?>(null) }
    var selectedDeadlineFilter by remember { mutableStateOf<String?>(null) }
    var selectedSortBy by remember { mutableStateOf<String?>(null) }

    var genres by remember { mutableStateOf<List<GenreResponse>>(emptyList()) }
    var genresLoading by remember { mutableStateOf(false) }

    val screenTitle = when (category) {
        "search" -> "Search Results"
        else -> "Browse Books"
    }

    val currentBooks = books

    val currentIsLoading = isLoading

    LaunchedEffect(showFilters) {
        if (showFilters && genres.isEmpty() && !genresLoading) {
            genresLoading = true
            getGenresUseCase()
                .onSuccess { genresList ->
                    genres = genresList
                }
                .onFailure { }
            genresLoading = false
        }
    }

    LaunchedEffect(currentSearchQuery) {
        if (showFiltersForBrowse && category == null && initialLoadCompleted) {
            if (currentSearchQuery != debouncedSearchQuery) {
                debouncedSearchQuery = currentSearchQuery
            }
        }
    }

    LaunchedEffect(category, searchQuery) {
        initialLoadCompleted = false
        filterEffectHasRun = false
        currentSkip = 0
        hasMore = false

        val initialQuery = searchQuery ?: ""
        currentSearchQuery = initialQuery
        debouncedSearchQuery = initialQuery
        previousSearchQuery = initialQuery

        when (category) {
            "search" -> {
                searchQuery?.let {
                bookViewModel.browseBooks(
                    query = it,
                    skip = 0,
                    take = pageSize,
                    append = false
                )
                }
            }

            null -> {
                bookViewModel.browseBooks(
                    query = null,
                    skip = 0,
                    take = pageSize,
                    append = false
                )
            }

            else -> {
            }
        }
    }

    LaunchedEffect(isLoading) {
        if (!isLoading && !initialLoadCompleted) {
            kotlinx.coroutines.delay(50)
            initialLoadCompleted = true
        }
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500)
        if (books.isEmpty() && !isLoading) {
            bookViewModel.browseBooks(
                query = if (category == "search") searchQuery else null,
                skip = 0,
                take = pageSize,
                append = false
            )
        }
    }

    LaunchedEffect(
        debouncedSearchQuery,
        selectedGenres,
        selectedAgeRating,
        selectedDistributionType,
        minRating,
        maxRating,
        selectedApplicationStatus,
        selectedDeadlineFilter,
        selectedSortBy
    ) {
        if (showFiltersForBrowse && category == null && initialLoadCompleted && filterEffectHasRun) {
            val searchChanged = debouncedSearchQuery != previousSearchQuery
            val genresChanged = selectedGenres != previousGenres
            val ageRatingChanged = selectedAgeRating != previousAgeRating
            val distributionTypeChanged = selectedDistributionType != previousDistributionType
            val minRatingChanged = minRating != previousMinRating
            val maxRatingChanged = maxRating != previousMaxRating
            val applicationStatusChanged = selectedApplicationStatus != previousApplicationStatus
            val deadlineFilterChanged = selectedDeadlineFilter != previousDeadlineFilter
            val sortByChanged = selectedSortBy != previousSortBy

            if (searchChanged || genresChanged || ageRatingChanged || distributionTypeChanged || minRatingChanged || maxRatingChanged || applicationStatusChanged || deadlineFilterChanged || sortByChanged) {
                val query = if (debouncedSearchQuery.isNotBlank()) debouncedSearchQuery else null
                val genreList = if (selectedGenres.isNotEmpty()) selectedGenres.toList() else null
                val ageRating = selectedAgeRating?.takeIf { it.isNotBlank() }
                val distributionType = selectedDistributionType?.takeIf { it.isNotBlank() }
                val minAvgRating = if (minRating > 0) minRating.toDouble() else null
                val maxAvgRating = if (maxRating < 5) maxRating.toDouble() else null
                val applicationStatus = selectedApplicationStatus?.takeIf { it.isNotBlank() }
                val deadlineFilter = selectedDeadlineFilter?.takeIf { it.isNotBlank() }
                val sortBy = selectedSortBy?.takeIf { it.isNotBlank() }

                currentSkip = 0
                hasMore = false
                bookViewModel.browseBooks(
                    query = query,
                    genres = genreList,
                    ageRating = ageRating,
                    distributionType = distributionType,
                    minAvgRating = minAvgRating,
                    maxAvgRating = maxAvgRating,
                    skip = 0,
                    take = pageSize,
                    applicationStatus = applicationStatus,
                    deadlineFilter = deadlineFilter,
                    sortBy = sortBy,
                    append = false
                )

                previousSearchQuery = debouncedSearchQuery
                previousGenres = selectedGenres
                previousAgeRating = selectedAgeRating
                previousDistributionType = selectedDistributionType
                previousMinRating = minRating
                previousMaxRating = maxRating
                previousApplicationStatus = selectedApplicationStatus
                previousDeadlineFilter = selectedDeadlineFilter
                previousSortBy = selectedSortBy
            }
        }
    }

    LaunchedEffect(initialLoadCompleted) {
        if (initialLoadCompleted && (category == null || category == "search")) {
            delay(150)
            filterEffectHasRun = true
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val recentSearches by searchHistoryManager.recentSearches.collectAsState(initial = emptyList())
    var showRecentSearches by remember { mutableStateOf(false) }

    LaunchedEffect(debouncedSearchQuery) {
        if (showFiltersForBrowse && category == null && debouncedSearchQuery.isNotBlank() && initialLoadCompleted) {
            coroutineScope.launch {
                searchHistoryManager.addSearch(debouncedSearchQuery)
            }
        }
    }

    fun loadMore() {
        if (!hasMore || isLoadingMore || isLoading || category != null) return

        coroutineScope.launch {
            isLoadingMore = true
            val nextSkip = currentSkip + pageSize
            val query = if (debouncedSearchQuery.isNotBlank()) debouncedSearchQuery else null
            val genreList = if (selectedGenres.isNotEmpty()) selectedGenres.toList() else null
            val ageRating = selectedAgeRating?.takeIf { it.isNotBlank() }
            val distributionType = selectedDistributionType?.takeIf { it.isNotBlank() }
            val minAvgRating = if (minRating > 0) minRating.toDouble() else null
            val maxAvgRating = if (maxRating < 5) maxRating.toDouble() else null
            val applicationStatus = selectedApplicationStatus?.takeIf { it.isNotBlank() }
            val deadlineFilter = selectedDeadlineFilter?.takeIf { it.isNotBlank() }
            val sortBy = selectedSortBy?.takeIf { it.isNotBlank() }

            bookViewModel.browseBooks(
                query = query,
                genres = genreList,
                ageRating = ageRating,
                distributionType = distributionType,
                minAvgRating = minAvgRating,
                maxAvgRating = maxAvgRating,
                skip = nextSkip,
                take = pageSize,
                applicationStatus = applicationStatus,
                deadlineFilter = deadlineFilter,
                sortBy = sortBy,
                append = true
            )

            currentSkip = nextSkip
            isLoadingMore = false
        }
    }

    Scaffold(
        topBar = {
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
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(
                                Icons.Filled.FilterList,
                                contentDescription = "Filters",
                                tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-175).dp, y = (-175).dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-135).dp, y = (-135).dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 175.dp, y = 175.dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 135.dp, y = 135.dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (category == null || category == "search") {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val interactionSource = remember { MutableInteractionSource() }
                            val isFocused by interactionSource.collectIsFocusedAsState()

                            LaunchedEffect(isFocused) {
                                if (isFocused && currentSearchQuery.isBlank() && recentSearches.isNotEmpty()) {
                                    showRecentSearches = true
                                }
                            }

                            OutlinedTextField(
                                value = currentSearchQuery,
                                onValueChange = { newValue ->
                                    currentSearchQuery = newValue
                                    showRecentSearches =
                                        newValue.isBlank() && recentSearches.isNotEmpty() && isFocused
                                },
                                placeholder = {
                                    Text(
                                        "Search by title, author, or series",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
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
                                    if (currentSearchQuery.isNotBlank()) {
                                        IconButton(onClick = {
                                            currentSearchQuery = ""
                                            showRecentSearches =
                                                recentSearches.isNotEmpty() && isFocused
                                        }) {
                                            Icon(
                                                Icons.Filled.Clear,
                                                contentDescription = "Clear"
                                            )
                                        }
                                    } else if (recentSearches.isNotEmpty()) {
                                        IconButton(onClick = {
                                            showRecentSearches = !showRecentSearches
                                        }) {
                                            Icon(
                                                if (showRecentSearches) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                                contentDescription = "Recent Searches"
                                            )
                                        }
                                    }
                                }
                            )

                            if (showFiltersForBrowse) {
                                IconButton(
                                    onClick = { showFilters = !showFilters },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.FilterList,
                                        contentDescription = "Filters",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                    }

                    if (showRecentSearches && recentSearches.isNotEmpty()) {
                        item {
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
                                            text = "Recent Searches",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        TextButton(onClick = {
                                            coroutineScope.launch {
                                                searchHistoryManager.clearHistory()
                                            }
                                        }) {
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
                                                    currentSearchQuery = search
                                                    debouncedSearchQuery = search
                                                    showRecentSearches = false
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
                    }
                }

                if (showFilters && showFiltersForBrowse) {
                    item {
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
                                            selectedGenres = emptySet()
                                            selectedAgeRating = null
                                            selectedDistributionType = null
                                            minRating = 0f
                                            maxRating = 5f
                                            selectedApplicationStatus = null
                                            selectedDeadlineFilter = null
                                            selectedSortBy = null
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
                                    if (genresLoading) {
                                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                                    } else {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            items(genres) { genre ->
                                                FilterChip(
                                                    selected = selectedGenres.contains(genre.id),
                                                    onClick = {
                                                        selectedGenres =
                                                            if (selectedGenres.contains(genre.id)) {
                                                                selectedGenres - genre.id
                                                            } else {
                                                                selectedGenres + genre.id
                                                            }
                                                    },
                                                    label = { Text(genre.name) }
                                                )
                                            }
                                        }
                                    }
                                }

                                AgeRatingFilter(
                                    selectedAgeRating = selectedAgeRating,
                                    onAgeRatingSelected = { selectedAgeRating = it }
                                )

                                DistributionTypeFilter(
                                    selectedDistributionType = selectedDistributionType,
                                    onDistributionTypeSelected = {
                                        selectedDistributionType = it
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
                                                minRating
                                            )
                                        } - ${String.format("%.1f", maxRating)}",
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
                                            text = String.format("%.1f", minRating),
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.width(40.dp)
                                        )
                                        Slider(
                                            value = minRating,
                                            onValueChange = {
                                                minRating = it.coerceAtMost(maxRating)
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
                                            text = String.format("%.1f", maxRating),
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.width(40.dp)
                                        )
                                        Slider(
                                            value = maxRating,
                                            onValueChange = {
                                                maxRating = it.coerceAtLeast(minRating)
                                            },
                                            valueRange = 0f..5f,
                                            steps = 49,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                ApplicationStatusFilter(
                                    selectedApplicationStatus = selectedApplicationStatus,
                                    onApplicationStatusSelected = {
                                        selectedApplicationStatus = it
                                    }
                                )

                                DeadlineFilter(
                                    selectedDeadlineFilter = selectedDeadlineFilter,
                                    onDeadlineFilterSelected = { selectedDeadlineFilter = it }
                                )

                                SortByFilter(
                                    selectedSortBy = selectedSortBy,
                                    onSortBySelected = { selectedSortBy = it }
                                )
                            }
                        }
                    }
                }

                if (showFiltersForBrowse && category == null) {
                    val activeFilters = buildList<String> {
                        if (debouncedSearchQuery.isNotBlank()) add("Search: $debouncedSearchQuery")
                        selectedGenres.forEach { genreId ->
                            genres.find { it.id == genreId }?.let { add("Genre: ${it.name}") }
                        }
                        selectedAgeRating?.let { add("Age: ${it.replaceFirstChar { it.uppercase() }}") }
                        selectedDistributionType?.let { add("Type: ${it.replaceFirstChar { it.uppercase() }}") }
                        if (minRating > 0 || maxRating < 5) add(
                            "Rating: ${
                                String.format(
                                    "%.1f",
                                    minRating
                                )
                            }-${String.format("%.1f", maxRating)}"
                        )
                        selectedApplicationStatus?.let {
                            add("Status: ${if (it == "accepting_applications") "Accepting Applications" else "All Books"}")
                        }
                        selectedDeadlineFilter?.let {
                            add("Deadline: ${if (it == "ending_soon") "Ending Soon" else "Still Time"}")
                        }
                        selectedSortBy?.let {
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

                    if (activeFilters.isNotEmpty()) {
                        item {
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
                                            selectedGenres = emptySet()
                                            selectedAgeRating = null
                                            selectedDistributionType = null
                                            minRating = 0f
                                            maxRating = 5f
                                            selectedApplicationStatus = null
                                            selectedDeadlineFilter = null
                                            selectedSortBy = null
                                            currentSearchQuery = ""
                                            debouncedSearchQuery = ""
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
                                                                currentSearchQuery = ""
                                                                debouncedSearchQuery = ""
                                                            }

                                                            filter.startsWith("Genre:") -> {
                                                                val genreName =
                                                                    filter.removePrefix("Genre: ")
                                                                genres.find { it.name == genreName }
                                                                    ?.let {
                                                                        selectedGenres =
                                                                            selectedGenres - it.id
                                                                    }
                                                            }

                                                            filter.startsWith("Age:") -> selectedAgeRating =
                                                                null

                                                            filter.startsWith("Type:") -> selectedDistributionType =
                                                                null

                                                            filter.startsWith("Rating:") -> {
                                                                minRating = 0f
                                                                maxRating = 5f
                                                            }

                                                            filter.startsWith("Status:") -> selectedApplicationStatus =
                                                                null

                                                            filter.startsWith("Deadline:") -> selectedDeadlineFilter =
                                                                null

                                                            filter.startsWith("Sort:") -> selectedSortBy =
                                                                null
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
                                        category == "search" -> "No books found for \"$debouncedSearchQuery\""
                                        debouncedSearchQuery.isBlank() && selectedGenres.isEmpty() && selectedAgeRating == null && selectedDistributionType == null -> "No books available"
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

                            if (index == currentBooks.size - 1 && hasMore && !isLoadingMore && category == null) {
                                loadMore()
                            }
                        }

                        if (isLoadingMore) {
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
                                    text = "No books found for \"$debouncedSearchQuery\"",
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