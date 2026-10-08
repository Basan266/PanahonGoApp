package com.example.weatherapp.domain.model

data class Weather(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    val windSpeed: Double,

    /*
     * Temporary compatibility code for the existing
     * briefing and outdoor-readiness logic.
     */
    val weatherCode: Int,

    /*
     * Original condition information returned by
     * Google Weather API.
     */
    val conditionType: String,
    val conditionDescription: String,
    val iconBaseUri: String,

    val isDay: Boolean,
    val observationTime: String,
    val savedAt: Long,
    val isFromCache: Boolean
)