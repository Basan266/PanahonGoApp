package com.example.weatherapp.data.remote.route

import com.google.gson.annotations.SerializedName

data class OsrmRouteResponse(
    @SerializedName("code")
    val code: String,

    @SerializedName("routes")
    val routes: List<OsrmRoute>
)

data class OsrmRoute(
    @SerializedName("distance")
    val distance: Double,

    @SerializedName("duration")
    val duration: Double,

    @SerializedName("geometry")
    val geometry: OsrmGeometry,

    @SerializedName("legs")
    val legs: List<OsrmLeg>
)

data class OsrmGeometry(
    @SerializedName("type")
    val type: String,

    @SerializedName("coordinates")
    val coordinates: List<List<Double>>
)

data class OsrmLeg(
    @SerializedName("steps")
    val steps: List<OsrmStep>
)

data class OsrmStep(
    @SerializedName("distance")
    val distance: Double,

    @SerializedName("duration")
    val duration: Double,

    @SerializedName("name")
    val name: String,

    @SerializedName("maneuver")
    val maneuver: OsrmManeuver
)

data class OsrmManeuver(
    @SerializedName("type")
    val type: String,

    @SerializedName("modifier")
    val modifier: String?,

    @SerializedName("location")
    val location: List<Double>
)