package com.example.booknest.data.service

import com.example.booknest.data.constants.PathConstants
import com.example.booknest.data.constants.Series
import com.example.booknest.domain.model.request.CreateSeriesRequest
import com.example.booknest.domain.model.request.UpdateSeriesRequest
import com.example.booknest.domain.model.response.SeriesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface SeriesService {
    @GET(Series.MY_SERIES)
    suspend fun getMySeries(): Response<List<SeriesResponse>>

    @POST(Series.CREATE)
    suspend fun createSeries(@Body series: CreateSeriesRequest): Response<SeriesResponse>

    @PATCH(Series.BY_ID)
    suspend fun updateSeries(
        @Path(PathConstants.SERIES_ID) seriesId: String,
        @Body series: UpdateSeriesRequest
    ): Response<SeriesResponse>

    @DELETE(Series.BY_ID)
    suspend fun deleteSeries(
        @Path(PathConstants.SERIES_ID) seriesId: String
    ): Response<Unit>
}

