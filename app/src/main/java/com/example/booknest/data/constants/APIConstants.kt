package com.example.booknest.data.constants

object Api {
    const val PREFIX = "/api"
}

object Auth {
    private const val AUTH = "${Api.PREFIX}/auth"
    const val LOGIN = "$AUTH/login"
    const val REGISTER = "$AUTH/register"
    const val REFRESH = "$AUTH/refresh-token"
    const val LOGOUT = "$AUTH/logout"
    const val VERIFY_EMAIL = "$AUTH/verify-email"
    const val RESEND_VERIFICATION = "$AUTH/resend-verification"
    const val REQUEST_PASSWORD_RESET = "$AUTH/request-password-reset"
    const val RESET_PASSWORD = "$AUTH/reset-password"
    const val CHECK_USERNAME = "$AUTH/check-username"
    const val CHECK_EMAIL = "$AUTH/check-email"
    const val REFRESH_STRING_CONCAT = "api/auth/refresh-token"
}

object Users {
    private const val USERS = "${Api.PREFIX}/users"
    const val ME = "$USERS/me"
    const val PROFILE = "$USERS/profile/{userId}"
    const val MY_STATS = "$USERS/me/stats"
    const val AUTHOR_STATS = "$USERS/profile/{authorId}/stats"
    const val UPLOAD_AVATAR = "$USERS/me/avatar"
    const val DELETE_AVATAR = "$USERS/me/avatar"
    const val DELETE_ACCOUNT = "$USERS/me"
}

object Profiles {
    private const val PROFILES = "${Api.PREFIX}/profiles"
    const val ME = "$PROFILES/me"
    const val USER = "$PROFILES/user/{username}"
    const val SOCIAL_MEDIA = "$PROFILES/me/social-media"
    const val PRIVACY = "$PROFILES/me/privacy"
    const val NOTIFICATIONS = "$PROFILES/me/notifications"
    const val ACTIVITY = "$PROFILES/me/activity"
    const val ACTIVITY_PUBLIC = "$PROFILES/me/activity/public"
    const val ACTIVITY_RECENT = "$PROFILES/me/activity/recent"
    const val ACTIVITY_STATS = "$PROFILES/me/activity/stats"
    const val USER_ACTIVITY_RECENT = "$PROFILES/user/{username}/activity/recent"
    const val ADDRESSES = "$PROFILES/me/addresses"
    const val ADDRESS_ID = "$PROFILES/me/addresses/{addressId}"
}

object Books {
    private const val BOOKS = "${Api.PREFIX}/books"
    const val LIST = BOOKS
    const val SEARCH = "$BOOKS/search"
    const val RECOMMENDED = "$BOOKS/recommended"
    const val TRENDING = "$BOOKS/trending"
    const val MY_BOOKS = "$BOOKS/my"
    const val BY_ID = "$BOOKS/{bookId}"
    const val PUBLISH = "$BOOKS/{bookId}/publish"
    const val STATS = "$BOOKS/{bookId}/stats"
    const val ANALYTICS = "$BOOKS/{bookId}/analytics"
    const val ANALYTICS_DETAILED = "$BOOKS/{bookId}/analytics/detailed"
    const val AUTHOR_ANALYTICS = "$BOOKS/analytics/author"
    const val BOOK_PERFORMANCE_COMPARISON = "$BOOKS/analytics/performance-comparison"
    const val UPLOAD = "$BOOKS/{bookId}/upload"
    const val COVER = "$BOOKS/{bookId}/cover"
    const val DELETE_COVER = "$BOOKS/{bookId}/cover"
    const val DOWNLOAD = "$BOOKS/{bookId}/download"
    const val ALL_REVIEWS = "$BOOKS/{bookId}/reviews/all"
}

object Genres {
    private const val GENRES = "${Api.PREFIX}/genres"
    const val LIST = GENRES
}

object GenrePreferences {
    private const val ME = "${Api.PREFIX}/me"
    const val LIST = "$ME/genre-preferences"
}

object Series {
    private const val SERIES = "${Api.PREFIX}/series"
    const val CREATE = SERIES
    const val MY_SERIES = "$SERIES/my"
    const val BY_ID = "$SERIES/{seriesId}"
}

object Applications {
    private const val APPLICATIONS = "${Api.PREFIX}/applications"
    const val CREATE = APPLICATIONS
    const val MY_APPLICATIONS = "$APPLICATIONS/my"
    const val CHECK = "$APPLICATIONS/check/{bookId}"
    const val BY_ID = "$APPLICATIONS/{applicationId}"
    const val BOOK_APPLICATIONS = "$APPLICATIONS/books/{bookId}"
    const val BULK_ACTION = "$APPLICATIONS/books/{bookId}/bulk-action"
    const val RUN_LOTTERY = "$APPLICATIONS/books/{bookId}/run-lottery"
    const val MARK_SENT = "$APPLICATIONS/{applicationId}/mark-sent"
    const val MARK_RECEIVED = "$APPLICATIONS/{applicationId}/mark-received"
    const val READING_STATUS = "$APPLICATIONS/{applicationId}/reading-status"
    const val READING_PROGRESS = "$APPLICATIONS/my/reading-progress"
    const val OVERDUE_REVIEWS = "$APPLICATIONS/overdue-reviews"
}

object Reviews {
    private const val REVIEWS = "${Api.PREFIX}/reviews"
    const val CREATE = REVIEWS
    const val BY_ID = "$REVIEWS/{reviewId}"
    const val BOOK_REVIEWS = "$REVIEWS/books/{bookId}"
    const val USER_REVIEWS = "$REVIEWS/users/{userId}"
    const val AUTHOR_LATEST = "$REVIEWS/author/latest"
}

object Friends {
    private const val FRIENDS = "${Api.PREFIX}/friends"
    const val LIST = "$FRIENDS/list"
    const val REQUEST = "$FRIENDS/request/{username}"
    const val ACCEPT = "$FRIENDS/accept/{requesterId}"
    const val DECLINE = "$FRIENDS/decline/{requesterId}"
    const val CANCEL = "$FRIENDS/cancel/{addresseeId}"
    const val UNFRIEND = "$FRIENDS/unfriend/{friendId"
    const val REQUESTS_SENT = "$FRIENDS/requests/sent"
    const val REQUESTS_RECEIVED = "$FRIENDS/requests/received"
    const val STATUS = "$FRIENDS/status/{userId}"
    const val SEARCH = "$FRIENDS/search"
    const val ACTIVITY = "$FRIENDS/activity"
}

object Authors {
    private const val AUTHORS = "${Api.PREFIX}/authors"
    const val FOLLOW = "$AUTHORS/follow/{authorId}"
    const val UNFOLLOW = "$AUTHORS/unfollow/{authorId}"
    const val FOLLOWING = "$AUTHORS/following"
    const val FOLLOWERS = "$AUTHORS/followers/{authorId}"
    const val CHECK_FOLLOWING = "$AUTHORS/following/check/{authorId}"
    const val FOLLOWING_BOOKS = "$AUTHORS/following/books"
}

object Notifications {
    private const val NOTIFICATIONS = "${Api.PREFIX}/notifications"
    const val LIST = NOTIFICATIONS
    const val UNREAD_COUNT = "$NOTIFICATIONS/unread-count"
    const val MARK_READ = "$NOTIFICATIONS/{notificationId}/read"
    const val MARK_ALL_READ = "$NOTIFICATIONS/read-all"
    const val DELETE = "$NOTIFICATIONS/{notificationId}"
    const val DELETE_ALL = "$NOTIFICATIONS/all"
}

object DeviceTokens {
    private const val DEVICE_TOKENS = "${Api.PREFIX}/device-tokens"
    const val REGISTER = "$DEVICE_TOKENS/register"
}

object PathConstants {
    const val BOOK_ID = "bookId"
    const val USER_ID = "userId"
    const val AUTHOR_ID = "authorId"
    const val APPLICATION_ID = "applicationId"
    const val REVIEW_ID = "reviewId"
    const val SERIES_ID = "seriesId"
    const val GENRE_ID = "genreId"
    const val ADDRESS_ID = "addressId"
    const val NOTIFICATION_ID = "notificationId"
    const val USERNAME = "username"
    const val REQUESTER_ID = "requesterId"
    const val ADDRESSEE_ID = "addresseeId"
    const val FRIEND_ID = "friendId"
}

object QueryConstants {
    const val SEARCH = "search"
    const val QUERY = "q"
    const val GENRES = "genres"
    const val AGE_RATING = "ageRating"
    const val DISTRIBUTION_TYPE = "distributionType"
    const val PUBLISHED_FROM = "publishedFrom"
    const val PUBLISHED_TO = "publishedTo"
    const val CREATED_FROM = "createdFrom"
    const val CREATED_TO = "createdTo"
    const val TITLE = "title"
    const val AUTHOR_NAME = "authorName"
    const val AUTHOR_ID = "authorId"
    const val SERIES_NAME = "seriesName"
    const val SERIES_ID = "seriesId"
    const val MIN_AVG_RATING = "minAvgRating"
    const val MAX_AVG_RATING = "maxAvgRating"
    const val SKIP = "skip"
    const val TAKE = "take"
    const val LIMIT = "limit"
    const val OFFSET = "offset"
    const val STATUS = "status"
    const val TYPE = "type"
    const val UNREAD_ONLY = "unreadOnly"
    const val DAYS = "days"
    const val APPLICATION_STATUS = "applicationStatus"
    const val DEADLINE_FILTER = "deadlineFilter"
    const val SORT_BY = "sortBy"
}

