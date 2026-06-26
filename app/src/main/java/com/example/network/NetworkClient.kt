package com.example.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class IpResponse(
    val ip: String
)

interface IpifyApi {
    @GET("?format=json")
    suspend fun getPublicIp(): IpResponse
}

object NetworkClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.ipify.org/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val ipifyApi: IpifyApi = retrofit.create(IpifyApi::class.java)
}
