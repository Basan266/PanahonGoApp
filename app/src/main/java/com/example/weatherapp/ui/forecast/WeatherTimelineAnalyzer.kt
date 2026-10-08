package com.example.weatherapp.ui.forecast

import com.example.weatherapp.domain.model.HourlyForecast
import java.util.Locale

data class WeatherTimelineEvent(

    val icon: String,

    val title: String,

    val message: String
)

object WeatherTimelineAnalyzer {

    fun analyze(
        hourlyForecast: List<HourlyForecast>
    ): List<WeatherTimelineEvent> {

        if (hourlyForecast.isEmpty()) {
            return emptyList()
        }

        val forecast =
            hourlyForecast.take(24)

        val events =
            mutableListOf<WeatherTimelineEvent>()

        findRainBeginning(
            forecast
        )?.let {
            events.add(it)
        }

        findThunderstorm(
            forecast
        )?.let {
            events.add(it)
        }

        findHottestPeriod(
            forecast
        )?.let {
            events.add(it)
        }

        findImprovingConditions(
            forecast
        )?.let {
            events.add(it)
        }

        if (events.isEmpty()) {
            events.add(
                WeatherTimelineEvent(
                    icon = "✓",
                    title = "Stable conditions",
                    message =
                    "No major weather changes are expected during the next 24 hours."
                )
            )
        }

        return events.take(4)
    }

    private fun findRainBeginning(
        forecast: List<HourlyForecast>
    ): WeatherTimelineEvent? {

        val rainHour =
            forecast
                .windowed(
                    size = 2,
                    step = 1,
                    partialWindows = false
                )
                .firstOrNull { pair ->
                    val previous =
                        pair[0]

                    val current =
                        pair[1]

                    previous.rainChance < 50 &&
                            current.rainChance >= 50
                }
                ?.get(1)
                ?: return null

        return WeatherTimelineEvent(
            icon = "🌧️",
            title =
            "Rain possible around " +
                    formatHour(
                        rainHour.hour
                    ),

            message =
            "${rainHour.rainChance}% chance of rain. " +
                    "Consider bringing an umbrella."
        )
    }

    private fun findThunderstorm(
        forecast: List<HourlyForecast>
    ): WeatherTimelineEvent? {

        val thunderstormHour =
            forecast.firstOrNull {
                it.thunderstormProbability >= 30 ||
                        it.conditionType
                            .uppercase(
                                Locale.US
                            )
                            .contains(
                                "THUNDER"
                            )
            } ?: return null

        return WeatherTimelineEvent(
            icon = "⛈️",
            title =
            "Thunderstorm risk at " +
                    formatHour(
                        thunderstormHour.hour
                    ),

            message =
            "Avoid exposed outdoor areas if thunder or lightning develops."
        )
    }

    private fun findHottestPeriod(
        forecast: List<HourlyForecast>
    ): WeatherTimelineEvent? {

        val hottestHour =
            forecast.maxByOrNull {
                it.feelsLikeTemperature
            } ?: return null

        if (
            hottestHour.feelsLikeTemperature <
            32.0
        ) {
            return null
        }

        return WeatherTimelineEvent(
            icon = "🌡️",
            title =
            "Warmest around " +
                    formatHour(
                        hottestHour.hour
                    ),

            message =
            String.format(
                Locale.getDefault(),
                "It may feel like %.0f°C. Stay hydrated and limit prolonged sun exposure.",
                hottestHour.feelsLikeTemperature
            )
        )
    }

    private fun findImprovingConditions(
        forecast: List<HourlyForecast>
    ): WeatherTimelineEvent? {

        val improvingHour =
            forecast
                .windowed(
                    size = 2,
                    step = 1,
                    partialWindows = false
                )
                .firstOrNull { pair ->
                    val previous =
                        pair[0]

                    val current =
                        pair[1]

                    previous.rainChance >= 50 &&
                            current.rainChance <= 30
                }
                ?.get(1)
                ?: return null

        return WeatherTimelineEvent(
            icon = "🌤️",
            title =
            "Conditions improve after " +
                    formatHour(
                        improvingHour.hour
                    ),

            message =
            "Rain probability decreases and outdoor conditions may become more favorable."
        )
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