// Top-level build file for project-wide configuration.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.serialization) apply false

    id(
        "com.google.android.libraries.mapsplatform.secrets-gradle-plugin"
    ) version "2.0.1" apply false
}