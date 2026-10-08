package com.example.weatherapp.data.remote.route

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OsrmRouteApi {

    @GET("route/v1/driving/{coordinates}")
    suspend fun getRoute(
        @Path(
            value = "coordinates",
            encoded = true
        )
        coordinates: String,

        @Query("overview")
        overview: String = "full",

        @Query("geometries")
        geometries: String = "geojson",

        @Query("steps")
        steps: Boolean = true,

        @Query("alternatives")
        alternatives: Boolean = true,

        @Query("radiuses")
        radiuses: String? = null
    ): OsrmRouteResponse
}