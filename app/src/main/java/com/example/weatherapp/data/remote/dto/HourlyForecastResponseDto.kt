package com.example.weatherapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class HourlyForecastResponseDto(

    @SerializedName("forecastHours")
    val forecastHours:
    List<HourlyForecastDto>?,

    @SerializedName("timeZone")
    val timeZone:
    WeatherTimeZoneDto?,

    @SerializedName("nextPageToken")
    val nextPageToken:
    String?
)

data class HourlyForecastDto(

    @SerializedName("interval")
    val interval:
    ForecastIntervalDto?,

    @SerializedName("displayDateTime")
    val displayDateTime:
    ForecastDateTimeDto?,

    @SerializedName("isDaytime")
    val isDaytime:
    Boolean?,

    @SerializedName("weatherCondition")
    val weatherCondition:
    WeatherConditionDto?,

    @SerializedName("temperature")
    val temperature:
    TemperatureDto?,

    @SerializedName("feelsLikeTemperature")
    val feelsLikeTemperature:
    TemperatureDto?,

    @SerializedName("relativeHumidity")
    val relativeHumidity:
    Int?,

    @SerializedName("uvIndex")
    val uvIndex:
    Int?,

    @SerializedName("precipitation")
    val precipitation:
    PrecipitationDto?,

    @SerializedName("thunderstormProbability")
    val thunderstormProbability:
    Int?,

    @SerializedName("wind")
    val wind:
    WindDto?,

    @SerializedName("visibility")
    val visibility:
    VisibilityDto?,

    @SerializedName("cloudCover")
    val cloudCover:
    Int?
)

data class ForecastIntervalDto(

    @SerializedName("startTime")
    val startTime:
    String?,

    @SerializedName("endTime")
    val endTime:
    String?
)

data class ForecastDateTimeDto(

    @SerializedName("year")
    val year:
    Int?,

    @SerializedName("month")
    val month:
    Int?,

    @SerializedName("day")
    val day:
    Int?,

    @SerializedName("hours")
    val hours:
    Int?,

    @SerializedName("minutes")
    val minutes:
    Int?,

    @SerializedName("seconds")
    val seconds:
    Int?,

    @SerializedName("utcOffset")
    val utcOffset:
    String?
)

data class PrecipitationDto(

    @SerializedName("probability")
    val probability:
    PrecipitationProbabilityDto?
)

data class PrecipitationProbabilityDto(

    @SerializedName("percent")
    val percent:
    Int?,

    @SerializedName("type")
    val type:
    String?
)

data class VisibilityDto(

    @SerializedName("distance")
    val distance:
    Double?,

    @SerializedName("unit")
    val unit:
    String?
)