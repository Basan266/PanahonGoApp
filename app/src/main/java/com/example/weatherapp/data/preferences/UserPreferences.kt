package com.example.weatherapp.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "user_preferences"
)

class UserPreferences(
    private val context: Context
) {

    companion object {
        private val DISPLAY_NAME =
            stringPreferencesKey("display_name")

        private val BIRTHDAY =
            longPreferencesKey("birthday")

        private val AGE =
            intPreferencesKey("age")

        private val GENDER =
            stringPreferencesKey("gender")

        private val ONBOARDING_COMPLETED =
            booleanPreferencesKey("onboarding_completed")

        private val SELECTED_ACTIVITIES =
            stringSetPreferencesKey("selected_activities")

        private val WEATHER_CONCERNS =
            stringSetPreferencesKey("weather_concerns")

        private val OUTDOOR_SCHEDULE =
            stringSetPreferencesKey("outdoor_schedule")

    }

    val onboardingCompleted: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }

    val displayName: Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[DISPLAY_NAME] ?: ""
        }

    val birthday: Flow<Long> =
        context.dataStore.data.map { preferences ->
            preferences[BIRTHDAY] ?: 0L
        }

    val age: Flow<Int> =
        context.dataStore.data.map { preferences ->
            preferences[AGE] ?: 0
        }

    val gender: Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[GENDER] ?: ""
        }

    val selectedActivities: Flow<Set<String>> =
        context.dataStore.data.map { preferences ->
            preferences[SELECTED_ACTIVITIES] ?: emptySet()
        }

    suspend fun finishOnboarding(
        schedule: Set<String>
    ) {
        context.dataStore.edit { preferences ->
            preferences[OUTDOOR_SCHEDULE] = schedule
            preferences[ONBOARDING_COMPLETED] = true
        }
    }


    suspend fun saveSelectedActivities(
        activities: Set<String>
    ) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_ACTIVITIES] = activities
        }
    }

    suspend fun saveWeatherConcerns(
        concerns: Set<String>
    ) {
        context.dataStore.edit { preferences ->
            preferences[WEATHER_CONCERNS] = concerns
        }
    }


    suspend fun savePersonalInformation(
        displayName: String,
        birthday: Long,
        age: Int,
        gender: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[DISPLAY_NAME] = displayName
            preferences[BIRTHDAY] = birthday
            preferences[AGE] = age
            preferences[GENDER] = gender
        }
    }

    suspend fun completeOnboarding(
        activities: Set<String>
    ) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_ACTIVITIES] = activities
            preferences[ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun skipOnboarding() {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
        }
    }
}