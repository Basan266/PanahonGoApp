package com.example.weatherapp.domain.model

data class DailyForecast(
    val year: Int,
    val month: Int,
    val day: Int,

    val conditionType: String,
    val conditionDescription: String,
    val iconBaseUri: String,

    val minimumTemperature: Double,
    val maximumTemperature: Double,

    val sunriseTime: String = "",
    val sunsetTime: String = ""
)