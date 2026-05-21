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
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.ui.components.AppScaffoldContentInsets
import com.example.booknest.ui.components.AppTopBar
import com.example.booknest.ui.components.BackButton
import com.example.booknest.ui.components.paddingTopFromScaffold
import com.example.booknest.viewmodel.books.BrowseBooksViewModel
import com.example.booknest.viewmodel.series.SeriesViewModel
import com.example.booknest.ui.author.components.series.CreateSeriesDialog
import com.example.booknest.ui.author.components.series.DeleteSeriesDialog
import com.example.booknest.ui.author.components.series.EditSeriesDialog
import com.example.booknest.ui.author.components.series.SeriesCard
import com.example.booknest.ui.components.BackgroundDecoration
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesManagementScreen(
    navController: NavHostController,
    sessionManager: SessionManager = koinInject(),
    seriesViewModel: SeriesViewModel = getViewModel(),
    browseBooksViewModel: BrowseBooksViewModel = getViewModel()
) {
    val series by seriesViewModel.series.collectAsState()
    val isLoading by seriesViewModel.isLoading.collectAsState()
    val seriesBooksMap by browseBooksViewModel.seriesBooksBySeriesId.collectAsState()
    val loadingBooksForSeries by browseBooksViewModel.seriesBooksLoadingIds.collectAsState()
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

    LaunchedEffect(Unit) {
        seriesViewModel.loadMySeries()
    }

    LaunchedEffect(series) {
        series.forEach { seriesItem ->
            browseBooksViewModel.ensureSeriesBooksLoaded(
                seriesId = seriesItem.id,
                forceRefresh = false,
                treatFailureAsEmptyCatalog = true,
                status = null,
            )
        }
    }

    LaunchedEffect(expandedSeriesIds) {
        expandedSeriesIds.forEach { seriesId ->
            browseBooksViewModel.ensureSeriesBooksLoaded(
                seriesId = seriesId,
                forceRefresh = false,
                treatFailureAsEmptyCatalog = true,
                status = null,
            )
        }
    }

    Scaffold(
        contentWindowInsets = AppScaffoldContentInsets,
        topBar = {
            AppTopBar(
                title = "Series Management",
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Series",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BackgroundDecoration(modifier = Modifier.fillMaxSize())

            if (isLoading && series.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .paddingTopFromScaffold(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (series.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .paddingTopFromScaffold(paddingValues),
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
                        .paddingTopFromScaffold(paddingValues),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 16.dp
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