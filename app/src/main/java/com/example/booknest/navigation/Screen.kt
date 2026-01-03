package com.example.booknest.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Landing : Screen("landing")
    object Login : Screen("login")
    object EmailVerification : Screen("email_verification") {
        fun createRoute(email: String? = null) =
            if (email != null) "email_verification?email=$email" else "email_verification"
    }

    object PasswordReset : Screen("password_reset/{email}") {
        fun createRoute(email: String) = "password_reset/$email"
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

    object SeriesBooks : Screen("series_books/{seriesId}") {
        fun createRoute(seriesId: String, seriesName: String? = null) =
            if (seriesName != null) "series_books/$seriesId?seriesName=$seriesName"
            else "series_books/$seriesId"
    }

    object Main : Screen("main")

    object BookCreation : Screen("book_creation")
    object BookEdit : Screen("book_edit/{bookId}") {
        fun createRoute(bookId: String) = "book_edit/$bookId"
    }

    object SeriesManagement : Screen("series_management")
    object BookApplicationDetail : Screen("book_applications/{bookId}") {
        fun createRoute(bookId: String) = "book_applications/$bookId"
    }

    object Profile : Screen("profile/{userId?}") {
        fun createRoute(userId: String? = null) =
            if (userId != null) "profile/$userId" else "profile"
    }

    object ProfileEdit : Screen("profile_edit")
    object Stats : Screen("stats/{authorId?}") {
        fun createRoute(authorId: String? = null) =
            if (authorId != null) "stats/$authorId" else "stats"
    }

    object BookAnalytics : Screen("book_analytics/{bookId}") {
        fun createRoute(bookId: String) = "book_analytics/$bookId"
    }

    object AuthorAnalytics : Screen("author_analytics")

    object Friends : Screen("friends")
    object FavoriteGenres : Screen("favorite_genres")
    object UserProfile : Screen("user_profile/{username}") {
        fun createRoute(username: String) = "user_profile/$username"
    }

    object PrivacySettings : Screen("privacy_settings")
    object SocialMediaManagement : Screen("social_media_management")

    object ReviewSubmission : Screen("review_submission/{applicationId}") {
        fun createRoute(applicationId: String, reviewId: String? = null) =
            if (reviewId != null) "review_submission/$applicationId?reviewId=$reviewId"
            else "review_submission/$applicationId"
    }

    object UserReviews : Screen("user_reviews/{userId}") {
        fun createRoute(userId: String, userName: String? = null) =
            if (userName != null) "user_reviews/$userId?userName=${android.net.Uri.encode(userName)}"
            else "user_reviews/$userId"
    }

    object Notifications : Screen("notifications")
}
