package com.example.booknest.ui.author

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.booknest.data.session.SessionManager
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.BookStatsResponse
import com.example.booknest.navigation.Screen
import com.example.booknest.navigation.AuthorBottomBarScreen
import com.example.booknest.ui.author.BookStatus
import com.example.booknest.ui.author.components.books.EnhancedBookCard
import com.example.booknest.ui.author.components.books.StatusChip
import com.example.booknest.ui.author.components.books.EmptyBooksState
import com.example.booknest.ui.author.components.books.BookStatusInfoCard
import com.example.booknest.ui.author.components.books.calculateDaysLeft
import com.example.booknest.ui.author.components.books.formatDate
import com.example.booknest.ui.author.components.books.parseDate
import com.example.booknest.viewmodel.AuthorViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

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
                            item {
                                BookStatusInfoCard()
                            }

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
