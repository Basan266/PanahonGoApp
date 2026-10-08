package com.example.weatherapp.ui.forecast

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.domain.model.DailyForecast
import com.example.weatherapp.domain.model.HourlyForecast
import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.ui.home.DailyForecastAdapter
import com.example.weatherapp.util.LocationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import java.util.TimeZone


class ForecastFragment :
    Fragment(R.layout.fragment_forecast) {


    private lateinit var cardWeatherAlert:
            MaterialCardView

    private lateinit var tvAlertIcon:
            TextView

    private lateinit var tvAlertTitle:
            TextView

    private lateinit var tvAlertMessage:
            TextView


    private lateinit var layoutWeatherTimeline:
            LinearLayout

    private lateinit var tvWeatherTimelineEmpty:
            TextView

    private lateinit var locationHelper:
            LocationHelper

    private lateinit var tvForecastLocation:
            TextView

    private lateinit var tvForecastUpdated:
            TextView

    private lateinit var btnRefreshForecast:
            ImageButton

    private lateinit var progressForecast:
            ProgressBar

    private lateinit var tvForecastError:
            TextView

    private lateinit var tvForecastDate:
            TextView

    private lateinit var tvCurrentTemperature:
            TextView

    private lateinit var tvCurrentCondition:
            TextView

    private lateinit var tvTodayHighLow:
            TextView

    private lateinit var ivCurrentWeatherIcon:
            ImageView

    private lateinit var rvActivityOutlook:
            RecyclerView

    private lateinit var rvDailyForecast:
            RecyclerView

    private lateinit var tvOutdoorWindowTime:
            TextView

    private lateinit var tvOutdoorWindowReason:
            TextView

    private lateinit var tvDetailFeelsLike:
            TextView

    private lateinit var tvDetailHumidity:
            TextView

    private lateinit var tvDetailWind:
            TextView

    private lateinit var tvDetailVisibility:
            TextView

    private lateinit var tvDetailUvIndex:
            TextView

    private lateinit var tvDetailSunriseSunset:
            TextView

    private val dailyForecastAdapter =
        DailyForecastAdapter()

    private var manualRefreshRequested =
        false

    private val weatherRepository by lazy {
        val application =
            requireActivity().application
                    as WeatherApplication

        application.weatherRepository
    }

    private val viewModel:
            ForecastViewModel by viewModels {

        ForecastViewModelFactory(
            weatherRepository =
            weatherRepository
        )
    }

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts
                .RequestMultiplePermissions()
        ) { permissions ->

            if (!isAdded || view == null) {
                return@registerForActivityResult
            }

            val fineLocationAllowed =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true

            if (fineLocationAllowed) {
                retrieveLocation()
            } else {
                displayLocationError(
                    "Precise location permission is required."
                )
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        locationHelper =
            LocationHelper(
                requireContext()
            )

        bindViews(view)
        configureNormalScreen(view)
        configureDailyForecast()
        configureRefreshButton()
        observeForecast()

        checkLocationPermission()
    }

    private fun bindViews(
        view: View
    ) {

        cardWeatherAlert =
            view.findViewById(
                R.id.cardWeatherAlert
            )

        tvAlertIcon =
            view.findViewById(
                R.id.tvAlertIcon
            )

        tvAlertTitle =
            view.findViewById(
                R.id.tvAlertTitle
            )

        tvAlertMessage =
            view.findViewById(
                R.id.tvAlertMessage
            )


        layoutWeatherTimeline =
            view.findViewById(
                R.id.layoutWeatherTimeline
            )

        tvWeatherTimelineEmpty =
            view.findViewById(
                R.id.tvWeatherTimelineEmpty
            )



        tvForecastUpdated =
            view.findViewById(
                R.id.tvForecastUpdated
            )

        btnRefreshForecast =
            view.findViewById(
                R.id.btnRefreshForecast
            )

        progressForecast =
            view.findViewById(
                R.id.progressForecast
            )

        tvForecastError =
            view.findViewById(
                R.id.tvForecastError
            )


        rvDailyForecast =
            view.findViewById(
                R.id.rvDailyForecast
            )

        tvOutdoorWindowTime =
            view.findViewById(
                R.id.tvOutdoorWindowTime
            )

        tvOutdoorWindowReason =
            view.findViewById(
                R.id.tvOutdoorWindowReason
            )

        tvDetailFeelsLike =
            view.findViewById(
                R.id.tvDetailFeelsLike
            )

        tvDetailHumidity =
            view.findViewById(
                R.id.tvDetailHumidity
            )

        tvDetailWind =
            view.findViewById(
                R.id.tvDetailWind
            )

        tvDetailVisibility =
            view.findViewById(
                R.id.tvDetailVisibility
            )

        tvDetailUvIndex =
            view.findViewById(
                R.id.tvDetailUvIndex
            )

        tvDetailSunriseSunset =
            view.findViewById(
                R.id.tvDetailSunriseSunset
            )
    }

    private fun configureNormalScreen(
        rootView: View
    ) {
        val window =
            requireActivity().window

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).show(
            WindowInsetsCompat.Type.systemBars()
        )

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        val originalLeft =
            rootView.paddingLeft

        val originalTop =
            rootView.paddingTop

        val originalRight =
            rootView.paddingRight

        val originalBottom =
            rootView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(
            rootView
        ) { currentView, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            currentView.setPadding(
                originalLeft +
                        systemBars.left,

                originalTop +
                        systemBars.top,

                originalRight +
                        systemBars.right,

                originalBottom +
                        systemBars.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(
            rootView
        )
    }

    private fun configureDailyForecast() {
        rvDailyForecast.layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )

        rvDailyForecast.adapter =
            dailyForecastAdapter

        rvDailyForecast.setHasFixedSize(
            true
        )

        rvDailyForecast.itemAnimator =
            null
    }

    private fun configureRefreshButton() {
        btnRefreshForecast.setOnClickListener {
            if (
                viewModel.uiState.value
                    ?.isLoading == true ||
                viewModel.uiState.value
                    ?.isRefreshing == true
            ) {
                return@setOnClickListener
            }

            manualRefreshRequested =
                true

            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        val fineLocationAllowed =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationAllowed) {
            retrieveLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun retrieveLocation() {
        progressForecast.visibility =
            View.VISIBLE



        hideError()

        locationHelper.getCurrentLocation(
            onSuccess = { location ->

                if (!isAdded || view == null) {
                    return@getCurrentLocation
                }



                viewModel.loadForecast(
                    location = location,
                    isManualRefresh =
                    manualRefreshRequested
                )

                manualRefreshRequested =
                    false
            },
            onError = { message ->

                if (!isAdded || view == null) {
                    return@getCurrentLocation
                }

                manualRefreshRequested =
                    false

                displayLocationError(
                    message
                )
            }
        )
    }

    private fun observeForecast() {
        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->

            progressForecast.visibility =
                if (
                    state.isLoading ||
                    state.isRefreshing
                ) {
                    View.VISIBLE
                } else {
                    View.GONE
                }



            displayHourlyForecast(
                hourlyForecast =
                state.hourlyForecast
            )

            displayDailyForecast(
                dailyForecast =
                state.dailyForecast
            )

            displaySunriseAndSunset(
                dailyForecast =
                state.dailyForecast,

                timezone =
                state.currentWeather
                    ?.timezone
                    ?: "Asia/Manila"
            )

            state.lastUpdatedAt?.let {
                    updatedAt ->

                displayLastUpdated(
                    updatedAt
                )
            }

            val availableError =
                state.errorMessage
                    ?: state.hourlyErrorMessage
                    ?: state.dailyErrorMessage

            if (
                availableError != null &&
                state.currentWeather == null &&
                state.hourlyForecast.isEmpty() &&
                state.dailyForecast.isEmpty()
            ) {
                showError(
                    availableError
                )
            } else {
                hideError()
            }
        }
    }



    private fun displayHourlyForecast(
        hourlyForecast: List<HourlyForecast>
    ) {
        displayOutdoorWindow(
            hourlyForecast
        )

        displayWeatherTimeline(
            hourlyForecast
        )

        displayForecastAlert(
            hourlyForecast
        )

        val firstHour =
            hourlyForecast.firstOrNull()

        if (firstHour != null) {
            tvDetailFeelsLike.text =
                String.format(
                    Locale.getDefault(),
                    "Feels like\n%.1f°C",
                    firstHour.feelsLikeTemperature
                )

            tvDetailHumidity.text =
                String.format(
                    Locale.getDefault(),
                    "Humidity\n%d%%",
                    firstHour.humidity
                )

            tvDetailWind.text =
                String.format(
                    Locale.getDefault(),
                    "Wind\n%.1f km/h",
                    firstHour.windSpeed
                )

            tvDetailVisibility.text =
                if (firstHour.visibility > 0.0) {
                    String.format(
                        Locale.getDefault(),
                        "Visibility\n%.1f km",
                        firstHour.visibility
                    )
                } else {
                    "Visibility\nUnavailable"
                }

            tvDetailUvIndex.text =
                "UV index\n${firstHour.uvIndex}"
        }
    }

    private fun displayDailyForecast(
        dailyForecast: List<DailyForecast>
    ) {
        dailyForecastAdapter.submitList(
            dailyForecast
        ) {
            rvDailyForecast.requestLayout()
        }

        rvDailyForecast.visibility =
            if (dailyForecast.isEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
    }


    private fun displaySunriseAndSunset(
        dailyForecast: List<DailyForecast>,
        timezone: String
    ) {
        val todayForecast =
            dailyForecast.firstOrNull()

        if (
            todayForecast == null ||
            todayForecast.sunriseTime.isBlank() ||
            todayForecast.sunsetTime.isBlank()
        ) {
            tvDetailSunriseSunset.text =
                "Sunrise / Sunset\nUnavailable"

            return
        }

        val sunrise =
            formatSunEventTime(
                isoTime =
                todayForecast.sunriseTime,

                timezone =
                timezone
            )

        val sunset =
            formatSunEventTime(
                isoTime =
                todayForecast.sunsetTime,

                timezone =
                timezone
            )

        tvDetailSunriseSunset.text =
            "Sunrise / Sunset\n$sunrise / $sunset"
    }

    private fun formatSunEventTime(
        isoTime: String,
        timezone: String
    ): String {
        return runCatching {
            /*
             * Google returns UTC values such as:
             * 2026-09-24T21:45:12.123456789Z
             *
             * The first 19 characters contain the
             * date and time without nanoseconds.
             */
            val datePart =
                isoTime.take(19)

            val inputFormat =
                SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss",
                    Locale.US
                ).apply {
                    timeZone =
                        TimeZone.getTimeZone(
                            "UTC"
                        )
                }

            val parsedDate =
                requireNotNull(
                    inputFormat.parse(
                        datePart
                    )
                )

            val outputFormat =
                SimpleDateFormat(
                    "h:mm a",
                    Locale.getDefault()
                ).apply {
                    timeZone =
                        TimeZone.getTimeZone(
                            timezone.ifBlank {
                                "Asia/Manila"
                            }
                        )
                }

            outputFormat.format(
                parsedDate
            )
        }.getOrElse {
            "--"
        }
    }


    private fun displayOutdoorWindow(
        hourlyForecast: List<HourlyForecast>
    ) {
        val result =
            OutdoorWindowCalculator.calculate(
                hourlyForecast
            )

        tvOutdoorWindowTime.text =
            result.title

        tvOutdoorWindowReason.text =
            result.reason

        if (result.isAvailable) {
            tvOutdoorWindowTime.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.white
                )
            )

            tvOutdoorWindowReason.setTextColor(
                android.graphics.Color.parseColor(
                    "#B7AFBB"
                )
            )
        } else {
            tvOutdoorWindowTime.setTextColor(
                android.graphics.Color.parseColor(
                    "#FFD36A"
                )
            )

            tvOutdoorWindowReason.setTextColor(
                android.graphics.Color.parseColor(
                    "#D3C1B8"
                )
            )
        }
    }


    private fun displayWeatherTimeline(
        hourlyForecast: List<HourlyForecast>
    ) {
        val timelineEvents =
            WeatherTimelineAnalyzer.analyze(
                hourlyForecast
            )

        layoutWeatherTimeline.removeAllViews()

        if (timelineEvents.isEmpty()) {
            val emptyView =
                layoutInflater.inflate(
                    R.layout.item_weather_timeline,
                    layoutWeatherTimeline,
                    false
                )

            val tvTimelineIcon =
                emptyView.findViewById<TextView>(
                    R.id.tvTimelineIcon
                )

            val tvTimelineTitle =
                emptyView.findViewById<TextView>(
                    R.id.tvTimelineTitle
                )

            val tvTimelineMessage =
                emptyView.findViewById<TextView>(
                    R.id.tvTimelineMessage
                )

            tvTimelineIcon.text =
                "—"

            tvTimelineTitle.text =
                "Timeline unavailable"

            tvTimelineMessage.text =
                "Hourly weather information is currently unavailable."

            layoutWeatherTimeline.addView(
                emptyView
            )

            return
        }

        timelineEvents.forEach { event ->

            val eventView =
                layoutInflater.inflate(
                    R.layout.item_weather_timeline,
                    layoutWeatherTimeline,
                    false
                )

            val tvTimelineIcon =
                eventView.findViewById<TextView>(
                    R.id.tvTimelineIcon
                )

            val tvTimelineTitle =
                eventView.findViewById<TextView>(
                    R.id.tvTimelineTitle
                )

            val tvTimelineMessage =
                eventView.findViewById<TextView>(
                    R.id.tvTimelineMessage
                )

            tvTimelineIcon.text =
                event.icon

            tvTimelineTitle.text =
                event.title

            tvTimelineMessage.text =
                event.message

            layoutWeatherTimeline.addView(
                eventView
            )
        }
    }




    private fun displayForecastAlert(
        hourlyForecast: List<HourlyForecast>
    ) {
        val alert =
            ForecastAlertAnalyzer.analyze(
                hourlyForecast
            )

        if (alert == null) {
            cardWeatherAlert.visibility =
                View.GONE

            return
        }

        tvAlertIcon.text =
            alert.icon

        tvAlertTitle.text =
            alert.title

        tvAlertMessage.text =
            alert.message

        cardWeatherAlert.visibility =
            View.VISIBLE
    }


    private fun formatHour(
        hour: Int
    ): String {
        return when {
            hour == 0 ->
                "12 AM"

            hour < 12 ->
                "$hour AM"

            hour == 12 ->
                "12 PM"

            else ->
                "${hour - 12} PM"
        }
    }

    private fun displayLastUpdated(
        timestamp: Long
    ) {
        val formattedTime =
            SimpleDateFormat(
                "h:mm a",
                Locale.getDefault()
            ).format(
                Date(timestamp)
            )

        tvForecastUpdated.text =
            "Last updated: $formattedTime"
    }

    private fun displayLocationError(
        message: String
    ) {
        progressForecast.visibility =
            View.GONE

        tvForecastLocation.text =
            "Location unavailable"

        showError(
            message
        )
    }

    private fun showError(
        message: String
    ) {
        tvForecastError.text =
            message

        tvForecastError.visibility =
            View.VISIBLE
    }

    private fun hideError() {
        tvForecastError.visibility =
            View.GONE
    }
}
