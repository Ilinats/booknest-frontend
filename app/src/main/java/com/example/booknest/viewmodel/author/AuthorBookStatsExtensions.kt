package com.example.booknest.viewmodel.author

import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.UserStatsDataResponse

data class AuthorBookStatusCounts(
    val total: Int,
    val draft: Int,
    val active: Int,
    val inProgress: Int,
    val completed: Int,
)

fun List<BookResponse>.toAuthorBookStatusCounts(): AuthorBookStatusCounts {
    val books = filter { it.status != BookStatus.ARCHIVED.value }
    return AuthorBookStatusCounts(
        total = books.size,
        draft = books.count { it.status == BookStatus.DRAFT.value },
        active = books.count { it.status == BookStatus.ACTIVE.value },
        inProgress = books.count { it.status == BookStatus.IN_PROGRESS.value },
        completed = books.count { it.status == BookStatus.COMPLETED.value },
    )
}

fun UserStatsDataResponse.withBookStatusCountsFrom(books: List<BookResponse>): UserStatsDataResponse {
    if (books.isEmpty()) return this
    val counts = books.toAuthorBookStatusCounts()
    return copy(
        totalBooks = counts.total,
        draftBooks = counts.draft,
        publishedBooks = counts.active,
        inProgressBooks = counts.inProgress,
        completedBooks = counts.completed,
    )
}
