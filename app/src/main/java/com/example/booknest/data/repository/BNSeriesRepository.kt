package com.example.booknest.data.repository

import com.example.booknest.data.datasource.SeriesDataSource
import com.example.booknest.data.datasource.resultBody
import com.example.booknest.domain.model.request.CreateSeriesRequest
import com.example.booknest.domain.model.request.UpdateSeriesRequest
import com.example.booknest.domain.model.response.SeriesResponse
import com.example.booknest.domain.repository.SeriesRepository

class BNSeriesRepository(private val seriesDataSource: SeriesDataSource) : SeriesRepository {

    override suspend fun getMySeries(): Result<List<SeriesResponse>> {
        return resultBody(seriesDataSource.getMySeries())
    }

    override suspend fun createSeries(series: CreateSeriesRequest): Result<SeriesResponse> {
        return resultBody(seriesDataSource.createSeries(series))
    }

    override suspend fun updateSeries(
        seriesId: String,
        series: UpdateSeriesRequest
    ): Result<SeriesResponse> {
        return resultBody(seriesDataSource.updateSeries(seriesId, series))
    }

    override suspend fun deleteSeries(seriesId: String): Result<Unit> {
        return resultBody(seriesDataSource.deleteSeries(seriesId))
    }
}

