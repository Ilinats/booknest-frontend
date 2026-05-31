package com.example.booknest.ui.friends.utils

import com.example.booknest.testutil.TestFixtures
import com.example.booknest.ui.friends.FriendsSortOption
import org.junit.Assert.assertEquals
import org.junit.Test

class FriendsUtilsTest {

    @Test
    fun getSortedFriends_alphabetical_sortsByDisplayName() {
        val friends = listOf(
            TestFixtures.user(id = "1", username = "zoe", firstName = "Zoe", lastName = "Adams"),
            TestFixtures.user(id = "2", username = "amy", firstName = "Amy", lastName = "Brown"),
        )

        val sorted = getSortedFriends(friends, FriendsSortOption.Alphabetical)

        assertEquals("2", sorted.first().id)
        assertEquals("1", sorted.last().id)
    }

    @Test
    fun getSortedFriends_recentlyAdded_sortsNewestFirst() {
        val friends = listOf(
            TestFixtures.user(id = "1", createdAt = "2024-01-01T00:00:00.000Z"),
            TestFixtures.user(id = "2", createdAt = "2024-06-01T00:00:00.000Z"),
        )

        val sorted = getSortedFriends(friends, FriendsSortOption.RecentlyAdded)

        assertEquals("2", sorted.first().id)
    }

    @Test
    fun getSortedFriends_mostActive_returnsOriginalOrder() {
        val friends = listOf(
            TestFixtures.user(id = "1"),
            TestFixtures.user(id = "2"),
        )

        val sorted = getSortedFriends(friends, FriendsSortOption.MostActive)

        assertEquals(friends, sorted)
    }
}
