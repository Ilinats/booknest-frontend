package com.example.booknest.data.service

import com.example.booknest.data.constants.Authors
import com.example.booknest.data.constants.PathConstants
import com.example.booknest.data.constants.QueryConstants
import com.example.booknest.domain.model.response.AuthorFollowResponse
import com.example.booknest.domain.model.response.AuthorFollowWithStatsResponse
import com.example.booknest.domain.model.response.RecommendedBookResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthorsService {
    @POST(Authors.FOLLOW)
    suspend fun followAuthor(
        @Path(PathConstants.AUTHOR_ID) authorId: String
    ): Response<AuthorFollowResponse>

    @DELETE(Authors.UNFOLLOW)
    suspend fun unfollowAuthor(
        @Path(PathConstants.AUTHOR_ID) authorId: String
    ): Response<Unit>

    @GET(Authors.FOLLOWING)
    suspend fun getFollowedAuthors(): Response<List<AuthorFollowResponse>>
    @GET(Authors.FOLLOWERS)
    suspend fun getAuthorFollowers(
        @Path(PathConstants.AUTHOR_ID) authorId: String
    ): Response<List<AuthorFollowResponse>>

    @GET(Authors.CHECK_FOLLOWING)
    suspend fun checkIfFollowingAuthor(
        @Path(PathConstants.AUTHOR_ID) authorId: String
    ): Response<Map<String, Boolean>>

    @GET(Authors.FOLLOWING_BOOKS)
    suspend fun getBooksFromFollowedAuthors(
        @Query(QueryConstants.LIMIT) limit: Int?
    ): Response<List<RecommendedBookResponse>>
}

