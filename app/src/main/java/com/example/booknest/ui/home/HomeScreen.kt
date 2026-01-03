package com.example.booknest.ui.home

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.ui.theme.SkyBluePeriwinkle
import com.example.booknest.ui.theme.DarkNavyBlue
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.navigation.BottomBarScreen
import com.example.booknest.navigation.Screen
import com.example.booknest.ui.books.BookItem
import com.example.booknest.ui.books.SimpleBookItem
import com.example.booknest.viewmodel.BookViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import com.example.booknest.viewmodel.FriendViewModel
import com.example.booknest.viewmodel.AuthorFollowViewModel
import com.example.booknest.viewmodel.ApplicationViewModel
import com.example.booknest.viewmodel.NotificationViewModel
import com.example.booknest.ui.components.ActivityItem
import com.example.booknest.ui.theme.BackgroundWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    bookViewModel: BookViewModel = getViewModel(),
    friendViewModel: FriendViewModel = getViewModel(),
    authorFollowViewModel: AuthorFollowViewModel = getViewModel(),
    applicationViewModel: ApplicationViewModel = getViewModel(),
    notificationViewModel: NotificationViewModel = getViewModel()
) {
    val recommendedBooks by bookViewModel.recommendedBooks.collectAsState()
    val newReleases by bookViewModel.newReleases.collectAsState()
    val isLoading by bookViewModel.isLoading.collectAsState()
    val currentUser by sessionManager.currentUser.collectAsState()

    val friendsActivity by friendViewModel.friendsActivity.collectAsState()
    val friendsActivityLoading by friendViewModel.isLoading.collectAsState()

    LaunchedEffect(friendsActivity) {
        android.util.Log.d(
            "HomeScreen",
            "Friends activity state updated: ${friendsActivity.size} activities"
        )
        friendsActivity.forEachIndexed { index, activity ->
            android.util.Log.d(
                "HomeScreen",
                "Activity $index: ${activity.activityType} by ${activity.user?.username}"
            )
        }
    }

    val booksFromFollowedAuthors by authorFollowViewModel.booksFromFollowedAuthors.collectAsState()
    val followedAuthorsLoading by authorFollowViewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val searchResults by bookViewModel.homeSearchResults.collectAsState()
    val isSearching by bookViewModel.isLoading.collectAsState()

    val myApplications by applicationViewModel.myApplications.collectAsState()
    val approvedApplications = myApplications.filter { it.status == "approved" }
    val activeReadingApplications = approvedApplications.filter {
        it.readingStatus != "reviewed"
    }
    val pendingApplications = myApplications.filter { it.status == "pending" }

    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    val trendingBooks by bookViewModel.trendingBooks.collectAsState()

    LaunchedEffect(Unit) {
        bookViewModel.getRecommendedBooks()
        bookViewModel.getNewReleases()
        bookViewModel.getTrendingBooks()
        friendViewModel.loadFriendsActivity()
        authorFollowViewModel.loadBooksFromFollowedAuthors()
        applicationViewModel.loadMyApplications()
        notificationViewModel.loadUnreadCount()
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            kotlinx.coroutines.delay(500)
            bookViewModel.searchForHomeScreen(query = searchQuery, take = 20)
        } else {
            bookViewModel.clearHomeSearchResults()
        }
    }

    Scaffold(
        containerColor = BackgroundWhite
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-175).dp, y = (-175).dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(SkyBluePeriwinkle.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-135).dp, y = (-135).dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(SkyBluePeriwinkle)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 175.dp, y = 175.dp)
                    .size(350.dp)
                    .clip(CircleShape)
                    .background(SkyBluePeriwinkle.copy(alpha = 0.3f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 135.dp, y = 135.dp)
                    .size(270.dp)
                    .clip(CircleShape)
                    .background(SkyBluePeriwinkle)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 64.dp, bottom = 64.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search books, authors, series...",
                                color = Color(0xFF757575)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = DarkNavyBlue
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 3.dp,
                                shape = RoundedCornerShape(28.dp)
                            ),
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color(0xFFE8DFE4),
                            unfocusedContainerColor = Color(0xFFE8DFE4),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                }

                if (searchQuery.isNotBlank()) {
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Search Results",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkNavyBlue
                                )
                                TextButton(
                                    onClick = {
                                        navController.navigate(
                                            BottomBarScreen.Browse.withQuery(
                                                searchQuery
                                            )
                                        )
                                    }
                                ) {
                                    Text("View All", color = DarkNavyBlue)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (isSearching && searchResults.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = DarkNavyBlue)
                                }
                            } else if (searchResults.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(searchResults.take(10)) { book ->
                                        BookItem(book = book, navController = navController)
                                    }
                                }
                                if (searchResults.size > 10) {
                                    TextButton(
                                        onClick = {
                                            navController.navigate(BottomBarScreen.Browse.route)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    ) {
                                        Text(
                                            "See all ${searchResults.size} results",
                                            color = DarkNavyBlue
                                        )
                                    }
                                }
                            } else if (searchQuery.isNotBlank() && !isSearching) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(2.dp, RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(
                                            0xFFE8DFE4
                                        )
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Text(
                                        text = "No books found for \"$searchQuery\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF757575),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column {
                        val user = currentUser
                        val greeting = when {
                            user?.firstName != null -> "Welcome back, ${user.firstName}!"
                            user?.username != null -> "Welcome back, ${user.username}!"
                            else -> "Welcome back!"
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavyBlue
                        )
                        Text(
                            text = "What would you like to read today?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF757575),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (activeReadingApplications.isNotEmpty()) {
                            QuickActionCard(
                                modifier = Modifier.weight(1f),
                                title = "Reading",
                                subtitle = "${activeReadingApplications.size} book(s)",
                                icon = Icons.Filled.Book,
                                onClick = {
                                    navController.navigate(BottomBarScreen.MyApplications.route)
                                }
                            )
                        }

                        if (pendingApplications.isNotEmpty()) {
                            QuickActionCard(
                                modifier = Modifier.weight(1f),
                                title = "Pending",
                                subtitle = "${pendingApplications.size} waiting",
                                icon = Icons.Filled.Book,
                                onClick = {
                                    navController.navigate(BottomBarScreen.MyApplications.route)
                                }
                            )
                        }

                        if (unreadCount > 0) {
                            QuickActionCard(
                                modifier = Modifier.weight(1f),
                                title = "Alerts",
                                subtitle = "$unreadCount new",
                                icon = Icons.Filled.Notifications,
                                onClick = {
                                    navController.navigate(Screen.Notifications.route)
                                }
                            )
                        }
                    }
                }

                item {
                    BookSection(
                        title = "Recommended for You",
                        books = recommendedBooks,
                        isLoading = isLoading && recommendedBooks.isEmpty(),
                        navController = navController,
                        onViewAllClick = {
                            navController.navigate("books/recommended")
                        }
                    )
                }

                item {
                    BookSection(
                        title = "New Releases",
                        books = newReleases,
                        isLoading = isLoading && newReleases.isEmpty(),
                        navController = navController,
                        onViewAllClick = {
                            navController.navigate("books/new_releases")
                        }
                    )
                }

                item {
                    BookSection(
                        title = "From Authors You Follow",
                        books = booksFromFollowedAuthors,
                        isLoading = followedAuthorsLoading && booksFromFollowedAuthors.isEmpty(),
                        navController = navController,
                        emptyMessage = "Follow some authors to see their latest books here!",
                        onViewAllClick = {
                            navController.navigate("books/followed_authors")
                        },
                        useSimpleItem = true
                    )
                }

                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6B35),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Trending This Week",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkNavyBlue
                                )
                            }
                            if (trendingBooks.isNotEmpty()) {
                                TextButton(onClick = { navController.navigate("books/trending") }) {
                                    Text("View All", color = DarkNavyBlue)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isLoading && trendingBooks.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = DarkNavyBlue)
                            }
                        } else if (trendingBooks.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(trendingBooks) { trending ->
                                    SimpleBookItem(
                                        book = trending.book,
                                        navController = navController
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "No trending books this week",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }

                item {
                    Column {
                        Text(
                            text = "Friends Activity",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavyBlue
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (friendsActivityLoading && friendsActivity.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = DarkNavyBlue)
                            }
                        } else if (friendsActivity.isNotEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                friendsActivity.take(5).forEach { activity ->
                                    ActivityItem(activity = activity, navController = navController)
                                }
                            }
                        } else {
                            Text(
                                text = "Add some friends to see their activity here!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8DFE4)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = DarkNavyBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = DarkNavyBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF757575),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BookSection(
    title: String,
    books: List<com.example.booknest.domain.model.response.RecommendedBookResponse>,
    isLoading: Boolean,
    navController: NavController,
    emptyMessage: String? = null,
    onViewAllClick: () -> Unit,
    useSimpleItem: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkNavyBlue
            )
            if (books.isNotEmpty()) {
                TextButton(onClick = onViewAllClick) {
                    Text("View All", color = DarkNavyBlue)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DarkNavyBlue)
            }
        } else if (books.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(books) { book ->
                    if (useSimpleItem) {
                        SimpleBookItem(book = book, navController = navController)
                    } else {
                        BookItem(book = book, navController = navController)
                    }
                }
            }
        } else if (emptyMessage != null) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575)
            )
        }
    }
}
