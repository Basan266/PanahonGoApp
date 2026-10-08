package com.example.weatherapp.ui.forecast

data class HourlyForecastItem(

    val time: String,

    val temperature: Double,

    val feelsLikeTemperature: Double,

    val rainChance: Int,

    val condition: String,

    val conditionType: String,

    val iconBaseUri: String?,

    val humidity: Int,

    val windSpeed: Double,

    val uvIndex: Int,

    val thunderstormProbability: Int,

    val visibility: Double
)