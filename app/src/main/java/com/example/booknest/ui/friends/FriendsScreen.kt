package com.example.booknest.ui.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.booknest.ui.theme.SkyBluePeriwinkle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.booknest.ui.components.BackButton
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.booknest.data.session.SessionManager
import com.example.booknest.navigation.Screen
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.viewmodel.FriendViewModel
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
    var suggestedFriends by remember { mutableStateOf<List<UserResponse>>(emptyList()) }

    LaunchedEffect(Unit) {
        friendViewModel.loadAll()
        friendViewModel.loadFriendSuggestions { suggestions ->
            suggestedFriends = suggestions
        }
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
                .background(Color(0xFFF1E9EE))
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
                        results = searchResults,
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
                                        suggestions = suggestedFriends,
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

@Composable
fun FriendsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search by username or name...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true
    )
}

@Composable
fun SearchResultsList(
    results: List<UserResponse>,
    onUserClick: (String) -> Unit,
    onAddFriendClick: (String) -> Unit,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (results.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "No users found",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(results) { user ->
                SearchResultItem(
                    user = user,
                    onUserClick = { onUserClick(user.username) },
                    onAddFriendClick = { onAddFriendClick(user.username) }
                )
            }
        }
    }
}

@Composable
fun SearchResultItem(
    user: UserResponse,
    onUserClick: () -> Unit,
    onAddFriendClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUserClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(user = user, size = 56.dp)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val fullName = listOfNotNull(user.firstName, user.lastName)
                    .joinToString(" ")
                    .ifBlank { user.username }

                Text(
                    text = fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                user.bio?.takeIf { it.isNotBlank() }?.let { bio ->
                    Text(
                        text = bio.take(50) + if (bio.length > 50) "…" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Button(
                onClick = onAddFriendClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }
    }
}

@Composable
fun FriendsSortOptions(
    selectedOption: FriendsSortOption,
    onOptionSelected: (FriendsSortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedOption == FriendsSortOption.Alphabetical,
            onClick = { onOptionSelected(FriendsSortOption.Alphabetical) },
            label = { Text("A-Z") }
        )
        FilterChip(
            selected = selectedOption == FriendsSortOption.RecentlyAdded,
            onClick = { onOptionSelected(FriendsSortOption.RecentlyAdded) },
            label = { Text("Recent") }
        )
        FilterChip(
            selected = selectedOption == FriendsSortOption.MostActive,
            onClick = { onOptionSelected(FriendsSortOption.MostActive) },
            label = { Text("Active") }
        )
    }
}

@Composable
fun SuggestedFriendsSection(
    suggestions: List<UserResponse>,
    onUserClick: (String) -> Unit,
    onAddFriendClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Suggested Friends",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Based on similar reading preferences",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            suggestions.take(5).forEach { user ->
                SuggestedFriendItem(
                    user = user,
                    onUserClick = { onUserClick(user.username) },
                    onAddFriendClick = { onAddFriendClick(user.username) }
                )
            }
        }
    }
}

@Composable
fun SuggestedFriendItem(
    user: UserResponse,
    onUserClick: () -> Unit,
    onAddFriendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUserClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            UserAvatar(user = user, size = 48.dp)
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val fullName = listOfNotNull(user.firstName, user.lastName)
                    .joinToString(" ")
                    .ifBlank { user.username }
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        OutlinedButton(
            onClick = onAddFriendClick,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add")
        }
    }
}

fun getSortedFriends(
    friends: List<UserResponse>,
    sortOption: FriendsSortOption
): List<UserResponse> {
    return when (sortOption) {
        FriendsSortOption.Alphabetical -> friends.sortedBy {
            listOfNotNull(it.firstName, it.lastName).joinToString(" ").ifBlank { it.username }
        }

        FriendsSortOption.RecentlyAdded -> friends.sortedByDescending { it.createdAt ?: "" }
        FriendsSortOption.MostActive -> friends
    }
}

@Composable
fun FriendsList(
    friends: List<UserResponse>,
    onFriendClick: (String) -> Unit,
    onUnfriendClick: (String) -> Unit,
    onViewProfileClick: (String) -> Unit
) {
    if (friends.isEmpty()) {
        EmptyFriendsState()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(friends) { friend ->
                UserListItem(
                    user = friend,
                    onUserClick = { onFriendClick(friend.username) },
                    showLastActive = true,
                    trailingContent = {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onViewProfileClick(friend.username) },
                                modifier = Modifier.width(120.dp)
                            ) {
                                Text("View Profile", fontSize = 12.sp)
                            }
                            TextButton(
                                onClick = { onUnfriendClick(friend.id) },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Unfriend", fontSize = 12.sp)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SentRequestsList(
    requests: List<UserResponse>,
    onUserClick: (String) -> Unit,
    onCancelRequest: (String) -> Unit
) {
    if (requests.isEmpty()) {
        EmptySentRequestsState()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(requests) { user ->
                UserListItem(
                    user = user,
                    onUserClick = { onUserClick(user.username) },
                    trailingContent = {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Sent",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { onCancelRequest(user.id) }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancel")
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ReceivedRequestsList(
    requests: List<UserResponse>,
    onUserClick: (String) -> Unit,
    onAcceptClick: (String) -> Unit,
    onDeclineClick: (String) -> Unit
) {
    if (requests.isEmpty()) {
        EmptyReceivedRequestsState()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(requests) { user ->
                ReceivedRequestItem(
                    user = user,
                    onUserClick = { onUserClick(user.username) },
                    onAcceptClick = { onAcceptClick(user.id) },
                    onDeclineClick = { onDeclineClick(user.id) }
                )
            }
        }
    }
}

@Composable
fun UserListItem(
    user: UserResponse,
    onUserClick: () -> Unit,
    trailingContent: @Composable () -> Unit = {},
    showLastActive: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUserClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(user = user, size = 56.dp)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val fullName = listOfNotNull(user.firstName, user.lastName)
                    .joinToString(" ")
                    .ifBlank { user.username }

                Text(
                    text = fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (showLastActive) {
                    Text(
                        text = "Last active: Recently",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            trailingContent()
        }
    }
}

@Composable
fun ReceivedRequestItem(
    user: UserResponse,
    onUserClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUserClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(user = user, size = 56.dp)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val fullName = listOfNotNull(user.firstName, user.lastName)
                        .joinToString(" ")
                        .ifBlank { user.username }

                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Requested to be friends",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAcceptClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept")
                }
                OutlinedButton(
                    onClick = onDeclineClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Decline")
                }
            }

            TextButton(
                onClick = onUserClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Profile")
            }
        }
    }
}

@Composable
fun EmptyFriendsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.People,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No friends yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Search for users to connect!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptySentRequestsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No sent requests",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "You haven't sent any friend requests yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyReceivedRequestsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Mail,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No pending friend requests",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "When someone sends you a friend request, it will appear here",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun UserAvatar(user: UserResponse, size: androidx.compose.ui.unit.Dp = 48.dp) {
    val avatarUrl = user.profilePictureUrl ?: user.avatarUrl

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrEmpty()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "${user.username} avatar",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            val initial =
                (user.firstName ?: user.username).firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Text(
                text = initial,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
