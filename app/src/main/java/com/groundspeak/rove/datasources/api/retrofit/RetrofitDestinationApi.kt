package com.groundspeak.rove.datasources.api.retrofit

import com.groundspeak.rove.models.Destination
import retrofit2.Response
import retrofit2.http.GET

interface RetrofitDestinationApi {
    @GET("/v3/9326cd89-1243-470b-b435-cd7ebd9ad5ba")
    suspend fun getDestinations(): Response<List<Destination>>
}
