package com.example.weatherapp.ui.onboarding

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.widget.ToggleButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.R
import com.example.weatherapp.data.local.preferences.UserPreferences
import kotlinx.coroutines.launch
import com.example.weatherapp.ui.home.HomeFragment
import com.example.weatherapp.data.repository.SupabaseOnboardingRepository


class ActivitySelectionFragment :
    Fragment(R.layout.fragment_activity_selection) {

    private lateinit var userPreferences: UserPreferences
    private val onboardingRepository = SupabaseOnboardingRepository()
    private lateinit var btnContinue: Button

    private val selectedActivities = mutableSetOf<String>()
    private val activityButtons = mutableMapOf<ToggleButton, String>()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())
        btnContinue = view.findViewById(R.id.btnContinue)

        activityButtons.putAll(
            mapOf(
                view.findViewById<ToggleButton>(R.id.btnRunning) to
                        "Running",

                view.findViewById<ToggleButton>(R.id.btnCycling) to
                        "Cycling",

                view.findViewById<ToggleButton>(R.id.btnGardening) to
                        "Gardening",

                view.findViewById<ToggleButton>(R.id.btnGolf) to
                        "Golf",

                view.findViewById<ToggleButton>(R.id.btnBeach) to
                        "Beach",

                view.findViewById<ToggleButton>(R.id.btnHiking) to
                        "Hiking",

                view.findViewById<ToggleButton>(R.id.btnKidsSports) to
                        "Kids sports",

                view.findViewById<ToggleButton>(R.id.btnDogWalking) to
                        "Dog walking",

                view.findViewById<ToggleButton>(R.id.btnSurfing) to
                        "Surfing",

                view.findViewById<ToggleButton>(R.id.btnSailing) to
                        "Sailing",

                view.findViewById<ToggleButton>(R.id.btnFarming) to
                        "Farming",

                view.findViewById<ToggleButton>(R.id.btnFlying) to
                        "Flying"
            )
        )

        configureActivityButtons()
        configureContinueButton()
        configureSkipButton(view)
        configureNormalScreen(view)
    }

    private fun configureNormalScreen(
        rootView: View
    ) {
        val window =
            requireActivity().window

        // Keep Android status and navigation bars visible.
        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).show(
            WindowInsetsCompat.Type.systemBars()
        )

        // We manually apply the safe screen area.
        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        val originalPaddingLeft =
            rootView.paddingLeft

        val originalPaddingTop =
            rootView.paddingTop

        val originalPaddingRight =
            rootView.paddingRight

        val originalPaddingBottom =
            rootView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(
            rootView
        ) { view, windowInsets ->

            val systemBars =
                windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                originalPaddingLeft,
                originalPaddingTop + systemBars.top,
                originalPaddingRight,
                originalPaddingBottom + systemBars.bottom
            )

            windowInsets
        }

        ViewCompat.requestApplyInsets(
            rootView
        )
    }


    private fun configureActivityButtons() {
        activityButtons.forEach { (button, activity) ->
            button.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedActivities.add(activity)
                } else {
                    selectedActivities.remove(activity)
                }

                btnContinue.isEnabled =
                    selectedActivities.isNotEmpty()
            }
        }
    }

    private fun configureContinueButton() {
        btnContinue.setOnClickListener {
            saveActivitiesAndContinue(
                activities = selectedActivities
            )
        }
    }


    private fun configureSkipButton(view: View) {
        val btnSkip =
            view.findViewById<Button>(
                R.id.btnSkip
            )

        btnSkip.setOnClickListener {
            saveActivitiesAndContinue(
                activities = emptySet()
            )
        }
    }


    private fun saveActivitiesAndContinue(
        activities: Set<String>
    ) {
        btnContinue.isEnabled = false
        btnContinue.text = "Saving..."

        viewLifecycleOwner.lifecycleScope.launch {
            val onlineResult =
                onboardingRepository
                    .saveSelectedActivities(
                        activities = activities
                    )

            onlineResult.onSuccess {
                userPreferences
                    .saveSelectedActivities(
                        activities
                    )

                openWeatherConcerns()
            }.onFailure { exception ->
                btnContinue.text = "Continue"

                btnContinue.isEnabled =
                    selectedActivities.isNotEmpty()

                Toast.makeText(
                    requireContext(),
                    exception.message
                        ?: "Unable to save selected activities.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun openWeatherConcerns() {
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                WeatherConcernsFragment()
            )
            .commit()
    }

    private fun openHomeScreen() {
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                HomeFragment()
            )
            .commit()
    }
}