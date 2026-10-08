package com.example.weatherapp.ui.forecast

import com.example.weatherapp.domain.model.HourlyForecast

data class OutdoorWindowResult(

    val startHour: Int?,

    val endHour: Int?,

    val title: String,

    val reason: String,

    val isAvailable: Boolean
)

object OutdoorWindowCalculator {

    fun calculate(
        hourlyForecast: List<HourlyForecast>
    ): OutdoorWindowResult {

        if (hourlyForecast.isEmpty()) {
            return OutdoorWindowResult(
                startHour = null,
                endHour = null,
                title = "Forecast unavailable",
                reason =
                "Hourly weather data is not available.",
                isAvailable = false
            )
        }

        val safeHours =
            hourlyForecast
                .take(24)
                .filter { forecast ->
                    isSuitableForOutdoorActivity(
                        forecast
                    )
                }

        if (safeHours.isEmpty()) {
            return OutdoorWindowResult(
                startHour = null,
                endHour = null,
                title = "No ideal outdoor window",
                reason =
                "Rain, heat, wind, UV, or thunderstorms may make outdoor activities uncomfortable today.",
                isAvailable = false
            )
        }

        val consecutiveGroups =
            createConsecutiveGroups(
                safeHours
            )

        val bestGroup =
            consecutiveGroups
                .maxWithOrNull(
                    compareBy<List<HourlyForecast>> {
                        it.size
                    }.thenByDescending {
                        calculateGroupScore(it)
                    }
                )
                .orEmpty()

        if (bestGroup.isEmpty()) {
            return OutdoorWindowResult(
                startHour = null,
                endHour = null,
                title = "No ideal outdoor window",
                reason =
                "No suitable outdoor period was found.",
                isAvailable = false
            )
        }

        val startHour =
            bestGroup.first().hour

        val endHour =
            (bestGroup.last().hour + 1) % 24

        val averageRain =
            bestGroup
                .map {
                    it.rainChance
                }
                .average()
                .toInt()

        val averageFeelsLike =
            bestGroup
                .map {
                    it.feelsLikeTemperature
                }
                .average()

        val averageWind =
            bestGroup
                .map {
                    it.windSpeed
                }
                .average()

        val reason =
            createReason(
                averageRain = averageRain,
                averageFeelsLike =
                averageFeelsLike,
                averageWind = averageWind
            )

        return OutdoorWindowResult(
            startHour = startHour,
            endHour = endHour,

            title =
            "${formatHour(startHour)} – " +
                    formatHour(endHour),

            reason =
            reason,

            isAvailable = true
        )
    }

    private fun isSuitableForOutdoorActivity(
        forecast: HourlyForecast
    ): Boolean {
        val condition =
            forecast.conditionType
                .uppercase()

        val dangerousCondition =
            condition.contains(
                "THUNDER"
            ) ||
                    condition.contains(
                        "HEAVY_RAIN"
                    ) ||
                    condition.contains(
                        "SNOWSTORM"
                    ) ||
                    condition.contains(
                        "HAIL"
                    )

        return forecast.isDaytime &&
                !dangerousCondition &&
                forecast.rainChance <= 35 &&
                forecast.thunderstormProbability <= 20 &&
                forecast.feelsLikeTemperature <= 34.0 &&
                forecast.windSpeed <= 25.0 &&
                forecast.uvIndex <= 7 &&
                (
                        forecast.visibility <= 0.0 ||
                                forecast.visibility >= 3.0
                        )
    }

    private fun createConsecutiveGroups(
        safeHours: List<HourlyForecast>
    ): List<List<HourlyForecast>> {
        if (safeHours.isEmpty()) {
            return emptyList()
        }

        val groups =
            mutableListOf<
                    MutableList<HourlyForecast>
                    >()

        safeHours.forEach { forecast ->
            val currentGroup =
                groups.lastOrNull()

            val previousForecast =
                currentGroup
                    ?.lastOrNull()

            val isConsecutive =
                previousForecast != null &&
                        isNextHour(
                            previous =
                            previousForecast,

                            current =
                            forecast
                        )

            if (
                currentGroup == null ||
                !isConsecutive
            ) {
                groups.add(
                    mutableListOf(
                        forecast
                    )
                )
            } else {
                currentGroup.add(
                    forecast
                )
            }
        }

        return groups
    }

    private fun isNextHour(
        previous: HourlyForecast,
        current: HourlyForecast
    ): Boolean {
        val expectedNextHour =
            (previous.hour + 1) % 24

        val sameDate =
            previous.year == current.year &&
                    previous.month == current.month &&
                    previous.day == current.day

        val nextDate =
            previous.hour == 23 &&
                    current.hour == 0

        return current.hour ==
                expectedNextHour &&
                (
                        sameDate ||
                                nextDate
                        )
    }

    private fun calculateGroupScore(
        group: List<HourlyForecast>
    ): Double {
        if (group.isEmpty()) {
            return 0.0
        }

        return group.sumOf { forecast ->
            100.0 -
                    forecast.rainChance -
                    forecast
                        .thunderstormProbability -
                    (
                            forecast.windSpeed *
                                    0.8
                            ) -
                    (
                            forecast.uvIndex *
                                    2.0
                            ) -
                    (
                            maxOf(
                                0.0,
                                forecast
                                    .feelsLikeTemperature -
                                        27.0
                            ) * 3.0
                            )
        } / group.size
    }

    private fun createReason(
        averageRain: Int,
        averageFeelsLike: Double,
        averageWind: Double
    ): String {
        return when {
            averageRain <= 10 &&
                    averageFeelsLike <= 30.0 &&
                    averageWind <= 15.0 -> {
                "Low rain chance, comfortable temperature, and light winds are expected."
            }

            averageRain <= 20 &&
                    averageFeelsLike <= 32.0 -> {
                "Generally comfortable conditions with a low chance of rain."
            }

            averageFeelsLike > 32.0 -> {
                "Conditions are acceptable, but stay hydrated because it may feel warm."
            }

            averageWind > 20.0 -> {
                "Conditions are acceptable, but moderate winds are possible."
            }

            else -> {
                "This period has the most favorable weather conditions available."
            }
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