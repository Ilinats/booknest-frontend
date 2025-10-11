package com.example.booknest.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AuthorBottomBarScreen(val route: String, val title: String, val icon: ImageVector) {
    object Home : AuthorBottomBarScreen("author_home", "Home", Icons.Default.Home)
    object MyBooks : AuthorBottomBarScreen("author_my_books", "My Books", Icons.Default.List)
    object Profile : AuthorBottomBarScreen("author_profile", "Profile", Icons.Default.Person)
}
