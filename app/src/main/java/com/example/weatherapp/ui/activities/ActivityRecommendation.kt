package com.example.weatherapp.ui.activities

data class ActivityRecommendation(

    val activityName: String,

    val icon: String,

    val status: ActivityStatus,

    val bestTime: String,

    val recommendation: String
)

enum class ActivityStatus {
    GOOD,
    CAUTION,
    AVOID,
    UNKNOWN
}