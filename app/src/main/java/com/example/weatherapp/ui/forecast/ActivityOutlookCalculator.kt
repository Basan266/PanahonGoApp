package com.example.weatherapp.ui.forecast

import com.example.weatherapp.domain.model.HourlyForecast
import java.util.Locale
import kotlin.math.abs

object ActivityOutlookCalculator {

    fun calculate(
        selectedActivities: List<String>,
        hourlyForecast: List<HourlyForecast>
    ): List<ActivityOutlookItem> {

        if (
            selectedActivities.isEmpty() ||
            hourlyForecast.isEmpty()
        ) {
            return emptyList()
        }

        val upcomingForecast =
            hourlyForecast
                .take(24)
                .filter {
                    it.isDaytime
                }

        if (upcomingForecast.isEmpty()) {
            return emptyList()
        }

        return selectedActivities.map {
                activity ->

            createActivityOutlook(
                activityName = activity,
                hourlyForecast = upcomingForecast
            )
        }
    }

    private fun createActivityOutlook(
        activityName: String,
        hourlyForecast: List<HourlyForecast>
    ): ActivityOutlookItem {

        val bestForecast =
            hourlyForecast.maxByOrNull {
                    forecast ->

                calculateScore(
                    activityName = activityName,
                    forecast = forecast
                )
            }

        if (bestForecast == null) {
            return ActivityOutlookItem(
                activityName = activityName,
                icon = getActivityIcon(activityName),
                status = ActivityOutlookStatus.AVOID,
                bestTime = "Unavailable",
                message =
                "Hourly forecast information is unavailable."
            )
        }

        val bestScore =
            calculateScore(
                activityName = activityName,
                forecast = bestForecast
            )

        val status =
            when {
                bestScore >= 70.0 ->
                    ActivityOutlookStatus.GOOD

                bestScore >= 35.0 ->
                    ActivityOutlookStatus.CAUTION

                else ->
                    ActivityOutlookStatus.AVOID
            }

        return ActivityOutlookItem(
            activityName = activityName,

            icon =
            getActivityIcon(
                activityName
            ),

            status =
            status,

            bestTime =
            if (
                status ==
                ActivityOutlookStatus.AVOID
            ) {
                "Avoid today"
            } else {
                "Best at " +
                        formatHour(
                            bestForecast.hour
                        )
            },

            message =
            createRecommendation(
                activityName = activityName,
                forecast = bestForecast,
                status = status
            )
        )
    }

    private fun calculateScore(
        activityName: String,
        forecast: HourlyForecast
    ): Double {

        var score =
            100.0

        score -=
            forecast.rainChance *
                    0.65

        score -=
            forecast
                .thunderstormProbability *
                    1.4

        if (
            forecast.conditionType
                .contains(
                    "THUNDER",
                    ignoreCase = true
                )
        ) {
            score -=
                60.0
        }

        if (
            forecast.conditionType
                .contains(
                    "HEAVY_RAIN",
                    ignoreCase = true
                )
        ) {
            score -=
                45.0
        }

        if (
            forecast.feelsLikeTemperature >
            31.0
        ) {
            score -=
                (
                        forecast
                            .feelsLikeTemperature -
                                31.0
                        ) * 6.0
        }

        if (
            forecast.humidity >
            80
        ) {
            score -=
                (
                        forecast.humidity -
                                80
                        ) * 0.8
        }

        if (
            forecast.visibility > 0.0 &&
            forecast.visibility < 3.0
        ) {
            score -=
                35.0
        }

        score +=
            calculateActivityAdjustment(
                activityName = activityName,
                forecast = forecast
            )

        return score.coerceIn(
            minimumValue = 0.0,
            maximumValue = 100.0
        )
    }

    private fun calculateActivityAdjustment(
        activityName: String,
        forecast: HourlyForecast
    ): Double {

        val normalizedActivity =
            activityName
                .trim()
                .lowercase(
                    Locale.US
                )

        return when (
            normalizedActivity
        ) {
            "running" -> {
                runningAdjustment(
                    forecast
                )
            }

            "cycling" -> {
                cyclingAdjustment(
                    forecast
                )
            }

            "gardening",
            "farming" -> {
                gardeningAdjustment(
                    forecast
                )
            }

            "golf" -> {
                golfAdjustment(
                    forecast
                )
            }

            "beach" -> {
                beachAdjustment(
                    forecast
                )
            }

            "hiking" -> {
                hikingAdjustment(
                    forecast
                )
            }

            "kids sports" -> {
                kidsSportsAdjustment(
                    forecast
                )
            }

            "dog walking" -> {
                dogWalkingAdjustment(
                    forecast
                )
            }

            "surfing" -> {
                surfingAdjustment(
                    forecast
                )
            }

            "sailing" -> {
                sailingAdjustment(
                    forecast
                )
            }

            "flying" -> {
                flyingAdjustment(
                    forecast
                )
            }

            else -> {
                generalAdjustment(
                    forecast
                )
            }
        }
    }

    private fun runningAdjustment(
        forecast: HourlyForecast
    ): Double {
        var adjustment =
            0.0

        adjustment -=
            maxOf(
                0.0,
                forecast.feelsLikeTemperature -
                        29.0
            ) * 7.0

        adjustment -=
            maxOf(
                0.0,
                forecast.windSpeed -
                        20.0
            ) * 2.0

        return adjustment
    }

    private fun cyclingAdjustment(
        forecast: HourlyForecast
    ): Double {
        var adjustment =
            0.0

        adjustment -=
            maxOf(
                0.0,
                forecast.windSpeed -
                        18.0
            ) * 3.0

        adjustment -=
            forecast.rainChance *
                    0.25

        return adjustment
    }

    private fun gardeningAdjustment(
        forecast: HourlyForecast
    ): Double {
        return -
        maxOf(
            0.0,
            forecast.feelsLikeTemperature -
                    32.0
        ) * 4.0
    }

    private fun golfAdjustment(
        forecast: HourlyForecast
    ): Double {
        return -
        maxOf(
            0.0,
            forecast.windSpeed -
                    20.0
        ) * 2.5
    }

    private fun beachAdjustment(
        forecast: HourlyForecast
    ): Double {
        var adjustment =
            0.0

        if (
            forecast.uvIndex in 2..6 &&
            forecast.rainChance <= 20
        ) {
            adjustment +=
                12.0
        }

        if (
            forecast.uvIndex >= 8
        ) {
            adjustment -=
                25.0
        }

        return adjustment
    }

    private fun hikingAdjustment(
        forecast: HourlyForecast
    ): Double {
        var adjustment =
            0.0

        adjustment -=
            forecast.rainChance *
                    0.30

        adjustment -=
            maxOf(
                0.0,
                forecast.feelsLikeTemperature -
                        30.0
            ) * 5.0

        return adjustment
    }

    private fun kidsSportsAdjustment(
        forecast: HourlyForecast
    ): Double {
        var adjustment =
            0.0

        if (
            forecast.feelsLikeTemperature >=
            33.0
        ) {
            adjustment -=
                35.0
        }

        if (
            forecast.uvIndex >= 8
        ) {
            adjustment -=
                25.0
        }

        return adjustment
    }

    private fun dogWalkingAdjustment(
        forecast: HourlyForecast
    ): Double {
        if (
            forecast.feelsLikeTemperature >=
            34.0
        ) {
            return -40.0
        }

        return 0.0
    }

    private fun surfingAdjustment(
        forecast: HourlyForecast
    ): Double {
        val preferredWind =
            15.0

        return -
        abs(
            forecast.windSpeed -
                    preferredWind
        ) * 1.5
    }

    private fun sailingAdjustment(
        forecast: HourlyForecast
    ): Double {
        return when {
            forecast.windSpeed in
                    10.0..25.0 -> {
                10.0
            }

            forecast.windSpeed >
                    35.0 -> {
                -45.0
            }

            else -> {
                -10.0
            }
        }
    }

    private fun flyingAdjustment(
        forecast: HourlyForecast
    ): Double {
        var adjustment =
            0.0

        if (
            forecast.rainChance >= 30
        ) {
            adjustment -=
                35.0
        }

        if (
            forecast.windSpeed >= 25.0
        ) {
            adjustment -=
                40.0
        }

        if (
            forecast.visibility > 0.0 &&
            forecast.visibility < 8.0
        ) {
            adjustment -=
                50.0
        }

        return adjustment
    }

    private fun generalAdjustment(
        forecast: HourlyForecast
    ): Double {
        return -
        maxOf(
            0.0,
            forecast.windSpeed -
                    25.0
        ) * 2.0
    }

    private fun createRecommendation(
        activityName: String,
        forecast: HourlyForecast,
        status: ActivityOutlookStatus
    ): String {

        if (
            status ==
            ActivityOutlookStatus.AVOID
        ) {
            return when {
                forecast
                    .thunderstormProbability >=
                        30 -> {
                    "Thunderstorms may make this activity unsafe."
                }

                forecast.rainChance >=
                        60 -> {
                    "Rain is likely during the available hours."
                }

                forecast
                    .feelsLikeTemperature >=
                        35.0 -> {
                    "Extreme heat may make this activity unsafe."
                }

                forecast.windSpeed >=
                        35.0 -> {
                    "Strong winds may make this activity unsafe."
                }

                else -> {
                    "Weather conditions are not favorable today."
                }
            }
        }

        if (
            status ==
            ActivityOutlookStatus.GOOD
        ) {
            return when {
                forecast.rainChance <=
                        15 &&
                        forecast.windSpeed <=
                        15.0 -> {
                    "Low rain chance and light winds are expected."
                }

                forecast
                    .feelsLikeTemperature <=
                        30.0 -> {
                    "Comfortable temperature is expected."
                }

                else -> {
                    "This is the most favorable period available."
                }
            }
        }

        return when {
            forecast.rainChance >=
                    35 -> {
                "Possible rain. Bring appropriate protection."
            }

            forecast
                .feelsLikeTemperature >=
                    32.0 -> {
                "Warm conditions expected. Stay hydrated."
            }

            forecast.windSpeed >=
                    25.0 -> {
                "Moderate winds may affect this activity."
            }

            forecast.uvIndex >=
                    7 -> {
                "High UV exposure. Use sun protection."
            }

            else -> {
                "Conditions are acceptable, but proceed with caution."
            }
        }
    }

    private fun getActivityIcon(
        activityName: String
    ): String {
        return when (
            activityName
                .trim()
                .lowercase(
                    Locale.US
                )
        ) {
            "running" ->
                "🏃"

            "cycling" ->
                "🚴"

            "gardening" ->
                "🌱"

            "golf" ->
                "🏌️"

            "beach" ->
                "🏖️"

            "hiking" ->
                "🥾"

            "kids sports" ->
                "⚽"

            "dog walking" ->
                "🐕"

            "surfing" ->
                "🏄"

            "sailing" ->
                "⛵"

            "farming" ->
                "🚜"

            "flying" ->
                "✈️"

            else ->
                "🏃"
        }
    }

    private fun formatHour(
        hour: Int
    ): String {
        return when {
            hour == 0 ->
                "12:00 AM"

            hour < 12 ->
                "$hour:00 AM"

            hour == 12 ->
                "12:00 PM"

            else ->
                "${hour - 12}:00 PM"
        }
    }
}