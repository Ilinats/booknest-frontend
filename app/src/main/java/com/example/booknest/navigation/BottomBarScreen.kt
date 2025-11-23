package com.example.booknest.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreen(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomBarScreen("home", "Home", Icons.Default.Home)
    object MyApplications : BottomBarScreen("my_applications", "My Applications", Icons.Default.List)
    object Browse : BottomBarScreen("browse?searchQuery={searchQuery}", "Browse", Icons.Default.Search) {
        fun withQuery(query: String) = "browse?searchQuery=$query"
    }
    object Friends : BottomBarScreen("friends", "Friends", Icons.Default.Favorite)
}