package com.example.weatherapp.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.util.LocationHelper
import com.example.weatherapp.util.WeatherCodeMapper
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.widget.ImageView
import androidx.annotation.DrawableRes
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.weatherapp.data.repository.SupabaseOnboardingRepository
import android.app.AlertDialog
import android.graphics.drawable.ColorDrawable
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import com.example.weatherapp.ui.forecast.ForecastViewModel
import com.example.weatherapp.ui.forecast.ForecastViewModelFactory
import com.example.weatherapp.ui.forecast.HourlyForecastAdapter
import com.example.weatherapp.ui.forecast.HourlyForecastItem
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator




class HomeFragment :
    Fragment(R.layout.fragment_home) {

    private var weatherIconAnimator:
            AnimatorSet? = null

    private lateinit var locationHelper:
            LocationHelper

    private lateinit var tvGreeting:
            TextView

    private lateinit var tvLocationSummary:
            TextView

    // Popular place banner (addition only)
    private lateinit var cardFeaturedPlace:
            MaterialCardView

    private lateinit var ivFeaturedPlace:
            ImageView

    private lateinit var tvFeaturedPlace:
            TextView

    private lateinit var tvFeaturedLocation:
            TextView

    private var lastFeaturedLocationKey:
            String? = null



    private lateinit var ivWeatherIcon:
            ImageView

    private lateinit var tvStatus:
            TextView


    private lateinit var tvTemperature:
            TextView

    private lateinit var tvCondition:
            TextView

    private lateinit var tvSmartBriefing:
            TextView


    private lateinit var tvLastUpdated:
            TextView

    private lateinit var tvOutdoorIcon:
            TextView

    private lateinit var tvOutdoorStatus:
            TextView

    private lateinit var tvOutdoorMessage:
            TextView



    private lateinit var tvFeelsLike:
            TextView

    private lateinit var tvHumidity:
            TextView

    private lateinit var tvWindSpeed:
            TextView



    private lateinit var cardOutdoorReadiness:
            MaterialCardView

    private lateinit var progressBar:
            ProgressBar

    private lateinit var swipeRefreshLayout:
            SwipeRefreshLayout

    private lateinit var rvHourlyForecast: RecyclerView
    private lateinit var tvForecastStatus: TextView
    private lateinit var tvForecastError: TextView


    private lateinit var ivUserAvatar: ImageView
    private lateinit var tvHeaderName: TextView
    private val onboardingRepository = SupabaseOnboardingRepository()


    private var currentAvatarSeed: String? = null


    private val hourlyForecastAdapter = HourlyForecastAdapter()
    private val weatherRepository by lazy {
        val application =
            requireActivity().application
                    as WeatherApplication

        application.weatherRepository
    }

    private val viewModel:
            HomeViewModel by viewModels {

        HomeViewModelFactory(
            weatherRepository
        )
    }

    private val forecastViewModel:
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
                showPreciseLocationError()
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
        configureHeader()
        loadUserProfile()
        configureAvatarSelection()

        configureSwipeRefresh()
        configureHourlyForecast()
        observeWeather()
        observeHourlyForecast()

        viewModel.loadCachedWeather()

        checkLocationPermission()


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

        val originalPaddingLeft =
            rootView.paddingLeft

        val originalPaddingTop =
            rootView.paddingTop

        val originalPaddingRight =
            rootView.paddingRight

        val originalPaddingBottom =
            rootView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(
            rootView
        ) { view, windowInsets ->

            val systemBars =
                windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                originalPaddingLeft +
                        systemBars.left,

                originalPaddingTop +
                        systemBars.top,

                originalPaddingRight +
                        systemBars.right,

                originalPaddingBottom +
                        systemBars.bottom
            )

            windowInsets
        }

        ViewCompat.requestApplyInsets(
            rootView
        )
    }

    private fun bindViews(
        view: View
    ) {


        ivUserAvatar =
            view.findViewById(
                R.id.ivUserAvatar
            )

        tvHeaderName =
            view.findViewById(
                R.id.tvHeaderName
            )


        tvGreeting =
            view.findViewById(
                R.id.tvGreeting
            )

        tvLocationSummary =
            view.findViewById(
                R.id.tvLocationSummary
            )

        cardFeaturedPlace =
            view.findViewById(
                R.id.cardFeaturedPlace
            )

        ivFeaturedPlace =
            view.findViewById(
                R.id.ivFeaturedPlace
            )

        tvFeaturedPlace =
            view.findViewById(
                R.id.tvFeaturedPlace
            )

        tvFeaturedLocation =
            view.findViewById(
                R.id.tvFeaturedLocation
            )



        ivWeatherIcon =
            view.findViewById(
                R.id.ivWeatherIcon
            )
        tvStatus =
            view.findViewById(
                R.id.tvStatus
            )



        tvTemperature =
            view.findViewById(
                R.id.tvTemperature
            )

        tvCondition =
            view.findViewById(
                R.id.tvCondition
            )




        tvLastUpdated =
            view.findViewById(
                R.id.tvLastUpdated
            )



        tvOutdoorStatus =
            view.findViewById(
                R.id.tvOutdoorStatus
            )

        tvOutdoorMessage =
            view.findViewById(
                R.id.tvOutdoorMessage
            )


        tvFeelsLike =
            view.findViewById(
                R.id.tvFeelsLike
            )

        tvHumidity =
            view.findViewById(
                R.id.tvHumidity
            )

        tvWindSpeed =
            view.findViewById(
                R.id.tvWindSpeed
            )



        progressBar =
            view.findViewById(
                R.id.progressBar
            )

        swipeRefreshLayout =
            view.findViewById(
                R.id.swipeRefreshLayout
            )

        rvHourlyForecast =
            view.findViewById(
                R.id.rvHourlyForecast
            )

        tvForecastStatus =
            view.findViewById(
                R.id.tvForecastStatus
            )

        tvForecastError =
            view.findViewById(
                R.id.tvForecastError
            )
    }

    private fun configureHeader() {
        tvGreeting.text =
            getGreeting()
    }


    private fun configureAvatarSelection() {
        ivUserAvatar.setOnClickListener {
            showAvatarPicker()
        }
    }

    private fun showAvatarPicker() {
        val dialogView =
            layoutInflater.inflate(
                R.layout.dialog_avatar_picker,
                null
            )

        val rvAvatars =
            dialogView.findViewById<RecyclerView>(
                R.id.rvAvatars
            )

        val btnClose =
            dialogView.findViewById<TextView>(
                R.id.btnCloseAvatarPicker
            )

        lateinit var dialog:
                AlertDialog

        val avatarAdapter =
            AvatarAdapter { selectedSeed ->
                saveSelectedAvatar(
                    avatarSeed = selectedSeed,
                    dialog = dialog
                )
            }

        rvAvatars.layoutManager =
            GridLayoutManager(
                requireContext(),
                3
            )

        rvAvatars.adapter =
            avatarAdapter

        avatarAdapter.submitAvatars(
            seeds = createAvatarSeeds(),
            currentSeed = currentAvatarSeed
        )

        dialog =
            AlertDialog.Builder(
                requireContext()
            )
                .setView(dialogView)
                .create()

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        dialog.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
    }

    private fun createAvatarSeeds(): List<String> {
        return (1..60).map { number ->
            "weather-avatar-" +
                    number
                        .toString()
                        .padStart(
                            length = 2,
                            padChar = '0'
                        )
        }
    }

    private fun saveSelectedAvatar(
        avatarSeed: String,
        dialog: AlertDialog
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            onboardingRepository
                .saveSelectedAvatar(
                    avatarSeed
                )
                .onSuccess {
                    currentAvatarSeed =
                        avatarSeed

                    loadUserAvatar(
                        avatarStyle = "toon-head",
                        avatarSeed = avatarSeed
                    )

                    Toast.makeText(
                        requireContext(),
                        "Avatar updated.",
                        Toast.LENGTH_SHORT
                    ).show()

                    dialog.dismiss()
                }
                .onFailure { error ->
                    Log.e(
                        "UserProfile",
                        "Unable to save avatar",
                        error
                    )

                    Toast.makeText(
                        requireContext(),
                        error.message
                            ?: "Unable to update avatar.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }


    private fun loadUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            onboardingRepository
                .getCurrentUserProfile()
                .onSuccess { profile ->
                    if (
                        profile == null ||
                        !isAdded ||
                        view == null
                    ) {
                        return@onSuccess
                    }

                    tvHeaderName.text =
                        profile.displayName
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: "WeatherApp User"


                    currentAvatarSeed =
                        profile.avatarSeed

                    loadUserAvatar(
                        avatarStyle =
                        profile.avatarStyle,

                        avatarSeed =
                        profile.avatarSeed
                    )
                }
                .onFailure { error ->
                    Log.e(
                        "UserProfile",
                        "Unable to load user profile",
                        error
                    )
                }
        }
    }

    private fun loadUserAvatar(
        avatarStyle: String,
        avatarSeed: String?
    ) {
        if (avatarSeed.isNullOrBlank()) {
            return
        }

        val encodedStyle =
            URLEncoder.encode(
                avatarStyle,
                "UTF-8"
            )

        val encodedSeed =
            URLEncoder.encode(
                avatarSeed,
                "UTF-8"
            )

        val avatarUrl =
            "https://api.dicebear.com/10.x/" +
                    "$encodedStyle/svg" +
                    "?seed=$encodedSeed"

        val imageLoader =
            ImageLoader.Builder(
                requireContext()
            )
                .components {
                    add(
                        SvgDecoder.Factory()
                    )
                }
                .build()

        val imageRequest =
            ImageRequest.Builder(
                requireContext()
            )
                .data(avatarUrl)
                .crossfade(true)
                .target(ivUserAvatar)
                .listener(
                    onError = { _, result ->
                        Log.e(
                            "UserProfile",
                            "Unable to load avatar: $avatarUrl",
                            result.throwable
                        )
                    }
                )
                .build()

        imageLoader.enqueue(
            imageRequest
        )
    }

    private fun configureSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeColors(
            Color.parseColor("#8F8F8F")
        )

        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(
            Color.parseColor("#181818")
        )

        swipeRefreshLayout.setOnRefreshListener {
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


    private fun configureHourlyForecast() {
        rvHourlyForecast.apply {
            layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            adapter =
                hourlyForecastAdapter

            setHasFixedSize(true)

            itemAnimator = null

            visibility =
                View.VISIBLE
        }
    }

    private fun retrieveLocation() {
        Log.d(
            "WeeklyForecast",
            "retrieveLocation() started"
        )

        tvStatus.text =
            "Detecting your location…"

        tvLocationSummary.text =
            "Getting current location…"

        progressBar.visibility =
            View.VISIBLE

        swipeRefreshLayout.isRefreshing =
            true

        locationHelper.getCurrentLocation(
            onSuccess = { location ->

                Log.d(
                    "WeeklyForecast",
                    "Location success: " +
                            "${location.latitude}, ${location.longitude}"
                )

                if (!isAdded || view == null) {
                    Log.e(
                        "WeeklyForecast",
                        "Fragment is no longer active"
                    )

                    return@getCurrentLocation
                }

                Log.d(
                    "WeeklyForecast",
                    "Calling viewModel.loadWeather()"
                )

                viewModel.loadWeather(
                    location
                )

                forecastViewModel.loadForecast(
                    location = location
                )
            },
            onError = { message ->

                Log.e(
                    "WeeklyForecast",
                    "Location error: $message"
                )

                if (!isAdded || view == null) {
                    return@getCurrentLocation
                }

                progressBar.visibility =
                    View.GONE

                swipeRefreshLayout.isRefreshing =
                    false

                tvStatus.text =
                    message

                tvLocationSummary.text =
                    "Location unavailable"
            }
        )
    }



    private fun observeWeather() {
        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->
            progressBar.visibility =
                if (state.isLoading) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            swipeRefreshLayout.isRefreshing =
                state.isLoading

            state.weather?.let { weather ->
                displayWeather(
                    weather
                )
            }

            state.errorMessage?.let { message ->
                tvStatus.text =
                    message
            }
        }
    }

    private fun observeHourlyForecast() {
        forecastViewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->
            val hourlyItems =
                state.hourlyForecast.map { forecast ->
                    HourlyForecastItem(
                        time = formatHour(forecast.hour),
                        temperature = forecast.temperature,
                        feelsLikeTemperature =
                        forecast.feelsLikeTemperature,
                        rainChance = forecast.rainChance,
                        condition = forecast.condition,
                        conditionType = forecast.conditionType,
                        iconBaseUri = forecast.iconBaseUri,
                        humidity = forecast.humidity,
                        windSpeed = forecast.windSpeed,
                        uvIndex = forecast.uvIndex,
                        thunderstormProbability =
                        forecast.thunderstormProbability,
                        visibility = forecast.visibility
                    )
                }

            hourlyForecastAdapter.submitList(
                hourlyItems
            ) {
                rvHourlyForecast.requestLayout()
            }

            tvForecastStatus.visibility =
                View.VISIBLE

            tvForecastStatus.text =
                if (state.isLoading || state.isRefreshing) {
                    "Updating..."
                } else {
                    "Next 24 hours"
                }

            rvHourlyForecast.visibility =
                if (hourlyItems.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            when {
                hourlyItems.isNotEmpty() -> {
                    tvForecastError.visibility =
                        View.GONE
                }

                state.hourlyErrorMessage != null -> {
                    tvForecastError.text =
                        "Hourly forecast unavailable: " +
                                state.hourlyErrorMessage

                    tvForecastError.visibility =
                        View.VISIBLE
                }

                state.isLoading || state.isRefreshing -> {
                    tvForecastError.visibility =
                        View.GONE
                }

                else -> {
                    tvForecastError.text =
                        "No hourly forecast data was returned."

                    tvForecastError.visibility =
                        View.VISIBLE
                }
            }
        }
    }

    private fun displayWeather(
        weather: Weather
    ) {
        val weatherIcon =
            getWeatherIcon(
                weather
            )

        tvGreeting.text =
            getGreeting()

        tvStatus.text =
            if (weather.isFromCache) {
                "Saved weather • Updating when online"
            } else {
                "Live weather • Current location"
            }

        ivWeatherIcon.setImageResource(
            weatherIcon
        )

        startWeatherIconAnimation(
            conditionType =
            weather.conditionType
        )


        tvTemperature.text =
            String.format(
                Locale.getDefault(),
                "%.1f°C",
                weather.temperature
            )

        tvCondition.text =
            weather.conditionDescription
                .takeIf {
                    it.isNotBlank()
                }
                ?: WeatherCodeMapper.getDescription(
                    weather.weatherCode
                )




        tvFeelsLike.text =
            String.format(
                Locale.getDefault(),
                "%.1f°C",
                weather.apparentTemperature
            )

        tvHumidity.text =
            String.format(
                Locale.getDefault(),
                "%d%%",
                weather.humidity
            )

        tvWindSpeed.text =
            String.format(
                Locale.getDefault(),
                "%.1f km/h",
                weather.windSpeed
            )

        loadLocationName(
            latitude = weather.latitude,
            longitude = weather.longitude
        )

        loadPopularPlace(
            latitude = weather.latitude,
            longitude = weather.longitude
        )


        displayLastUpdatedTime(
            weather.savedAt
        )
    }


    private fun startWeatherIconAnimation(
        conditionType: String
    ) {
        weatherIconAnimator
            ?.cancel()

        ivWeatherIcon.animate()
            .cancel()

        ivWeatherIcon.rotation =
            0f

        ivWeatherIcon.translationX =
            0f

        ivWeatherIcon.translationY =
            0f

        ivWeatherIcon.scaleX =
            1f

        ivWeatherIcon.scaleY =
            1f

        ivWeatherIcon.alpha =
            1f

        val normalizedCondition =
            conditionType
                .trim()
                .uppercase(
                    Locale.US
                )

        weatherIconAnimator =
            when (normalizedCondition) {
                "CLEAR",
                "MOSTLY_CLEAR" -> {
                    createSunnyAnimation()
                }

                "PARTLY_CLOUDY",
                "MOSTLY_CLOUDY",
                "CLOUDY" -> {
                    createCloudAnimation()
                }

                "WINDY" -> {
                    createWindAnimation()
                }

                "WIND_AND_RAIN",
                "LIGHT_RAIN_SHOWERS",
                "CHANCE_OF_SHOWERS",
                "SCATTERED_SHOWERS",
                "RAIN_SHOWERS",
                "HEAVY_RAIN_SHOWERS",
                "LIGHT_TO_MODERATE_RAIN",
                "MODERATE_TO_HEAVY_RAIN",
                "RAIN",
                "LIGHT_RAIN",
                "HEAVY_RAIN",
                "RAIN_PERIODICALLY_HEAVY" -> {
                    createRainAnimation()
                }

                "THUNDERSTORM",
                "THUNDERSHOWER",
                "LIGHT_THUNDERSTORM_RAIN",
                "SCATTERED_THUNDERSTORMS",
                "HEAVY_THUNDERSTORM" -> {
                    createThunderstormAnimation()
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
                "HEAVY_SNOW_STORM",
                "BLOWING_SNOW",
                "SNOW_PERIODICALLY_HEAVY",
                "RAIN_AND_SNOW",
                "HAIL",
                "HAIL_SHOWERS" -> {
                    createSnowAnimation()
                }

                else -> {
                    createFogAnimation()
                }
            }

        weatherIconAnimator
            ?.start()
    }

    private fun createSunnyAnimation():
            AnimatorSet {
        val rotation =
            ObjectAnimator.ofFloat(
                ivWeatherIcon,
                View.ROTATION,
                0f,
                360f
            ).apply {
                duration =
                    12_000L

                repeatCount =
                    ValueAnimator.INFINITE

                interpolator =
                    LinearInterpolator()
            }

        return AnimatorSet().apply {
            playTogether(
                rotation
            )
        }
    }

    private fun createCloudAnimation():
            AnimatorSet {
        val move =
            ObjectAnimator.ofFloat(
                ivWeatherIcon,
                View.TRANSLATION_X,
                -5f,
                5f
            ).apply {
                duration =
                    2_500L

                repeatCount =
                    ValueAnimator.INFINITE

                repeatMode =
                    ValueAnimator.REVERSE

                interpolator =
                    AccelerateDecelerateInterpolator()
            }

        val scaleX =
            ObjectAnimator.ofFloat(
                ivWeatherIcon,
                View.SCALE_X,
                1f,
                1.05f
            ).apply {
                duration =
                    2_500L

                repeatCount =
                    ValueAnimator.INFINITE

                repeatMode =
                    ValueAnimator.REVERSE
            }

        val scaleY =
            ObjectAnimator.ofFloat(
                ivWeatherIcon,
                View.SCALE_Y,
                1f,
                1.05f
            ).apply {
                duration =
                    2_500L

                repeatCount =
                    ValueAnimator.INFINITE

                repeatMode =
                    ValueAnimator.REVERSE
            }

        return AnimatorSet().apply {
            playTogether(
                move,
                scaleX,
                scaleY
            )
        }
    }

    private fun createWindAnimation():
            AnimatorSet {
        val move =
            ObjectAnimator.ofFloat(
                ivWeatherIcon,
                View.TRANSLATION_X,
                -8f,
                8f
            ).apply {
                duration =
                    750L

                repeatCount =
                    ValueAnimator.INFINITE

                repeatMode =
                    ValueAnimator.REVERSE

                interpolator =
                    AccelerateDecelerateInterpolator()
            }

        val rotation =
            ObjectAnimator.ofFloat(
                ivWeatherIcon,
                View.ROTATION,
                -3f,
                3f
            ).apply {
                duration =
                    750L

                repeatCount =
                    ValueAnimator.INFINITE

                repeatMode =
                    ValueAnimator.REVERSE
            }

        return AnimatorSet().apply {
            playTogether(
                move,
                rotation
            )
        }
    }

    private fun createRainAnimation():
            AnimatorSet {
        val falling =
            ObjectAnimator.ofFloat(
                ivWeatherIcon,
                View.TRANSLATION_Y,
                -4f,
                7f
            ).apply {
                duration =
                    850L

                repeatCount =
                    ValueAnimator.INFINITE

                repeatMode =
                    ValueAnimator.REVERSE

                interpolator =
                    AccelerateDecelerateInterpolator()
            }

        val fade =
            ObjectAnimator.ofFloat(
                ivWeatherIcon,
                View.ALPHA,
                1f,
                0.75f
            ).apply {
                duration =
                    850L

                repeatCount =
                    ValueAnimator.INFINITE

                repeatMode =
                    ValueAnimator.REVERSE
            }

        return AnimatorSet().apply {
            playTogether(
                falling,
                fade
            )
        }
    }

    private fun createThunderstormAnimation():
            AnimatorSet {
        val shake =
            ObjectAnimator.ofFloat(
                ivWeatherIcon,
                View.TRANSLATION_X,
                0f,
                -5f,
                5f,
                -3f,
                3f,
                0f
            ).apply {
                duration =
                    900L

                repeatCount =
                    ValueAnimator.INFINITE

                interpolator =
                    AccelerateDecelerateInterpolator()
            }

        val flash =
            ObjectAnimator.ofFloat(
                ivWeatherIcon,
                View.ALPHA,
                1f,
                0.45f,
                1f
            ).apply {
                duration =
                    900L

                repeatCount =
                    ValueAnimator.INFINITE
            }

        val scaleX =
            ObjectAnimator.ofFloat(
                ivWeatherIcon,
                View.SCALE_X,
                1f,
                1.08f,
                1f
            ).apply {
                duration =
                    900L

                repeatCount =
                    ValueAnimator.INFINITE
            }

        val scaleY =
            ObjectAnimator.ofFloat(
                ivWeatherIcon,
                View.SCALE_Y,
                1f,
                1.08f,
                1f
            ).apply {
                duration =
                    900L

                repeatCount =
                    ValueAnimator.INFINITE
            }

        return AnimatorSet().apply {
            playTogether(
                shake,
                flash,
                scaleX,
                scaleY
            )
        }
    }

    private fun createSnowAnimation():
            AnimatorSet {
        val falling =
            ObjectAnimator.ofFloat(
                ivWeatherIcon,
                View.TRANSLATION_Y,
                -5f,
                6f
            ).apply {
                duration =
                    1_800L

                repeatCount =
                    ValueAnimator.INFINITE

                repeatMode =
                    ValueAnimator.REVERSE

                interpolator =
                    AccelerateDecelerateInterpolator()
            }

        val rotation =
            ObjectAnimator.ofFloat(
                ivWeatherIcon,
                View.ROTATION,
                -5f,
                5f
            ).apply {
                duration =
                    1_800L

                repeatCount =
                    ValueAnimator.INFINITE

                repeatMode =
                    ValueAnimator.REVERSE
            }

        return AnimatorSet().apply {
            playTogether(
                falling,
                rotation
            )
        }
    }

    private fun createFogAnimation():
            AnimatorSet {
        val move =
            ObjectAnimator.ofFloat(
                ivWeatherIcon,
                View.TRANSLATION_X,
                -6f,
                6f
            ).apply {
                duration =
                    3_000L

                repeatCount =
                    ValueAnimator.INFINITE

                repeatMode =
                    ValueAnimator.REVERSE

                interpolator =
                    AccelerateDecelerateInterpolator()
            }

        val fade =
            ObjectAnimator.ofFloat(
                ivWeatherIcon,
                View.ALPHA,
                0.65f,
                1f
            ).apply {
                duration =
                    3_000L

                repeatCount =
                    ValueAnimator.INFINITE

                repeatMode =
                    ValueAnimator.REVERSE
            }

        return AnimatorSet().apply {
            playTogether(
                move,
                fade
            )
        }
    }


    private fun loadLocationName(
        latitude: Double,
        longitude: Double
    ) {
        val fallbackCoordinates =
            formatCoordinates(
                latitude,
                longitude
            )

        if (!Geocoder.isPresent()) {
            displayLocationName(
                fallbackCoordinates
            )

            return
        }

        tvLocationSummary.text =
            "Finding place name…"

        val geocoder =
            Geocoder(
                requireContext(),
                Locale.getDefault()
            )

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            runCatching {
                geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1,
                    object :
                        Geocoder.GeocodeListener {

                        override fun onGeocode(
                            addresses:
                            MutableList<Address>
                        ) {
                            val placeName =
                                addresses
                                    .firstOrNull()
                                    ?.let { address ->
                                        createPlaceName(
                                            address
                                        )
                                    }
                                    ?: fallbackCoordinates

                            displayLocationName(
                                placeName
                            )
                        }

                        override fun onError(
                            errorMessage: String?
                        ) {
                            displayLocationName(
                                fallbackCoordinates
                            )
                        }
                    }
                )
            }.onFailure {
                displayLocationName(
                    fallbackCoordinates
                )
            }
        } else {
            loadLegacyLocationName(
                geocoder = geocoder,
                latitude = latitude,
                longitude = longitude
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun loadLegacyLocationName(
        geocoder: Geocoder,
        latitude: Double,
        longitude: Double
    ) {
        val fallbackCoordinates =
            formatCoordinates(
                latitude,
                longitude
            )

        viewLifecycleOwner.lifecycleScope.launch {
            val placeName =
                withContext(
                    Dispatchers.IO
                ) {
                    runCatching {
                        geocoder.getFromLocation(
                            latitude,
                            longitude,
                            1
                        )
                            ?.firstOrNull()
                            ?.let { address ->
                                createPlaceName(
                                    address
                                )
                            }
                    }.getOrNull()
                } ?: fallbackCoordinates

            displayLocationName(
                placeName
            )
        }
    }

    private fun displayLocationName(
        placeName: String
    ) {
        tvLocationSummary.post {
            if (
                !isAdded ||
                view == null
            ) {
                return@post
            }

            tvLocationSummary.text =
                placeName


        }
    }

    // Loads one notable place near the detected coordinates.
    // This is independent from the existing weather and address logic.

    private fun loadPopularPlace(
        latitude: Double,
        longitude: Double
    ) {
        val locationKey =
            String.format(
                Locale.US,
                "%.3f,%.3f",
                latitude,
                longitude
            )

        if (lastFeaturedLocationKey == locationKey) {
            return
        }

        lastFeaturedLocationKey =
            locationKey

        cardFeaturedPlace.visibility =
            View.VISIBLE

        tvFeaturedPlace.text =
            "Finding a nearby sports complex…"

        tvFeaturedLocation.text =
            "Searching within 10 km"

        viewLifecycleOwner.lifecycleScope.launch {
            val featuredPlace =
                withContext(Dispatchers.IO) {
                    fetchPopularPlace(
                        latitude = latitude,
                        longitude = longitude
                    )
                }

            if (!isAdded || view == null) {
                return@launch
            }

            if (featuredPlace == null) {
                tvFeaturedPlace.text =
                    "No nearby sports complex found"

                tvFeaturedLocation.text =
                    "Try exploring the map for more places"

                return@launch
            }

            tvFeaturedPlace.text =
                featuredPlace.title

            tvFeaturedLocation.text =
                "Nearby sports facility"

            featuredPlace.imageUrl?.let { imageUrl ->
                val bitmap =
                    withContext(Dispatchers.IO) {
                        downloadPlaceImage(
                            imageUrl = imageUrl
                        )
                    }

                if (
                    bitmap != null &&
                    isAdded &&
                    view != null
                ) {
                    ivFeaturedPlace.setImageBitmap(
                        bitmap
                    )
                }
            }
        }
    }

    private fun fetchPopularPlace(
        latitude: Double,
        longitude: Double
    ): FeaturedPlace? {
        return runCatching {
            val coordinates =
                URLEncoder.encode(
                    "$latitude|$longitude",
                    "UTF-8"
                )

            val requestUrl =
                "https://en.wikipedia.org/w/api.php" +
                        "?action=query" +
                        "&generator=geosearch" +
                        "&ggsprimary=all" +
                        "&ggsnamespace=0" +
                        "&ggsradius=10000" +
                        "&ggslimit=50" +
                        "&ggscoord=$coordinates" +
                        "&prop=pageimages" +
                        "&piprop=thumbnail" +
                        "&pithumbsize=1000" +
                        "&format=json" +
                        "&origin=*"

            val connection =
                URL(requestUrl)
                    .openConnection()

            connection.connectTimeout =
                10_000

            connection.readTimeout =
                10_000

            connection.setRequestProperty(
                "User-Agent",
                "WeatherApp/1.0 (Android)"
            )

            val responseText =
                connection.getInputStream()
                    .bufferedReader()
                    .use { reader ->
                        reader.readText()
                    }

            val pages =
                JSONObject(responseText)
                    .optJSONObject("query")
                    ?.optJSONObject("pages")
                    ?: return@runCatching null

            val sportsKeywords =
                listOf(
                    "sports complex",
                    "sports center",
                    "sports centre",
                    "sports arena",
                    "sports club",
                    "stadium",
                    "arena",
                    "coliseum",
                    "gymnasium",
                    "athletic field",
                    "athletics center",
                    "athletics centre",
                    "football field",
                    "football stadium",
                    "basketball court",
                    "basketball arena",
                    "tennis court",
                    "swimming complex",
                    "aquatic center",
                    "aquatic centre",
                    "sports venue"
                )

            var nearestSportsPlace:
                    FeaturedPlace? = null

            var nearestSportsIndex =
                Int.MAX_VALUE

            val pageIds =
                pages.keys()

            while (pageIds.hasNext()) {
                val pageId =
                    pageIds.next()

                val page =
                    pages.optJSONObject(
                        pageId
                    ) ?: continue

                val title =
                    page.optString(
                        "title",
                        ""
                    ).trim()

                if (title.isBlank()) {
                    continue
                }

                val normalizedTitle =
                    title.lowercase(
                        Locale.US
                    )

                val isSportsPlace =
                    sportsKeywords.any { keyword ->
                        normalizedTitle.contains(
                            keyword
                        )
                    }

                if (!isSportsPlace) {
                    continue
                }

                val resultIndex =
                    page.optInt(
                        "index",
                        Int.MAX_VALUE
                    )

                val imageUrl =
                    page.optJSONObject(
                        "thumbnail"
                    )
                        ?.optString(
                            "source"
                        )
                        ?.takeIf { source ->
                            source.isNotBlank()
                        }

                if (
                    nearestSportsPlace == null ||
                    resultIndex < nearestSportsIndex
                ) {
                    nearestSportsIndex =
                        resultIndex

                    nearestSportsPlace =
                        FeaturedPlace(
                            title = title,
                            imageUrl = imageUrl
                        )
                }
            }

            nearestSportsPlace
        }.onFailure { error ->
            Log.e(
                "SportsComplex",
                "Unable to load nearby sports complex",
                error
            )
        }.getOrNull()
    }

    private fun downloadPlaceImage(
        imageUrl: String
    ): Bitmap? {
        return runCatching {
            val connection =
                URL(imageUrl)
                    .openConnection()

            connection.connectTimeout =
                10_000

            connection.readTimeout =
                10_000

            connection.setRequestProperty(
                "User-Agent",
                "WeatherApp/1.0 (Android)"
            )

            connection.getInputStream()
                .use { inputStream ->
                    BitmapFactory.decodeStream(
                        inputStream
                    )
                }
        }.onFailure { error ->
            Log.e(
                "SportsComplex",
                "Unable to download sports complex image",
                error
            )
        }.getOrNull()
    }



    private data class FeaturedPlace(
        val title: String,
        val imageUrl: String?
    )

    private fun formatCoordinates(
        latitude: Double,
        longitude: Double
    ): String {
        return String.format(
            Locale.getDefault(),
            "%.4f, %.4f",
            latitude,
            longitude
        )
    }

    private fun createPlaceName(
        address: Address
    ): String {
        val barangay =
            address.subLocality
                ?.takeIf {
                    it.isNotBlank()
                }

        val city =
            (
                    address.locality
                        ?: address.subAdminArea
                    )
                ?.takeIf {
                    it.isNotBlank()
                }

        val provinceOrRegion =
            address.adminArea
                ?.takeIf {
                    it.isNotBlank()
                }

        val placeParts =
            listOfNotNull(
                barangay,
                city,
                provinceOrRegion
            )
                .distinct()

        if (placeParts.isNotEmpty()) {
            return placeParts.joinToString(
                separator = ", "
            )
        }

        return address.getAddressLine(0)
            ?.takeIf {
                it.isNotBlank()
            }
            ?: "Current location"
    }

    private fun createSmartWeatherBriefing(
        weather: Weather
    ): String {
        val conditionType =
            normalizeConditionType(
                weather.conditionType
            )

        val conditionDescription =
            weather.conditionDescription
                .takeIf {
                    it.isNotBlank()
                }
                ?: WeatherCodeMapper.getDescription(
                    weather.weatherCode
                )

        val hasThunderstorm =
            isThunderstormCondition(
                conditionType
            )

        val hasHeavyRain =
            isHeavyRainCondition(
                conditionType
            )

        val hasRain =
            isRainCondition(
                conditionType
            )

        val hasSnow =
            isSnowCondition(
                conditionType
            )

        val hasStrongWind =
            conditionType == "WINDY" ||
                    conditionType == "WIND_AND_RAIN" ||
                    weather.windSpeed > 25.0

        val hasHighHumidity =
            weather.humidity >= 80

        val feelsHotter =
            weather.apparentTemperature -
                    weather.temperature >= 3.0

        return when {
            hasThunderstorm -> {
                "Thunderstorms are currently detected. " +
                        "Stay indoors when possible and avoid exposed areas."
            }

            hasHeavyRain -> {
                "Heavy rain may reduce visibility and make roads slippery. " +
                        "Consider delaying unnecessary travel."
            }

            hasRain && hasStrongWind -> {
                "$conditionDescription with strong winds may affect walking " +
                        "and cycling. Use extra caution when travelling."
            }

            hasRain -> {
                "Current conditions indicate $conditionDescription. " +
                        "Bring an umbrella when going outside."
            }

            hasSnow -> {
                "$conditionDescription may make roads and walkways slippery. " +
                        "Plan outdoor travel carefully."
            }

            hasStrongWind -> {
                String.format(
                    Locale.getDefault(),
                    "Wind speed is currently %.1f km/h. " +
                            "Cycling and outdoor travel may be more difficult.",
                    weather.windSpeed
                )
            }

            feelsHotter && hasHighHumidity -> {
                "High humidity makes it feel warmer than the actual " +
                        "temperature. Remember to stay hydrated."
            }

            weather.temperature > 33.0 -> {
                "Hot conditions are expected. Avoid prolonged sun exposure " +
                        "and remember to stay hydrated."
            }

            weather.temperature < 18.0 -> {
                "The weather is cooler than usual. Consider wearing an " +
                        "additional layer outside."
            }

            else -> {
                "Conditions are generally comfortable for outdoor activities. " +
                        "It is currently $conditionDescription."
            }
        }
    }







    private fun normalizeConditionType(
        conditionType: String
    ): String {
        return conditionType
            .trim()
            .uppercase(Locale.US)
    }

    private fun isThunderstormCondition(
        conditionType: String
    ): Boolean {
        return conditionType in setOf(
            "THUNDERSTORM",
            "THUNDERSHOWER",
            "LIGHT_THUNDERSTORM_RAIN",
            "SCATTERED_THUNDERSTORMS",
            "HEAVY_THUNDERSTORM"
        )
    }

    private fun isHeavyRainCondition(
        conditionType: String
    ): Boolean {
        return conditionType in setOf(
            "HEAVY_RAIN_SHOWERS",
            "MODERATE_TO_HEAVY_RAIN",
            "HEAVY_RAIN",
            "RAIN_PERIODICALLY_HEAVY",
            "WIND_AND_RAIN"
        )
    }

    private fun isRainCondition(
        conditionType: String
    ): Boolean {
        return conditionType in setOf(
            "LIGHT_RAIN_SHOWERS",
            "CHANCE_OF_SHOWERS",
            "SCATTERED_SHOWERS",
            "RAIN_SHOWERS",
            "HEAVY_RAIN_SHOWERS",
            "LIGHT_TO_MODERATE_RAIN",
            "MODERATE_TO_HEAVY_RAIN",
            "RAIN",
            "LIGHT_RAIN",
            "HEAVY_RAIN",
            "RAIN_PERIODICALLY_HEAVY",
            "WIND_AND_RAIN"
        )
    }

    private fun isSnowCondition(
        conditionType: String
    ): Boolean {
        return conditionType in setOf(
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
            "RAIN_AND_SNOW"
        )
    }



    private fun updateOutdoorCard(
        icon: String,
        status: String,
        message: String,
        borderColor: String
    ) {
        tvOutdoorIcon.text =
            icon

        tvOutdoorStatus.text =
            status

        tvOutdoorMessage.text =
            message

        cardOutdoorReadiness
            .setCardBackgroundColor(
                Color.parseColor(
                    "#181818"
                )
            )

        cardOutdoorReadiness.setStrokeColor(
            Color.parseColor(
                borderColor
            )
        )
    }

    private fun displayLastUpdatedTime(
        savedAt: Long
    ) {
        val dateFormat =
            SimpleDateFormat(
                "MMM dd, yyyy • hh:mm a",
                Locale.getDefault()
            )

        dateFormat.timeZone =
            TimeZone.getTimeZone(
                "Asia/Manila"
            )

        tvLastUpdated.text =
            "Last updated ${
                dateFormat.format(
                    Date(savedAt)
                )
            }"
    }

    private fun getGreeting(): String {
        val calendar =
            Calendar.getInstance(
                TimeZone.getTimeZone(
                    "Asia/Manila"
                )
            )

        return when (
            calendar.get(
                Calendar.HOUR_OF_DAY
            )
        ) {
            in 5..11 ->
                "Good morning"

            in 12..17 ->
                "Good afternoon"

            else ->
                "Good evening"
        }
    }


    @DrawableRes
    private fun getWeatherIcon(
        weather: Weather
    ): Int {
        val conditionType =
            weather.conditionType
                .trim()
                .uppercase(Locale.US)

        return when (conditionType) {
            "CLEAR",
            "MOSTLY_CLEAR" -> {
                if (weather.isDay) {
                    R.drawable.ic_weather_sunny_outline
                } else {
                    R.drawable.ic_weather_cloudy_outline
                }
            }

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
            "HEAVY_SNOW_STORM",
            "BLOWING_SNOW",
            "SNOW_PERIODICALLY_HEAVY",
            "RAIN_AND_SNOW",
            "HAIL",
            "HAIL_SHOWERS" ->
                R.drawable.ic_weather_snow_outline

            else ->
                R.drawable.ic_weather_fog_outline
        }
    }


    private fun getFallbackWeatherIcon(
        weatherCode: Int,
        isDay: Boolean
    ): String {
        return when {
            weatherCode == 0 ->
                if (isDay) "☀️" else "🌙"

            weatherCode in 1..3 ->
                "⛅"

            weatherCode in 45..48 ->
                "🌫️"

            weatherCode in 51..67 ->
                "🌧️"

            weatherCode in 71..77 ->
                "❄️"

            weatherCode in 80..82 ->
                "🌦️"

            weatherCode in 85..86 ->
                "🌨️"

            weatherCode in 95..99 ->
                "⛈️"

            else ->
                "☁️"
        }
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


    private fun showPreciseLocationError() {
        tvStatus.text =
            "Precise location is not enabled"

        tvLocationSummary.text =
            "Enable precise location"


        progressBar.visibility =
            View.GONE

        swipeRefreshLayout.isRefreshing =
            false

        Toast.makeText(
            requireContext(),
            "Select Precise location and allow access while using the app.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        weatherIconAnimator
            ?.cancel()

        weatherIconAnimator =
            null

        if (::ivWeatherIcon.isInitialized) {
            ivWeatherIcon.animate()
                .cancel()

            ivWeatherIcon.rotation =
                0f

            ivWeatherIcon.translationX =
                0f

            ivWeatherIcon.translationY =
                0f

            ivWeatherIcon.scaleX =
                1f

            ivWeatherIcon.scaleY =
                1f

            ivWeatherIcon.alpha =
                1f
        }

        if (::locationHelper.isInitialized) {
            locationHelper.cancelLocationRequest()
        }

        super.onDestroyView()
    }


}
