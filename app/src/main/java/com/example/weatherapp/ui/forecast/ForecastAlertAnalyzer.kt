package com.example.weatherapp.ui.forecast

import com.example.weatherapp.domain.model.HourlyForecast
import java.util.Locale

data class ForecastAlert(

    val icon: String,

    val title: String,

    val message: String
)

object ForecastAlertAnalyzer {

    fun analyze(
        hourlyForecast: List<HourlyForecast>
    ): ForecastAlert? {

        val forecast =
            hourlyForecast.take(24)

        if (forecast.isEmpty()) {
            return null
        }

        findThunderstormAlert(
            forecast
        )?.let {
            return it
        }

        findHeavyRainAlert(
            forecast
        )?.let {
            return it
        }

        findExtremeHeatAlert(
            forecast
        )?.let {
            return it
        }

        findStrongWindAlert(
            forecast
        )?.let {
            return it
        }

        findLowVisibilityAlert(
            forecast
        )?.let {
            return it
        }

        return null
    }

    private fun findThunderstormAlert(
        forecast: List<HourlyForecast>
    ): ForecastAlert? {

        val affectedHour =
            forecast.firstOrNull {
                it.thunderstormProbability >= 40 ||
                        it.conditionType
                            .uppercase(
                                Locale.US
                            )
                            .contains(
                                "THUNDER"
                            )
            } ?: return null

        return ForecastAlert(
            icon = "⛈️",

            title =
            "Thunderstorm warning",

            message =
            "Thunderstorms may develop around " +
                    formatHour(
                        affectedHour.hour
                    ) +
                    ". Avoid exposed outdoor areas."
        )
    }

    private fun findHeavyRainAlert(
        forecast: List<HourlyForecast>
    ): ForecastAlert? {

        val affectedHour =
            forecast.firstOrNull {
                it.rainChance >= 80 ||
                        it.conditionType
                            .uppercase(
                                Locale.US
                            )
                            .contains(
                                "HEAVY_RAIN"
                            )
            } ?: return null

        return ForecastAlert(
            icon = "🌧️",

            title =
            "Heavy rain possible",

            message =
            "Heavy rainfall may occur around " +
                    formatHour(
                        affectedHour.hour
                    ) +
                    ". Plan outdoor travel carefully."
        )
    }

    private fun findExtremeHeatAlert(
        forecast: List<HourlyForecast>
    ): ForecastAlert? {

        val affectedHour =
            forecast.maxByOrNull {
                it.feelsLikeTemperature
            } ?: return null

        if (
            affectedHour.feelsLikeTemperature <
            40.0
        ) {
            return null
        }

        return ForecastAlert(
            icon = "🌡️",

            title =
            "Extreme heat risk",

            message =
            String.format(
                Locale.getDefault(),
                "It may feel like %.0f°C around %s. Avoid prolonged sun exposure and stay hydrated.",
                affectedHour.feelsLikeTemperature,
                formatHour(
                    affectedHour.hour
                )
            )
        )
    }

    private fun findStrongWindAlert(
        forecast: List<HourlyForecast>
    ): ForecastAlert? {

        val affectedHour =
            forecast.firstOrNull {
                it.windSpeed >= 40.0
            } ?: return null

        return ForecastAlert(
            icon = "💨",

            title =
            "Strong wind expected",

            message =
            String.format(
                Locale.getDefault(),
                "Winds may reach %.0f km/h around %s. Use caution during outdoor activities.",
                affectedHour.windSpeed,
                formatHour(
                    affectedHour.hour
                )
            )
        )
    }

    private fun findLowVisibilityAlert(
        forecast: List<HourlyForecast>
    ): ForecastAlert? {

        val affectedHour =
            forecast.firstOrNull {
                it.visibility > 0.0 &&
                        it.visibility <= 1.0
            } ?: return null

        return ForecastAlert(
            icon = "🌫️",

            title =
            "Low visibility possible",

            message =
            "Visibility may be reduced around " +
                    formatHour(
                        affectedHour.hour
                    ) +
                    ". Take extra care while traveling."
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