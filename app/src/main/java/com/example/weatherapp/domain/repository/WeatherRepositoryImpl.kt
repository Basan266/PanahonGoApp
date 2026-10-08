package com.example.weatherapp.data.repository

import android.util.Log
import com.example.weatherapp.data.local.dao.WeatherDao
import com.example.weatherapp.data.local.entity.WeatherEntity
import com.example.weatherapp.data.remote.api.WeatherApi
import com.example.weatherapp.domain.model.DailyForecast
import com.example.weatherapp.domain.model.HourlyForecast
import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.domain.repository.WeatherRepository
import java.util.Locale
import kotlin.math.abs

class WeatherRepositoryImpl(
    private val weatherApi: WeatherApi,
    private val weatherDao: WeatherDao
) : WeatherRepository {

    override suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double
    ): Result<Weather> {
        return try {
            loadFromInternet(
                latitude = latitude,
                longitude = longitude
            )
        } catch (exception: Exception) {
            Log.e(
                "GoogleWeather",
                "Live Google Weather request failed for " +
                        "$latitude, $longitude",
                exception
            )

            loadFromDatabase(
                originalException = exception,
                latitude = latitude,
                longitude = longitude
            )
        }
    }

    override suspend fun getCachedWeather():
            Weather? {
        return weatherDao
            .getLatestWeather()
            ?.toDomain(
                isFromCache = true
            )
    }

    override suspend fun getDailyForecast(
        latitude: Double,
        longitude: Double
    ): Result<List<DailyForecast>> {
        return try {
            val response =
                weatherApi.getDailyForecast(
                    latitude = latitude,
                    longitude = longitude,
                    days = 7,
                    pageSize = 7
                )

            val forecasts =
                response.forecastDays
                    .mapNotNull { forecastDay ->

                        val displayDate =
                            forecastDay.displayDate
                                ?: return@mapNotNull null

                        val weatherCondition =
                            forecastDay.daytimeForecast
                                ?.weatherCondition
                                ?: forecastDay
                                    .nighttimeForecast
                                    ?.weatherCondition
                                ?: return@mapNotNull null

                        val maximumTemperature =
                            forecastDay.maxTemperature
                                ?.degrees
                                ?: return@mapNotNull null

                        val minimumTemperature =
                            forecastDay.minTemperature
                                ?.degrees
                                ?: return@mapNotNull null

                        val normalizedConditionType =
                            weatherCondition.type
                                .orEmpty()
                                .trim()
                                .uppercase(
                                    Locale.US
                                )

                        DailyForecast(
                            year =
                            displayDate.year,

                            month =
                            displayDate.month,

                            day =
                            displayDate.day,

                            conditionType =
                            normalizedConditionType,

                            conditionDescription =
                            weatherCondition.description
                                ?.text
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: createConditionDescription(
                                    normalizedConditionType
                                ),

                            iconBaseUri =
                            weatherCondition.iconBaseUri
                                .orEmpty(),

                            minimumTemperature =
                            minimumTemperature,

                            maximumTemperature =
                            maximumTemperature,

                            sunriseTime =
                            forecastDay.sunEvents
                                ?.sunriseTime
                                .orEmpty(),

                            sunsetTime =
                            forecastDay.sunEvents
                                ?.sunsetTime
                                .orEmpty()
                        )
                    }

            if (forecasts.isEmpty()) {
                Result.failure(
                    IllegalStateException(
                        "Google Weather returned an empty daily forecast."
                    )
                )
            } else {
                Result.success(
                    forecasts.take(7)
                )
            }
        } catch (exception: Exception) {
            Log.e(
                "GoogleWeather",
                "Unable to retrieve daily forecast.",
                exception
            )

            Result.failure(
                exception
            )
        }
    }

    override suspend fun getHourlyForecast(
        latitude: Double,
        longitude: Double
    ): Result<List<HourlyForecast>> {
        return try {
            val response =
                weatherApi.getHourlyForecast(
                    latitude = latitude,
                    longitude = longitude,
                    hours = 24,
                    pageSize = 24
                )

            val hourlyForecasts =
                response.forecastHours
                    .orEmpty()
                    .mapNotNull { forecastHour ->

                        val displayDateTime =
                            forecastHour.displayDateTime
                                ?: return@mapNotNull null

                        val temperature =
                            forecastHour.temperature
                                ?.degrees
                                ?: return@mapNotNull null

                        val condition =
                            forecastHour.weatherCondition

                        val conditionType =
                            condition
                                ?.type
                                .orEmpty()
                                .trim()
                                .uppercase(
                                    Locale.US
                                )

                        val conditionDescription =
                            condition
                                ?.description
                                ?.text
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: createConditionDescription(
                                    conditionType
                                )

                        HourlyForecast(
                            startTime =
                            forecastHour.interval
                                ?.startTime
                                .orEmpty(),

                            year =
                            displayDateTime.year
                                ?: return@mapNotNull null,

                            month =
                            displayDateTime.month
                                ?: return@mapNotNull null,

                            day =
                            displayDateTime.day
                                ?: return@mapNotNull null,

                            hour =
                            displayDateTime.hours
                                ?: return@mapNotNull null,

                            isDaytime =
                            forecastHour.isDaytime
                                ?: true,

                            temperature =
                            temperature,

                            feelsLikeTemperature =
                            forecastHour
                                .feelsLikeTemperature
                                ?.degrees
                                ?: temperature,

                            rainChance =
                            forecastHour.precipitation
                                ?.probability
                                ?.percent
                                ?: 0,

                            precipitationType =
                            forecastHour.precipitation
                                ?.probability
                                ?.type
                                .orEmpty(),

                            condition =
                            conditionDescription,

                            conditionType =
                            conditionType,

                            iconBaseUri =
                            condition
                                ?.iconBaseUri,

                            humidity =
                            forecastHour.relativeHumidity
                                ?: 0,

                            windSpeed =
                            forecastHour.wind
                                ?.speed
                                ?.value
                                ?: 0.0,

                            windDirection =
                            forecastHour.wind
                                ?.direction
                                ?.cardinal
                                .orEmpty(),

                            uvIndex =
                            forecastHour.uvIndex
                                ?: 0,

                            thunderstormProbability =
                            forecastHour
                                .thunderstormProbability
                                ?: 0,

                            visibility =
                            forecastHour.visibility
                                ?.distance
                                ?: 0.0,

                            cloudCover =
                            forecastHour.cloudCover
                                ?: 0
                        )
                    }

            if (hourlyForecasts.isEmpty()) {
                Result.failure(
                    IllegalStateException(
                        "Google Weather returned an empty hourly forecast."
                    )
                )
            } else {
                Result.success(
                    hourlyForecasts.take(24)
                )
            }
        } catch (exception: Exception) {
            Log.e(
                "GoogleWeather",
                "Unable to retrieve hourly forecast.",
                exception
            )

            Result.failure(
                exception
            )
        }
    }




    private suspend fun loadFromInternet(
        latitude: Double,
        longitude: Double
    ): Result<Weather> {
        val response =
            weatherApi.getCurrentWeather(
                latitude = latitude,
                longitude = longitude
            )

        Log.d(
            "GoogleWeather",
            "Requested coordinates: " +
                    "$latitude, $longitude\n" +
                    "Current time: " +
                    "${response.currentTime}\n" +
                    "Condition type: " +
                    "${response.weatherCondition?.type}\n" +
                    "Condition description: " +
                    "${response.weatherCondition?.description?.text}\n" +
                    "Temperature: " +
                    "${response.temperature?.degrees}\n" +
                    "Thunderstorm probability: " +
                    "${response.thunderstormProbability}\n" +
                    "Cloud cover: " +
                    "${response.cloudCover}"
        )

        val temperature =
            requireNotNull(
                response.temperature
                    ?.degrees
            ) {
                "Temperature was not included in the Google Weather response."
            }

        val apparentTemperature =
            response.feelsLikeTemperature
                ?.degrees
                ?: temperature

        val conditionType =
            response.weatherCondition
                ?.type
                .orEmpty()
                .trim()
                .uppercase(
                    Locale.US
                )

        val rawConditionDescription =
            response.weatherCondition
                ?.description
                ?.text
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: createConditionDescription(
                    conditionType
                )

        val thunderstormProbability =
            response.thunderstormProbability
                ?.coerceIn(
                    minimumValue = 0,
                    maximumValue = 100
                )
                ?: 0

        val isThunderstormCondition =
            conditionType in setOf(
                "THUNDERSTORM",
                "THUNDERSHOWER",
                "LIGHT_THUNDERSTORM_RAIN",
                "SCATTERED_THUNDERSTORMS",
                "HEAVY_THUNDERSTORM"
            )

        val conditionDescription =
            if (
                isThunderstormCondition &&
                thunderstormProbability < 80
            ) {
                "Thunderstorms possible " +
                        "($thunderstormProbability%)"
            } else {
                rawConditionDescription
            }

        val iconBaseUri =
            response.weatherCondition
                ?.iconBaseUri
                .orEmpty()

        val entity =
            WeatherEntity(
                latitude =
                latitude,

                longitude =
                longitude,

                timezone =
                response.timeZone
                    ?.id
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: "Asia/Manila",

                temperature =
                temperature,

                apparentTemperature =
                apparentTemperature,

                humidity =
                response.relativeHumidity
                    ?: 0,

                windSpeed =
                response.wind
                    ?.speed
                    ?.value
                    ?: 0.0,

                weatherCode =
                mapGoogleConditionToWeatherCode(
                    conditionType
                ),

                conditionType =
                conditionType,

                conditionDescription =
                conditionDescription,

                iconBaseUri =
                iconBaseUri,

                isDay =
                response.isDaytime
                    ?: true,

                observationTime =
                response.currentTime
                    .orEmpty(),

                savedAt =
                System.currentTimeMillis()
            )

        weatherDao.saveWeather(
            entity
        )

        Log.d(
            "GoogleWeather",
            "Live weather saved successfully. " +
                    "Condition=$conditionType, " +
                    "description=$conditionDescription, " +
                    "thunderstormProbability=" +
                    "$thunderstormProbability"
        )

        return Result.success(
            entity.toDomain(
                isFromCache = false
            )
        )
    }




    private suspend fun loadFromDatabase(
        originalException: Exception,
        latitude: Double,
        longitude: Double
    ): Result<Weather> {
        val cachedWeather =
            weatherDao.getLatestWeather()

        if (cachedWeather == null) {
            Log.e(
                "GoogleWeather",
                "No cached weather is available."
            )

            return Result.failure(
                originalException
            )
        }

        val cacheAge =
            System.currentTimeMillis() -
                    cachedWeather.savedAt

        val latitudeDifference =
            abs(
                cachedWeather.latitude -
                        latitude
            )

        val longitudeDifference =
            abs(
                cachedWeather.longitude -
                        longitude
            )

        val isCacheFresh =
            cacheAge in
                    0..MAXIMUM_CACHE_AGE_MILLISECONDS

        val isSameArea =
            latitudeDifference <=
                    MAXIMUM_COORDINATE_DIFFERENCE &&
                    longitudeDifference <=
                    MAXIMUM_COORDINATE_DIFFERENCE

        if (!isCacheFresh || !isSameArea) {
            Log.e(
                "GoogleWeather",
                "Cached weather rejected. " +
                        "Age=" +
                        "${cacheAge / 60_000L} minutes, " +
                        "sameArea=$isSameArea"
            )

            return Result.failure(
                originalException
            )
        }

        Log.d(
            "GoogleWeather",
            "Using cached weather. " +
                    "Age=" +
                    "${cacheAge / 60_000L} minutes, " +
                    "condition=" +
                    cachedWeather.conditionType
        )

        return Result.success(
            cachedWeather.toDomain(
                isFromCache = true
            )
        )
    }

    private fun createConditionDescription(
        conditionType: String
    ): String {
        if (conditionType.isBlank()) {
            return "Weather condition unavailable"
        }

        return conditionType
            .lowercase(
                Locale.getDefault()
            )
            .replace(
                oldChar = '_',
                newChar = ' '
            )
            .replaceFirstChar { character ->
                if (character.isLowerCase()) {
                    character.titlecase(
                        Locale.getDefault()
                    )
                } else {
                    character.toString()
                }
            }
    }

    private fun mapGoogleConditionToWeatherCode(
        conditionType: String
    ): Int {
        return when (conditionType) {
            "CLEAR" ->
                0

            "MOSTLY_CLEAR" ->
                1

            "PARTLY_CLOUDY" ->
                2

            "MOSTLY_CLOUDY",
            "CLOUDY",
            "WINDY" ->
                3

            "LIGHT_RAIN_SHOWERS",
            "CHANCE_OF_SHOWERS",
            "LIGHT_RAIN" ->
                51

            "SCATTERED_SHOWERS",
            "RAIN_SHOWERS",
            "LIGHT_TO_MODERATE_RAIN" ->
                61

            "MODERATE_TO_HEAVY_RAIN",
            "RAIN",
            "WIND_AND_RAIN" ->
                63

            "HEAVY_RAIN_SHOWERS",
            "HEAVY_RAIN",
            "RAIN_PERIODICALLY_HEAVY" ->
                65

            "LIGHT_SNOW_SHOWERS",
            "CHANCE_OF_SNOW_SHOWERS",
            "LIGHT_SNOW" ->
                71

            "SCATTERED_SNOW_SHOWERS",
            "SNOW_SHOWERS",
            "LIGHT_TO_MODERATE_SNOW",
            "SNOW",
            "RAIN_AND_SNOW" ->
                73

            "HEAVY_SNOW_SHOWERS",
            "MODERATE_TO_HEAVY_SNOW",
            "HEAVY_SNOW",
            "SNOW_PERIODICALLY_HEAVY",
            "BLOWING_SNOW" ->
                75

            "SNOWSTORM",
            "HEAVY_SNOW_STORM" ->
                77

            "HAIL",
            "HAIL_SHOWERS" ->
                82

            "THUNDERSTORM",
            "THUNDERSHOWER",
            "LIGHT_THUNDERSTORM_RAIN",
            "SCATTERED_THUNDERSTORMS" ->
                95

            "HEAVY_THUNDERSTORM" ->
                99

            else ->
                3
        }
    }

    private fun WeatherEntity.toDomain(
        isFromCache: Boolean
    ): Weather {
        return Weather(
            latitude =
            latitude,

            longitude =
            longitude,

            timezone =
            timezone,

            temperature =
            temperature,

            apparentTemperature =
            apparentTemperature,

            humidity =
            humidity,

            windSpeed =
            windSpeed,

            weatherCode =
            weatherCode,

            conditionType =
            conditionType,

            conditionDescription =
            conditionDescription,

            iconBaseUri =
            iconBaseUri,

            isDay =
            isDay,

            observationTime =
            observationTime,

            savedAt =
            savedAt,

            isFromCache =
            isFromCache
        )
    }

    private companion object {

        const val MAXIMUM_CACHE_AGE_MILLISECONDS =
            30L * 60L * 1000L

        const val MAXIMUM_COORDINATE_DIFFERENCE =
            0.05
    }
}