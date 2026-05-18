package com.example.booknest.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.ui.components.BackgroundDecoration
import com.example.booknest.ui.home.components.sections.BookSection
import com.example.booknest.ui.home.components.sections.FriendsActivitySection
import com.example.booknest.ui.home.components.sections.GreetingSection
import com.example.booknest.ui.home.components.sections.QuickActionsSection
import com.example.booknest.ui.home.components.sections.SearchSection
import com.example.booknest.ui.home.components.sections.TrendingSection
import com.example.booknest.viewmodel.applications.ApplicationViewModel
import com.example.booknest.viewmodel.author.AuthorFollowViewModel
import com.example.booknest.viewmodel.books.BookViewModel
import com.example.booknest.viewmodel.friends.FriendViewModel
import com.example.booknest.viewmodel.notifications.NotificationViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

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


    val booksFromFollowedAuthors by authorFollowViewModel.booksFromFollowedAuthors.collectAsState()
    val followedAuthorsLoading by authorFollowViewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val searchResults by bookViewModel.homeSearchResults.collectAsState()
    val isSearching by bookViewModel.isLoading.collectAsState()

    val activeReadingApplications by applicationViewModel.activeReadingApplications.collectAsState()
    val pendingApplications by applicationViewModel.pendingApplications.collectAsState()

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
        bookViewModel.updateSearchQuery(searchQuery)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BackgroundDecoration(modifier = Modifier.fillMaxSize())

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 15.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
                item {
                    SearchSection(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        searchResults = searchResults,
                        isSearching = isSearching,
                        navController = navController
                    )
                }

                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        GreetingSection(currentUser = currentUser)
                    }
                }

                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        QuickActionsSection(
                            activeReadingApplications = activeReadingApplications,
                            pendingApplications = pendingApplications,
                            unreadCount = unreadCount,
                            navController = navController
                        )
                    }
                }

                item {
                    BookSection(
                        title = "Recommended for You",
                        books = recommendedBooks,
                        isLoading = isLoading && recommendedBooks.isEmpty(),
                        navController = navController,
                        onViewAllClick = {
                            navController.navigate(Screen.Books.createRoute("recommended"))
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
                            navController.navigate(Screen.Books.createRoute("new_releases"))
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
                            navController.navigate(Screen.Books.createRoute("followed_authors"))
                        },
                        useSimpleItem = true
                    )
                }

                item {
                    TrendingSection(
                        trendingBooks = trendingBooks,
                        isLoading = isLoading && trendingBooks.isEmpty(),
                        navController = navController
                    )
                }

                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        FriendsActivitySection(
                            friendsActivity = friendsActivity,
                            isLoading = friendsActivityLoading,
                            navController = navController
                        )
                    }
                }
        }
    }
}
