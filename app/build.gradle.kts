plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.weatherapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.weatherapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        multiDexEnabled = true

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        buildConfig = true
    }

}

dependencies {

    coreLibraryDesugaring(
        "com.android.tools:desugar_jdk_libs_nio:2.0.3"
    )

    // Compatible AndroidX versions for AGP 8.6.1
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.fragment:fragment-ktx:1.8.6")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    // Material Design components
    implementation("com.google.android.material:material:1.12.0")

    // Location
    implementation(
        "com.google.android.gms:play-services-location:21.3.0"
    )

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // MVVM
    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7"
    )
    implementation(
        "androidx.lifecycle:lifecycle-livedata-ktx:2.8.7"
    )

    // Saves onboarding status and selected activities
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Interactive open-source map
    implementation("org.maplibre.gl:android-sdk:11.8.0")

// Room offline database
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    kapt("androidx.room:room-compiler:2.8.4")

    // Google Navigation SDK
    implementation(
        "com.google.android.libraries.navigation:navigation:7.2.0"
    )

    implementation(
        "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
    )

    // Horizontal weekly forecast list
    implementation(
        "androidx.recyclerview:recyclerview:1.4.0"
    )

// Loads Google Weather icons, including SVG
    implementation(
        "io.coil-kt:coil:2.7.0"
    )

    implementation(
        "io.coil-kt:coil-svg:2.7.0"
    )


    // Supabase PostgreSQL database access
    implementation(
        libs.supabase.postgrest
    )

// Supabase login and user sessions
    implementation(
        libs.supabase.auth
    )

// Network engine used by Supabase
    implementation(
        libs.ktor.client.okhttp
    )


    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}