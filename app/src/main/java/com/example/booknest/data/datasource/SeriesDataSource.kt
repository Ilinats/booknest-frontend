package com.example.booknest.data.datasource

import com.example.booknest.domain.model.request.CreateSeriesRequest
import com.example.booknest.domain.model.request.UpdateSeriesRequest
import com.example.booknest.domain.model.response.SeriesResponse

interface SeriesDataSource {
    suspend fun getMySeries(): Result<List<SeriesResponse>>
    suspend fun createSeries(series: CreateSeriesRequest): Result<SeriesResponse>
    suspend fun updateSeries(seriesId: String, series: UpdateSeriesRequest): Result<SeriesResponse>
    suspend fun deleteSeries(seriesId: String): Result<Unit>
}

