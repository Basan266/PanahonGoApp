package com.example.weatherapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.domain.model.DailyForecast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DailyForecastAdapter :
    ListAdapter<
            DailyForecast,
            DailyForecastAdapter.DailyForecastViewHolder
            >(DailyForecastDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DailyForecastViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_daily_forecast,
                    parent,
                    false
                )

        return DailyForecastViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: DailyForecastViewHolder,
        position: Int
    ) {
        holder.bind(
            forecast = getItem(position)
        )
    }

    class DailyForecastViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvForecastDay: TextView =
            itemView.findViewById(
                R.id.tvForecastDay
            )

        private val ivForecastIcon: ImageView =
            itemView.findViewById(
                R.id.ivForecastIcon
            )

        private val tvForecastCondition: TextView =
            itemView.findViewById(
                R.id.tvForecastCondition
            )

        private val tvForecastMaximum: TextView =
            itemView.findViewById(
                R.id.tvForecastMaximum
            )

        private val tvForecastMinimum: TextView =
            itemView.findViewById(
                R.id.tvForecastMinimum
            )

        fun bind(
            forecast: DailyForecast
        ) {
            tvForecastDay.text =
                getDayLabel(
                    forecast
                )

            tvForecastCondition.text =
                forecast.conditionDescription
                    .ifBlank {
                        "Weather"
                    }

            tvForecastMaximum.text =
                String.format(
                    Locale.getDefault(),
                    "%.0f°",
                    forecast.maximumTemperature
                )

            tvForecastMinimum.text =
                String.format(
                    Locale.getDefault(),
                    "%.0f°",
                    forecast.minimumTemperature
                )

            ivForecastIcon.setImageResource(
                getWeatherIcon(
                    forecast.conditionType
                )
            )
        }

        @DrawableRes
        private fun getWeatherIcon(
            conditionType: String
        ): Int {
            return when (
                conditionType
                    .trim()
                    .uppercase(Locale.US)
            ) {
                "CLEAR",
                "MOSTLY_CLEAR" ->
                    R.drawable.ic_weather_sunny_outline

                "PARTLY_CLOUDY" ->
                    R.drawable.ic_weather_partly_cloudy_outline

                "MOSTLY_CLOUDY",
                "CLOUDY" ->
                    R.drawable.ic_weather_cloudy_outline

                "WINDY" ->
                    R.drawable.ic_weather_windy_outline

                "WIND_AND_RAIN",
                "HEAVY_RAIN",
                "HEAVY_RAIN_SHOWERS",
                "MODERATE_TO_HEAVY_RAIN",
                "RAIN_PERIODICALLY_HEAVY" ->
                    R.drawable.ic_weather_heavy_rain_outline

                "LIGHT_RAIN_SHOWERS",
                "CHANCE_OF_SHOWERS",
                "SCATTERED_SHOWERS",
                "RAIN_SHOWERS",
                "LIGHT_TO_MODERATE_RAIN",
                "RAIN",
                "LIGHT_RAIN" ->
                    R.drawable.ic_weather_rain_outline

                "THUNDERSTORM",
                "THUNDERSHOWER",
                "LIGHT_THUNDERSTORM_RAIN",
                "SCATTERED_THUNDERSTORMS",
                "HEAVY_THUNDERSTORM" ->
                    R.drawable.ic_weather_thunderstorm_outline

                "LIGHT_SNOW_SHOWERS",
                "CHANCE_OF_SNOW_SHOWERS",
                "SCATTERED_SNOW_SHOWERS",
                "SNOW_SHOWERS",
                "HEAVY_SNOW_SHOWERS",
                "LIGHT_TO_MODERATE_SNOW",
                "MODERATE_TO_HEAVY_SNOW",
                "SNOW",
                "LIGHT_SNOW",
                "HEAVY_SNOW",
                "SNOWSTORM",
                "SNOW_PERIODICALLY_HEAVY",
                "HEAVY_SNOW_STORM",
                "BLOWING_SNOW",
                "RAIN_AND_SNOW",
                "HAIL",
                "HAIL_SHOWERS" ->
                    R.drawable.ic_weather_snow_outline

                else ->
                    R.drawable.ic_weather_fog_outline
            }
        }

        private fun getDayLabel(
            forecast: DailyForecast
        ): String {
            val forecastCalendar =
                Calendar.getInstance().apply {
                    set(
                        forecast.year,
                        forecast.month - 1,
                        forecast.day,
                        0,
                        0,
                        0
                    )

                    set(
                        Calendar.MILLISECOND,
                        0
                    )
                }

            val today =
                Calendar.getInstance()

            val isToday =
                forecastCalendar.get(Calendar.YEAR) ==
                        today.get(Calendar.YEAR) &&
                        forecastCalendar.get(
                            Calendar.DAY_OF_YEAR
                        ) ==
                        today.get(
                            Calendar.DAY_OF_YEAR
                        )

            return if (isToday) {
                "TODAY"
            } else {
                SimpleDateFormat(
                    "EEE",
                    Locale.getDefault()
                ).format(
                    forecastCalendar.time
                ).uppercase(
                    Locale.getDefault()
                )
            }
        }
    }

    private class DailyForecastDiffCallback :
        DiffUtil.ItemCallback<DailyForecast>() {

        override fun areItemsTheSame(
            oldItem: DailyForecast,
            newItem: DailyForecast
        ): Boolean {
            return oldItem.year ==
                    newItem.year &&
                    oldItem.month ==
                    newItem.month &&
                    oldItem.day ==
                    newItem.day
        }

        override fun areContentsTheSame(
            oldItem: DailyForecast,
            newItem: DailyForecast
        ): Boolean {
            return oldItem == newItem
        }
    }
}