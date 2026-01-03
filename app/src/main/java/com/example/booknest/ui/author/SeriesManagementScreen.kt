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

@Composable
fun SeriesCard(
    series: com.example.booknest.domain.model.response.SeriesResponse,
    books: List<RecommendedBookResponse>,
    isLoadingBooks: Boolean,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean,
    navController: NavHostController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = series.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${books.size} book${if (books.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    series.description?.let { description ->
                        if (description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = if (isExpanded) Int.MAX_VALUE else 1
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(
                        onClick = onDelete,
                        enabled = canDelete
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = if (canDelete) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.38f
                            )
                        )
                    }
                    IconButton(onClick = onExpandToggle) {
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand"
                        )
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoadingBooks) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else if (books.isEmpty()) {
                    Text(
                        text = "No books in this series yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Text(
                        text = "Books in Series:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    books.sortedBy { it.seriesOrder ?: Int.MAX_VALUE }
                        .forEachIndexed { index, book ->
                            BookInSeriesItem(
                                book = book,
                                order = book.seriesOrder ?: (index + 1),
                                onClick = {
                                    navController.navigate(Screen.BookApplicationDetail.createRoute(book.id))
                                }
                            )
                            if (index < books.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                }
            }
        }
    }
}

@Composable
fun BookInSeriesItem(
    book: RecommendedBookResponse,
    order: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = order.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AsyncImage(
                model = book.coverImageUrl,
                contentDescription = book.title,
                modifier = Modifier.size(48.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                book.resolvedAuthorName?.let { authorName ->
                    Text(
                        text = authorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CreateSeriesDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Series") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Series Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim(), description.trim().takeIf { it.isNotBlank() }) },
                enabled = name.trim().isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditSeriesDialog(
    series: com.example.booknest.domain.model.response.SeriesResponse,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf(series.name) }
    var description by remember { mutableStateOf(series.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Series") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Series Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim(), description.trim().takeIf { it.isNotBlank() }) },
                enabled = name.trim().isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteSeriesDialog(
    series: com.example.booknest.domain.model.response.SeriesResponse,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Series") },
        text = {
            Text("Are you sure you want to delete \"${series.name}\"? This action cannot be undone.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
