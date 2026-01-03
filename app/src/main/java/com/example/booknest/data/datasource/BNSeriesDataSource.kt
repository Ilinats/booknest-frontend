package com.example.booknest.data.datasource

import com.example.booknest.data.service.SeriesService
import com.example.booknest.domain.model.request.CreateSeriesRequest
import com.example.booknest.domain.model.request.UpdateSeriesRequest
import com.example.booknest.domain.model.response.SeriesResponse

class BNSeriesDataSource(private val seriesService: SeriesService) : SeriesDataSource {

    override suspend fun getMySeries(): Result<List<SeriesResponse>> {
        return requestBody(seriesService.getMySeries())
    }

    override suspend fun createSeries(series: CreateSeriesRequest): Result<SeriesResponse> {
        return requestBody(seriesService.createSeries(series))
    }

    override suspend fun updateSeries(
        seriesId: String,
        series: UpdateSeriesRequest
    ): Result<SeriesResponse> {
        return requestBody(seriesService.updateSeries(seriesId, series))
    }

    override suspend fun deleteSeries(seriesId: String): Result<Unit> {
        return requestBodyUnit(seriesService.deleteSeries(seriesId))
    }
}

