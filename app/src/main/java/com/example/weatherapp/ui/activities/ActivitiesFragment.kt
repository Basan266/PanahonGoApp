package com.example.weatherapp.ui.activities

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.data.repository.SupabaseOnboardingRepository
import com.example.weatherapp.domain.model.Weather
import com.example.weatherapp.ui.home.HomeViewModel
import com.example.weatherapp.ui.home.HomeViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.weatherapp.util.LocationHelper

import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.widget.LinearLayout
import com.google.android.material.checkbox.MaterialCheckBox



class ActivitiesFragment :
    Fragment(R.layout.fragment_activities) {

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts
                .RequestMultiplePermissions()
        ) { permissions ->

            val fineLocationAllowed =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true

            if (fineLocationAllowed) {
                retrieveCurrentWeather()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location permission is needed for live activity recommendations.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    private val availableActivities =
        listOf(
            "Running",
            "Cycling",
            "Gardening",
            "Golf",
            "Beach",
            "Hiking",
            "Kids sports",
            "Dog walking",
            "Surfing",
            "Sailing",
            "Farming",
            "Flying"
        )

    private val onboardingRepository =
        SupabaseOnboardingRepository()

    private val activitiesAdapter =
        ActivitiesAdapter { activity ->
            showActivityDetailDialog(
                activity
            )
        }

    private val weatherRepository by lazy {
        val application =
            requireActivity().application
                    as WeatherApplication

        application.weatherRepository
    }

    private val viewModel:
            HomeViewModel by viewModels {
        HomeViewModelFactory(
            weatherRepository
        )
    }

    private lateinit var locationHelper:
            LocationHelper


    private lateinit var cardOverallReadiness:
            MaterialCardView

    private lateinit var tvOverallStatus:
            TextView

    private lateinit var tvOverallMessage:
            TextView

    private lateinit var tvEditActivities:
            TextView

    private lateinit var progressActivities:
            ProgressBar

    private lateinit var rvActivities:
            RecyclerView

    private lateinit var layoutEmptyActivities:
            View

    private lateinit var btnChooseActivities:
            MaterialButton

    private var selectedActivities:
            List<String> = emptyList()

    private var preferredSchedule:
            List<String> = emptyList()

    private var currentWeather:
            Weather? = null

    private var profileLoaded =
        false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        locationHelper = LocationHelper(
            requireContext()
        )

        bindViews(view)
        configureNormalScreen(view)
        configureActivitiesList()
        configureButtons()
        observeWeather()
        loadUserActivities()

        viewModel.loadCachedWeather()
        loadCurrentWeather()
    }

    private fun bindViews(
        view: View
    ) {
        cardOverallReadiness =
            view.findViewById(
                R.id.cardOverallReadiness
            )

        tvOverallStatus =
            view.findViewById(
                R.id.tvOverallStatus
            )

        tvOverallMessage =
            view.findViewById(
                R.id.tvOverallMessage
            )

        tvEditActivities =
            view.findViewById(
                R.id.tvEditActivities
            )

        progressActivities =
            view.findViewById(
                R.id.progressActivities
            )

        rvActivities =
            view.findViewById(
                R.id.rvActivities
            )

        layoutEmptyActivities =
            view.findViewById(
                R.id.layoutEmptyActivities
            )

        btnChooseActivities =
            view.findViewById(
                R.id.btnChooseActivities
            )
    }


    private fun loadCurrentWeather() {
        val fineLocationAllowed =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationAllowed) {
            retrieveCurrentWeather()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun retrieveCurrentWeather() {
        locationHelper.getCurrentLocation(
            onSuccess = { location ->
                if (
                    !isAdded ||
                    view == null
                ) {
                    return@getCurrentLocation
                }

                viewModel.loadWeather(
                    location
                )
            },
            onError = { message ->
                if (
                    !isAdded ||
                    view == null
                ) {
                    return@getCurrentLocation
                }

                Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }



    private fun configureActivitiesList() {
        rvActivities.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        rvActivities.adapter =
            activitiesAdapter

        rvActivities.setHasFixedSize(
            true
        )
    }

    private fun showActivityDetailDialog(
        activity: ActivityRecommendation
    ) {
        val dialogView =
            layoutInflater.inflate(
                R.layout.dialog_activity_detail,
                null
            )

        val tvActivityIcon =
            dialogView.findViewById<TextView>(
                R.id.tvDetailActivityIcon
            )

        val tvActivityName =
            dialogView.findViewById<TextView>(
                R.id.tvDetailActivityName
            )

        val tvActivityStatus =
            dialogView.findViewById<TextView>(
                R.id.tvDetailActivityStatus
            )

        val tvTemperature =
            dialogView.findViewById<TextView>(
                R.id.tvDetailTemperature
            )

        val tvHumidity =
            dialogView.findViewById<TextView>(
                R.id.tvDetailHumidity
            )

        val tvWind =
            dialogView.findViewById<TextView>(
                R.id.tvDetailWind
            )

        val tvBestTime =
            dialogView.findViewById<TextView>(
                R.id.tvDetailBestTime
            )

        val tvRecommendation =
            dialogView.findViewById<TextView>(
                R.id.tvDetailRecommendation
            )

        val btnClose =
            dialogView.findViewById<MaterialButton>(
                R.id.btnCloseActivityDetail
            )

        tvActivityIcon.text =
            activity.icon

        tvActivityName.text =
            activity.activityName

        tvBestTime.text =
            activity.bestTime

        tvRecommendation.text =
            activity.recommendation

        val statusText =
            when (activity.status) {
                ActivityStatus.GOOD ->
                    "Good conditions"

                ActivityStatus.CAUTION ->
                    "Proceed with caution"

                ActivityStatus.AVOID ->
                    "Not recommended"

                ActivityStatus.UNKNOWN ->
                    "Weather unavailable"
            }

        val statusColor =
            when (activity.status) {
                ActivityStatus.GOOD ->
                    "#73E6A0"

                ActivityStatus.CAUTION ->
                    "#FFD166"

                ActivityStatus.AVOID ->
                    "#FF7777"

                ActivityStatus.UNKNOWN ->
                    "#B5B5B5"
            }

        tvActivityStatus.text =
            statusText

        tvActivityStatus.setTextColor(
            Color.parseColor(
                statusColor
            )
        )

        val weather =
            currentWeather

        if (weather == null) {
            tvTemperature.text =
                "--"

            tvHumidity.text =
                "--"

            tvWind.text =
                "--"
        } else {
            tvTemperature.text =
                String.format(
                    Locale.getDefault(),
                    "%.1f°C",
                    weather.temperature
                )

            tvHumidity.text =
                String.format(
                    Locale.getDefault(),
                    "%d%%",
                    weather.humidity
                )

            tvWind.text =
                String.format(
                    Locale.getDefault(),
                    "%.1f km/h",
                    weather.windSpeed
                )
        }

        val dialog =
            AlertDialog.Builder(
                requireContext()
            )
                .setView(
                    dialogView
                )
                .create()

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        dialog.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
    }

    private fun configureButtons() {
        tvEditActivities.setOnClickListener {
            showActivitySelectionDialog()
        }

        btnChooseActivities.setOnClickListener {
            showActivitySelectionDialog()
        }
    }

    private fun showActivitySelectionDialog() {
        val dialogView =
            layoutInflater.inflate(
                R.layout.dialog_edit_activities,
                null
            )

        val activityOptionsContainer =
            dialogView.findViewById<LinearLayout>(
                R.id.activityOptionsContainer
            )

        val btnCancel =
            dialogView.findViewById<TextView>(
                R.id.btnCancelActivities
            )

        val btnSave =
            dialogView.findViewById<TextView>(
                R.id.btnSaveActivities
            )

        val temporarySelection =
            selectedActivities
                .toMutableSet()

        val density =
            resources.displayMetrics.density

        val checkBoxColors =
            ColorStateList(
                arrayOf(
                    intArrayOf(
                        android.R.attr.state_checked
                    ),
                    intArrayOf()
                ),
                intArrayOf(
                    Color.parseColor(
                        "#FFFFFF"
                    ),
                    Color.parseColor(
                        "#777777"
                    )
                )
            )

        availableActivities.forEach { activity ->
            val activityCheckBox =
                MaterialCheckBox(
                    requireContext()
                ).apply {
                    text =
                        "${getActivityIcon(activity)}   $activity"

                    setTextColor(
                        Color.WHITE
                    )

                    textSize =
                        15f

                    gravity =
                        android.view.Gravity.CENTER_VERTICAL

                    minimumHeight =
                        (52f * density).toInt()

                    buttonTintList =
                        checkBoxColors

                    isChecked =
                        activity in temporarySelection

                    setPadding(
                        (4f * density).toInt(),
                        0,
                        (4f * density).toInt(),
                        0
                    )

                    setOnCheckedChangeListener {
                            _,
                            isChecked ->

                        if (isChecked) {
                            temporarySelection.add(
                                activity
                            )
                        } else {
                            temporarySelection.remove(
                                activity
                            )
                        }
                    }
                }

            activityOptionsContainer.addView(
                activityCheckBox,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        val dialog =
            AlertDialog.Builder(
                requireContext()
            )
                .setView(
                    dialogView
                )
                .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            saveUpdatedActivities(
                temporarySelection
            )

            dialog.dismiss()
        }

        dialog.show()

        dialog.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
    }

    private fun saveUpdatedActivities(
        activities: Set<String>
    ) {
        progressActivities.visibility =
            View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            onboardingRepository
                .updateSelectedActivities(
                    activities
                )
                .onSuccess {
                    selectedActivities =
                        activities.sorted()

                    progressActivities.visibility =
                        View.GONE

                    displayActivities()

                    Toast.makeText(
                        requireContext(),
                        "Activities updated.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onFailure { error ->
                    progressActivities.visibility =
                        View.GONE

                    Toast.makeText(
                        requireContext(),
                        error.message
                            ?: "Unable to update activities.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun observeWeather() {
        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->

            progressActivities.visibility =
                if (
                    state.isLoading &&
                    !profileLoaded
                ) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            state.weather?.let { weather ->
                currentWeather =
                    weather

                displayActivities()
            }

            state.errorMessage?.let { message ->
                if (
                    currentWeather == null &&
                    profileLoaded
                ) {
                    tvOverallStatus.text =
                        "Weather unavailable"

                    tvOverallMessage.text =
                        message
                }
            }
        }
    }

    private fun loadUserActivities() {
        progressActivities.visibility =
            View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            onboardingRepository
                .getCurrentUserProfile()
                .onSuccess { profile ->
                    if (
                        !isAdded ||
                        view == null
                    ) {
                        return@onSuccess
                    }

                    selectedActivities =
                        profile
                            ?.selectedActivities
                            .orEmpty()

                    preferredSchedule =
                        profile
                            ?.outdoorSchedule
                            .orEmpty()

                    profileLoaded =
                        true

                    progressActivities.visibility =
                        View.GONE

                    displayActivities()
                }
                .onFailure { error ->
                    if (
                        !isAdded ||
                        view == null
                    ) {
                        return@onFailure
                    }

                    profileLoaded =
                        true

                    progressActivities.visibility =
                        View.GONE

                    displayActivities()

                    Toast.makeText(
                        requireContext(),
                        error.message
                            ?: "Unable to load activities.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun displayActivities() {
        if (!profileLoaded) {
            return
        }

        if (selectedActivities.isEmpty()) {
            rvActivities.visibility =
                View.GONE

            layoutEmptyActivities.visibility =
                View.VISIBLE

            displayOverallStatus(
                ActivityStatus.UNKNOWN
            )

            return
        }

        rvActivities.visibility =
            View.VISIBLE

        layoutEmptyActivities.visibility =
            View.GONE

        val recommendations =
            selectedActivities.map { activity ->
                createRecommendation(
                    activityName = activity,
                    weather = currentWeather
                )
            }

        activitiesAdapter.submitList(
            recommendations
        )

        val overallStatus =
            determineOverallStatus(
                recommendations
            )

        displayOverallStatus(
            overallStatus
        )
    }

    private fun createRecommendation(
        activityName: String,
        weather: Weather?
    ): ActivityRecommendation {
        val icon =
            getActivityIcon(
                activityName
            )

        val preferredTime =
            getPreferredTime()

        if (weather == null) {
            return ActivityRecommendation(
                activityName = activityName,
                icon = icon,
                status = ActivityStatus.UNKNOWN,
                bestTime = preferredTime,
                recommendation =
                "Weather information is not available yet."
            )
        }

        val activity =
            activityName
                .trim()
                .lowercase(Locale.US)

        val condition =
            weather.conditionType
                .trim()
                .uppercase(Locale.US)

        val hasThunderstorm =
            condition.contains(
                "THUNDER"
            )

        val hasHeavyRain =
            condition.contains("HEAVY") &&
                    condition.contains("RAIN")

        val hasRain =
            condition.contains("RAIN") ||
                    condition.contains("DRIZZLE")

        val hasModerateWind =
            weather.windSpeed >= 20.0

        val hasStrongWind =
            weather.windSpeed >= 30.0

        val isHot =
            weather.apparentTemperature >= 33.0

        val isVeryHot =
            weather.apparentTemperature >= 36.0

        val hasHighHumidity =
            weather.humidity >= 80

        fun result(
            status: ActivityStatus,
            bestTime: String,
            message: String
        ): ActivityRecommendation {
            return ActivityRecommendation(
                activityName = activityName,
                icon = icon,
                status = status,
                bestTime = bestTime,
                recommendation = message
            )
        }

        /*
         * Thunderstorms and heavy rain are unsafe
         * for almost every outdoor activity.
         */
        if (hasThunderstorm) {
            return result(
                status = ActivityStatus.AVOID,
                bestTime = "After the thunderstorm",
                message =
                "Thunderstorms are detected. Avoid open areas and postpone this activity."
            )
        }

        if (hasHeavyRain) {
            return result(
                status = ActivityStatus.AVOID,
                bestTime = "After heavy rainfall",
                message =
                "Heavy rain may cause slippery surfaces, poor visibility and unsafe conditions."
            )
        }

        return when (activity) {
            "running" -> {
                when {
                    isVeryHot || hasHighHumidity ->
                        result(
                            status = ActivityStatus.CAUTION,
                            bestTime = "Early morning",
                            message =
                            "Heat or high humidity may increase fatigue. Reduce intensity and stay hydrated."
                        )

                    hasRain ->
                        result(
                            status = ActivityStatus.CAUTION,
                            bestTime = "After rainfall",
                            message =
                            "Wet paths may be slippery. Wear suitable footwear and reduce your pace."
                        )

                    else ->
                        result(
                            status = ActivityStatus.GOOD,
                            bestTime = preferredTime,
                            message =
                            "Current conditions are generally suitable for running."
                        )
                }
            }

            "cycling" -> {
                when {
                    hasStrongWind || hasRain ->
                        result(
                            status = ActivityStatus.AVOID,
                            bestTime =
                            "After rain and strong winds",

                            message =
                            "Wet roads or strong winds may reduce control and make cycling unsafe."
                        )

                    isVeryHot || hasModerateWind ->
                        result(
                            status = ActivityStatus.CAUTION,
                            bestTime = "Early morning",
                            message =
                            "Warm or windy conditions may make cycling more difficult. Bring water."
                        )

                    else ->
                        result(
                            status = ActivityStatus.GOOD,
                            bestTime = preferredTime,
                            message =
                            "Current conditions are generally suitable for cycling."
                        )
                }
            }

            "gardening" -> {
                when {
                    isVeryHot ->
                        result(
                            status = ActivityStatus.CAUTION,
                            bestTime =
                            "Early morning or late afternoon",

                            message =
                            "Avoid prolonged sun exposure and take frequent water breaks."
                        )

                    hasRain ->
                        result(
                            status = ActivityStatus.CAUTION,
                            bestTime = "After light rainfall",
                            message =
                            "The ground may be wet or muddy. Use suitable footwear."
                        )

                    hasHighHumidity ->
                        result(
                            status = ActivityStatus.CAUTION,
                            bestTime = "Early morning",
                            message =
                            "High humidity may make physical work uncomfortable. Take regular breaks."
                        )

                    else ->
                        result(
                            status = ActivityStatus.GOOD,
                            bestTime = preferredTime,
                            message =
                            "Conditions are generally suitable for gardening."
                        )
                }
            }

            "golf" -> {
                when {
                    hasStrongWind || hasRain ->
                        result(
                            status = ActivityStatus.AVOID,
                            bestTime =
                            "After rain and strong winds",

                            message =
                            "Rain or strong winds may affect visibility, footing and ball control."
                        )

                    isVeryHot ->
                        result(
                            status = ActivityStatus.CAUTION,
                            bestTime = "Early morning",
                            message =
                            "Extended sun exposure may cause heat stress. Bring water and sun protection."
                        )

                    else ->
                        result(
                            status = ActivityStatus.GOOD,
                            bestTime = preferredTime,
                            message =
                            "Current conditions are generally suitable for golf."
                        )
                }
            }

            "beach" -> {
                when {
                    hasStrongWind || hasRain ->
                        result(
                            status = ActivityStatus.AVOID,
                            bestTime =
                            "After rain and strong winds",

                            message =
                            "Rain and strong winds may make beach conditions uncomfortable or unsafe."
                        )

                    isVeryHot ->
                        result(
                            status = ActivityStatus.CAUTION,
                            bestTime =
                            "Early morning or late afternoon",

                            message =
                            "Very warm conditions are expected. Use sun protection and stay hydrated."
                        )

                    else ->
                        result(
                            status = ActivityStatus.GOOD,
                            bestTime = preferredTime,
                            message =
                            "Conditions are generally suitable for a beach visit."
                        )
                }
            }

            "hiking" -> {
                when {
                    hasStrongWind || hasRain ->
                        result(
                            status = ActivityStatus.AVOID,
                            bestTime =
                            "After conditions improve",

                            message =
                            "Rain or strong winds may make trails slippery and exposed areas unsafe."
                        )

                    isHot || hasHighHumidity ->
                        result(
                            status = ActivityStatus.CAUTION,
                            bestTime = "Early morning",
                            message =
                            "Warm or humid conditions may increase fatigue. Bring enough water."
                        )

                    else ->
                        result(
                            status = ActivityStatus.GOOD,
                            bestTime = preferredTime,
                            message =
                            "Current conditions are generally suitable for hiking."
                        )
                }
            }

            "kids sports" -> {
                when {
                    hasRain || hasStrongWind ->
                        result(
                            status = ActivityStatus.AVOID,
                            bestTime =
                            "After conditions improve",

                            message =
                            "Wet surfaces or strong winds may make outdoor play unsafe."
                        )

                    isHot || hasHighHumidity ->
                        result(
                            status = ActivityStatus.CAUTION,
                            bestTime = "Early morning",
                            message =
                            "Schedule frequent water breaks and reduce prolonged sun exposure."
                        )

                    else ->
                        result(
                            status = ActivityStatus.GOOD,
                            bestTime = preferredTime,
                            message =
                            "Conditions are generally suitable for outdoor sports."
                        )
                }
            }

            "dog walking" -> {
                when {
                    isVeryHot ->
                        result(
                            status = ActivityStatus.AVOID,
                            bestTime =
                            "Early morning or evening",

                            message =
                            "Very hot surfaces may hurt your dog's paws. Wait until temperatures become cooler."
                        )

                    hasRain || isHot ->
                        result(
                            status = ActivityStatus.CAUTION,
                            bestTime =
                            "Early morning or after rainfall",

                            message =
                            "Keep the walk short and check pavement conditions before going outside."
                        )

                    else ->
                        result(
                            status = ActivityStatus.GOOD,
                            bestTime = preferredTime,
                            message =
                            "Current conditions are generally suitable for dog walking."
                        )
                }
            }

            "surfing" -> {
                when {
                    hasStrongWind ->
                        result(
                            status = ActivityStatus.AVOID,
                            bestTime =
                            "When winds become lighter",

                            message =
                            "Strong winds may create difficult and unpredictable water conditions."
                        )

                    hasRain || hasModerateWind ->
                        result(
                            status = ActivityStatus.CAUTION,
                            bestTime =
                            "When local water conditions improve",

                            message =
                            "Check official coastal and wave conditions before entering the water."
                        )

                    else ->
                        result(
                            status = ActivityStatus.GOOD,
                            bestTime = preferredTime,
                            message =
                            "Surface weather appears acceptable, but check local wave and coastal advisories."
                        )
                }
            }

            "sailing" -> {
                when {
                    hasStrongWind ->
                        result(
                            status = ActivityStatus.AVOID,
                            bestTime =
                            "When winds become lighter",

                            message =
                            "Strong winds may make sailing difficult or unsafe."
                        )

                    hasRain || hasModerateWind ->
                        result(
                            status = ActivityStatus.CAUTION,
                            bestTime =
                            "When conditions stabilize",

                            message =
                            "Use caution and check official marine forecasts before departure."
                        )

                    else ->
                        result(
                            status = ActivityStatus.GOOD,
                            bestTime = preferredTime,
                            message =
                            "Surface weather appears acceptable. Confirm local marine conditions before sailing."
                        )
                }
            }

            "farming" -> {
                when {
                    hasStrongWind ->
                        result(
                            status = ActivityStatus.AVOID,
                            bestTime =
                            "When winds become lighter",

                            message =
                            "Strong winds may make equipment use and field work unsafe."
                        )

                    isVeryHot || hasHighHumidity ->
                        result(
                            status = ActivityStatus.CAUTION,
                            bestTime = "Early morning",
                            message =
                            "Heat or humidity may increase fatigue. Take frequent breaks and stay hydrated."
                        )

                    hasRain ->
                        result(
                            status = ActivityStatus.CAUTION,
                            bestTime = "After rainfall",
                            message =
                            "Wet ground may affect field access, equipment and soil conditions."
                        )

                    else ->
                        result(
                            status = ActivityStatus.GOOD,
                            bestTime = preferredTime,
                            message =
                            "Conditions are generally suitable for outdoor farm work."
                        )
                }
            }

            "flying" -> {
                when {
                    hasRain || hasStrongWind ->
                        result(
                            status = ActivityStatus.AVOID,
                            bestTime =
                            "After conditions improve",

                            message =
                            "Rain or strong winds may affect flight safety. Use certified aviation weather information."
                        )

                    hasModerateWind ->
                        result(
                            status = ActivityStatus.CAUTION,
                            bestTime =
                            "When winds become lighter",

                            message =
                            "Wind may affect flight conditions. Check an official aviation weather service."
                        )

                    else ->
                        result(
                            status = ActivityStatus.GOOD,
                            bestTime = preferredTime,
                            message =
                            "General surface weather appears acceptable. Always verify official aviation forecasts."
                        )
                }
            }

            else -> {
                when {
                    hasStrongWind ||
                            hasRain ||
                            isVeryHot ->
                        result(
                            status = ActivityStatus.CAUTION,
                            bestTime = preferredTime,
                            message =
                            "Current weather may affect this activity. Proceed with caution."
                        )

                    else ->
                        result(
                            status = ActivityStatus.GOOD,
                            bestTime = preferredTime,
                            message =
                            "Current conditions are generally suitable for this activity."
                        )
                }
            }
        }
    }



    private fun goodRecommendation(
        activityName: String,
        icon: String,
        bestTime: String
    ): ActivityRecommendation {
        return ActivityRecommendation(
            activityName = activityName,
            icon = icon,
            status = ActivityStatus.GOOD,
            bestTime = bestTime,
            recommendation =
            "Current conditions are generally suitable for this activity."
        )
    }

    private fun getPreferredTime(): String {
        return preferredSchedule
            .firstOrNull()
            ?.takeIf {
                it.isNotBlank()
            }
            ?: "Morning or late afternoon"
    }

    private fun getActivityIcon(
        activityName: String
    ): String {
        return when (
            activityName
                .trim()
                .lowercase(Locale.US)
        ) {
            "running" ->
                "🏃"

            "cycling" ->
                "🚴"

            "gardening" ->
                "🌱"

            "golf" ->
                "🏌️"

            "beach" ->
                "🏖️"

            "hiking" ->
                "🥾"

            "kids sports" ->
                "⚽"

            "dog walking" ->
                "🐕"

            "surfing" ->
                "🏄"

            "sailing" ->
                "⛵"

            "farming" ->
                "🚜"

            "flying" ->
                "✈️"

            else ->
                "🌤️"
        }
    }

    private fun determineOverallStatus(
        recommendations:
        List<ActivityRecommendation>
    ): ActivityStatus {
        return when {
            recommendations.any {
                it.status ==
                        ActivityStatus.AVOID
            } ->
                ActivityStatus.AVOID

            recommendations.any {
                it.status ==
                        ActivityStatus.CAUTION
            } ->
                ActivityStatus.CAUTION

            recommendations.all {
                it.status ==
                        ActivityStatus.GOOD
            } ->
                ActivityStatus.GOOD

            else ->
                ActivityStatus.UNKNOWN
        }
    }

    private fun displayOverallStatus(
        status: ActivityStatus
    ) {
        val statusColor =
            when (status) {
                ActivityStatus.GOOD ->
                    "#73E6A0"

                ActivityStatus.CAUTION ->
                    "#FFD166"

                ActivityStatus.AVOID ->
                    "#FF7777"

                ActivityStatus.UNKNOWN ->
                    "#B5B5B5"
            }

        tvOverallStatus.setTextColor(
            Color.parseColor(
                statusColor
            )
        )

        when (status) {
            ActivityStatus.GOOD -> {
                tvOverallStatus.text =
                    "Good for outdoor activities"

                tvOverallMessage.text =
                    "Current conditions are generally suitable for your selected activities."
            }

            ActivityStatus.CAUTION -> {
                tvOverallStatus.text =
                    "Proceed with caution"

                tvOverallMessage.text =
                    "Some weather conditions may affect your selected activities."
            }

            ActivityStatus.AVOID -> {
                tvOverallStatus.text =
                    "Consider staying indoors"

                tvOverallMessage.text =
                    "Current conditions may make one or more activities unsafe."
            }

            ActivityStatus.UNKNOWN -> {
                tvOverallStatus.text =
                    "Weather unavailable"

                tvOverallMessage.text =
                    "Refresh the weather forecast to receive activity recommendations."
            }
        }
    }

    private fun configureNormalScreen(
        rootView: View
    ) {
        val window =
            requireActivity().window

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).show(
            WindowInsetsCompat.Type.systemBars()
        )

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        val originalLeft =
            rootView.paddingLeft

        val originalTop =
            rootView.paddingTop

        val originalRight =
            rootView.paddingRight

        val originalBottom =
            rootView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(
            rootView
        ) { view, windowInsets ->

            val systemBars =
                windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                originalLeft + systemBars.left,
                originalTop + systemBars.top,
                originalRight + systemBars.right,
                originalBottom + systemBars.bottom
            )

            windowInsets
        }

        ViewCompat.requestApplyInsets(
            rootView
        )
    }
}