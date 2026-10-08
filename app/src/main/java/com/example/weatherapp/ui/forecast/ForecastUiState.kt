package com.example.weatherapp.ui.forecast

import com.example.weatherapp.domain.model.DailyForecast
import com.example.weatherapp.domain.model.DeviceLocation
import com.example.weatherapp.domain.model.HourlyForecast
import com.example.weatherapp.domain.model.Weather

data class ForecastUiState(

    val isLoading: Boolean = false,

    val isRefreshing: Boolean = false,

    val location: DeviceLocation? = null,

    val currentWeather: Weather? = null,

    val hourlyForecast:
    List<HourlyForecast> =
        emptyList(),

    val dailyForecast:
    List<DailyForecast> =
        emptyList(),

    val errorMessage: String? = null,

    val hourlyErrorMessage: String? = null,

    val dailyErrorMessage: String? = null,

    val lastUpdatedAt: Long? = null
)