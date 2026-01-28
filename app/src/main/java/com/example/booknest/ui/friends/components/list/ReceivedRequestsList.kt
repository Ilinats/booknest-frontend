package com.example.booknest.ui.friends.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.ui.friends.components.empty.EmptyReceivedRequestsState
import com.example.booknest.ui.friends.components.item.ReceivedRequestItem

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

