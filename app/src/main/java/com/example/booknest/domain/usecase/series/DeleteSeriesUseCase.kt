package com.example.booknest.domain.usecase.series

import com.example.booknest.domain.repository.SeriesRepository

class DeleteSeriesUseCase(
    private val seriesRepository: SeriesRepository
) {
    suspend operator fun invoke(seriesId: String): Result<Unit> {
        return seriesRepository.deleteSeries(seriesId)
    }
}


