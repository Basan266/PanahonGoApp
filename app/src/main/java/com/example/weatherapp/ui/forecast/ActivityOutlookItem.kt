package com.example.weatherapp.ui.forecast

data class ActivityOutlookItem(

    val activityName: String,

    val icon: String,

    val status: ActivityOutlookStatus,

    val bestTime: String,

    val message: String
)

enum class ActivityOutlookStatus {

    GOOD,

    CAUTION,

    AVOID
}