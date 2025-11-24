package com.example.booknest.navigation

sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Login : Screen("login")
    object EmailVerification : Screen("email_verification") {
        fun createRoute(email: String? = null) = if (email != null) "email_verification?email=$email" else "email_verification"
    }
    object AccountType : Screen("account_type")
    object PersonalInfo : Screen("personal_info")
    object ProfileDetails : Screen("profile_details")
    object Bio : Screen("bio")
    object Genres : Screen("genres")
    object SocialMedia : Screen("social_media")
    object Home : Screen("home")
    object BookList : Screen("book_list")
    object BookDetails : Screen("book_details/{bookId}") {
        fun createRoute(bookId: String) = "book_details/$bookId"
    }
    object Main : Screen("main")
    
    // Author-specific screens
    object BookCreation : Screen("book_creation")
    object SeriesManagement : Screen("series_management")
    object BookApplicationDetail : Screen("book_applications/{bookId}") {
        fun createRoute(bookId: String) = "book_applications/$bookId"
    }
    
    // Profile and Stats screens
    object Profile : Screen("profile/{userId?}") {
        fun createRoute(userId: String? = null) = if (userId != null) "profile/$userId" else "profile"
    }
    object ProfileEdit : Screen("profile_edit")
    object Stats : Screen("stats/{authorId?}") {
        fun createRoute(authorId: String? = null) = if (authorId != null) "stats/$authorId" else "stats"
    }
    
    // Analytics screens
    object BookAnalytics : Screen("book_analytics/{bookId}") {
        fun createRoute(bookId: String) = "book_analytics/$bookId"
    }
    object AuthorAnalytics : Screen("author_analytics")
    
    // Friend screens
    object Friends : Screen("friends")
    object UserProfile : Screen("user_profile/{username}") {
        fun createRoute(username: String) = "user_profile/$username"
    }
    object PrivacySettings : Screen("privacy_settings")
    object SocialMediaManagement : Screen("social_media_management")
    
    // Review screens
    object ReviewSubmission : Screen("review_submission/{applicationId}") {
        fun createRoute(applicationId: String) = "review_submission/$applicationId"
    }
}
