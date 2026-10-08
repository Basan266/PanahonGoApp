package com.example.weatherapp.data.remote.geocoding

import retrofit2.http.GET
import retrofit2.http.Query

interface PhotonApi {

    @GET("api/")
    suspend fun searchPlaces(
        @Query("q") query: String,
        @Query("limit") limit: Int = 8,
        @Query("lat") latitude: Double? = null,
        @Query("lon") longitude: Double? = null,
        @Query("lang") language: String = "en"
    ): PhotonResponse
}