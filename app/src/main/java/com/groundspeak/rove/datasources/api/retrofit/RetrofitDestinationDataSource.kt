package com.groundspeak.rove.datasources.api.retrofit

import com.groundspeak.rove.datasources.api.ApiDestinationDataSource
import com.groundspeak.rove.models.Destination
import kotlin.jvm.Throws

class RetrofitDestinationDataSource(
    private val retrofitDestinationApi: RetrofitDestinationApi
) : ApiDestinationDataSource {
    @Throws(Exception::class)
    override suspend fun request(): List<Destination>? {
        val result = retrofitDestinationApi.getDestinations()
        return when(result.isSuccessful) {
            true -> result.body()
            false -> throw Exception(
                "Error receiving destinations: ${result.raw().request().url()} -> ${result.code()} - ${result.message()}"
            )
        }
    }
}
