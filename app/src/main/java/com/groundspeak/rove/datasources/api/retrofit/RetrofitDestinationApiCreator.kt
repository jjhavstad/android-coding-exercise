package com.groundspeak.rove.datasources.api.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitDestinationApiCreator {
    fun create(baseUrl: String, httpClientBuilder: OkHttpClient.Builder): RetrofitDestinationApi {
        val builder = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
        val retrofit = builder.client(httpClientBuilder.build()).build()
        return retrofit.create(RetrofitDestinationApi::class.java)
    }
}
