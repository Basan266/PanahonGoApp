package com.example.weatherapp.data.repository

import com.example.weatherapp.data.remote.dto.ActivitySelectionDto
import com.example.weatherapp.data.remote.dto.OutdoorScheduleDto
import com.example.weatherapp.data.remote.dto.PersonalInformationDto
import com.example.weatherapp.data.remote.dto.UserProfileDto
import com.example.weatherapp.data.remote.dto.WeatherConcernsDto
import com.example.weatherapp.data.remote.supabase.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.weatherapp.data.remote.dto.AvatarSelectionDto
import com.example.weatherapp.data.remote.dto.SelectedActivitiesUpdateDto

class SupabaseOnboardingRepository {

    private val supabase =
        SupabaseClientProvider.client

    suspend fun savePersonalInformation(
        displayName: String,
        birthdayMillis: Long,
        gender: String
    ): Result<Unit> {
        return runCatching {
            val userId =
                requireCurrentUserId()

            val birthday =
                convertBirthdayToDatabaseDate(
                    birthdayMillis
                )

            val avatarSeed =
                createDefaultAvatarSeed(
                    userId = userId,
                    birthdayMillis = birthdayMillis,
                    gender = gender
                )

            val information =
                PersonalInformationDto(
                    userId = userId,
                    displayName = displayName,
                    birthday = birthday,
                    gender = gender,
                    avatarStyle = "toon-head",
                    avatarSeed = avatarSeed,
                    onboardingStep = 1
                )

            supabase
                .from(PROFILE_TABLE)
                .upsert(information) {
                    onConflict = "user_id"
                }
        }
    }

    suspend fun saveSelectedActivities(
        activities: Set<String>
    ): Result<Unit> {
        return runCatching {
            val userId =
                requireCurrentUserId()

            val activitySelection =
                ActivitySelectionDto(
                    userId = userId,
                    selectedActivities =
                    activities.sorted(),
                    onboardingStep = 2
                )

            supabase
                .from(PROFILE_TABLE)
                .update(activitySelection) {
                    filter {
                        eq(
                            column = "user_id",
                            value = userId
                        )
                    }
                }
        }
    }

    suspend fun saveWeatherConcerns(
        concerns: Set<String>
    ): Result<Unit> {
        return runCatching {
            val userId =
                requireCurrentUserId()

            val weatherConcerns =
                WeatherConcernsDto(
                    userId = userId,
                    weatherConcerns =
                    concerns.sorted(),
                    onboardingStep = 3
                )

            supabase
                .from(PROFILE_TABLE)
                .update(weatherConcerns) {
                    filter {
                        eq(
                            column = "user_id",
                            value = userId
                        )
                    }
                }
        }
    }

    suspend fun finishOnboarding(
        schedule: Set<String>
    ): Result<Unit> {
        return runCatching {
            val userId =
                requireCurrentUserId()

            val outdoorSchedule =
                OutdoorScheduleDto(
                    userId = userId,
                    outdoorSchedule =
                    schedule.sorted(),
                    onboardingStep = 4,
                    onboardingCompleted = true
                )

            supabase
                .from(PROFILE_TABLE)
                .update(outdoorSchedule) {
                    filter {
                        eq(
                            column = "user_id",
                            value = userId
                        )
                    }
                }
        }
    }

    /*
 * Updates the current user's selected avatar.
 */
    suspend fun saveSelectedAvatar(
        avatarSeed: String
    ): Result<Unit> {
        return runCatching {
            val userId =
                requireCurrentUserId()

            val avatarSelection =
                AvatarSelectionDto(
                    avatarStyle = "toon-head",
                    avatarSeed = avatarSeed
                )

            supabase
                .from(PROFILE_TABLE)
                .update(
                    avatarSelection
                ) {
                    filter {
                        eq(
                            column = "user_id",
                            value = userId
                        )
                    }
                }
        }
    }


    suspend fun updateSelectedActivities(
        activities: Set<String>
    ): Result<Unit> {
        return runCatching {
            val userId =
                requireCurrentUserId()

            val update =
                SelectedActivitiesUpdateDto(
                    selectedActivities =
                    activities.sorted()
                )

            supabase
                .from(PROFILE_TABLE)
                .update(update) {
                    filter {
                        eq(
                            column = "user_id",
                            value = userId
                        )
                    }
                }
        }
    }


    suspend fun getCurrentUserProfile():
            Result<UserProfileDto?> {
        return runCatching {
            val userId =
                requireCurrentUserId()

            supabase
                .from(PROFILE_TABLE)
                .select {
                    filter {
                        eq(
                            column = "user_id",
                            value = userId
                        )
                    }

                    limit(1)
                }
                .decodeSingleOrNull<UserProfileDto>()
        }
    }

    private fun requireCurrentUserId(): String {
        val session =
            supabase
                .auth
                .currentSessionOrNull()

        val userId =
            session
                ?.user
                ?.id
                ?: supabase
                    .auth
                    .currentUserOrNull()
                    ?.id

        return requireNotNull(
            userId
        ) {
            "No authenticated Supabase user."
        }
    }

    private fun convertBirthdayToDatabaseDate(
        birthdayMillis: Long
    ): String {
        return Instant
            .ofEpochMilli(birthdayMillis)
            .atZone(
                ZoneId.systemDefault()
            )
            .toLocalDate()
            .format(
                DateTimeFormatter.ISO_LOCAL_DATE
            )
    }

    private fun createDefaultAvatarSeed(
        userId: String,
        birthdayMillis: Long,
        gender: String
    ): String {
        val birthday =
            Instant
                .ofEpochMilli(birthdayMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

        val age =
            java.time.Period.between(
                birthday,
                java.time.LocalDate.now()
            ).years

        val ageGroup =
            when {
                age < 13 -> "child"
                age < 18 -> "teen"
                age < 60 -> "adult"
                else -> "senior"
            }

        val genderGroup =
            when (gender.trim().lowercase()) {
                "male" -> "male"
                "female" -> "female"
                else -> "neutral"
            }

        val shortUserId =
            userId
                .replace("-", "")
                .take(8)

        return "$ageGroup-$genderGroup-$shortUserId"
    }

    companion object {
        private const val PROFILE_TABLE =
            "user_profiles"
    }
}