package com.example.weatherapp.ui.forecast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.domain.model.DeviceLocation
import com.example.weatherapp.domain.repository.WeatherRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ForecastViewModel(
    private val weatherRepository:
    WeatherRepository
) : ViewModel() {

    private val _uiState =
        MutableLiveData(
            ForecastUiState()
        )

    val uiState:
            LiveData<ForecastUiState> =
        _uiState

    fun loadForecast(
        location: DeviceLocation,
        isManualRefresh: Boolean = false
    ) {
        val currentState =
            _uiState.value
                ?: ForecastUiState()

        val alreadyHasData =
            currentState.currentWeather != null ||
                    currentState.hourlyForecast.isNotEmpty() ||
                    currentState.dailyForecast.isNotEmpty()

        _uiState.value =
            currentState.copy(
                isLoading =
                !alreadyHasData,

                isRefreshing =
                isManualRefresh && alreadyHasData,

                location =
                location,

                errorMessage =
                null,

                hourlyErrorMessage =
                null,

                dailyErrorMessage =
                null
            )

        viewModelScope.launch {
            val currentWeatherRequest =
                async {
                    weatherRepository
                        .getCurrentWeather(
                            latitude =
                            location.latitude,

                            longitude =
                            location.longitude
                        )
                }

            val hourlyForecastRequest =
                async {
                    weatherRepository
                        .getHourlyForecast(
                            latitude =
                            location.latitude,

                            longitude =
                            location.longitude
                        )
                }

            val dailyForecastRequest =
                async {
                    weatherRepository
                        .getDailyForecast(
                            latitude =
                            location.latitude,

                            longitude =
                            location.longitude
                        )
                }

            val currentWeatherResult =
                currentWeatherRequest.await()

            val hourlyForecastResult =
                hourlyForecastRequest.await()

            val dailyForecastResult =
                dailyForecastRequest.await()

            val latestState =
                _uiState.value
                    ?: ForecastUiState()

            val currentWeather =
                currentWeatherResult
                    .getOrNull()

            val hourlyForecast =
                hourlyForecastResult
                    .getOrNull()

            val dailyForecast =
                dailyForecastResult
                    .getOrNull()

            val hasSuccessfulResult =
                currentWeather != null ||
                        !hourlyForecast.isNullOrEmpty() ||
                        !dailyForecast.isNullOrEmpty()

            _uiState.value =
                latestState.copy(
                    isLoading =
                    false,

                    isRefreshing =
                    false,

                    location =
                    location,

                    currentWeather =
                    currentWeather
                        ?: latestState.currentWeather,

                    hourlyForecast =
                    if (
                        !hourlyForecast.isNullOrEmpty()
                    ) {
                        hourlyForecast
                    } else {
                        latestState.hourlyForecast
                    },

                    dailyForecast =
                    if (
                        !dailyForecast.isNullOrEmpty()
                    ) {
                        dailyForecast
                    } else {
                        latestState.dailyForecast
                    },

                    errorMessage =
                    if (currentWeather == null) {
                        currentWeatherResult
                            .exceptionOrNull()
                            ?.message
                            ?: "Unable to retrieve current weather."
                    } else {
                        null
                    },

                    hourlyErrorMessage =
                    if (
                        hourlyForecast.isNullOrEmpty()
                    ) {
                        hourlyForecastResult
                            .exceptionOrNull()
                            ?.message
                            ?: "Unable to retrieve hourly forecast."
                    } else {
                        null
                    },

                    dailyErrorMessage =
                    if (
                        dailyForecast.isNullOrEmpty()
                    ) {
                        dailyForecastResult
                            .exceptionOrNull()
                            ?.message
                            ?: "Unable to retrieve seven-day forecast."
                    } else {
                        null
                    },

                    lastUpdatedAt =
                    if (hasSuccessfulResult) {
                        System.currentTimeMillis()
                    } else {
                        latestState.lastUpdatedAt
                    }
                )
        }
    }

    fun clearErrors() {
        val currentState =
            _uiState.value
                ?: return

        _uiState.value =
            currentState.copy(
                errorMessage = null,
                hourlyErrorMessage = null,
                dailyErrorMessage = null
            )
    }
}