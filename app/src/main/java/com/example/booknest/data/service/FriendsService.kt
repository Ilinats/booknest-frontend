package com.example.booknest.data.service

import com.example.booknest.data.constants.Friends
import com.example.booknest.data.constants.PathConstants
import com.example.booknest.data.constants.QueryConstants
import com.example.booknest.domain.model.response.FriendRequestResponse
import com.example.booknest.domain.model.response.FriendshipStatusResponse
import com.example.booknest.domain.model.response.UserActivityResponse
import com.example.booknest.domain.model.response.UserResponse
import com.example.booknest.domain.model.response.UserSearchResultItemResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FriendsService {
    @POST(Friends.REQUEST)
    suspend fun sendFriendRequest(
        @Path(PathConstants.USERNAME) username: String
    ): Response<FriendRequestResponse>

    @POST(Friends.ACCEPT)
    suspend fun acceptFriendRequest(
        @Path(PathConstants.REQUESTER_ID) requesterId: String
    ): Response<FriendRequestResponse>

    @DELETE(Friends.DECLINE)
    suspend fun declineFriendRequest(
        @Path(PathConstants.REQUESTER_ID) requesterId: String
    ): Response<Unit>

    @DELETE(Friends.CANCEL)
    suspend fun cancelFriendRequest(
        @Path(PathConstants.ADDRESSEE_ID) addresseeId: String
    ): Response<Unit>

    @DELETE(Friends.UNFRIEND)
    suspend fun unfriendUser(
        @Path(PathConstants.FRIEND_ID) friendId: String
    ): Response<Unit>

    @GET(Friends.LIST)
    suspend fun getFriends(
        @Query(QueryConstants.STATUS) status: String? = "accepted",
        @Query(QueryConstants.TYPE) type: String? = null,
        @Query(QueryConstants.SORT_BY) sortBy: String? = null,
    ): Response<List<UserResponse>>

    @GET(Friends.STATUS)
    suspend fun getFriendshipStatus(
        @Path(PathConstants.USER_ID) userId: String
    ): Response<FriendshipStatusResponse>

    @GET(Friends.SEARCH)
    suspend fun searchUsers(
        @Query(QueryConstants.QUERY) query: String,
        @Query(QueryConstants.LIMIT) limit: Int?
    ): Response<List<UserSearchResultItemResponse>>

    @GET(Friends.ACTIVITY)
    suspend fun getFriendsActivity(
        @Query(QueryConstants.LIMIT) limit: Int?
    ): Response<List<UserActivityResponse>>
}

