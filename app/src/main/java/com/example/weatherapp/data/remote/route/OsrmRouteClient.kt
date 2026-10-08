package com.example.weatherapp.data.remote.route

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OsrmRouteClient {

    private const val BASE_URL =
        "https://router.project-osrm.org/"

    val routeApi: OsrmRouteApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(OsrmRouteApi::class.java)
    }
}