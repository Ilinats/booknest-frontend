package com.example.booknest.ui.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.presentation.navigation.Screen
import com.example.booknest.ui.components.AppScaffoldContentInsets
import com.example.booknest.ui.components.AppTopBar
import com.example.booknest.ui.components.BackButton
import com.example.booknest.ui.components.BackgroundDecoration
import com.example.booknest.ui.components.appListContentPadding
import com.example.booknest.ui.components.paddingTopFromScaffold
import com.example.booknest.ui.friends.components.list.friendsTabItems
import com.example.booknest.ui.friends.components.list.receivedRequestsTabItems
import com.example.booknest.ui.friends.components.list.sentRequestsTabItems
import com.example.booknest.ui.friends.components.search.FriendsSearchBar
import com.example.booknest.ui.friends.components.search.searchResultsTabItems
import com.example.booknest.ui.friends.utils.getSortedFriends
import com.example.booknest.viewmodel.friends.FriendViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject

enum class FriendsSortOption {
    Alphabetical, RecentlyAdded, MostActive
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    friendViewModel: FriendViewModel = getViewModel(),
) {
    val friends by friendViewModel.friends.collectAsState()
    val sentRequests by friendViewModel.sentRequests.collectAsState()
    val receivedRequests by friendViewModel.receivedRequests.collectAsState()
    val searchResults by friendViewModel.searchResults.collectAsState()
    val isLoading by friendViewModel.isLoading.collectAsState()
    val error by friendViewModel.error.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(FriendsSortOption.Alphabetical) }
    var suggestedFriends by remember {
        mutableStateOf<List<com.example.booknest.domain.model.response.UserResponse>>(emptyList())
    }

    LaunchedEffect(Unit) {
        friendViewModel.loadAll()
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            delay(500)
            friendViewModel.searchUsers(searchQuery)
            showSearchResults = true
        } else {
            showSearchResults = false
        }
    }

    val readerSearchResults = searchResults.filter { it.userType?.lowercase() == "reader" }
    val readerSuggestions = suggestedFriends.filter { it.userType?.lowercase() == "reader" }
    val sortedFriends = getSortedFriends(friends, sortOption)
    val isSearchActive = showSearchResults && searchQuery.isNotBlank()

    Scaffold(
        contentWindowInsets = AppScaffoldContentInsets,
        topBar = {
            AppTopBar(
                title = "Friends",
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            BackgroundDecoration(modifier = Modifier.fillMaxSize())

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .paddingTopFromScaffold(paddingValues),
                contentPadding = appListContentPadding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "search") {
                    FriendsSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onClear = {
                            searchQuery = ""
                            showSearchResults = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (isSearchActive) {
                    searchResultsTabItems(
                        results = readerSearchResults,
                        isLoading = isLoading,
                        onUserClick = { username ->
                            navController.navigate(Screen.UserProfile.createRoute(username))
                        },
                        onAddFriendClick = { username ->
                            friendViewModel.sendFriendRequest(username)
                            searchQuery = ""
                            showSearchResults = false
                        },
                    )
                } else {
                    item(key = "tabs") {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            SegmentedButton(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                modifier = Modifier.weight(1f),
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                            ) {
                                Text("Friends (${friends.size})")
                            }
                            SegmentedButton(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                modifier = Modifier.weight(1f),
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                            ) {
                                Text("Sent (${sentRequests.size})")
                            }
                            SegmentedButton(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                modifier = Modifier.weight(1f),
                                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text("Received")
                                    if (receivedRequests.isNotEmpty()) {
                                        Badge {
                                            Text(receivedRequests.size.toString())
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isLoading && selectedTab == 0 && friends.isEmpty()) {
                        item(key = "loading") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else {
                        when (selectedTab) {
                            0 -> friendsTabItems(
                                friends = sortedFriends,
                                suggestedFriends = readerSuggestions,
                                sortOption = sortOption,
                                onSortOptionSelected = { sortOption = it },
                                onFriendClick = { username ->
                                    navController.navigate(Screen.UserProfile.createRoute(username))
                                },
                                onUnfriendClick = { friendId ->
                                    friendViewModel.unfriendUser(friendId)
                                },
                                onViewProfileClick = { username ->
                                    navController.navigate(Screen.UserProfile.createRoute(username))
                                },
                                onSuggestedUserClick = { username ->
                                    navController.navigate(Screen.UserProfile.createRoute(username))
                                },
                                onSuggestedAddFriendClick = { username ->
                                    friendViewModel.sendFriendRequest(username)
                                },
                            )

                            1 -> sentRequestsTabItems(
                                requests = sentRequests,
                                onUserClick = { username ->
                                    navController.navigate(Screen.UserProfile.createRoute(username))
                                },
                                onCancelRequest = { userId ->
                                    friendViewModel.cancelFriendRequest(userId)
                                },
                            )

                            2 -> receivedRequestsTabItems(
                                requests = receivedRequests,
                                onUserClick = { username ->
                                    navController.navigate(Screen.UserProfile.createRoute(username))
                                },
                                onAcceptClick = { requesterId ->
                                    friendViewModel.acceptFriendRequest(requesterId)
                                },
                                onDeclineClick = { requesterId ->
                                    friendViewModel.declineFriendRequest(requesterId)
                                },
                            )
                        }
                    }
                }

                if (!error.isNullOrEmpty()) {
                    item(key = "error") {
                        Text(
                            text = error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
