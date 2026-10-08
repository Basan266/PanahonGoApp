package com.example.weatherapp.data.remote.route

import com.google.gson.annotations.SerializedName

data class ValhallaRouteRequest(
    @SerializedName("locations")
    val locations: List<ValhallaLocation>,

    @SerializedName("costing")
    val costing: String,

    @SerializedName("units")
    val units: String = "kilometers",

    @SerializedName("language")
    val language: String = "en-US",

    @SerializedName("directions_options")
    val directionsOptions:
    ValhallaDirectionsOptions =
        ValhallaDirectionsOptions()
)

data class ValhallaLocation(
    @SerializedName("lat")
    val latitude: Double,

    @SerializedName("lon")
    val longitude: Double,

    @SerializedName("type")
    val type: String = "break"
)

data class ValhallaDirectionsOptions(
    @SerializedName("units")
    val units: String = "kilometers",

    @SerializedName("language")
    val language: String = "en-US"
)

data class ValhallaRouteResponse(
    @SerializedName("trip")
    val trip: ValhallaTrip?
)

data class ValhallaTrip(
    @SerializedName("status")
    val status: Int,

    @SerializedName("status_message")
    val statusMessage: String,

    @SerializedName("summary")
    val summary: ValhallaSummary,

    @SerializedName("legs")
    val legs: List<ValhallaLeg>
)

data class ValhallaSummary(
    @SerializedName("length")
    val length: Double,

    @SerializedName("time")
    val time: Double
)

data class ValhallaLeg(
    @SerializedName("shape")
    val shape: String,

    @SerializedName("summary")
    val summary: ValhallaSummary,

    @SerializedName("maneuvers")
    val maneuvers: List<ValhallaManeuver>
)

data class ValhallaManeuver(
    @SerializedName("type")
    val type: Int,

    @SerializedName("instruction")
    val instruction: String,

    @SerializedName("verbal_pre_transition_instruction")
    val verbalInstruction: String?,

    @SerializedName("length")
    val length: Double,

    @SerializedName("time")
    val time: Double,

    @SerializedName("begin_shape_index")
    val beginShapeIndex: Int,

    @SerializedName("end_shape_index")
    val endShapeIndex: Int
)