package com.example.booknest.ui.friends.components.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.ui.friends.components.empty.EmptyReceivedRequestsState
import com.example.booknest.ui.friends.components.item.ReceivedRequestItem

fun LazyListScope.receivedRequestsTabItems(
    requests: List<UserResponse>,
    onUserClick: (String) -> Unit,
    onAcceptClick: (String) -> Unit,
    onDeclineClick: (String) -> Unit,
) {
    if (requests.isEmpty()) {
        item(key = "empty_received") {
            EmptyReceivedRequestsState(modifier = Modifier.fillMaxWidth())
        }
    } else {
        items(
            items = requests,
            key = { it.id },
        ) { user ->
            ReceivedRequestItem(
                user = user,
                onUserClick = { onUserClick(user.username) },
                onAcceptClick = { onAcceptClick(user.id) },
                onDeclineClick = { onDeclineClick(user.id) },
            )
        }
    }
}
