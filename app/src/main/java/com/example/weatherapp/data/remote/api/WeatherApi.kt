package com.example.weatherapp.data.remote.api

import com.example.weatherapp.data.remote.dto.DailyForecastResponseDto
import com.example.weatherapp.data.remote.dto.HourlyForecastResponseDto
import com.example.weatherapp.data.remote.dto.WeatherResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("v1/currentConditions:lookup")
    suspend fun getCurrentWeather(
        @Query("location.latitude")
        latitude: Double,

        @Query("location.longitude")
        longitude: Double,

        @Query("unitsSystem")
        unitsSystem: String = "METRIC",

        @Query("languageCode")
        languageCode: String = "en"
    ): WeatherResponseDto

    @GET("v1/forecast/days:lookup")
    suspend fun getDailyForecast(
        @Query("location.latitude")
        latitude: Double,

        @Query("location.longitude")
        longitude: Double,

        @Query("days")
        days: Int = 7,

        @Query("pageSize")
        pageSize: Int = 7,

        @Query("unitsSystem")
        unitsSystem: String = "METRIC",

        @Query("languageCode")
        languageCode: String = "en"
    ): DailyForecastResponseDto

    @GET("v1/forecast/hours:lookup")
    suspend fun getHourlyForecast(
        @Query("location.latitude")
        latitude: Double,

        @Query("location.longitude")
        longitude: Double,

        @Query("hours")
        hours: Int = 24,

        @Query("pageSize")
        pageSize: Int = 24,

        @Query("unitsSystem")
        unitsSystem: String = "METRIC",

        @Query("languageCode")
        languageCode: String = "en"
    ): HourlyForecastResponseDto
}