package com.example.weatherapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WeatherResponseDto(

    @SerializedName("currentTime")
    val currentTime: String?,

    @SerializedName("timeZone")
    val timeZone: WeatherTimeZoneDto?,

    @SerializedName("weatherCondition")
    val weatherCondition: WeatherConditionDto?,

    @SerializedName("temperature")
    val temperature: TemperatureDto?,

    @SerializedName("feelsLikeTemperature")
    val feelsLikeTemperature: TemperatureDto?,

    @SerializedName("wind")
    val wind: WindDto?,

    @SerializedName("isDaytime")
    val isDaytime: Boolean?,

    @SerializedName("relativeHumidity")
    val relativeHumidity: Int?,

    @SerializedName("uvIndex")
    val uvIndex: Int?,

    @SerializedName("thunderstormProbability")
    val thunderstormProbability: Int?,

    @SerializedName("cloudCover")
    val cloudCover: Int?
)

data class WeatherTimeZoneDto(

    @SerializedName("id")
    val id: String?
)

data class WeatherConditionDto(

    @SerializedName("iconBaseUri")
    val iconBaseUri: String?,

    @SerializedName("description")
    val description: LocalizedTextDto?,

    @SerializedName("type")
    val type: String?
)

data class LocalizedTextDto(

    @SerializedName("text")
    val text: String?,

    @SerializedName("languageCode")
    val languageCode: String?
)

data class TemperatureDto(

    @SerializedName("degrees")
    val degrees: Double?,

    @SerializedName("unit")
    val unit: String?
)

data class WindDto(

    @SerializedName("direction")
    val direction: WindDirectionDto?,

    @SerializedName("speed")
    val speed: WindSpeedDto?,

    @SerializedName("gust")
    val gust: WindSpeedDto?
)

data class WindDirectionDto(

    @SerializedName("cardinal")
    val cardinal: String?,

    @SerializedName("degrees")
    val degrees: Int?
)

data class WindSpeedDto(

    @SerializedName("unit")
    val unit: String?,

    @SerializedName("value")
    val value: Double?
)