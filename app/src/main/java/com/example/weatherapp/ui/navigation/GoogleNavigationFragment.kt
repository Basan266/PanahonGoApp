package com.example.weatherapp.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.example.weatherapp.R
import com.google.android.libraries.navigation.NavigationApi
import com.google.android.libraries.navigation.Navigator
import com.google.android.libraries.navigation.RoutingOptions
import com.google.android.libraries.navigation.SupportNavigationFragment
import com.google.android.libraries.navigation.Waypoint
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.CameraPerspective
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.navigation.ForceNightMode
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.util.WeatherCodeMapper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.Marker

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import com.google.android.libraries.navigation.RoadSnappedLocationProvider


class GoogleNavigationFragment :
    Fragment(R.layout.fragment_google_navigation) {

    private var navigator: Navigator? = null

    private var navigationFragment:
            SupportNavigationFragment? = null

    private lateinit var progressNavigation: ProgressBar
    private lateinit var btnBackNavigation: TextView
    private lateinit var locationClient: FusedLocationProviderClient

    private var googleMap: GoogleMap? = null

    private var roadSnappedLocationProvider:
            RoadSnappedLocationProvider? = null

    private var roadSnappedLocationListener:
            RoadSnappedLocationProvider.LocationListener? = null

    private lateinit var navigationWeatherStatus: LinearLayout
    private lateinit var tvNavigationWeatherIcon: TextView
    private lateinit var tvNavigationWeather: TextView
    private lateinit var weatherWarningPanel: LinearLayout
    private lateinit var tvWeatherWarningTitle: TextView
    private lateinit var tvWeatherWarningMessage: TextView
    private lateinit var btnDismissWeatherWarning: TextView


    private var weatherMonitoringJob: Job? = null
    private var lastWarnedWeatherCode: Int? = null
    private val weatherRepository by lazy {
        val application =
            requireActivity().application
                    as WeatherApplication

        application.weatherRepository
    }

    private val destinationLatitude: Double
        get() = requireArguments().getDouble(
            ARG_DESTINATION_LATITUDE
        )

    private val destinationLongitude: Double
        get() = requireArguments().getDouble(
            ARG_DESTINATION_LONGITUDE
        )

    private val destinationName: String
        get() = requireArguments().getString(
            ARG_DESTINATION_NAME
        ).orEmpty()

    private val routeMode: String
        get() = requireArguments().getString(
            ARG_ROUTE_MODE
        ) ?: MODE_WALKING

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        progressNavigation =
            view.findViewById(
                R.id.progressNavigation
            )

        btnBackNavigation =
            view.findViewById(
                R.id.btnBackNavigation
            )



        locationClient =
            LocationServices.getFusedLocationProviderClient(
                requireActivity()
            )

        navigationWeatherStatus =
            view.findViewById(
                R.id.navigationWeatherStatus
            )

        tvNavigationWeatherIcon =
            view.findViewById(
                R.id.tvNavigationWeatherIcon
            )

        tvNavigationWeather =
            view.findViewById(
                R.id.tvNavigationWeather
            )

        weatherWarningPanel =
            view.findViewById(
                R.id.weatherWarningPanel
            )

        tvWeatherWarningTitle =
            view.findViewById(
                R.id.tvWeatherWarningTitle
            )

        tvWeatherWarningMessage =
            view.findViewById(
                R.id.tvWeatherWarningMessage
            )

        btnDismissWeatherWarning =
            view.findViewById(
                R.id.btnDismissWeatherWarning
            )

        btnDismissWeatherWarning.setOnClickListener {
            weatherWarningPanel.visibility =
                View.GONE
        }



        configureNormalScreen(view)
        configureSystemBackButton()
        configureBackButton()
        createNavigationFragment()
        initializeNavigator()
    }




    private fun configureNormalScreen(
        rootView: View
    ) {
        val window =
            requireActivity().window

        // Keep Android status and navigation bars visible.
        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).show(
            WindowInsetsCompat.Type.systemBars()
        )

        // We manually apply the safe screen area.
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
                originalPaddingLeft,
                originalPaddingTop + systemBars.top,
                originalPaddingRight,
                originalPaddingBottom + systemBars.bottom
            )

            windowInsets
        }

        ViewCompat.requestApplyInsets(
            rootView
        )
    }


    private fun exitFullScreenMode() {
        val window =
            requireActivity().window

        WindowCompat.setDecorFitsSystemWindows(
            window,
            true
        )

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).show(
            WindowInsetsCompat.Type.systemBars()
        )

        (requireActivity() as? AppCompatActivity)
            ?.supportActionBar
            ?.show()
    }

    private fun configureSystemBackButton() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {

                    override fun handleOnBackPressed() {
                        closeNavigation()
                    }
                }
            )
    }

    private fun configureBackButton() {
        btnBackNavigation.setOnClickListener {
            closeNavigation()
        }
    }

    private fun createNavigationFragment() {
        val existingFragment =
            childFragmentManager.findFragmentByTag(
                NAVIGATION_FRAGMENT_TAG
            ) as? SupportNavigationFragment

        if (existingFragment != null) {
            navigationFragment =
                existingFragment

            return
        }

        val newNavigationFragment =
            SupportNavigationFragment()

        navigationFragment =
            newNavigationFragment

        childFragmentManager
            .beginTransaction()
            .replace(
                R.id.googleNavigationContainer,
                newNavigationFragment,
                NAVIGATION_FRAGMENT_TAG
            )
            .commitNow()
    }

    private fun initializeNavigator() {
        progressNavigation.visibility =
            View.VISIBLE

        NavigationApi.getNavigator(
            requireActivity(),
            object : NavigationApi.NavigatorListener {

                override fun onNavigatorReady(
                    readyNavigator: Navigator
                ) {
                    navigator =
                        readyNavigator

                    startNavigation()
                }

                override fun onError(
                    errorCode: Int
                ) {
                    progressNavigation.visibility =
                        View.GONE

                    val message =
                        when (errorCode) {
                            NavigationApi.ErrorCode.NOT_AUTHORIZED ->
                                "API key is not authorized for Navigation SDK."

                            NavigationApi.ErrorCode.TERMS_NOT_ACCEPTED ->
                                "Navigation Terms were not accepted."

                            NavigationApi.ErrorCode.LOCATION_PERMISSION_MISSING ->
                                "Precise location permission is required."

                            else ->
                                "Unable to initialize navigation: $errorCode"
                        }

                    Toast.makeText(
                        requireContext(),
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    private fun startNavigation() {
        val activeNavigator =
            navigator ?: return

        val locationPermissionGranted =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!locationPermissionGranted) {
            showNavigationError(
                "Precise location permission is required."
            )

            return
        }

        val destination = try {
            Waypoint.builder()
                .setLatLng(
                    destinationLatitude,
                    destinationLongitude
                )
                .setTitle(
                    destinationName.ifBlank {
                        "Selected destination"
                    }
                )
                .setVehicleStopover(
                    routeMode == MODE_MOTORCYCLE
                )
                .build()
        } catch (exception: Exception) {
            showNavigationError(
                exception.message
                    ?: "Invalid destination."
            )

            return
        }

        val routingOptions =
            createRoutingOptions()

        activeNavigator
            .setDestination(
                destination,
                routingOptions
            )
            .setOnResultListener { routeStatus ->
                if (!isAdded || view == null) {
                    return@setOnResultListener
                }

                when (routeStatus) {
                    Navigator.RouteStatus.OK -> {
                        beginGuidance(
                            activeNavigator
                        )
                    }

                    Navigator.RouteStatus.NO_ROUTE_FOUND -> {
                        showNavigationError(
                            "No route was found for the selected travel mode."
                        )
                    }

                    Navigator.RouteStatus.NETWORK_ERROR -> {
                        showNavigationError(
                            "Unable to connect to Google Navigation."
                        )
                    }

                    Navigator.RouteStatus.ROUTE_CANCELED -> {
                        showNavigationError(
                            "Route calculation was canceled."
                        )
                    }

                    else -> {
                        showNavigationError(
                            "Unable to calculate route: $routeStatus"
                        )
                    }
                }
            }
    }

    private fun createRoutingOptions(): RoutingOptions {
        val selectedTravelMode =
            when (routeMode) {
                MODE_BICYCLE ->
                    RoutingOptions.TravelMode.CYCLING

                MODE_MOTORCYCLE ->
                    RoutingOptions.TravelMode.TWO_WHEELER

                else ->
                    RoutingOptions.TravelMode.WALKING
            }

        return RoutingOptions().apply {
            travelMode(
                selectedTravelMode
            )
        }
    }


    private fun createNavigationMarkerIcon(
        @DrawableRes drawableResource: Int
    ): BitmapDescriptor? {

        val drawable =
            AppCompatResources.getDrawable(
                requireContext(),
                drawableResource
            ) ?: return null

        val markerSize =
            (24 * resources.displayMetrics.density)
                .toInt()

        drawable.setBounds(
            0,
            0,
            markerSize,
            markerSize
        )

        val bitmap =
            Bitmap.createBitmap(
                markerSize,
                markerSize,
                Bitmap.Config.ARGB_8888
            )

        val canvas =
            Canvas(bitmap)

        drawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(
            bitmap
        )
    }




    private fun getClosestRotation(
        currentRotation: Float,
        targetRotation: Float
    ): Float {
        var difference =
            (targetRotation - currentRotation) % 360f

        if (difference > 180f) {
            difference -= 360f
        }

        if (difference < -180f) {
            difference += 360f
        }

        return currentRotation + difference
    }

    private fun startNavigationMarkerUpdates() {
        val provider =
            NavigationApi.getRoadSnappedLocationProvider(
                requireActivity().application
            ) ?: return

        val listener =
            object :
                RoadSnappedLocationProvider.LocationListener {

                override fun onLocationChanged(
                    location: Location
                ) {
                    if (!isAdded || view == null) {
                        return
                    }

                }
            }

        roadSnappedLocationProvider =
            provider

        roadSnappedLocationListener =
            listener

        provider.addLocationListener(
            listener
        )
    }


    private fun beginGuidance(
        activeNavigator: Navigator
    ) {
        activeNavigator.setAudioGuidance(
            Navigator.AudioGuidance
                .VOICE_ALERTS_AND_GUIDANCE
        )

        activeNavigator.startGuidance()

        configureNavigationCamera()
        startWeatherMonitoring()

        progressNavigation.visibility =
            View.GONE
    }

    private fun startWeatherMonitoring() {
        weatherMonitoringJob?.cancel()

        weatherMonitoringJob =
            viewLifecycleOwner.lifecycleScope.launch {

                while (isActive) {
                    checkWeatherAtCurrentLocation()

                    // Refresh every five minutes.
                    delay(5 * 60 * 1000L)
                }
            }
    }

    private fun checkWeatherAtCurrentLocation() {
        val hasFineLocationPermission =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocationPermission =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (
            !hasFineLocationPermission &&
            !hasCoarseLocationPermission
        ) {
            tvNavigationWeather.text =
                "Weather unavailable"

            return
        }

        val priority =
            if (hasFineLocationPermission) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }

        val cancellationTokenSource =
            CancellationTokenSource()

        locationClient.getCurrentLocation(
            priority,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->

            if (
                location == null ||
                !isAdded ||
                view == null
            ) {
                return@addOnSuccessListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                val result =
                    weatherRepository.getCurrentWeather(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )

                result.onSuccess { weather ->
                    if (isAdded && view != null) {
                        displayNavigationWeather(weather)
                    }
                }.onFailure {
                    if (isAdded && view != null) {
                        tvNavigationWeather.text =
                            "Weather unavailable"
                    }
                }
            }
        }.addOnFailureListener {
            if (isAdded && view != null) {
                tvNavigationWeather.text =
                    "Weather unavailable"
            }
        }
    }

    private fun displayNavigationWeather(
        weather: Weather
    ) {
        val condition =
            WeatherCodeMapper.getDescription(
                weather.weatherCode
            )

        tvNavigationWeather.text =
            String.format(
                Locale.getDefault(),
                "%.1f°C • %s",
                weather.temperature,
                condition
            )

        tvNavigationWeatherIcon.text =
            getNavigationWeatherIcon(
                weather.weatherCode
            )

        val hasThunderstorm =
            weather.weatherCode in 95..99

        val hasHeavyRain =
            weather.weatherCode in 63..67 ||
                    weather.weatherCode in 81..82

        val hasRain =
            weather.weatherCode in 51..67 ||
                    weather.weatherCode in 80..82

        when {
            hasThunderstorm -> {
                showWeatherWarning(
                    weatherCode = weather.weatherCode,
                    title = "Thunderstorm warning",
                    message =
                    "Thunderstorms are detected near your current route. " +
                            "Consider stopping at a safe location."
                )
            }

            hasHeavyRain -> {
                showWeatherWarning(
                    weatherCode = weather.weatherCode,
                    title = "Heavy rain warning",
                    message =
                    "Heavy rain is detected near your current route. " +
                            "Reduce speed and use extra caution."
                )
            }

            hasRain -> {
                showWeatherWarning(
                    weatherCode = weather.weatherCode,
                    title = "Rain detected",
                    message =
                    "Rain is detected near your current location. " +
                            "Roads may become slippery."
                )
            }

            else -> {
                weatherWarningPanel.visibility =
                    View.GONE

                lastWarnedWeatherCode = null
            }
        }
    }

    private fun showWeatherWarning(
        weatherCode: Int,
        title: String,
        message: String
    ) {
        tvWeatherWarningTitle.text = title
        tvWeatherWarningMessage.text = message

        weatherWarningPanel.visibility =
            View.VISIBLE

        // Prevent the same warning from repeatedly appearing.
        if (lastWarnedWeatherCode != weatherCode) {
            lastWarnedWeatherCode = weatherCode

            Toast.makeText(
                requireContext(),
                title,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun getNavigationWeatherIcon(
        weatherCode: Int
    ): String {
        return when {
            weatherCode == 0 ->
                "☀️"

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

            weatherCode in 95..99 ->
                "⛈️"

            else ->
                "☁️"
        }
    }


    private fun configureNavigationCamera() {
        val activeNavigationFragment =
            navigationFragment ?: return

        activeNavigationFragment.setForceNightMode(
            ForceNightMode.FORCE_NIGHT
        )

        activeNavigationFragment.setHeaderEnabled(false)
        activeNavigationFragment.setEtaCardEnabled(false)

        activeNavigationFragment.getMapAsync {
                googleMap: GoogleMap ->

            if (!isAdded) {
                return@getMapAsync
            }

            val hasLocationPermission =
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

            if (!hasLocationPermission) {
                showNavigationError(
                    "Precise location permission is required."
                )

                return@getMapAsync
            }

            this.googleMap = googleMap
            startNavigationMarkerUpdates()

            try {
                // Same dark palette as MapDashboardFragment.
                googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.google_map_dark_style
                    )
                )

                // Enable 3D buildings and traffic.
                googleMap.isBuildingsEnabled = true
                googleMap.isTrafficEnabled = true

                // Tilted turn-by-turn navigation camera.
                googleMap.followMyLocation(
                    CameraPerspective.TILTED
                )
            } catch (exception: SecurityException) {
                showNavigationError(
                    "Location permission was rejected."
                )
            } catch (exception: Exception) {
                showNavigationError(
                    exception.message
                        ?: "Unable to configure navigation map."
                )
            }
        }
    }


    private fun stopNavigationMarkerUpdates() {
        val provider =
            roadSnappedLocationProvider

        val listener =
            roadSnappedLocationListener

        if (
            provider != null &&
            listener != null
        ) {
            provider.removeLocationListener(
                listener
            )
        }

        roadSnappedLocationListener = null
        roadSnappedLocationProvider = null



        googleMap = null
    }


    private fun showNavigationError(
        message: String
    ) {
        if (!isAdded || view == null) {
            return
        }

        progressNavigation.visibility =
            View.GONE

        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun closeNavigation() {
        navigator?.stopGuidance()
        navigator?.clearDestinations()

        exitFullScreenMode()

        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        weatherMonitoringJob?.cancel()
        weatherMonitoringJob = null

        stopNavigationMarkerUpdates()

        exitFullScreenMode()

        navigationFragment = null
        navigator = null

        super.onDestroyView()
    }

    companion object {

        private const val NAVIGATION_FRAGMENT_TAG =
            "google_navigation_fragment"

        private const val ARG_DESTINATION_LATITUDE =
            "destination_latitude"

        private const val ARG_DESTINATION_LONGITUDE =
            "destination_longitude"

        private const val ARG_DESTINATION_NAME =
            "destination_name"

        private const val ARG_ROUTE_MODE =
            "route_mode"

        const val MODE_WALKING =
            "walking"

        const val MODE_BICYCLE =
            "bicycle"

        const val MODE_MOTORCYCLE =
            "motorcycle"

        fun newInstance(
            latitude: Double,
            longitude: Double,
            destinationName: String,
            routeMode: String
        ): GoogleNavigationFragment {
            return GoogleNavigationFragment().apply {
                arguments = Bundle().apply {
                    putDouble(
                        ARG_DESTINATION_LATITUDE,
                        latitude
                    )

                    putDouble(
                        ARG_DESTINATION_LONGITUDE,
                        longitude
                    )

                    putString(
                        ARG_DESTINATION_NAME,
                        destinationName
                    )

                    putString(
                        ARG_ROUTE_MODE,
                        routeMode
                    )
                }
            }
        }
    }
}