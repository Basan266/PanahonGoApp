package com.example.weatherapp.util

import org.maplibre.android.geometry.LatLng

object Polyline6Decoder {

    private const val PRECISION =
        1_000_000.0

    fun decode(
        encodedPolyline: String
    ): List<LatLng> {
        if (encodedPolyline.isBlank()) {
            return emptyList()
        }

        val coordinates =
            mutableListOf<LatLng>()

        var index = 0
        var latitude = 0
        var longitude = 0

        while (index < encodedPolyline.length) {
            val latitudeResult =
                decodeValue(
                    encodedPolyline,
                    index
                )

            index =
                latitudeResult.nextIndex

            latitude +=
                latitudeResult.value

            if (index >= encodedPolyline.length) {
                break
            }

            val longitudeResult =
                decodeValue(
                    encodedPolyline,
                    index
                )

            index =
                longitudeResult.nextIndex

            longitude +=
                longitudeResult.value

            coordinates.add(
                LatLng(
                    latitude / PRECISION,
                    longitude / PRECISION
                )
            )
        }

        return coordinates
    }

    private fun decodeValue(
        encodedPolyline: String,
        startingIndex: Int
    ): DecodedValue {
        var index = startingIndex
        var result = 0
        var shift = 0
        var currentByte: Int

        do {
            if (index >= encodedPolyline.length) {
                return DecodedValue(
                    value = 0,
                    nextIndex = index
                )
            }

            currentByte =
                encodedPolyline[index]
                    .code - 63

            index++

            result =
                result or
                        (
                                currentByte and 0x1F
                                        shl shift
                                )

            shift += 5

        } while (currentByte >= 0x20)

        val decodedValue =
            if ((result and 1) != 0) {
                (result shr 1).inv()
            } else {
                result shr 1
            }

        return DecodedValue(
            value = decodedValue,
            nextIndex = index
        )
    }

    private data class DecodedValue(
        val value: Int,
        val nextIndex: Int
    )
}