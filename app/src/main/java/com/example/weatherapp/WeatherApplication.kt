package com.example.weatherapp

import android.app.Application
import com.example.weatherapp.data.local.database.WeatherDatabase
import com.example.weatherapp.data.remote.api.WeatherApiClient
import com.example.weatherapp.data.repository.WeatherRepositoryImpl
import com.example.weatherapp.domain.repository.WeatherRepository
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

class WeatherApplication : Application() {

    lateinit var weatherRepository: WeatherRepository
        private set

    override fun onCreate() {
        super.onCreate()

        /*
         * MapLibre must be initialized before Android inflates
         * any XML layout containing a MapView.
         */
        MapLibre.getInstance(
            this,
            null,
            WellKnownTileServer.MapLibre
        )

        val database =
            WeatherDatabase.getInstance(this)

        weatherRepository =
            WeatherRepositoryImpl(
                weatherApi = WeatherApiClient.create(this),
                weatherDao = database.weatherDao()
            )
    }
}