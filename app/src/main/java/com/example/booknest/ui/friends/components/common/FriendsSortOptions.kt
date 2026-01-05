package com.example.booknest.ui.friends.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.booknest.ui.friends.FriendsSortOption

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

