package com.example.booknest.viewmodel.books

import com.example.booknest.domain.model.response.BookResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory catalog of list-shaped books so detail screens can show cached stubs
 * before the full [BookResponse] loads.
 */
class BookCatalogCache {

    private val entries = MutableStateFlow<Map<String, RecommendedBookResponse>>(emptyMap())

    fun register(books: List<RecommendedBookResponse>) {
        if (books.isEmpty()) return
        entries.update { current ->
            val merged = current.toMutableMap()
            books.forEach { merged[it.id] = it }
            merged
        }
    }

    fun registerFull(book: BookResponse) {
        entries.update { current ->
            current + (book.id to book.toRecommendedStub())
        }
    }

    fun findBook(bookId: String): BookResponse? {
        val cached = entries.value[bookId] ?: return null
        return BookResponse(
            id = cached.id,
            title = cached.title,
            authorName = cached.resolvedAuthorName,
            coverImageUrl = cached.coverImageUrl,
            rating = cached.rating,
            seriesName = cached.seriesName,
            seriesOrder = cached.seriesOrder,
            publishedAt = cached.publishedAt,
            applicationDeadline = cached.applicationDeadline,
            availableCopies = cached.availableCopies,
            totalCopies = cached.totalCopies,
            genres = cached.genres,
            distributionType = cached.distributionType,
            author = cached.author,
            authorId = cached.author?.id,
            fullDescription = null,
            shortDescription = null,
            pageCount = null,
            ageRating = null,
            seriesId = null,
            series = null,
        )
    }

    private fun BookResponse.toRecommendedStub(): RecommendedBookResponse =
        RecommendedBookResponse(
            id = id,
            title = title,
            coverImageUrl = coverImageUrl,
            rating = rating,
            seriesName = seriesName,
            seriesOrder = seriesOrder,
            publishedAt = publishedAt,
            applicationDeadline = applicationDeadline,
            availableCopies = availableCopies,
            totalCopies = totalCopies,
            genres = genres,
            distributionType = distributionType,
            author = author,
            authorName = authorName,
        )
}
