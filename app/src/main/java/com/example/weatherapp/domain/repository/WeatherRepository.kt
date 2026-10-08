package com.example.weatherapp.domain.repository

import com.example.weatherapp.domain.model.DailyForecast
import com.example.weatherapp.domain.model.HourlyForecast
import com.example.weatherapp.domain.model.Weather

interface WeatherRepository {

    suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double
    ): Result<Weather>

    suspend fun getCachedWeather():
            Weather?

    suspend fun getDailyForecast(
        latitude: Double,
        longitude: Double
    ): Result<List<DailyForecast>>

    suspend fun getHourlyForecast(
        latitude: Double,
        longitude: Double
    ): Result<List<HourlyForecast>>
}