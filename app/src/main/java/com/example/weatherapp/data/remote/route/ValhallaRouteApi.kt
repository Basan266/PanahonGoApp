package com.example.weatherapp.data.remote.route

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ValhallaRouteApi {

    @Headers(
        "Content-Type: application/json",
        "X-Client-Id: com.example.weatherapp"
    )
    @POST("route")
    suspend fun getRoute(
        @Body request: ValhallaRouteRequest
    ): ValhallaRouteResponse
}