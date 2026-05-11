package com.example.booknest.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Landing : Screen("landing")
    data object Login : Screen("login")
    data object EmailVerification : Screen("email_verification") {
        fun createRoute(email: String? = null) =
            if (email != null) {
                val encodedEmail = android.net.Uri.encode(email)
                "email_verification?email=$encodedEmail"
            } else {
                "email_verification"
            }
    }

    data object PasswordReset : Screen("password_reset/{email}") {
        fun createRoute(email: String) = "password_reset/$email"
    }

    data object AccountType : Screen("account_type")
    data object PersonalInfo : Screen("personal_info")
    data object ProfileDetails : Screen("profile_details")
data object Genres : Screen("genres")
    data object SocialMedia : Screen("social_media")
    data object Home : Screen("home")
    data object BookList : Screen("book_list")
    data object BookDetails : Screen("book_details/{bookId}") {
        fun createRoute(bookId: String) = "book_details/$bookId"
    }

    data object SeriesBooks : Screen("series_books/{seriesId}") {
        fun createRoute(seriesId: String, seriesName: String? = null) =
            if (seriesName != null) "series_books/$seriesId?seriesName=$seriesName"
            else "series_books/$seriesId"
    }

    data object Main : Screen("main")

    data object BookCreation : Screen("book_creation")
    data object BookEdit : Screen("book_edit/{bookId}") {
        fun createRoute(bookId: String) = "book_edit/$bookId"
    }

    data object SeriesManagement : Screen("series_management")
    data object BookApplicationDetail : Screen("book_applications/{bookId}") {
        fun createRoute(bookId: String) = "book_applications/$bookId"
    }

    data object Profile : Screen("profile?userId={userId}") {
        fun createRoute(userId: String? = null) =
            if (userId != null) "profile?userId=$userId" else "profile"
    }

    data object ProfileEdit : Screen("profile_edit")
    data object Stats : Screen("stats?authorId={authorId}") {
        fun createRoute(authorId: String? = null) =
            if (authorId != null) "stats?authorId=$authorId" else "stats"
    }

    data object BookAnalytics : Screen("book_analytics/{bookId}") {
        fun createRoute(bookId: String) = "book_analytics/$bookId"
    }

    data object AuthorAnalytics : Screen("author_analytics")

    data object Friends : Screen("friends")
    data object FavoriteGenres : Screen("favorite_genres")
    data object UserProfile : Screen("user_profile/{username}") {
        fun createRoute(username: String) = "user_profile/$username"
    }

    data object PrivacySettings : Screen("privacy_settings")
    data object SocialMediaManagement : Screen("social_media_management")

    data object ReviewSubmission : Screen("review_submission/{applicationId}") {
        fun createRoute(applicationId: String, reviewId: String? = null) =
            if (reviewId != null) "review_submission/$applicationId?reviewId=$reviewId"
            else "review_submission/$applicationId"
    }

    data object UserReviews : Screen("user_reviews/{userId}") {
        fun createRoute(userId: String, userName: String? = null) =
            if (userName != null) "user_reviews/$userId?userName=${android.net.Uri.encode(userName)}"
            else "user_reviews/$userId"
    }

    data object Notifications : Screen("notifications")

    data object Browse : Screen("browse") {
        fun createRoute(searchQuery: String? = null) =
            if (searchQuery != null && searchQuery.isNotBlank()) {
                val encodedQuery = android.net.Uri.encode(searchQuery)
                "browse/$encodedQuery"
            } else {
                "browse"
            }
    }

    data object Books : Screen("books/{category}") {
        fun createRoute(category: String) = "books/$category"
    }
}
