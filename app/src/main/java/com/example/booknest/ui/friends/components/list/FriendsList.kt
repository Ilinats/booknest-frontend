package com.example.booknest.ui.friends.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.ui.friends.components.empty.EmptyFriendsState
import com.example.booknest.ui.friends.components.item.UserListItem

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
                                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error
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

