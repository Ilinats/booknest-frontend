package com.example.booknest.ui.friends.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.ui.friends.FriendsSortOption
import com.example.booknest.ui.friends.components.common.FriendsSortOptions
import com.example.booknest.ui.friends.components.empty.EmptyFriendsState
import com.example.booknest.ui.friends.components.item.UserListItem
import com.example.booknest.ui.friends.components.suggested.SuggestedFriendsSection

fun LazyListScope.friendsTabItems(
    friends: List<UserResponse>,
    suggestedFriends: List<UserResponse>,
    sortOption: FriendsSortOption,
    onSortOptionSelected: (FriendsSortOption) -> Unit,
    onFriendClick: (String) -> Unit,
    onUnfriendClick: (String) -> Unit,
    onViewProfileClick: (String) -> Unit,
    onSuggestedUserClick: (String) -> Unit,
    onSuggestedAddFriendClick: (String) -> Unit,
) {
    if (friends.isEmpty() && suggestedFriends.isNotEmpty()) {
        item(key = "suggested_friends") {
            SuggestedFriendsSection(
                suggestions = suggestedFriends,
                onUserClick = onSuggestedUserClick,
                onAddFriendClick = onSuggestedAddFriendClick,
            )
        }
    }

    if (friends.isNotEmpty()) {
        item(key = "friends_sort") {
            FriendsSortOptions(
                selectedOption = sortOption,
                onOptionSelected = onSortOptionSelected,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (friends.isEmpty()) {
        item(key = "empty_friends") {
            EmptyFriendsState(modifier = Modifier.fillMaxWidth())
        }
    } else {
        items(
            items = friends,
            key = { it.id },
        ) { friend ->
            UserListItem(
                user = friend,
                onUserClick = { onFriendClick(friend.username) },
                showLastActive = true,
                trailingContent = {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = { onViewProfileClick(friend.username) },
                            modifier = Modifier.width(120.dp),
                        ) {
                            Text("View Profile", fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = { onUnfriendClick(friend.id) },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Text("Unfriend", fontSize = 12.sp)
                        }
                    }
                },
            )
        }
    }
}
