package com.example.booknest.ui.friends.utils

import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.ui.friends.FriendsSortOption

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

