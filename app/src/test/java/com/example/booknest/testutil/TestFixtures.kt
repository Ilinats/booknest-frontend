package com.example.booknest.testutil

import com.example.booknest.domain.model.enums.NotificationType
import com.example.booknest.domain.model.request.CreateBookRequest
import com.example.booknest.domain.model.response.AuthorStatsResponse
import com.example.booknest.domain.model.response.PublicProfileResponse
import com.example.booknest.domain.model.response.PublicUserProfileResponse
import com.example.booknest.domain.model.response.ApplicationCheckApplicationResponse
import com.example.booknest.domain.model.response.UserActivityResponse
import com.example.booknest.domain.model.response.ApplicationCheckResponse
import com.example.booknest.domain.model.response.ApplicationReaderResponse
import com.example.booknest.domain.model.response.ApplicationResponse
import com.example.booknest.domain.model.response.AuthorFollowResponse
import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.GenreResponse
import com.example.booknest.domain.model.response.LoginDataResponse
import com.example.booknest.domain.model.response.LotteryResponse
import com.example.booknest.domain.model.response.NotificationResponse
import com.example.booknest.domain.model.response.NotificationsListResponse
import com.example.booknest.domain.model.response.ReaderAddressResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.model.response.SeriesResponse
import com.example.booknest.domain.model.response.ReviewResponse
import com.example.booknest.domain.model.response.UnreadCountResponse
import com.example.booknest.domain.model.response.BookStatsResponse
import com.example.booknest.domain.model.response.UserProfileResponse
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.domain.model.response.UserStatsDataResponse
import com.example.booknest.domain.model.response.UserStatsResponse
import java.time.Instant

object TestFixtures {
    fun book(
        id: String = "book-1",
        title: String = "Test Book",
        seriesId: String? = null,
        seriesOrder: Int? = null,
    ) = RecommendedBookResponse(
        id = id,
        title = title,
        seriesId = seriesId,
        seriesOrder = seriesOrder,
    )

    fun bookDetails(
        id: String = "book-1",
        title: String = "Test Book",
        fullDescription: String? = "Full description",
        status: String? = "active",
        createdAt: String? = "2024-06-01T00:00:00.000Z",
        shortDescription: String? = "Short",
    ) = BookResponse(
        id = id,
        title = title,
        fullDescription = fullDescription,
        status = status,
        createdAt = createdAt,
        shortDescription = shortDescription,
    )

    fun bookStats(
        totalApplications: Int? = 10,
        pendingApplications: Int? = 3,
        approvedReaders: Int = 5,
    ) = BookStatsResponse(
        totalApplications = totalApplications,
        approvedReaders = approvedReaders,
        pendingApplications = pendingApplications,
    )

    fun userStats(
        totalBooks: Int = 5,
        publishedBooks: Int = 3,
        totalApplications: Int = 20,
        pendingApplications: Int = 4,
        approvedApplications: Int = 10,
    ) = UserStatsResponse(
        user = user(userType = "author"),
        stats = UserStatsDataResponse(
            totalBooks = totalBooks,
            publishedBooks = publishedBooks,
            totalApplications = totalApplications,
            approvedApplications = approvedApplications,
            pendingApplications = pendingApplications,
            userType = "author",
        ),
    )

    fun authorStatsResponse(
        authorId: String = "author-1",
    ) = AuthorStatsResponse(
        author = user(id = authorId, userType = "author"),
        stats = UserStatsDataResponse(
            totalBooks = 4,
            publishedBooks = 2,
            totalApplications = 12,
            approvedApplications = 6,
            pendingApplications = 2,
            userType = "author",
        ),
    )

    fun userActivity(
        id: String = "activity-1",
        activityType: String = "application_submitted",
    ) = UserActivityResponse(
        id = id,
        userId = "user-1",
        activityType = activityType,
        createdAt = "2024-06-01T00:00:00.000Z",
    )

    fun application(
        id: String = "app-1",
        status: String = "pending",
        readingStatus: String = "not_started",
        bookId: String = "book-1",
        bookTitle: String? = "Test Book",
        authorName: String? = "Test Author",
        appliedAt: String = "2024-06-15T10:00:00.000Z",
        reviewSubmittedAt: String? = null,
        copyReceivedAt: String? = null,
        applicationMessage: String? = null,
        book: BookResponse? = null,
    ) = ApplicationResponse(
        id = id,
        status = status,
        appliedAt = appliedAt,
        bookId = bookId,
        bookTitle = bookTitle,
        authorName = authorName,
        readingStatus = readingStatus,
        reviewSubmittedAt = reviewSubmittedAt,
        copyReceivedAt = copyReceivedAt,
        applicationMessage = applicationMessage,
        book = book,
    )

    fun applicationReader(
        id: String = "reader-1",
        username: String = "reader1",
        email: String = "reader@example.com",
        firstName: String = "Jane",
        lastName: String = "Doe",
    ) = ApplicationReaderResponse(
        id = id,
        username = username,
        email = email,
        firstName = firstName,
        lastName = lastName,
    )

    fun applicationCheck(
        hasApplied: Boolean = false,
        applicationId: String? = null,
        status: String = "pending",
    ) = ApplicationCheckResponse(
        hasApplied = hasApplied,
        application = applicationId?.let {
            ApplicationCheckApplicationResponse(
                id = it,
                status = status,
                appliedAt = "2024-06-15T10:00:00.000Z",
            )
        },
    )

    fun review(
        id: String = "review-1",
        applicationId: String = "app-1",
        rating: Double = 4.0,
    ) = ReviewResponse(
        id = id,
        applicationId = applicationId,
        rating = rating,
        isPublic = true,
        createdAt = "2024-01-01T00:00:00.000Z",
        updatedAt = "2024-01-01T00:00:00.000Z",
    )

    fun loginData(
        accessToken: String = "access-token",
        refreshToken: String = "refresh-token",
    ) = LoginDataResponse(
        accessToken = accessToken,
        refreshToken = refreshToken,
    )

    fun user(
        id: String = "user-1",
        username: String = "testuser",
        email: String = "test@example.com",
        firstName: String = "Test",
        lastName: String = "User",
        userType: String = "reader",
        createdAt: String? = "2024-01-01T00:00:00.000Z",
    ) = UserResponse(
        id = id,
        username = username,
        email = email,
        firstName = firstName,
        lastName = lastName,
        userType = userType,
        createdAt = createdAt,
    )

    fun userProfile(
        id: String = "profile-1",
        username: String = "testuser",
    ) = UserProfileResponse(
        id = id,
        username = username,
        firstName = "Test",
        lastName = "User",
        createdAt = "2024-01-01T00:00:00.000Z",
    )

    fun publicUserProfile(
        username: String = "reader1",
        isFriend: Boolean = false,
    ) = PublicUserProfileResponse(
        user = user(username = username),
        profile = PublicProfileResponse(
            username = username,
            firstName = "Jane",
            lastName = "Reader",
            bio = "Avid reader",
        ),
        isFriend = isFriend,
    )

    fun genre(id: Int = 1, name: String = "Fantasy") = GenreResponse(
        id = id,
        name = name,
    )

    fun notification(
        id: String = "notif-1",
        isRead: Boolean = false,
        type: String = NotificationType.APPLICATION_APPROVED,
        createdAt: String = Instant.now().toString(),
        title: String = "Update",
        body: String = "Body",
    ) = NotificationResponse(
        id = id,
        userId = "user-1",
        type = type,
        title = title,
        body = body,
        isRead = isRead,
        createdAt = createdAt,
        updatedAt = createdAt,
    )

    fun notificationsList(
        notifications: List<NotificationResponse>,
        hasMore: Boolean = false,
    ) = NotificationsListResponse(
        data = notifications,
        total = notifications.size,
        hasMore = hasMore,
    )

    fun unreadCount(count: Int = 3) = UnreadCountResponse(count = count)

    fun authorFollow(
        id: String = "follow-1",
        authorId: String = "author-1",
        followerId: String = "user-1",
    ) = AuthorFollowResponse(
        id = id,
        authorId = authorId,
        followerId = followerId,
        createdAt = "2024-01-01T00:00:00.000Z",
    )

    fun lotteryResult(approved: Int = 3, rejected: Int = 2) = LotteryResponse(
        approved = approved,
        rejected = rejected,
        message = "Lottery complete",
    )

    fun createBookRequest(title: String = "New Book") = CreateBookRequest(
        title = title,
        ageRating = "16+",
        distributionType = "digital",
        applicationDeadline = "2025-12-01T00:00:00.000Z",
    )

    fun series(
        id: String = "series-1",
        name: String = "My Series",
    ) = SeriesResponse(
        id = id,
        authorId = "author-1",
        name = name,
        createdAt = "2024-01-01T00:00:00.000Z",
        updatedAt = "2024-01-01T00:00:00.000Z",
    )

    fun address(
        id: String = "addr-1",
        street: String = "123 Main St",
        isPrimary: Boolean = true,
    ) = ReaderAddressResponse(
        id = id,
        streetAddress = street,
        city = "Sofia",
        postalCode = "1000",
        country = "Bulgaria",
        isPrimary = isPrimary,
    )
}
