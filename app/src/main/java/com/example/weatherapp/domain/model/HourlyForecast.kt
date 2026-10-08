package com.example.weatherapp.domain.model

data class HourlyForecast(

    val startTime: String,

    val year: Int,

    val month: Int,

    val day: Int,

    val hour: Int,

    val isDaytime: Boolean,

    val temperature: Double,

    val feelsLikeTemperature: Double,

    val rainChance: Int,

    val precipitationType: String,

    val condition: String,

    val conditionType: String,

    val iconBaseUri: String?,

    val humidity: Int,

    val windSpeed: Double,

    val windDirection: String,

    val uvIndex: Int,

    val thunderstormProbability: Int,

    val visibility: Double,

    val cloudCover: Int
)