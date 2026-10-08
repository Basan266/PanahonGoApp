package com.example.weatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PersonalInformationDto(

    @SerialName("user_id")
    val userId: String,

    @SerialName("display_name")
    val displayName: String,

    @SerialName("birthday")
    val birthday: String,

    @SerialName("gender")
    val gender: String,

    @SerialName("avatar_style")
    val avatarStyle: String,

    @SerialName("avatar_seed")
    val avatarSeed: String,

    @SerialName("onboarding_step")
    val onboardingStep: Int
)



@Serializable
data class ActivitySelectionDto(

    @SerialName("user_id")
    val userId: String,

    @SerialName("selected_activities")
    val selectedActivities: List<String>,

    @SerialName("onboarding_step")
    val onboardingStep: Int
)

@Serializable
data class WeatherConcernsDto(

    @SerialName("user_id")
    val userId: String,

    @SerialName("weather_concerns")
    val weatherConcerns: List<String>,

    @SerialName("onboarding_step")
    val onboardingStep: Int
)

@Serializable
data class OutdoorScheduleDto(

    @SerialName("user_id")
    val userId: String,

    @SerialName("outdoor_schedule")
    val outdoorSchedule: List<String>,

    @SerialName("onboarding_step")
    val onboardingStep: Int,

    @SerialName("onboarding_completed")
    val onboardingCompleted: Boolean
)

@Serializable
data class UserProfileDto(

    @SerialName("user_id")
    val userId: String,

    @SerialName("display_name")
    val displayName: String? = null,

    @SerialName("birthday")
    val birthday: String? = null,

    @SerialName("gender")
    val gender: String? = null,

    @SerialName("selected_activities")
    val selectedActivities: List<String> =
        emptyList(),

    @SerialName("weather_concerns")
    val weatherConcerns: List<String> =
        emptyList(),

    @SerialName("outdoor_schedule")
    val outdoorSchedule: List<String> =
        emptyList(),

    @SerialName("onboarding_step")
    val onboardingStep: Int = 0,

    @SerialName("onboarding_completed")
    val onboardingCompleted: Boolean = false,

    @SerialName("avatar_style")
    val avatarStyle: String = "toon-head",

    @SerialName("avatar_seed")
    val avatarSeed: String? = null
)

@Serializable
data class AvatarSelectionDto(

    @SerialName("avatar_style")
    val avatarStyle: String,

    @SerialName("avatar_seed")
    val avatarSeed: String
)

@Serializable
data class SelectedActivitiesUpdateDto(

    @SerialName("selected_activities")
    val selectedActivities: List<String>
)