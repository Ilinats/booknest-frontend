package com.example.booknest.navigation

sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Login : Screen("login")
    object AccountType : Screen("account_type")
    object PersonalInfo : Screen("personal_info")
    object ProfileDetails : Screen("profile_details")
    object Bio : Screen("bio")
    object Genres : Screen("genres")
    object Home : Screen("home")
    object BookList : Screen("book_list")
    object Main : Screen("main")
}
