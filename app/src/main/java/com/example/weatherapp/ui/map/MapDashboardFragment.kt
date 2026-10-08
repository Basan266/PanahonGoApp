package com.example.weatherapp.ui.map

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.R
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.domain.model.DeviceLocation
import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.ui.home.HomeViewModel
import com.example.weatherapp.ui.home.HomeViewModelFactory
import com.example.weatherapp.util.WeatherCodeMapper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import android.location.Location
import android.widget.ImageButton
import com.example.weatherapp.data.remote.route.OsrmGeometry
import com.example.weatherapp.data.remote.route.OsrmRoute
import kotlin.math.ceil
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.example.weatherapp.data.remote.geocoding.PhotonApiClient
import com.example.weatherapp.data.remote.geocoding.PhotonFeature
import com.example.weatherapp.ui.navigation.GoogleNavigationFragment
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions




class MapDashboardFragment :
    Fragment(R.layout.fragment_map_dashboard) {


    private enum class RouteMode(
        val costing: String
    ) {
        WALKING("pedestrian"),
        BICYCLE("bicycle"),
        MOTORCYCLE("motorcycle")
    }

    private var selectedRouteMode =
        RouteMode.WALKING


    private lateinit var mapView: MapView
    private lateinit var locationClient: FusedLocationProviderClient

    private lateinit var etPlaceSearch: AutoCompleteTextView
    private lateinit var tvSearchResult: TextView

    private lateinit var routeModeGroup:
            MaterialButtonToggleGroup

    private lateinit var rbWalking:
            MaterialButton

    private lateinit var rbMotorcycle:
            MaterialButton


    private var currentLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null
    private var destinationName: String = ""

    private var suggestionJob: Job? = null

    private var suggestionAdapter:
            ArrayAdapter<String>? = null

    private val suggestionPlaces =
        linkedMapOf<String, PhotonFeature>()


    private lateinit var progressWeather: ProgressBar
    private lateinit var tvOutdoorStatus: TextView
    private lateinit var tvOutdoorMessage: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvCondition: TextView
    private lateinit var tvWeatherDetails: TextView

    private lateinit var searchPanel: View
    private lateinit var weatherPanel: View
    private lateinit var btnExitNavigation: ImageButton

    private var isNavigationMode = false

    private val weatherRepository by lazy {
        val application =
            requireActivity().application as WeatherApplication

        application.weatherRepository
    }

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(weatherRepository)
    }
    private var googleMap: GoogleMap? = null

    /*
     * Stores the requested 3D camera position while
     * Google Maps is still loading its map tiles.
     */
    private var pending3DLocation:
            LatLng? = null

    private var pending3DBearing:
            Float = 0f

    /*
     * Used to retry the 3D camera after the map
     * and building tiles have had time to load.
     */
    private var cameraRetryJob:
            Job? = null

    private var routeOutlinePolyline:
            Polyline? = null

    private var routePolyline:
            Polyline? = null



    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineLocationGranted =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true

            val coarseLocationGranted =
                permissions[
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ] == true

            if (
                fineLocationGranted ||
                coarseLocationGranted
            ) {
                loadCurrentLocation()
            } else {
                showLocationPermissionRequired()
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

        mapView =
            view.findViewById(R.id.mapView)

        etPlaceSearch =
            view.findViewById(R.id.etPlaceSearch)

        tvSearchResult =
            view.findViewById(R.id.tvSearchResult)

        progressWeather =
            view.findViewById(R.id.progressWeather)

        tvOutdoorStatus =
            view.findViewById(R.id.tvOutdoorStatus)

        tvOutdoorMessage =
            view.findViewById(R.id.tvOutdoorMessage)

        tvTemperature =
            view.findViewById(R.id.tvTemperature)

        tvCondition =
            view.findViewById(R.id.tvCondition)

        tvWeatherDetails =
            view.findViewById(R.id.tvWeatherDetails)

        searchPanel =
            view.findViewById(R.id.search_panel)

        weatherPanel =
            view.findViewById(R.id.weatherPanel)

        btnExitNavigation =
            view.findViewById(R.id.btnExitNavigation)

        btnExitNavigation.setOnClickListener {
            exitNavigationMode()
        }

        routeModeGroup =
            view.findViewById(R.id.routeModeGroup)

        rbWalking =
            view.findViewById(R.id.rbWalking)

        rbMotorcycle =
            view.findViewById(R.id.rbMotorcycle)



        locationClient =
            LocationServices.getFusedLocationProviderClient(
                requireActivity()
            )

        configureSearch()
        configurePlaceSuggestions()

        mapView.onCreate(savedInstanceState)

        tvSearchResult.text =
            "Finding your current location…"


        mapView.getMapAsync { map ->
            googleMap = map

            configureMapControls(map)

            map.mapType =
                GoogleMap.MAP_TYPE_NORMAL

            map.isBuildingsEnabled = true
            map.isTrafficEnabled = true

            runCatching {
                map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.google_map_dark_style
                    )
                )
            }

            /*
             * Start retrieving the location immediately.
             */
            checkLocationPermission()

            /*
             * Reapply the 3D camera after Google Maps
             * finishes loading its initial map tiles.
             */
            map.setOnMapLoadedCallback {
                map.isBuildingsEnabled = true

                currentLatLng?.let { currentLocation ->
                    moveCameraTo3D(
                        location = currentLocation
                    )
                }
            }
        }



        configureNormalScreen(view)
        configureRouteMode()
        observeWeather()
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



    private fun moveCameraTo3D(
        location: LatLng,
        bearing: Float = 0f
    ) {
        /*
         * Remember the latest requested camera position.
         */
        pending3DLocation = location
        pending3DBearing = bearing

        /*
         * Move immediately so the user sees the
         * location without waiting.
         */
        apply3DCamera(
            location = location,
            bearing = bearing,
            animate = true
        )

        /*
         * Apply the same camera again after the map
         * has loaded the nearby building tiles.
         */
        cameraRetryJob?.cancel()

        cameraRetryJob =
            viewLifecycleOwner.lifecycleScope.launch {
                delay(1_500)

                if (
                    isAdded &&
                    googleMap != null
                ) {
                    apply3DCamera(
                        location = pending3DLocation
                            ?: location,
                        bearing = pending3DBearing,
                        animate = false
                    )
                }
            }
    }



    private fun apply3DCamera(
        location: LatLng,
        bearing: Float,
        animate: Boolean
    ) {
        val map =
            googleMap ?: return

        map.isBuildingsEnabled = true

        val cameraPosition =
            CameraPosition.Builder()
                .target(location)
                .zoom(18.0f)
                .tilt(60f)
                .bearing(bearing)
                .build()

        val cameraUpdate =
            CameraUpdateFactory.newCameraPosition(
                cameraPosition
            )

        if (animate) {
            map.animateCamera(
                cameraUpdate,
                1_200,
                null
            )
        } else {
            map.moveCamera(
                cameraUpdate
            )
        }
    }



    private fun enterNavigationMode() {
        isNavigationMode = true

        searchPanel.visibility = View.GONE
        weatherPanel.visibility = View.GONE

        btnExitNavigation.visibility = View.VISIBLE
    }

    private fun exitNavigationMode() {
        isNavigationMode = false

        searchPanel.visibility = View.VISIBLE
        weatherPanel.visibility = View.VISIBLE

        btnExitNavigation.visibility = View.GONE

        removeNavigationRoute()

        currentLatLng?.let { current ->
            displayCurrentLocation(
                latitude = current.latitude,
                longitude = current.longitude
            )
        }
    }


    private fun configureRouteMode() {
        routeModeGroup.addOnButtonCheckedListener {
                _,
                checkedId,
                isChecked ->

            if (!isChecked) {
                return@addOnButtonCheckedListener
            }

            selectedRouteMode =
                when (checkedId) {
                    R.id.rbMotorcycle ->
                        RouteMode.MOTORCYCLE

                    else ->
                        RouteMode.WALKING
                }

            tvSearchResult.text =
                when (selectedRouteMode) {
                    RouteMode.MOTORCYCLE ->
                        "Motorcycle route selected"

                    else ->
                        "Walking route selected"
                }
        }
    }


    private fun removeNavigationRoute() {
        routePolyline?.remove()
        routeOutlinePolyline?.remove()
        routePolyline = null
        routeOutlinePolyline = null
    }

    private fun configureSearch() {
        etPlaceSearch.setOnEditorActionListener {
                _,
                actionId,
                _ ->

            if (
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_GO ||
                actionId == EditorInfo.IME_ACTION_DONE
            ) {
                searchPlace()
                true
            } else {
                false
            }
        }
    }

    private fun configurePlaceSuggestions() {
        etPlaceSearch.threshold = 3

        etPlaceSearch.doAfterTextChanged { editable ->
            val query = editable
                ?.toString()
                ?.trim()
                .orEmpty()

            suggestionJob?.cancel()

            if (query.length < 3) {
                suggestionPlaces.clear()
                etPlaceSearch.dismissDropDown()
                return@doAfterTextChanged
            }

            suggestionJob =
                viewLifecycleOwner.lifecycleScope.launch {
                    // Mas mabilis kaysa dating 650 milliseconds.
                    delay(350)

                    loadPlaceSuggestions(query)
                }
        }

        etPlaceSearch.setOnItemClickListener {
                parent,
                _,
                position,
                _ ->

            suggestionJob?.cancel()

            val selectedText =
                parent.getItemAtPosition(position)
                    .toString()

            val selectedPlace =
                suggestionPlaces[selectedText]
                    ?: return@setOnItemClickListener

            val destination =
                selectedPlace.toMapLatLng()
                    ?: return@setOnItemClickListener

            destinationName = selectedText
            destinationLatLng = destination

            etPlaceSearch.setText(
                selectedText,
                false
            )

            etPlaceSearch.dismissDropDown()
            hideKeyboard()

            displayDestination(destination)

            // Automatic navigation pagkatapos pumili.
            openNavigation(destination)
        }
    }


    private suspend fun loadPlaceSuggestions(
        query: String
    ) {
        val locationBias = currentLatLng

        val features = try {
            PhotonApiClient.api.searchPlaces(
                query = query,
                limit = 8,
                latitude = locationBias?.latitude,
                longitude = locationBias?.longitude,
                language = "en"
            ).features
        } catch (_: Exception) {
            emptyList()
        }

        if (!isAdded || view == null) {
            return
        }

        // Huwag ipakita ang lumang results kapag may
        // bago nang tina-type ang user.
        if (
            etPlaceSearch.text
                .toString()
                .trim() != query
        ) {
            return
        }

        suggestionPlaces.clear()

        features
            .filter {
                it.geometry.coordinates.size >= 2
            }
            .forEach { feature ->
                val displayName =
                    createSuggestionName(feature)

                suggestionPlaces[displayName] = feature
            }

        val suggestions =
            suggestionPlaces.keys.toList()

        suggestionAdapter?.clear()

        suggestionAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            ArrayList(suggestions)
        )

        etPlaceSearch.setAdapter(
            suggestionAdapter
        )


        if (
            suggestions.isNotEmpty() &&
            etPlaceSearch.hasFocus()
        ) {
            etPlaceSearch.showDropDown()
        } else if (suggestions.isEmpty()) {
            tvSearchResult.text =
                "No matching places found."
        }
    }


    private fun createSuggestionName(
        feature: PhotonFeature
    ): String {
        val properties = feature.properties

        val displayName = listOfNotNull(
            properties.name.cleanPlacePart(),
            properties.street.cleanPlacePart(),
            properties.district.cleanPlacePart(),
            properties.city.cleanPlacePart(),
            properties.county.cleanPlacePart(),
            properties.state.cleanPlacePart()
        )
            .distinct()
            .joinToString(", ")

        return displayName.ifBlank {
            val coordinates =
                feature.geometry.coordinates

            if (coordinates.size >= 2) {
                String.format(
                    Locale.getDefault(),
                    "%.5f, %.5f",
                    coordinates[1],
                    coordinates[0]
                )
            } else {
                "Unknown location"
            }
        }
    }

    private fun String?.cleanPlacePart(): String? {
        return this
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun PhotonFeature.toMapLatLng(): LatLng? {
        val coordinates = geometry.coordinates

        if (coordinates.size < 2) {
            return null
        }

        val longitude = coordinates[0]
        val latitude = coordinates[1]

        if (
            latitude !in -90.0..90.0 ||
            longitude !in -180.0..180.0
        ) {
            return null
        }

        return LatLng(
            latitude,
            longitude
        )
    }


    private fun searchPlace() {
        val query =
            etPlaceSearch.text
                .toString()
                .trim()

        if (query.isEmpty()) {
            etPlaceSearch.error =
                "Enter a place, building, or address."

            return
        }

        suggestionJob?.cancel()
        etPlaceSearch.dismissDropDown()
        hideKeyboard()

        tvSearchResult.text =
            "Searching for $query…"

        viewLifecycleOwner.lifecycleScope.launch {
            val existingSuggestion =
                suggestionPlaces[query]

            val feature = existingSuggestion ?: try {
                val locationBias = currentLatLng

                PhotonApiClient.api.searchPlaces(
                    query = query,
                    limit = 1,
                    latitude = locationBias?.latitude,
                    longitude = locationBias?.longitude,
                    language = "en"
                ).features.firstOrNull()
            } catch (_: Exception) {
                null
            }

            val destination =
                feature?.toMapLatLng()

            if (feature == null || destination == null) {
                tvSearchResult.text =
                    "Place not found. Try the complete place name."

                Toast.makeText(
                    requireContext(),
                    "Unable to find that location.",
                    Toast.LENGTH_SHORT
                ).show()

                return@launch
            }

            destinationName =
                createSuggestionName(feature)

            destinationLatLng = destination

            etPlaceSearch.setText(
                destinationName,
                false
            )

            displayDestination(destination)

            // Automatic navigation kapag keyboard Search ang ginamit.
            openNavigation(destination)
        }
    }


    private fun displayDestination(
        destination: LatLng
    ) {
        val map =
            googleMap ?: return

        // Remove previous markers and polylines.
        map.clear()

        routePolyline = null
        routeOutlinePolyline = null

        // Current-location marker.
        currentLatLng?.let { current ->
            map.addMarker(
                MarkerOptions()
                    .position(current)
                    .title("Your location")
                    .snippet("Starting point")
            )
        }

        // Destination marker.
        map.addMarker(
            MarkerOptions()
                .position(destination)
                .title(
                    destinationName.ifBlank {
                        "Selected destination"
                    }
                )
                .snippet("Destination")
        )

        // Point the 3D camera toward the destination.
        val cameraBearing =
            currentLatLng?.let { current ->
                calculateBearing(
                    current,
                    destination
                ).toFloat()
            } ?: 0f

        moveCameraTo3D(
            location = destination,
            bearing = cameraBearing
        )

        tvSearchResult.text =
            destinationName.ifBlank {
                "Selected destination"
            }

        viewModel.loadWeather(
            DeviceLocation(
                latitude = destination.latitude,
                longitude = destination.longitude
            )
        )
    }



    private fun openNavigation(
        destination: LatLng
    ) {
        if (currentLatLng == null) {
            Toast.makeText(
                requireContext(),
                "Current location is not available yet.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val fineLocationGranted =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted) {
            Toast.makeText(
                requireContext(),
                "Allow precise location before starting navigation.",
                Toast.LENGTH_LONG
            ).show()

            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )

            return
        }

        val googleRouteMode =
            when (selectedRouteMode) {
                RouteMode.BICYCLE ->
                    GoogleNavigationFragment.MODE_BICYCLE

                RouteMode.MOTORCYCLE ->
                    GoogleNavigationFragment.MODE_MOTORCYCLE

                RouteMode.WALKING ->
                    GoogleNavigationFragment.MODE_WALKING
            }

        val navigationScreen =
            GoogleNavigationFragment.newInstance(
                latitude = destination.latitude,
                longitude = destination.longitude,
                destinationName =
                destinationName.ifBlank {
                    "Selected destination"
                },
                routeMode = googleRouteMode
            )

        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                navigationScreen
            )
            .addToBackStack(
                "map_dashboard"
            )
            .commit()
    }


    private fun getRouteModeName(): String {
        return when (selectedRouteMode) {
            RouteMode.WALKING ->
                "walking"

            RouteMode.BICYCLE ->
                "bicycle"

            RouteMode.MOTORCYCLE ->
                "motorcycle"
        }
    }

    private fun showRouteError(
        message: String
    ) {
        progressWeather.visibility =
            View.GONE

        tvSearchResult.text =
            "Unable to calculate route."

        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_LONG
        ).show()
    }


    private fun drawRouteOnMap(
        origin: LatLng,
        route: OsrmRoute
    ) {
        val map = googleMap ?: return

        val coordinates =
            route.geometry.coordinates

        if (coordinates.size < 2) {
            showRouteError(
                "The route does not contain enough points."
            )

            return
        }

        val routePoints = coordinates.mapNotNull { coordinate ->
            if (coordinate.size >= 2) {
                LatLng(coordinate[1], coordinate[0])
            } else {
                null
            }
        }

        removeNavigationRoute()

        routeOutlinePolyline = map.addPolyline(
            PolylineOptions()
                .addAll(routePoints)
                .color(android.graphics.Color.rgb(32, 32, 32))
                .width(18f)
                .zIndex(1f)
        )

        routePolyline = map.addPolyline(
            PolylineOptions()
                .addAll(routePoints)
                .color(android.graphics.Color.rgb(198, 92, 244))
                .width(11f)
                .zIndex(2f)
        )

        moveCameraToNavigationView(
            origin = origin,
            geometry = route.geometry
        )

        enterNavigationMode()
        progressWeather.visibility = View.GONE
    }


    private fun moveCameraToNavigationView(
        origin: LatLng,
        geometry: OsrmGeometry
    ) {
        val map = googleMap ?: return

        val nextCoordinate =
            geometry.coordinates
                .drop(1)
                .firstOrNull()
                ?: return

        if (nextCoordinate.size < 2) {
            return
        }

        val nextPosition =
            LatLng(
                nextCoordinate[1],
                nextCoordinate[0]
            )

        val bearing =
            calculateBearing(
                origin,
                nextPosition
            )

        val navigationCamera =
            CameraPosition.Builder()
                .target(origin)
                .zoom(17.5f)
                .tilt(60.0f)
                .bearing(bearing.toFloat())
                .build()

        map.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                navigationCamera
            ),
            1800,
            null
        )
    }



    private fun distanceBetween(
        origin: LatLng,
        destination: LatLng
    ): Float {
        val results = FloatArray(1)

        Location.distanceBetween(
            origin.latitude,
            origin.longitude,
            destination.latitude,
            destination.longitude,
            results
        )

        return results[0]
    }

    private fun calculateBearing(
        origin: LatLng,
        destination: LatLng
    ): Double {
        val startLocation =
            Location("route-start").apply {
                latitude = origin.latitude
                longitude = origin.longitude
            }

        val nextLocation =
            Location("route-next").apply {
                latitude = destination.latitude
                longitude = destination.longitude
            }

        return startLocation
            .bearingTo(nextLocation)
            .toDouble()
    }

    private fun displayRouteInformation(
        route: OsrmRoute
    ) {
        val distanceKilometers =
            route.distance / 1000.0

        val durationMinutes =
            ceil(
                route.duration / 60.0
            ).toInt()

        val firstStep =
            route.legs
                .flatMap { leg ->
                    leg.steps
                }
                .firstOrNull { step ->
                    step.distance > 5.0
                }

        tvSearchResult.text =
            String.format(
                Locale.getDefault(),
                "%s • %s • %d min • %.1f km",
                destinationName,
                getRouteModeName(),
                durationMinutes,
                distanceKilometers
            )

        tvOutdoorStatus.text =
            "Navigation Ready"

        tvOutdoorMessage.text =
            if (firstStep != null) {
                createTurnInstruction(
                    type = firstStep.maneuver.type,
                    modifier =
                    firstStep.maneuver.modifier,
                    roadName = firstStep.name
                )
            } else {
                "Follow the highlighted route."
            }
    }


    private fun createTurnInstruction(
        type: String,
        modifier: String?,
        roadName: String
    ): String {
        val direction =
            when (modifier) {
                "left" ->
                    "Turn left"

                "right" ->
                    "Turn right"

                "slight left" ->
                    "Keep slightly left"

                "slight right" ->
                    "Keep slightly right"

                "sharp left" ->
                    "Make a sharp left"

                "sharp right" ->
                    "Make a sharp right"

                "straight" ->
                    "Continue straight"

                "uturn" ->
                    "Make a U-turn"

                else ->
                    when (type) {
                        "depart" ->
                            "Start"

                        "arrive" ->
                            "You have arrived"

                        "roundabout" ->
                            "Enter the roundabout"

                        else ->
                            "Continue"
                    }
            }

        return if (roadName.isBlank()) {
            direction
        } else {
            "$direction onto $roadName"
        }
    }



    private fun hideKeyboard() {
        val inputMethodManager =
            requireContext().getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager

        inputMethodManager.hideSoftInputFromWindow(
            etPlaceSearch.windowToken,
            0
        )

        etPlaceSearch.clearFocus()
    }

    private fun configureMapControls(
        map: GoogleMap
    ) {
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true
        map.uiSettings.isScrollGesturesEnabled = true
        map.uiSettings.isRotateGesturesEnabled = true
        map.uiSettings.isTiltGesturesEnabled = true
        map.uiSettings.isMapToolbarEnabled = false
    }

    private fun checkLocationPermission() {
        val fineLocationGranted =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (
            fineLocationGranted ||
            coarseLocationGranted
        ) {
            loadCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun observeWeather() {
        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->

            progressWeather.visibility =
                if (state.isLoading) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            state.weather?.let { weather ->
                displayWeather(weather)
            }

            state.errorMessage?.let { message ->
                tvOutdoorStatus.text =
                    "Weather unavailable"

                tvOutdoorMessage.text = message
            }
        }
    }

    private fun displayWeather(
        weather: Weather
    ) {
        val condition =
            WeatherCodeMapper.getDescription(
                weather.weatherCode
            )

        val weatherEmoji =
            getWeatherEmoji(
                weather.weatherCode
            )

        tvTemperature.text =
            String.format(
                Locale.getDefault(),
                "%.1f°C",
                weather.temperature
            )

        tvCondition.text =
            "$weatherEmoji $condition"

        tvWeatherDetails.text =
            String.format(
                Locale.getDefault(),
                "Humidity: %d%%   •   Wind: %.1f km/h",
                weather.humidity,
                weather.windSpeed
            )

        val outdoorReady =
            isOutdoorWeatherSuitable(weather)

        if (outdoorReady) {
            tvOutdoorStatus.text =
                "Outdoor Ready"

            tvOutdoorMessage.text =
                "Conditions are good for being outside today."
        } else {
            tvOutdoorStatus.text =
                "Take Extra Care"

            tvOutdoorMessage.text =
                "Current conditions may affect your outdoor activities."
        }
    }



    private fun isOutdoorWeatherSuitable(
        weather: Weather
    ): Boolean {
        val hasRain =
            weather.weatherCode in 51..67 ||
                    weather.weatherCode in 80..82 ||
                    weather.weatherCode in 95..99

        val comfortableTemperature =
            weather.temperature in 18.0..33.0

        val safeWind =
            weather.windSpeed <= 25.0

        return !hasRain &&
                comfortableTemperature &&
                safeWind
    }

    private fun loadCurrentLocation() {
        val fineLocationGranted =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (
            !fineLocationGranted &&
            !coarseLocationGranted
        ) {
            showLocationPermissionRequired()
            return
        }

        tvSearchResult.text =
            "Getting your exact location…"

        val priority =
            if (fineLocationGranted) {
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

            if (location != null) {
                displayCurrentLocation(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            } else {
                loadLastKnownLocation()
            }

        }.addOnFailureListener { exception ->

            tvSearchResult.text =
                "Unable to retrieve your location"

            Toast.makeText(
                requireContext(),
                exception.message
                    ?: "Please check if GPS is enabled.",
                Toast.LENGTH_LONG
            ).show()
        }
    }



    private fun loadLastKnownLocation() {
        val permissionGranted =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            showLocationPermissionRequired()
            return
        }

        locationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    displayCurrentLocation(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                } else {
                    tvSearchResult.text =
                        "Turn on GPS and try again"

                    Toast.makeText(
                        requireContext(),
                        "Unable to determine your location.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { exception ->
                tvSearchResult.text =
                    "Unable to retrieve your location"

                Toast.makeText(
                    requireContext(),
                    exception.message
                        ?: "Unable to determine your location.",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun displayCurrentLocation(
        latitude: Double,
        longitude: Double
    ) {
        val map = googleMap ?: return

        val currentPosition =
            LatLng(
                latitude,
                longitude
            )

        currentLatLng = currentPosition

        map.clear()

        map.addMarker(
            MarkerOptions()
                .position(currentPosition)
                .title("You are here")
        )

        moveCameraTo3D(
            location = currentPosition
        )

        tvSearchResult.text =
            "Search for a place you want to visit"

        viewModel.loadWeather(
            DeviceLocation(
                latitude = latitude,
                longitude = longitude
            )
        )
    }

    private fun showLocationPermissionRequired() {
        tvSearchResult.text =
            "Location permission is required"

        Toast.makeText(
            requireContext(),
            "Allow location access to show your position.",
            Toast.LENGTH_LONG
        ).show()
    }


    private fun getWeatherEmoji(
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


    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroyView() {
        suggestionJob?.cancel()
        suggestionJob = null

        etPlaceSearch.dismissDropDown()
        etPlaceSearch.setAdapter(null)

        suggestionAdapter = null
        suggestionPlaces.clear()

        routePolyline = null
        routeOutlinePolyline = null
        googleMap = null
        mapView.onDestroy()

        super.onDestroyView()
    }


    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
