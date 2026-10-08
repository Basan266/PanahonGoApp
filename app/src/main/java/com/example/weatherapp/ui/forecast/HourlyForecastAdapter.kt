package com.example.weatherapp.ui.forecast

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
import java.util.Locale
import kotlin.math.roundToInt

class HourlyForecastAdapter :
    ListAdapter<
            HourlyForecastItem,
            HourlyForecastAdapter.HourlyForecastViewHolder
            >(HourlyForecastDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HourlyForecastViewHolder {
        val view =
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.item_hourly_forecast,
                parent,
                false
            )

        return HourlyForecastViewHolder(
            view
        )
    }

    override fun onBindViewHolder(
        holder: HourlyForecastViewHolder,
        position: Int
    ) {
        holder.bind(
            item = getItem(position),
            isFirstItem = position == 0
        )
    }

    class HourlyForecastViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(
        itemView
    ) {

        private val tvHourlyTime =
            itemView.findViewById<TextView>(
                R.id.tvHourlyTime
            )

        private val ivHourlyWeatherIcon =
            itemView.findViewById<ImageView>(
                R.id.ivHourlyWeatherIcon
            )

        private val tvHourlyTemperature =
            itemView.findViewById<TextView>(
                R.id.tvHourlyTemperature
            )

        private val tvHourlyRainChance =
            itemView.findViewById<TextView>(
                R.id.tvHourlyRainChance
            )

        fun bind(
            item: HourlyForecastItem,
            isFirstItem: Boolean
        ) {
            tvHourlyTime.text =
                if (isFirstItem) {
                    "Now"
                } else {
                    item.time
                }

            ivHourlyWeatherIcon.setImageResource(
                getWeatherIcon(
                    conditionType =
                    item.conditionType
                )
            )

            ivHourlyWeatherIcon.contentDescription =
                item.condition

            tvHourlyTemperature.text =
                String.format(
                    Locale.getDefault(),
                    "%d°",
                    item.temperature
                        .roundToInt()
                )

            tvHourlyRainChance.text =
                "${item.rainChance.coerceIn(0, 100)}%"
        }

        @DrawableRes
        private fun getWeatherIcon(
            conditionType: String
        ): Int {
            return when (
                conditionType
                    .trim()
                    .uppercase(
                        Locale.US
                    )
            ) {
                "CLEAR",
                "MOSTLY_CLEAR" -> {
                    R.drawable
                        .ic_weather_sunny_outline
                }

                "PARTLY_CLOUDY" -> {
                    R.drawable
                        .ic_weather_partly_cloudy_outline
                }

                "MOSTLY_CLOUDY",
                "CLOUDY" -> {
                    R.drawable
                        .ic_weather_cloudy_outline
                }

                "WINDY" -> {
                    R.drawable
                        .ic_weather_windy_outline
                }

                "WIND_AND_RAIN",
                "HEAVY_RAIN",
                "HEAVY_RAIN_SHOWERS",
                "MODERATE_TO_HEAVY_RAIN",
                "RAIN_PERIODICALLY_HEAVY" -> {
                    R.drawable
                        .ic_weather_heavy_rain_outline
                }

                "LIGHT_RAIN_SHOWERS",
                "CHANCE_OF_SHOWERS",
                "SCATTERED_SHOWERS",
                "RAIN_SHOWERS",
                "LIGHT_TO_MODERATE_RAIN",
                "RAIN",
                "LIGHT_RAIN" -> {
                    R.drawable
                        .ic_weather_rain_outline
                }

                "THUNDERSTORM",
                "THUNDERSHOWER",
                "LIGHT_THUNDERSTORM_RAIN",
                "SCATTERED_THUNDERSTORMS",
                "HEAVY_THUNDERSTORM" -> {
                    R.drawable
                        .ic_weather_thunderstorm_outline
                }

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
                "HAIL_SHOWERS" -> {
                    R.drawable
                        .ic_weather_snow_outline
                }

                else -> {
                    R.drawable
                        .ic_weather_fog_outline
                }
            }
        }
    }

    private class HourlyForecastDiffCallback :
        DiffUtil.ItemCallback<HourlyForecastItem>() {

        override fun areItemsTheSame(
            oldItem: HourlyForecastItem,
            newItem: HourlyForecastItem
        ): Boolean {
            return oldItem.time ==
                    newItem.time
        }

        override fun areContentsTheSame(
            oldItem: HourlyForecastItem,
            newItem: HourlyForecastItem
        ): Boolean {
            return oldItem ==
                    newItem
        }
    }
}