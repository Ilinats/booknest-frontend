package com.example.booknest.domain.usecase.series

import com.example.booknest.domain.model.request.CreateSeriesRequest
import com.example.booknest.domain.model.response.SeriesResponse
import com.example.booknest.domain.repository.SeriesRepository

class CreateSeriesUseCase(
    private val seriesRepository: SeriesRepository
) {
    suspend operator fun invoke(request: CreateSeriesRequest): Result<SeriesResponse> {
        return seriesRepository.createSeries(request)
    }
}


