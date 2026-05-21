package com.example.booknest.ui.friends.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.ui.friends.components.empty.EmptySentRequestsState
import com.example.booknest.ui.friends.components.item.UserListItem

fun LazyListScope.sentRequestsTabItems(
    requests: List<UserResponse>,
    onUserClick: (String) -> Unit,
    onCancelRequest: (String) -> Unit,
) {
    if (requests.isEmpty()) {
        item(key = "empty_sent") {
            EmptySentRequestsState(modifier = Modifier.fillMaxWidth())
        }
    } else {
        items(
            items = requests,
            key = { it.id },
        ) { user ->
            UserListItem(
                user = user,
                onUserClick = { onUserClick(user.username) },
                trailingContent = {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Sent",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedButton(
                            onClick = { onCancelRequest(user.id) },
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel")
                        }
                    }
                },
            )
        }
    }
}
