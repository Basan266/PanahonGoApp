package com.example.weatherapp.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.example.weatherapp.domain.model.DeviceLocation
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class LocationHelper(
    context: Context
) {

    private val applicationContext =
        context.applicationContext

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(
            applicationContext
        )

    private val locationManager =
        applicationContext.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager

    private val mainHandler =
        Handler(
            Looper.getMainLooper()
        )

    private var cancellationTokenSource:
            CancellationTokenSource? = null

    private var platformLocationListener:
            LocationListener? = null

    private var platformTimeoutRunnable:
            Runnable? = null

    private var bestPlatformLocation:
            Location? = null

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        onSuccess: (DeviceLocation) -> Unit,
        onError: (String) -> Unit
    ) {
        cancelLocationRequest()

        val currentTokenSource =
            CancellationTokenSource()

        cancellationTokenSource =
            currentTokenSource

        val currentLocationRequest =
            CurrentLocationRequest.Builder()
                .setPriority(
                    Priority.PRIORITY_HIGH_ACCURACY
                )
                .setGranularity(
                    Granularity.GRANULARITY_FINE
                )
                .setMaxUpdateAgeMillis(
                    0L
                )
                .setDurationMillis(
                    FUSED_LOCATION_TIMEOUT_MILLIS
                )
                .build()

        try {
            fusedLocationClient.getCurrentLocation(
                currentLocationRequest,
                currentTokenSource.token
            ).addOnSuccessListener { location ->

                if (
                    cancellationTokenSource !==
                    currentTokenSource
                ) {
                    return@addOnSuccessListener
                }

                if (
                    location != null &&
                    isLocationAccurate(location)
                ) {
                    finishWithSuccess(
                        currentTokenSource = currentTokenSource,
                        location = location,
                        onSuccess = onSuccess
                    )
                } else {
                    startPlatformLocationFallback(
                        currentTokenSource = currentTokenSource,
                        onSuccess = onSuccess,
                        onError = onError
                    )
                }
            }.addOnFailureListener {

                if (
                    cancellationTokenSource !==
                    currentTokenSource
                ) {
                    return@addOnFailureListener
                }

                /*
                 * Google Play Services may be unavailable,
                 * outdated or invalid. Use Android's native
                 * GPS and network providers instead.
                 */
                startPlatformLocationFallback(
                    currentTokenSource = currentTokenSource,
                    onSuccess = onSuccess,
                    onError = onError
                )
            }
        } catch (exception: Exception) {
            if (
                cancellationTokenSource ===
                currentTokenSource
            ) {
                startPlatformLocationFallback(
                    currentTokenSource = currentTokenSource,
                    onSuccess = onSuccess,
                    onError = onError
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startPlatformLocationFallback(
        currentTokenSource: CancellationTokenSource,
        onSuccess: (DeviceLocation) -> Unit,
        onError: (String) -> Unit
    ) {
        if (
            cancellationTokenSource !==
            currentTokenSource
        ) {
            return
        }

        stopPlatformLocationUpdates()

        bestPlatformLocation = null

        val availableProviders =
            getAvailableProviders()

        if (availableProviders.isEmpty()) {
            finishWithError(
                currentTokenSource = currentTokenSource,
                message =
                "GPS and network location are unavailable. " +
                        "Turn on Location Services and try again.",
                onError = onError
            )

            return
        }

        val recentLocation =
            getBestLastKnownLocation(
                availableProviders
            )

        if (
            recentLocation != null &&
            isRecentLocation(recentLocation) &&
            isLocationAccurate(recentLocation)
        ) {
            finishWithSuccess(
                currentTokenSource = currentTokenSource,
                location = recentLocation,
                onSuccess = onSuccess
            )

            return
        }

        bestPlatformLocation =
            recentLocation

        val listener =
            object : LocationListener {

                override fun onLocationChanged(
                    location: Location
                ) {
                    if (
                        cancellationTokenSource !==
                        currentTokenSource
                    ) {
                        return
                    }

                    updateBestPlatformLocation(
                        location
                    )

                    if (isLocationAccurate(location)) {
                        finishWithSuccess(
                            currentTokenSource =
                            currentTokenSource,

                            location =
                            location,

                            onSuccess =
                            onSuccess
                        )
                    }
                }

                @Deprecated(
                    "Deprecated in Android"
                )
                override fun onStatusChanged(
                    provider: String?,
                    status: Int,
                    extras: Bundle?
                ) {
                    // No action required.
                }

                override fun onProviderEnabled(
                    provider: String
                ) {
                    // Wait for the next location update.
                }

                override fun onProviderDisabled(
                    provider: String
                ) {
                    // Other providers may still return a location.
                }
            }

        platformLocationListener =
            listener

        try {
            availableProviders.forEach { provider ->
                locationManager.requestLocationUpdates(
                    provider,
                    MINIMUM_UPDATE_TIME_MILLIS,
                    MINIMUM_UPDATE_DISTANCE_METERS,
                    listener,
                    Looper.getMainLooper()
                )
            }
        } catch (exception: SecurityException) {
            finishWithError(
                currentTokenSource = currentTokenSource,
                message =
                "Location permission is required.",
                onError = onError
            )

            return
        } catch (exception: Exception) {
            finishWithError(
                currentTokenSource = currentTokenSource,
                message =
                exception.message
                    ?: "Unable to start GPS location.",
                onError = onError
            )

            return
        }

        val timeoutRunnable =
            Runnable {
                if (
                    cancellationTokenSource !==
                    currentTokenSource
                ) {
                    return@Runnable
                }

                val bestLocation =
                    bestPlatformLocation

                if (
                    bestLocation != null &&
                    isAcceptableFallbackLocation(
                        bestLocation
                    )
                ) {
                    finishWithSuccess(
                        currentTokenSource =
                        currentTokenSource,

                        location =
                        bestLocation,

                        onSuccess =
                        onSuccess
                    )
                } else {
                    finishWithError(
                        currentTokenSource =
                        currentTokenSource,

                        message =
                        "Unable to get an accurate GPS location. " +
                                "Move near a window or go outdoors, " +
                                "then try again.",

                        onError =
                        onError
                    )
                }
            }

        platformTimeoutRunnable =
            timeoutRunnable

        mainHandler.postDelayed(
            timeoutRunnable,
            PLATFORM_LOCATION_TIMEOUT_MILLIS
        )
    }

    private fun getAvailableProviders():
            List<String> {
        val providers =
            mutableListOf<String>()

        if (
            isProviderEnabled(
                LocationManager.GPS_PROVIDER
            )
        ) {
            providers.add(
                LocationManager.GPS_PROVIDER
            )
        }

        if (
            isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        ) {
            providers.add(
                LocationManager.NETWORK_PROVIDER
            )
        }

        return providers
    }

    private fun isProviderEnabled(
        provider: String
    ): Boolean {
        return try {
            locationManager.isProviderEnabled(
                provider
            )
        } catch (exception: Exception) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    private fun getBestLastKnownLocation(
        providers: List<String>
    ): Location? {
        return providers
            .mapNotNull { provider ->
                try {
                    locationManager.getLastKnownLocation(
                        provider
                    )
                } catch (exception: Exception) {
                    null
                }
            }
            .minByOrNull { location ->
                if (location.hasAccuracy()) {
                    location.accuracy
                } else {
                    Float.MAX_VALUE
                }
            }
    }

    private fun updateBestPlatformLocation(
        newLocation: Location
    ) {
        val currentBest =
            bestPlatformLocation

        if (
            currentBest == null ||
            isBetterLocation(
                newLocation = newLocation,
                currentLocation = currentBest
            )
        ) {
            bestPlatformLocation =
                newLocation
        }
    }

    private fun isBetterLocation(
        newLocation: Location,
        currentLocation: Location
    ): Boolean {
        val newAccuracy =
            if (newLocation.hasAccuracy()) {
                newLocation.accuracy
            } else {
                Float.MAX_VALUE
            }

        val currentAccuracy =
            if (currentLocation.hasAccuracy()) {
                currentLocation.accuracy
            } else {
                Float.MAX_VALUE
            }

        return newAccuracy < currentAccuracy ||
                newLocation.time > currentLocation.time
    }

    private fun isRecentLocation(
        location: Location
    ): Boolean {
        val locationAgeMillis =
            if (location.elapsedRealtimeNanos > 0L) {
                (
                        SystemClock.elapsedRealtimeNanos() -
                                location.elapsedRealtimeNanos
                        ) / 1_000_000L
            } else {
                System.currentTimeMillis() -
                        location.time
            }

        return locationAgeMillis in
                0..MAXIMUM_LOCATION_AGE_MILLIS
    }

    private fun isLocationAccurate(
        location: Location
    ): Boolean {
        return location.hasAccuracy() &&
                location.accuracy <=
                MAXIMUM_ACCEPTED_ACCURACY_METERS
    }

    private fun isAcceptableFallbackLocation(
        location: Location
    ): Boolean {
        return location.hasAccuracy() &&
                location.accuracy <=
                MAXIMUM_FALLBACK_ACCURACY_METERS
    }

    private fun finishWithSuccess(
        currentTokenSource: CancellationTokenSource,
        location: Location,
        onSuccess: (DeviceLocation) -> Unit
    ) {
        if (
            cancellationTokenSource !==
            currentTokenSource
        ) {
            return
        }

        stopPlatformLocationUpdates()

        cancellationTokenSource = null
        bestPlatformLocation = null

        onSuccess(
            DeviceLocation(
                latitude =
                location.latitude,

                longitude =
                location.longitude
            )
        )
    }

    private fun finishWithError(
        currentTokenSource: CancellationTokenSource,
        message: String,
        onError: (String) -> Unit
    ) {
        if (
            cancellationTokenSource !==
            currentTokenSource
        ) {
            return
        }

        stopPlatformLocationUpdates()

        cancellationTokenSource = null
        bestPlatformLocation = null

        onError(
            message
        )
    }

    private fun stopPlatformLocationUpdates() {
        platformTimeoutRunnable?.let { runnable ->
            mainHandler.removeCallbacks(
                runnable
            )
        }

        platformTimeoutRunnable = null

        platformLocationListener?.let { listener ->
            try {
                locationManager.removeUpdates(
                    listener
                )
            } catch (_: Exception) {
                // Nothing else needs to be cleaned up.
            }
        }

        platformLocationListener = null
    }

    fun cancelLocationRequest() {
        cancellationTokenSource?.cancel()
        cancellationTokenSource = null

        stopPlatformLocationUpdates()

        bestPlatformLocation = null
    }

    companion object {

        private const val FUSED_LOCATION_TIMEOUT_MILLIS =
            12_000L

        private const val PLATFORM_LOCATION_TIMEOUT_MILLIS =
            20_000L

        private const val MINIMUM_UPDATE_TIME_MILLIS =
            1_000L

        private const val MINIMUM_UPDATE_DISTANCE_METERS =
            0f

        private const val MAXIMUM_LOCATION_AGE_MILLIS =
            2 * 60 * 1000L

        private const val MAXIMUM_ACCEPTED_ACCURACY_METERS =
            100f

        private const val MAXIMUM_FALLBACK_ACCURACY_METERS =
            250f
    }
}