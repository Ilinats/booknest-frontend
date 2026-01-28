package com.example.booknest.ui.author

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.components.BackButton
import com.example.booknest.viewmodel.BookViewModel
import com.example.booknest.viewmodel.SeriesViewModel
import com.example.booknest.domain.usecase.books.BrowseBooksUseCase
import com.example.booknest.ui.author.components.series.CreateSeriesDialog
import com.example.booknest.ui.author.components.series.DeleteSeriesDialog
import com.example.booknest.ui.author.components.series.EditSeriesDialog
import com.example.booknest.ui.author.components.series.SeriesCard
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesManagementScreen(
    navController: NavHostController,
    sessionManager: SessionManager = koinInject(),
    seriesViewModel: SeriesViewModel = getViewModel(),
    bookViewModel: BookViewModel = getViewModel(),
    browseBooksUseCase: BrowseBooksUseCase = koinInject()
) {
    val series by seriesViewModel.series.collectAsState()
    val isLoading by seriesViewModel.isLoading.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingSeries by remember {
        mutableStateOf<com.example.booknest.domain.model.response.SeriesResponse?>(
            null
        )
    }
    var deletingSeries by remember {
        mutableStateOf<com.example.booknest.domain.model.response.SeriesResponse?>(
            null
        )
    }
    var expandedSeriesIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    var seriesBooksMap by remember {
        mutableStateOf<Map<String, List<RecommendedBookResponse>>>(
            emptyMap()
        )
    }
    var loadingBooksForSeries by remember { mutableStateOf<Set<String>>(emptySet()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        seriesViewModel.loadMySeries()
    }

    LaunchedEffect(series) {
        series.forEach { seriesItem ->
            if (!seriesBooksMap.containsKey(seriesItem.id) && !loadingBooksForSeries.contains(
                    seriesItem.id
                )
            ) {
                loadingBooksForSeries = loadingBooksForSeries + seriesItem.id

                scope.launch {
                    browseBooksUseCase(
                        seriesId = seriesItem.id,
                        status = "active",
                        take = 100
                    ).onSuccess { fetchedBooks ->
                        seriesBooksMap = seriesBooksMap + (seriesItem.id to fetchedBooks.sortedBy {
                            it.seriesOrder ?: Int.MAX_VALUE
                        })
                        loadingBooksForSeries = loadingBooksForSeries - seriesItem.id
                    }.onFailure { e ->
                        println("Failed to load books for series ${seriesItem.id}: ${e.message}")
                        loadingBooksForSeries = loadingBooksForSeries - seriesItem.id
                        seriesBooksMap = seriesBooksMap + (seriesItem.id to emptyList())
                    }
                }
            }
        }
    }

    LaunchedEffect(expandedSeriesIds) {
        expandedSeriesIds.forEach { seriesId ->
            if (!seriesBooksMap.containsKey(seriesId) && !loadingBooksForSeries.contains(seriesId)) {
                loadingBooksForSeries = loadingBooksForSeries + seriesId

                scope.launch {
                    browseBooksUseCase(
                        seriesId = seriesId,
                        status = "active",
                        take = 100
                    ).onSuccess { fetchedBooks ->
                        seriesBooksMap = seriesBooksMap + (seriesId to fetchedBooks.sortedBy {
                            it.seriesOrder ?: Int.MAX_VALUE
                        })
                        loadingBooksForSeries = loadingBooksForSeries - seriesId
                    }.onFailure { e ->
                        println("Failed to load books for series $seriesId: ${e.message}")
                        loadingBooksForSeries = loadingBooksForSeries - seriesId
                        seriesBooksMap = seriesBooksMap + (seriesId to emptyList())
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "Series Management",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    navigationIcon = {
                        BackButton(onClick = { navController.popBackStack() })
                    },
                    actions = {
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Series",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Series")
            }
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

            if (isLoading && series.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (series.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.CollectionsBookmark,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No series created yet",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Create your first series to organize your books",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { showCreateDialog = true }) {
                            Text("Create Series")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(series) { seriesItem ->
                        SeriesCard(
                            series = seriesItem,
                            books = seriesBooksMap[seriesItem.id] ?: emptyList(),
                            isLoadingBooks = loadingBooksForSeries.contains(seriesItem.id),
                            isExpanded = expandedSeriesIds.contains(seriesItem.id),
                            onExpandToggle = {
                                expandedSeriesIds = if (expandedSeriesIds.contains(seriesItem.id)) {
                                    expandedSeriesIds - seriesItem.id
                                } else {
                                    expandedSeriesIds + seriesItem.id
                                }
                            },
                            onEdit = { editingSeries = seriesItem },
                            onDelete = { deletingSeries = seriesItem },
                            canDelete = seriesBooksMap[seriesItem.id]?.isEmpty() != false,
                            navController = navController
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateSeriesDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, description ->
                seriesViewModel.createSeries(name, description)
                showCreateDialog = false
            }
        )
    }

    editingSeries?.let { series ->
        EditSeriesDialog(
            series = series,
            onDismiss = { editingSeries = null },
            onConfirm = { name, description ->
                seriesViewModel.updateSeries(series.id, name, description)
                editingSeries = null
            }
        )
    }

    deletingSeries?.let { series ->
        DeleteSeriesDialog(
            series = series,
            onDismiss = { deletingSeries = null },
            onConfirm = {
                seriesViewModel.deleteSeries(series.id)
                deletingSeries = null
            }
        )
    }
}