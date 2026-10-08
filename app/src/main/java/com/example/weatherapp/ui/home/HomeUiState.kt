package com.example.weatherapp.ui.home

import com.example.weatherapp.domain.model.DailyForecast
import com.example.weatherapp.domain.model.DeviceLocation
import com.example.weatherapp.domain.model.Weather

data class HomeUiState(
    val isLoading: Boolean = false,

    val location: DeviceLocation? = null,

    val weather: Weather? = null,

    val dailyForecast: List<DailyForecast> =
        emptyList(),

    val errorMessage: String? = null,

    val forecastErrorMessage: String? = null
)