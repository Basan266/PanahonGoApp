package com.example.weatherapp.data.remote.geocoding

import com.google.gson.annotations.SerializedName

data class PhotonResponse(
    val features: List<PhotonFeature>
)

data class PhotonFeature(
    val geometry: PhotonGeometry,
    val properties: PhotonProperties
)

data class PhotonGeometry(
    val coordinates: List<Double>
)

data class PhotonProperties(
    val name: String?,
    val street: String?,
    val district: String?,
    val city: String?,
    val county: String?,
    val state: String?,
    val country: String?,
    val postcode: String?,

    @SerializedName("osm_key")
    val osmKey: String?,

    @SerializedName("osm_value")
    val osmValue: String?
)