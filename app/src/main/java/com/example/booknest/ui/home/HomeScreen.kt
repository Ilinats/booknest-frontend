package com.example.booknest.ui.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.booknest.data.AuthManager
import com.example.booknest.navigation.BottomBarScreen
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.books.BookItem
import com.example.booknest.viewmodel.BookViewModel
import com.example.booknest.viewmodel.BookViewModelFactory
import com.example.booknest.viewmodel.FriendViewModel
import com.example.booknest.viewmodel.FriendViewModelFactory
import com.example.booknest.viewmodel.AuthorFollowViewModel
import com.example.booknest.viewmodel.AuthorFollowViewModelFactory
import com.example.booknest.network.UserActivity
import com.example.booknest.ui.components.ActivityItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authManager: AuthManager,
    bookViewModel: BookViewModel = viewModel(
        factory = BookViewModelFactory(authManager)
    ),
    friendViewModel: FriendViewModel = viewModel(
        factory = FriendViewModelFactory(authManager)
    ),
    authorFollowViewModel: AuthorFollowViewModel = viewModel(
        factory = AuthorFollowViewModelFactory(authManager)
    )
) {
    val recommendedBooks by bookViewModel.recommendedBooks.collectAsState()
    val newReleases by bookViewModel.newReleases.collectAsState()
    val isLoading by bookViewModel.isLoading.collectAsState()
    val currentUser by authManager.currentUser.collectAsState()
    
    // Friend activity data
    val friendsActivity by friendViewModel.friendsActivity.collectAsState()
    val friendsActivityLoading by friendViewModel.isLoading.collectAsState()
    
    // Books from followed authors
    val booksFromFollowedAuthors by authorFollowViewModel.booksFromFollowedAuthors.collectAsState()
    val followedAuthorsLoading by authorFollowViewModel.isLoading.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        bookViewModel.getRecommendedBooks()
        bookViewModel.getNewReleases()
        friendViewModel.loadFriendsActivity()
        authorFollowViewModel.loadBooksFromFollowedAuthors()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "BookNest",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                actions = {
                    IconButton(onClick = { 
                        if (searchQuery.isNotBlank()) {
                            navController.navigate(BottomBarScreen.Browse.withQuery(searchQuery))
                        } else {
                            navController.navigate(BottomBarScreen.Browse.route)
                        }
                    }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { 
                        currentUser?.id?.let { userId ->
                            try {
                                navController.navigate(Screen.Profile.createRoute(userId))
                            } catch (e: Exception) {
                                println("Navigation error: ${e.message}")
                                // Fallback navigation
                                navController.navigate("profile/$userId")
                            }
                        }
                    }) {
                        Icon(
                            Icons.Filled.AccountCircle, 
                            contentDescription = "Profile",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Search Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search books...") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        singleLine = true
                    )
                }
            }

            // Welcome Section
            item {
                Column {
                    Text(
                        text = "Welcome back!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentUser?.username ?: "User",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Recommended for You Section
            item {
                Column {
                    Text(
                        text = "Recommended for You",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    if (isLoading && recommendedBooks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(recommendedBooks) { book ->
                                BookItem(book = book, navController = navController)
                            }
                        }
                    }
                }
            }

            // New Releases Section
            item {
                Column {
                    Text(
                        text = "New Releases",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    if (isLoading && newReleases.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(newReleases) { book ->
                                BookItem(book = book, navController = navController)
                            }
                        }
                    }
                }
            }

            // Books from Followed Authors Section
            item {
                Column {
                    Text(
                        text = "From Authors You Follow",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    if (followedAuthorsLoading && booksFromFollowedAuthors.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (booksFromFollowedAuthors.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(booksFromFollowedAuthors) { book ->
                                BookItem(book = book, navController = navController)
                            }
                        }
                    } else {
                        Text(
                            text = "Follow some authors to see their latest books here!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Friends Activity Feed Section
            item {
                Column {
                    Text(
                        text = "Friends Activity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    if (friendsActivityLoading && friendsActivity.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (friendsActivity.isNotEmpty()) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(friendsActivity) { activity ->
                                ActivityItem(activity = activity, navController = navController)
                            }
                        }
                    } else {
                        Text(
                            text = "Add some friends to see their activity here!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
