package com.example.booknest.testutil

object DataSourceJsonFixtures {

    const val USER =
        """{"id":"user-1","username":"testuser","email":"test@example.com","firstName":"Test","lastName":"User","userType":"reader","emailVerified":true,"createdAt":"2024-01-01T00:00:00.000Z"}"""

    val userStats: String
        get() = """
            {
              "user": $USER,
              "stats": {
                "totalApplications": 5,
                "approvedApplications": 2,
                "pendingApplications": 1,
                "userType": "reader"
              }
            }
        """.trimIndent()

    val userProfile: String
        get() = """
            {
              "id": "profile-1",
              "userId": "user-1",
              "createdAt": "2024-01-01T00:00:00.000Z"
            }
        """.trimIndent()

    const val RECOMMENDED_BOOK = """{"id":"book-1","title":"Test Book"}"""

    fun paginatedBooks(booksJson: String = RECOMMENDED_BOOK): String =
        """{"data":[$booksJson]}"""

    const val BOOK_DETAILS = """{"id":"book-1","title":"Test Book","status":"active"}"""

    val trendingBook: String
        get() = """
            {
              "book": $RECOMMENDED_BOOK,
              "applicationCount": 12
            }
        """.trimIndent()

    val review: String
        get() = """
            {
              "id": "review-1",
              "applicationId": "app-1",
              "rating": 4.5,
              "isPublic": true,
              "createdAt": "2024-01-01T00:00:00.000Z",
              "updatedAt": "2024-01-01T00:00:00.000Z"
            }
        """.trimIndent()

    val series: String
        get() = """
            {
              "id": "series-1",
              "authorId": "author-1",
              "name": "My Series",
              "createdAt": "2024-01-01T00:00:00.000Z",
              "updatedAt": "2024-01-01T00:00:00.000Z"
            }
        """.trimIndent()

    val authorFollow: String
        get() = """
            {
              "id": "follow-1",
              "followerId": "user-1",
              "authorId": "author-1",
              "createdAt": "2024-01-01T00:00:00.000Z"
            }
        """.trimIndent()

    val friendRequest: String
        get() = """
            {
              "id": "req-1",
              "requesterId": "user-1",
              "addresseeId": "user-2",
              "status": "pending",
              "createdAt": "2024-01-01T00:00:00.000Z",
              "updatedAt": "2024-01-01T00:00:00.000Z"
            }
        """.trimIndent()

    const val FRIENDSHIP_STATUS = """{"status":"accepted","isRequester":false}"""

    val notification: String
        get() = """
            {
              "id": "notif-1",
              "userId": "user-1",
              "type": "application_approved",
              "title": "Approved",
              "body": "Your application was approved",
              "isRead": false,
              "createdAt": "2024-06-01T00:00:00.000Z",
              "updatedAt": "2024-06-01T00:00:00.000Z"
            }
        """.trimIndent()

    val notificationsList: String
        get() = """{"data":[$notification],"total":1}"""

    const val UNREAD_COUNT = """{"count":3}"""

    fun errorBody(message: String, statusCode: Int = 400): String =
        """{"message":"$message","statusCode":$statusCode}"""
}
