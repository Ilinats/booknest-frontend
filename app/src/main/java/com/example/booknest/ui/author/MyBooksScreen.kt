package com.example.booknest.ui.author

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Done // promeni go za published da e
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.booknest.data.AuthManager
import com.example.booknest.navigation.Screen
import com.example.booknest.network.Book
import com.example.booknest.network.BookStatus
import com.example.booknest.viewmodel.AuthorViewModel
import com.example.booknest.viewmodel.AuthorViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBooksScreen(
    navController: NavController,
    authManager: AuthManager,
    authorViewModel: AuthorViewModel = viewModel(
        factory = AuthorViewModelFactory(authManager)
    )
) {
    val myBooks by authorViewModel.myBooks.collectAsState()
    val isLoadingBooks by authorViewModel.isLoadingBooks.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Active", "In Progress", "Completed", "Archived")

    LaunchedEffect(Unit) {
        authorViewModel.loadMyBooks()
    }

    // Filter books based on selected tab
    val filteredBooks = remember(selectedTab, myBooks) {
        when (selectedTab) {
            0 -> myBooks.filter { it.status == BookStatus.ACTIVE }
            1 -> myBooks.filter { it.status == BookStatus.IN_PROGRESS }
            2 -> myBooks.filter { it.status == BookStatus.COMPLETED }
            3 -> myBooks.filter { it.status == BookStatus.ARCHIVED }
            else -> myBooks
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "My Books",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { navController.navigate(Screen.BookCreation.route) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Create New Campaign")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab navigation
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Content based on selected tab
            if (isLoadingBooks && filteredBooks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredBooks.isEmpty()) {
                EmptyBooksState(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    onCreateBook = { navController.navigate(Screen.BookCreation.route) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredBooks) { book ->
                        BookCampaignCard(
                            book = book,
                            onNavigateToApplications = { 
                                navController.navigate("book_applications/${book.id}")
                            },
                            onEdit = { /* TODO: Navigate to edit book */ },
                            onPublish = { /* TODO: Publish book */ },
                            onDelete = { /* TODO: Delete book */ },
                            onViewStats = { 
                                navController.navigate("book_analytics/${book.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookCampaignCard(
    book: Book,
    onNavigateToApplications: () -> Unit,
    onEdit: () -> Unit,
    onPublish: () -> Unit,
    onDelete: () -> Unit,
    onViewStats: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToApplications() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Book details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Status
                Text(
                    text = when (book.status) {
                        BookStatus.ACTIVE -> "Accepting Applications"
                        BookStatus.IN_PROGRESS -> "In Progress"
                        BookStatus.COMPLETED -> "Campaign Completed"
                        BookStatus.ARCHIVED -> "Archived"
                        else -> "Draft"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Book title
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Stats
                Text(
                    text = "Applicants: ${book.totalCopies} · Approved: ${book.availableCopies}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Reviews: 0", // TODO: Get actual review count
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Right side - Book cover placeholder and menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp, 80.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = "Book Cover",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Menu button
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
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
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }
                        )
                        
                        if (book.status == BookStatus.DRAFT) {
                            DropdownMenuItem(
                                text = { Text("Publish") },
                                onClick = {
                                    showMenu = false
                                    onPublish()
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Done, contentDescription = "Publish")
                                }
                            )
                        }
                        
                        DropdownMenuItem(
                            text = { Text("View Stats") },
                            onClick = {
                                showMenu = false
                                onViewStats()
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookManagementCard(
    book: Book,
    onEdit: () -> Unit,
    onPublish: () -> Unit,
    onDelete: () -> Unit,
    onViewStats: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navigate to book details */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    book.shortDescription?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusChip(status = book.status)
                        Text(
                            text = "${book.availableCopies}/${book.totalCopies} copies",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
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
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }
                        )
                        
                        if (book.status == BookStatus.DRAFT) {
                            DropdownMenuItem(
                                text = { Text("Publish") },
                                onClick = {
                                    showMenu = false
                                    onPublish()
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Done, contentDescription = "Publish")
                                }
                            )
                        }
                        
                        DropdownMenuItem(
                            text = { Text("View Stats") },
                            onClick = {
                                showMenu = false
                                onViewStats()
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Quick actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (book.status == BookStatus.DRAFT) {
                    Button(
                        onClick = onPublish,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.Done,
                            contentDescription = "Publish",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Publish")
                    }
                }
                
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: BookStatus?) {
    val (backgroundColor, textColor, statusText) = when (status) {
        BookStatus.DRAFT -> Triple(
            MaterialTheme.colorScheme.surfaceVariant, 
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Draft"
        )
        BookStatus.ACTIVE -> Triple(
            MaterialTheme.colorScheme.primaryContainer, 
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Active"
        )
        BookStatus.IN_PROGRESS -> Triple(
            MaterialTheme.colorScheme.secondaryContainer, 
            MaterialTheme.colorScheme.onSecondaryContainer,
            "In Progress"
        )
        BookStatus.COMPLETED -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer, 
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Completed"
        )
        BookStatus.ARCHIVED -> Triple(
            MaterialTheme.colorScheme.errorContainer, 
            MaterialTheme.colorScheme.onErrorContainer,
            "Archived"
        )
        null -> Triple(
            MaterialTheme.colorScheme.surfaceVariant, 
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Unknown"
        )
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyBooksState(
    modifier: Modifier = Modifier,
    onCreateBook: () -> Unit
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
            text = "No Books Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Create your first book to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onCreateBook
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Create Book",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Your First Book")
        }
    }
}
