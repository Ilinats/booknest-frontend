package com.example.booknest.ui.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.booknest.data.session.SessionManager
import com.example.booknest.navigation.Screen
import com.example.booknest.viewmodel.friends.FriendViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.compose.koinInject
import com.example.booknest.ui.components.BackButton
import com.example.booknest.ui.friends.components.search.FriendsSearchBar
import com.example.booknest.ui.friends.components.search.SearchResultsList
import com.example.booknest.ui.friends.components.common.FriendsSortOptions
import com.example.booknest.ui.friends.components.suggested.SuggestedFriendsSection
import com.example.booknest.ui.friends.components.list.FriendsList
import com.example.booknest.ui.friends.components.list.SentRequestsList
import com.example.booknest.ui.friends.components.list.ReceivedRequestsList
import com.example.booknest.ui.friends.utils.getSortedFriends
import com.example.booknest.ui.components.BackgroundDecoration

enum class FriendsSortOption {
    Alphabetical, RecentlyAdded, MostActive
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject(),
    friendViewModel: FriendViewModel = getViewModel()
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
    var suggestedFriends by remember { mutableStateOf<List<com.example.booknest.domain.model.response.UserResponse>>(emptyList()) }

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

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.shadow(elevation = 4.dp)
            ) {
                TopAppBar(
                    title = { Text("Friends") },
                    navigationIcon = {
                        BackButton(onClick = { navController.popBackStack() })
                    }
                )
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
                Spacer(modifier = Modifier.size(8.dp))
                FriendsSearchBar(
                    query = searchQuery,
                    onQueryChange = { query: String -> searchQuery = query },
                    onClear = {
                        searchQuery = ""
                        showSearchResults = false
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (showSearchResults && searchQuery.isNotBlank()) {
                    SearchResultsList(
                        results = searchResults.filter { it.userType?.lowercase() == "reader" },
                        onUserClick = { username: String ->
                            navController.navigate(Screen.UserProfile.createRoute(username))
                        },
                        onAddFriendClick = { username: String ->
                            friendViewModel.sendFriendRequest(username)
                            searchQuery = ""
                            showSearchResults = false
                        },
                        isLoading = isLoading
                    )
                } else {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        SegmentedButton(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.weight(1f),
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                        ) {
                            Text("Friends (${friends.size})")
                        }
                        SegmentedButton(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f),
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                        ) {
                            Text("Sent (${sentRequests.size})")
                        }
                        SegmentedButton(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            modifier = Modifier.weight(1f),
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
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

                    when {
                        isLoading && selectedTab == 0 && friends.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        selectedTab == 0 -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                if (friends.isEmpty() && suggestedFriends.isNotEmpty()) {
                                    SuggestedFriendsSection(
                                        suggestions = suggestedFriends.filter { it.userType?.lowercase() == "reader" },
                                        onUserClick = { username: String ->
                                            navController.navigate(
                                                Screen.UserProfile.createRoute(
                                                    username
                                                )
                                            )
                                        },
                                        onAddFriendClick = { username: String ->
                                            friendViewModel.sendFriendRequest(username)
                                        }
                                    )
                                }

                                if (friends.isNotEmpty()) {
                                    FriendsSortOptions(
                                        selectedOption = sortOption,
                                        onOptionSelected = { option: FriendsSortOption ->
                                            sortOption = option
                                        },
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 8.dp
                                        )
                                    )
                                }

                                FriendsList(
                                    friends = getSortedFriends(friends, sortOption),
                                    onFriendClick = { username: String ->
                                        navController.navigate(
                                            Screen.UserProfile.createRoute(
                                                username
                                            )
                                        )
                                    },
                                    onUnfriendClick = { friendId: String ->
                                        friendViewModel.unfriendUser(friendId)
                                    },
                                    onViewProfileClick = { username: String ->
                                        navController.navigate(
                                            Screen.UserProfile.createRoute(
                                                username
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        selectedTab == 1 -> {
                            SentRequestsList(
                                requests = sentRequests,
                                onUserClick = { username: String ->
                                    navController.navigate(Screen.UserProfile.createRoute(username))
                                },
                                onCancelRequest = { userId: String ->
                                    friendViewModel.cancelFriendRequest(userId)
                                }
                            )
                        }

                        selectedTab == 2 -> {
                            ReceivedRequestsList(
                                requests = receivedRequests,
                                onUserClick = { username: String ->
                                    navController.navigate(Screen.UserProfile.createRoute(username))
                                },
                                onAcceptClick = { requesterId: String ->
                                    friendViewModel.acceptFriendRequest(requesterId)
                                },
                                onDeclineClick = { requesterId: String ->
                                    friendViewModel.declineFriendRequest(requesterId)
                                }
                            )
                        }
                    }
                }

                if (!error.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = error ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
