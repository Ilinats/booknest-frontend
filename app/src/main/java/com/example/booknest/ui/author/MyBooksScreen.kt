package com.example.booknest.ui.author

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.BookStatsResponse
import com.example.booknest.navigation.Screen
import com.example.booknest.navigation.AuthorBottomBarScreen
import com.example.booknest.ui.author.BookStatus
import com.example.booknest.ui.components.BackButton
import com.example.booknest.viewmodel.AuthorViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale

enum class BookSortOption {
    DATE_CREATED,
    TITLE,
    STATUS,
    APPLICATION_COUNT
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBooksScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    authorViewModel: AuthorViewModel = getViewModel()
) {
    val myBooks by authorViewModel.myBooks.collectAsState()
    val isLoadingBooks by authorViewModel.isLoadingBooks.collectAsState()
    val bookStats by authorViewModel.bookStats.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(BookSortOption.DATE_CREATED) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        authorViewModel.loadMyBooks()
    }

    LaunchedEffect(currentRoute) {
        if (currentRoute == AuthorBottomBarScreen.MyBooks.route) {
            authorViewModel.loadMyBooks()
        }
    }

    LaunchedEffect(myBooks) {
        myBooks.forEach { book ->
            authorViewModel.getBookStats(book.id)
        }
    }

    val nonArchivedBooks = remember(myBooks) {
        myBooks.filter { it.status != BookStatus.ARCHIVED.value }
    }

    val tabCounts = remember(nonArchivedBooks) {
        mapOf(
            0 to nonArchivedBooks.size,
            1 to nonArchivedBooks.count { it.status == BookStatus.DRAFT.value },
            2 to nonArchivedBooks.count { it.status == BookStatus.ACTIVE.value },
            3 to nonArchivedBooks.count { it.status == BookStatus.IN_PROGRESS.value },
            4 to nonArchivedBooks.count { it.status == BookStatus.COMPLETED.value }
        )
    }

    val filteredAndSortedBooks = remember(selectedTab, myBooks, searchQuery, sortOption) {
        val nonArchived = myBooks.filter { it.status != BookStatus.ARCHIVED.value }

        val filtered = when (selectedTab) {
            0 -> nonArchived
            1 -> nonArchived.filter { it.status == BookStatus.DRAFT.value }
            2 -> nonArchived.filter { it.status == BookStatus.ACTIVE.value }
            3 -> nonArchived.filter { it.status == BookStatus.IN_PROGRESS.value }
            4 -> nonArchived.filter { it.status == BookStatus.COMPLETED.value }
            else -> nonArchived
        }

        val searched = if (searchQuery.isBlank()) {
            filtered
        } else {
            filtered.filter { book ->
                book.title.contains(searchQuery, ignoreCase = true) ||
                        book.shortDescription?.contains(searchQuery, ignoreCase = true) == true
            }
        }

        when (sortOption) {
            BookSortOption.DATE_CREATED -> searched.sortedByDescending {
                parseDate(it.createdAt)?.time ?: 0L
            }

            BookSortOption.TITLE -> searched.sortedBy { it.title }
            BookSortOption.STATUS -> searched.sortedBy { it.status }
            BookSortOption.APPLICATION_COUNT -> searched.sortedByDescending { book ->
                bookStats[book.id]?.effectiveTotalApplications ?: 0
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
                            "My Books",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { navController.navigate(Screen.BookCreation.route) }
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Create New Book",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.BookCreation.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Book")
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(28.dp)
                        ),
                    placeholder = { Text("Search books...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear")
                            }
                        }
                    },
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
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        OutlinedButton(
                            onClick = { showSortMenu = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Default.Sort,
                                contentDescription = "Sort",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (sortOption) {
                                    BookSortOption.DATE_CREATED -> "Date Created"
                                    BookSortOption.TITLE -> "Title"
                                    BookSortOption.STATUS -> "Status"
                                    BookSortOption.APPLICATION_COUNT -> "Application Count"
                                },
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Date Created") },
                                onClick = {
                                    sortOption = BookSortOption.DATE_CREATED
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Title") },
                                onClick = {
                                    sortOption = BookSortOption.TITLE
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Status") },
                                onClick = {
                                    sortOption = BookSortOption.STATUS
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Application Count") },
                                onClick = {
                                    sortOption = BookSortOption.APPLICATION_COUNT
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        edgePadding = 0.dp
                    ) {
                        listOf(
                            "All",
                            "Draft",
                            "Active",
                            "In Progress",
                            "Completed"
                        ).forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    val count = tabCounts[index] ?: 0
                                    Text(
                                        text = if (count > 0) "$title ($count)" else title,
                                        color = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (isLoadingBooks && filteredAndSortedBooks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (filteredAndSortedBooks.isEmpty()) {
                        EmptyBooksState(
                            modifier = Modifier.fillMaxSize(),
                            onCreateBook = { navController.navigate(Screen.BookCreation.route) },
                            hasSearchQuery = searchQuery.isNotEmpty()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                top = 16.dp,
                                end = 16.dp,
                                bottom = 90.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredAndSortedBooks) { book ->
                                EnhancedBookCard(
                                    book = book,
                                    stats = bookStats[book.id],
                                    onNavigateToApplications = {
                                        navController.navigate("book_applications/${book.id}")
                                    },
                                    onEdit = {
                                        navController.navigate(Screen.BookEdit.createRoute(book.id))
                                    },
                                    onPublish = {
                                        authorViewModel.publishBook(book.id)
                                    },
                                    onDelete = {
                                        showDeleteDialog = book.id
                                    },
                                    onViewStats = {
                                        navController.navigate("book_analytics/${book.id}")
                                    },
                                    onViewProgress = {
                                        navController.navigate("book_applications/${book.id}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        showDeleteDialog?.let { bookId ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Book?") },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                text = { Text("This action cannot be undone. Are you sure you want to delete this book?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            authorViewModel.deleteBook(bookId)
                            showDeleteDialog = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EnhancedBookCard(
    book: BookResponse,
    stats: BookStatsResponse?,
    onNavigateToApplications: () -> Unit,
    onEdit: () -> Unit,
    onPublish: () -> Unit,
    onDelete: () -> Unit,
    onViewStats: () -> Unit,
    onViewProgress: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp, 120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (book.coverImageUrl != null) {
                        AsyncImage(
                            model = book.coverImageUrl,
                            contentDescription = book.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("📖", fontSize = 32.sp)
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(status = book.status)

                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    book.selectionMethod?.takeIf { it.isNotBlank() }?.let { selectionMethod ->
                        val displayName = formatSelectionMethod(selectionMethod)
                        Text(
                            text = "Selection: $displayName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    when (book.status) {
                        BookStatus.DRAFT.value -> {
                            book.createdAt?.let { date ->
                                Text(
                                    text = "Created: ${formatDate(date)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        BookStatus.ACTIVE.value -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Slots: ${stats?.approvedReaders ?: 0}/${book.totalCopies ?: 0} filled",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            book.applicationDeadline?.let { deadline ->
                                val daysLeft = calculateDaysLeft(deadline)
                                Text(
                                    text = if (daysLeft >= 0) "Ends in $daysLeft ${if (daysLeft == 1L) "day" else "days"}" else "Deadline passed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (daysLeft <= 3L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            val pendingCount = (stats?.effectiveTotalApplications
                                ?: 0) - (stats?.approvedReaders ?: 0)
                            if (pendingCount > 0) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "$pendingCount new ${if (pendingCount == 1) "application" else "applications"}",
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        BookStatus.IN_PROGRESS.value -> {
                            Text(
                                text = "${stats?.approvedReaders ?: 0} readers",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Reviews: ${stats?.reviewsSubmitted ?: 0}/${stats?.approvedReaders ?: 0}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        BookStatus.COMPLETED.value -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${stats?.reviewsSubmitted ?: 0} reviews",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                stats?.averageRating?.let { rating ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = String.format("%.1f", rating),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )

                        if (book.status == BookStatus.DRAFT.value) {
                            DropdownMenuItem(
                                text = { Text("Publish") },
                                onClick = {
                                    showMenu = false
                                    onPublish()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Publish, contentDescription = null)
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("View Stats") },
                            onClick = {
                                showMenu = false
                                onViewStats()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Analytics, contentDescription = null)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            when (book.status) {
                BookStatus.DRAFT.value -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Continue Editing")
                        }
                        Button(
                            onClick = onPublish,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Publish,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Publish")
                        }
                    }
                }

                BookStatus.ACTIVE.value -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToApplications,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View")
                        }
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }
                    }
                }

                BookStatus.IN_PROGRESS.value -> {
                    Button(
                        onClick = onViewProgress,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Progress")
                    }
                }

                BookStatus.COMPLETED.value -> {
                    Button(
                        onClick = onViewStats,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Analytics")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String?) {
    val (backgroundColor, textColor, statusText) = when (status) {
        BookStatus.DRAFT.value -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.primary,
            "Draft"
        )

        BookStatus.ACTIVE.value -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Active"
        )

        BookStatus.IN_PROGRESS.value -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "In Progress"
        )

        BookStatus.COMPLETED.value -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Completed"
        )

        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Unknown"
        )
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyBooksState(
    modifier: Modifier = Modifier,
    onCreateBook: () -> Unit,
    hasSearchQuery: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📚",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (hasSearchQuery) "No books found" else "No Books Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (hasSearchQuery) "Try adjusting your search" else "Create your first book to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!hasSearchQuery) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCreateBook,
                modifier = Modifier
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(color = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Book",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Create Your First Book",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateDaysLeft(deadline: String): Long {
    return try {
        val deadlineDate = Instant.parse(deadline)
        val now = Instant.now()
        ChronoUnit.DAYS.between(now, deadlineDate)
    } catch (e: Exception) {
        -1L
    }
}

fun formatDate(dateString: String?): String {
    if (dateString == null) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

fun parseDate(dateString: String?): java.util.Date? {
    if (dateString == null) return null
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        format.parse(dateString)
    } catch (e: Exception) {
        null
    }
}

fun formatSelectionMethod(selectionMethod: String): String {
    return when (selectionMethod) {
        "author_selects" -> "Author Selects"
        "first_come" -> "First Come First Served"
        "lottery" -> "Random Selection"
        else -> selectionMethod.replace("_", " ").split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { char -> char.uppercase() }
        }
    }
}