package com.example.weatherapp.data.remote.route

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ModeRouteClient {

    private const val FOOT_BASE_URL =
        "https://routing.openstreetmap.de/routed-foot/"

    private const val BICYCLE_BASE_URL =
        "https://routing.openstreetmap.de/routed-bike/"

    private const val MOTOR_BASE_URL =
        "https://routing.openstreetmap.de/routed-car/"

    private val httpClient =
        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(
                30,
                TimeUnit.SECONDS
            )
            .readTimeout(
                40,
                TimeUnit.SECONDS
            )
            .writeTimeout(
                30,
                TimeUnit.SECONDS
            )
            .addInterceptor { chain ->
                val request =
                    chain.request()
                        .newBuilder()
                        .header(
                            "User-Agent",
                            "WeatherApp/1.0 " +
                                    "(com.example.weatherapp)"
                        )
                        .build()

                chain.proceed(request)
            }
            .build()

    private val walkingApi: OsrmRouteApi by lazy {
        createApi(FOOT_BASE_URL)
    }

    private val bicycleApi: OsrmRouteApi by lazy {
        createApi(BICYCLE_BASE_URL)
    }

    private val motorcycleApi: OsrmRouteApi by lazy {
        createApi(MOTOR_BASE_URL)
    }

    fun getApi(
        costing: String
    ): OsrmRouteApi {
        return when (costing) {
            "pedestrian" ->
                walkingApi

            "bicycle" ->
                bicycleApi

            "motorcycle" ->
                motorcycleApi

            else ->
                walkingApi
        }
    }

    private fun createApi(
        baseUrl: String
    ): OsrmRouteApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(
                OsrmRouteApi::class.java
            )
    }
}