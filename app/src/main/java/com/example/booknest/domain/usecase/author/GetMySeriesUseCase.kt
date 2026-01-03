package com.example.booknest.domain.usecase.author

import com.example.booknest.domain.model.response.SeriesResponse
import com.example.booknest.domain.repository.SeriesRepository

class GetMySeriesUseCase(
    private val repository: SeriesRepository
) {
    suspend operator fun invoke(): Result<List<SeriesResponse>> =
        repository.getMySeries()
}
