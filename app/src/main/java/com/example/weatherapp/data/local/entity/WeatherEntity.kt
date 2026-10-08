package com.example.weatherapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "weather"
)
data class WeatherEntity(
    @PrimaryKey
    val id: Int = 1,

    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    val windSpeed: Double,

    /*
     * Retained for compatibility with the existing
     * weather recommendation logic.
     */
    val weatherCode: Int,

    /*
     * Original values returned by Google Weather API.
     */
    val conditionType: String,
    val conditionDescription: String,
    val iconBaseUri: String,

    val isDay: Boolean,
    val observationTime: String,
    val savedAt: Long
)