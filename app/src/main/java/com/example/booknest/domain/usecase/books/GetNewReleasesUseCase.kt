package com.example.booknest.domain.usecase.books

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.booknest.domain.model.response.RecommendedBookResponse
import com.example.booknest.domain.repository.BooksRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GetNewReleasesUseCase(
    private val booksRepository: BooksRepository
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend operator fun invoke(daysBack: Long = 30, take: Int? = 10): Result<List<RecommendedBookResponse>> {
        val publishedFrom = LocalDateTime.now()
            .minusDays(daysBack)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        
        return booksRepository.browseBooks(
            publishedFrom = publishedFrom,
            take = take,
            status = "active"
        )
    }
}
