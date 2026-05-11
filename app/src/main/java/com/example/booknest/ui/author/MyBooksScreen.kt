package com.example.booknest.ui.author

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.booknest.data.session.SessionManager
import com.example.booknest.navigation.Screen
import com.example.booknest.navigation.AuthorBottomBarScreen
import com.example.booknest.ui.components.BackgroundDecoration
import com.example.booknest.ui.author.components.books.EnhancedBookCard
import com.example.booknest.ui.author.components.books.StatusChip
import com.example.booknest.ui.author.components.books.EmptyBooksState
import com.example.booknest.ui.author.components.books.BookStatusInfoCard
import com.example.booknest.viewmodel.author.AuthorBooksViewModel
import com.example.booknest.viewmodel.author.BookSortOption
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBooksScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    authorBooksViewModel: AuthorBooksViewModel = getViewModel()
) {
    val filteredBooks by authorBooksViewModel.filteredBooks.collectAsState()
    val tabCounts by authorBooksViewModel.tabCounts.collectAsState()
    val isLoadingBooks by authorBooksViewModel.isLoadingBooks.collectAsState()
    val searchQuery by authorBooksViewModel.searchQuery.collectAsState()
    val selectedTab by authorBooksViewModel.selectedTab.collectAsState()
    val sortOption by authorBooksViewModel.sortOption.collectAsState()
    val bookStats by authorBooksViewModel.bookStats.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        if (currentRoute == AuthorBottomBarScreen.MyBooks.route) {
            authorBooksViewModel.loadMyBooks()
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
            BackgroundDecoration(modifier = Modifier.fillMaxSize())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { authorBooksViewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(28.dp)),
                    placeholder = { Text("Search books...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { authorBooksViewModel.updateSearchQuery("") }) {
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
                            Icon(Icons.Default.Sort, contentDescription = "Sort", modifier = Modifier.size(18.dp))
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
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            DropdownMenuItem(text = { Text("Date Created") }, onClick = {
                                authorBooksViewModel.updateSortOption(BookSortOption.DATE_CREATED)
                                showSortMenu = false
                            })
                            DropdownMenuItem(text = { Text("Title") }, onClick = {
                                authorBooksViewModel.updateSortOption(BookSortOption.TITLE)
                                showSortMenu = false
                            })
                            DropdownMenuItem(text = { Text("Status") }, onClick = {
                                authorBooksViewModel.updateSortOption(BookSortOption.STATUS)
                                showSortMenu = false
                            })
                            DropdownMenuItem(text = { Text("Application Count") }, onClick = {
                                authorBooksViewModel.updateSortOption(BookSortOption.APPLICATION_COUNT)
                                showSortMenu = false
                            })
                        }
                    }
                }

                Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        edgePadding = 0.dp
                    ) {
                        listOf("All", "Draft", "Active", "In Progress", "Completed").forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { authorBooksViewModel.updateSelectedTab(index) },
                                text = {
                                    val count = tabCounts[index] ?: 0
                                    Text(
                                        text = if (count > 0) "$title ($count)" else title,
                                        color = if (selectedTab == index) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    if (isLoadingBooks && filteredBooks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (filteredBooks.isEmpty()) {
                        EmptyBooksState(
                            modifier = Modifier.fillMaxSize(),
                            onCreateBook = { navController.navigate(Screen.BookCreation.route) },
                            hasSearchQuery = searchQuery.isNotEmpty()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item { BookStatusInfoCard() }
                            items(filteredBooks) { book ->
                                EnhancedBookCard(
                                    book = book,
                                    stats = bookStats[book.id],
                                    onNavigateToApplications = {
                                        navController.navigate(Screen.BookApplicationDetail.createRoute(book.id))
                                    },
                                    onEdit = { navController.navigate(Screen.BookEdit.createRoute(book.id)) },
                                    onPublish = { authorBooksViewModel.publishBook(book.id) },
                                    onDelete = { showDeleteDialog = book.id },
                                    onViewStats = { navController.navigate(Screen.BookAnalytics.createRoute(book.id)) },
                                    onViewProgress = {
                                        navController.navigate(Screen.BookApplicationDetail.createRoute(book.id))
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
                    TextButton(onClick = {
                        authorBooksViewModel.deleteBook(bookId)
                        showDeleteDialog = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
                }
            )
        }
    }
}
