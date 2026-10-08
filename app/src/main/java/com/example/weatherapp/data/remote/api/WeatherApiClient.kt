package com.example.weatherapp.data.remote.api

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.MessageDigest
import java.util.Locale

object WeatherApiClient {

    private const val BASE_URL =
        "https://weather.googleapis.com/"

    private const val GOOGLE_MAPS_API_KEY_METADATA =
        "com.google.android.geo.API_KEY"

    fun create(context: Context): WeatherApi {
        val applicationContext =
            context.applicationContext

        val apiKey =
            getApiKey(applicationContext)

        val certificateFingerprint =
            getCertificateFingerprint(
                applicationContext
            )

        val httpClient =
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val originalRequest =
                        chain.request()

                    val updatedUrl =
                        originalRequest.url
                            .newBuilder()
                            .addQueryParameter(
                                "key",
                                apiKey
                            )
                            .build()

                    val updatedRequest =
                        originalRequest
                            .newBuilder()
                            .url(updatedUrl)
                            .header(
                                "X-Android-Package",
                                applicationContext.packageName
                            )
                            .header(
                                "X-Android-Cert",
                                certificateFingerprint
                            )
                            .build()

                    chain.proceed(updatedRequest)
                }
                .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(WeatherApi::class.java)
    }

    private fun getApiKey(
        context: Context
    ): String {
        val applicationInfo =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU
            ) {
                context.packageManager
                    .getApplicationInfo(
                        context.packageName,
                        PackageManager.ApplicationInfoFlags.of(
                            PackageManager.GET_META_DATA
                                .toLong()
                        )
                    )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager
                    .getApplicationInfo(
                        context.packageName,
                        PackageManager.GET_META_DATA
                    )
            }

        val apiKey =
            applicationInfo.metaData
                ?.getString(
                    GOOGLE_MAPS_API_KEY_METADATA
                )
                .orEmpty()

        check(apiKey.isNotBlank()) {
            "Google Maps API key is missing from AndroidManifest.xml."
        }

        return apiKey
    }

    private fun getCertificateFingerprint(
        context: Context
    ): String {
        val packageManager =
            context.packageManager

        val packageInfo =
            when {
                Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.TIRAMISU -> {

                    packageManager.getPackageInfo(
                        context.packageName,
                        PackageManager.PackageInfoFlags.of(
                            PackageManager
                                .GET_SIGNING_CERTIFICATES
                                .toLong()
                        )
                    )
                }

                Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.P -> {

                    @Suppress("DEPRECATION")
                    packageManager.getPackageInfo(
                        context.packageName,
                        PackageManager.GET_SIGNING_CERTIFICATES
                    )
                }

                else -> {
                    @Suppress("DEPRECATION")
                    packageManager.getPackageInfo(
                        context.packageName,
                        PackageManager.GET_SIGNATURES
                    )
                }
            }

        val signature =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.P
            ) {
                val signingInfo =
                    checkNotNull(
                        packageInfo.signingInfo
                    ) {
                        "Android signing information is unavailable."
                    }

                val signatures =
                    if (
                        signingInfo.hasMultipleSigners()
                    ) {
                        signingInfo.apkContentsSigners
                    } else {
                        signingInfo.signingCertificateHistory
                    }

                signatures.firstOrNull()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
                    ?.firstOrNull()
            }

        checkNotNull(signature) {
            "Unable to read the Android signing certificate."
        }

        val digest =
            MessageDigest.getInstance("SHA-1")
                .digest(
                    signature.toByteArray()
                )

        return digest.joinToString(
            separator = ""
        ) { byte ->
            String.format(
                Locale.US,
                "%02X",
                byte.toInt() and 0xFF
            )
        }
    }
}