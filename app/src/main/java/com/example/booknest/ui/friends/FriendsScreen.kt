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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import com.example.booknest.ui.components.BackButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.booknest.data.AuthManager
import com.example.booknest.navigation.Screen
import com.example.booknest.network.FriendRequest
import com.example.booknest.network.UserData
import com.example.booknest.viewmodel.FriendViewModel
import com.example.booknest.viewmodel.FriendViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    navController: NavController,
    authManager: AuthManager,
    friendViewModel: FriendViewModel = viewModel(factory = FriendViewModelFactory(authManager))
) {
    val friends by friendViewModel.friends.collectAsState()
    val isLoading by friendViewModel.isLoading.collectAsState()
    val error by friendViewModel.error.collectAsState()
    val currentUser by authManager.currentUser.collectAsState(initial = null)

    LaunchedEffect(Unit) {
        friendViewModel.loadFriends()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends") },
                navigationIcon = {
                    BackButton(onClick = { navController.popBackStack() })
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading && friends.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            friends.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You haven't added any friends yet.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(friends) { friend ->
                        FriendListItem(
                            friendRequest = friend,
                            currentUserId = currentUser?.id,
                            onFriendClick = { userId ->
                                navController.navigate(Screen.Profile.createRoute(userId))
                            }
                        )
                    }
                }
            }
        }

        if (!error.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
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

@Composable
private fun FriendListItem(
    friendRequest: FriendRequest,
    currentUserId: String?,
    onFriendClick: (String) -> Unit
) {
    val friendUser = determineFriendUser(friendRequest, currentUserId) ?: return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onFriendClick(friendUser.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FriendAvatar(user = friendUser)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                val fullName = listOfNotNull(friendUser.firstName, friendUser.lastName)
                    .joinToString(" ")
                    .ifBlank { friendUser.username }

                Text(
                    text = fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@${friendUser.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                friendUser.bio?.takeIf { it.isNotBlank() }?.let { bio ->
                    Text(
                        text = bio.take(80) + if (bio.length > 80) "…" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendAvatar(user: UserData) {
    val avatarUrl = user.profilePictureUrl ?: user.avatarUrl

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrEmpty()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "${user.username} avatar",
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
            )
        } else {
            val initial = (user.firstName ?: user.username).firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Text(
                text = initial,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun determineFriendUser(request: FriendRequest, currentUserId: String?): UserData? {
    return when (currentUserId) {
        request.requesterId -> request.addressee ?: request.requester
        request.addresseeId -> request.requester ?: request.addressee
        else -> request.requester ?: request.addressee
    }
}
