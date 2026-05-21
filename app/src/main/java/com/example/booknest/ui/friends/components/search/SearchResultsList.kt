package com.example.booknest.ui.friends.components.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.ui.friends.components.item.SearchResultItem

fun LazyListScope.searchResultsTabItems(
    results: List<UserResponse>,
    isLoading: Boolean,
    onUserClick: (String) -> Unit,
    onAddFriendClick: (String) -> Unit,
) {
    if (isLoading) {
        item(key = "search_loading") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    } else if (results.isEmpty()) {
        item(key = "search_empty") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "No users found",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    } else {
        items(
            items = results,
            key = { it.id },
        ) { user ->
            SearchResultItem(
                user = user,
                onUserClick = { onUserClick(user.username) },
                onAddFriendClick = { onAddFriendClick(user.username) },
            )
        }
    }
}
