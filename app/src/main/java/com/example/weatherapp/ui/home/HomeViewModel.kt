package com.example.weatherapp.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.domain.model.DeviceLocation
import com.example.weatherapp.domain.repository.WeatherRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HomeViewModel(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState =
        MutableLiveData(
            HomeUiState()
        )

    val uiState:
            LiveData<HomeUiState> =
        _uiState

    fun loadCachedWeather() {
        viewModelScope.launch {
            val cachedWeather =
                weatherRepository
                    .getCachedWeather()

            if (cachedWeather != null) {
                val currentState =
                    _uiState.value
                        ?: HomeUiState()

                _uiState.value =
                    currentState.copy(
                        isLoading = false,
                        weather = cachedWeather,
                        errorMessage = null
                    )
            }
        }
    }

    fun loadWeather(
        location: DeviceLocation
    ) {
        val currentState =
            _uiState.value
                ?: HomeUiState()

        _uiState.value =
            currentState.copy(
                isLoading = true,
                location = location,
                errorMessage = null,
                forecastErrorMessage = null
            )

        viewModelScope.launch {
            /*
             * Run both Google Weather requests in parallel.
             * This is faster than waiting for one before
             * starting the other.
             */
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

            val dailyForecastResult =
                dailyForecastRequest.await()

            val latestState =
                _uiState.value
                    ?: HomeUiState()

            val currentWeather =
                currentWeatherResult
                    .getOrNull()

            val dailyForecast =
                dailyForecastResult
                    .getOrNull()

            val currentWeatherError =
                currentWeatherResult
                    .exceptionOrNull()

            val dailyForecastError =
                dailyForecastResult
                    .exceptionOrNull()

            Log.d(
                "WeeklyForecast",
                "Forecast count: ${dailyForecast?.size ?: 0}"
            )

            if (dailyForecastError != null) {
                Log.e(
                    "WeeklyForecast",
                    "Unable to load weekly forecast",
                    dailyForecastError
                )
            }

            _uiState.value =
                latestState.copy(
                    isLoading = false,
                    location = location,

                    weather =
                    currentWeather
                        ?: latestState.weather,

                    dailyForecast =
                    dailyForecast
                        ?: latestState.dailyForecast,

                    errorMessage =
                    if (currentWeather == null) {
                        currentWeatherError
                            ?.message
                            ?: "Unable to retrieve current weather."
                    } else {
                        null
                    },

                    forecastErrorMessage =
                    if (dailyForecast == null) {
                        dailyForecastError
                            ?.message
                            ?: "Unable to retrieve weekly forecast."
                    } else {
                        null
                    }
                )
        }
    }
}