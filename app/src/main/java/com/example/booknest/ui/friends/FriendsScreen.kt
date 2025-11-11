package com.example.booknest.ui.friends

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.booknest.data.AuthManager
import com.example.booknest.network.FriendRequest
import com.example.booknest.network.UserData
import com.example.booknest.navigation.Screen
import com.example.booknest.viewmodel.FriendViewModel
import com.example.booknest.viewmodel.FriendViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    navController: NavController,
    authManager: AuthManager,
    friendViewModel: FriendViewModel = viewModel(
        factory = FriendViewModelFactory(authManager)
    )
) {
    val friends by friendViewModel.friends.collectAsState()
    val friendRequests by friendViewModel.friendRequests.collectAsState()
    val searchResults by friendViewModel.searchResults.collectAsState()
    val isLoading by friendViewModel.isLoading.collectAsState()
    val error by friendViewModel.error.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        friendViewModel.loadFriends()
        friendViewModel.loadFriendRequests("received")
    }
    
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            friendViewModel.searchUsers(searchQuery)
            showSearchResults = true
        } else {
            showSearchResults = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends") },
                actions = {
                    IconButton(onClick = { 
                        // Navigate to profile or settings
                        authManager.getCurrentUser()?.id?.let { userId ->
                            navController.navigate(Screen.Profile.createRoute(userId))
                        }
                    }) {
                        Icon(Icons.Filled.Person, contentDescription = "Profile")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search users...") },
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
            
            // Search Results
            if (showSearchResults && searchResults.isNotEmpty()) {
                item {
                    Text(
                        text = "Search Results",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(searchResults) { user ->
                    UserSearchItem(
                        user = user,
                        onAddFriend = { username ->
                            friendViewModel.sendFriendRequest(username)
                        },
                        onViewProfile = { userId ->
                            navController.navigate(Screen.Profile.createRoute(userId))
                        }
                    )
                }
            }
            
            // Friend Requests
            if (friendRequests.isNotEmpty()) {
                item {
                    Text(
                        text = "Friend Requests",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(friendRequests) { request ->
                    FriendRequestItem(
                        request = request,
                        onAccept = { requesterId ->
                            friendViewModel.acceptFriendRequest(requesterId)
                        },
                        onDecline = { requesterId ->
                            friendViewModel.declineFriendRequest(requesterId)
                        }
                    )
                }
            }
            
            // Friends List
            if (friends.isNotEmpty()) {
                item {
                    Text(
                        text = "Your Friends",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(friends) { friend ->
                    FriendItem(
                        friend = friend,
                        onViewProfile = { userId ->
                            navController.navigate(Screen.Profile.createRoute(userId))
                        },
                        onUnfriend = { friendId ->
                            friendViewModel.unfriendUser(friendId)
                        }
                    )
                }
            }
            
            // Loading State
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Error State
            if (error != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserSearchItem(
    user: UserData,
    onAddFriend: (String) -> Unit,
    onViewProfile: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewProfile(user.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Avatar",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${user.firstName ?: ""} ${user.lastName ?: ""}".trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Add friend button
            Button(
                onClick = { onAddFriend(user.username) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Friend")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }
    }
}

@Composable
fun FriendRequestItem(
    request: FriendRequest,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Avatar",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = request.requester?.username ?: "Unknown User",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "wants to be your friend",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Action buttons
            Row {
                Button(
                    onClick = { onAccept(request.requesterId) }
                ) {
                    Text("Accept")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onDecline(request.requesterId) }
                ) {
                    Text("Decline")
                }
            }
        }
    }
}

@Composable
fun FriendItem(
    friend: FriendRequest,
    onViewProfile: (String) -> Unit,
    onUnfriend: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                friend.requester?.id?.let { onViewProfile(it) }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Avatar",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = friend.requester?.username ?: "Unknown User",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Friends since ${formatDate(friend.createdAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Unfriend button
            Button(
                onClick = { 
                    friend.requester?.id?.let { onUnfriend(it) }
                }
            ) {
                Text("Unfriend")
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}
