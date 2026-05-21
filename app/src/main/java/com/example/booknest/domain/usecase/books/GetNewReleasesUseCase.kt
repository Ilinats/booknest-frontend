package com.example.booknest.domain.usecase.books

import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.repository.BooksRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

class GetNewReleasesUseCase(
    private val booksRepository: BooksRepository
) {
    suspend operator fun invoke(daysBack: Long = 30, limit: Int? = 10): Result<List<RecommendedBookResponse>> {
        val publishedFrom = Instant.now()
            .minus(daysBack, ChronoUnit.DAYS)
            .toString()

        return booksRepository.browseBooks(
            publishedFrom = publishedFrom,
            page = 1,
            limit = limit,
            status = "active",
        )
    }
}
