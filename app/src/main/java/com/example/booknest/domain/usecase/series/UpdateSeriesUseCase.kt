package com.example.booknest.domain.usecase.series

import com.example.booknest.domain.model.request.UpdateSeriesRequest
import com.example.booknest.domain.model.response.SeriesResponse
import com.example.booknest.domain.repository.SeriesRepository

class UpdateSeriesUseCase(
    private val seriesRepository: SeriesRepository
) {
    suspend operator fun invoke(seriesId: String, request: UpdateSeriesRequest): Result<SeriesResponse> {
        return seriesRepository.updateSeries(seriesId, request)
    }
}


