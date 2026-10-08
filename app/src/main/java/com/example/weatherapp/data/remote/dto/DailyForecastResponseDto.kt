package com.example.weatherapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DailyForecastResponseDto(
    @SerializedName("forecastDays")
    val forecastDays: List<ForecastDayDto> = emptyList(),

    @SerializedName("timeZone")
    val timeZone: WeatherTimeZoneDto? = null,

    @SerializedName("nextPageToken")
    val nextPageToken: String? = null
)

data class ForecastDayDto(
    @SerializedName("displayDate")
    val displayDate: ForecastDateDto? = null,

    @SerializedName("daytimeForecast")
    val daytimeForecast: ForecastDayPartDto? = null,

    @SerializedName("nighttimeForecast")
    val nighttimeForecast: ForecastDayPartDto? = null,

    @SerializedName("maxTemperature")
    val maxTemperature: TemperatureDto? = null,

    @SerializedName("minTemperature")
    val minTemperature: TemperatureDto? = null,

    @SerializedName("sunEvents")
    val sunEvents: SunEventsDto? = null
)

data class ForecastDateDto(
    @SerializedName("year")
    val year: Int = 0,

    @SerializedName("month")
    val month: Int = 0,

    @SerializedName("day")
    val day: Int = 0
)

data class ForecastDayPartDto(
    @SerializedName("weatherCondition")
    val weatherCondition: WeatherConditionDto? = null,

    @SerializedName("relativeHumidity")
    val relativeHumidity: Int? = null,

    @SerializedName("uvIndex")
    val uvIndex: Int? = null,

    @SerializedName("thunderstormProbability")
    val thunderstormProbability: Int? = null,

    @SerializedName("cloudCover")
    val cloudCover: Int? = null
)

data class SunEventsDto(

    @SerializedName("sunriseTime")
    val sunriseTime: String? = null,

    @SerializedName("sunsetTime")
    val sunsetTime: String? = null
)