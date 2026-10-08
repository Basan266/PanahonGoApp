package com.example.weatherapp.ui.onboarding

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ToggleButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.R
import com.example.weatherapp.data.local.preferences.UserPreferences
import com.example.weatherapp.ui.home.HomeFragment
import com.example.weatherapp.ui.map.MapDashboardFragment
import kotlinx.coroutines.launch
import android.widget.Toast
import com.example.weatherapp.data.repository.SupabaseOnboardingRepository

class OutdoorScheduleFragment :
    Fragment(R.layout.fragment_outdoor_schedule) {

    private lateinit var userPreferences: UserPreferences
    private val onboardingRepository = SupabaseOnboardingRepository()
    private lateinit var btnFinish: Button

    private val selectedSchedule = mutableSetOf<String>()

    private val scheduleButtons =
        mutableMapOf<ToggleButton, String>()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())
        btnFinish = view.findViewById(R.id.btnFinish)

        scheduleButtons.putAll(
            mapOf(
                view.findViewById<ToggleButton>(
                    R.id.btnEarlyMorning
                ) to "Early morning",

                view.findViewById<ToggleButton>(
                    R.id.btnMorning
                ) to "Morning",

                view.findViewById<ToggleButton>(
                    R.id.btnMidday
                ) to "Midday",

                view.findViewById<ToggleButton>(
                    R.id.btnAfternoon
                ) to "Afternoon",

                view.findViewById<ToggleButton>(
                    R.id.btnEvening
                ) to "Evening",

                view.findViewById<ToggleButton>(
                    R.id.btnWeekdays
                ) to "Weekdays",

                view.findViewById<ToggleButton>(
                    R.id.btnWeekends
                ) to "Weekends"
            )
        )

        configureScheduleButtons()
        configureFinishButton()
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


    private fun configureScheduleButtons() {
        scheduleButtons.forEach { (button, schedule) ->
            button.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    selectedSchedule.add(schedule)
                } else {
                    selectedSchedule.remove(schedule)
                }

                btnFinish.isEnabled =
                    selectedSchedule.isNotEmpty()
            }
        }
    }

    private fun configureFinishButton() {
        btnFinish.setOnClickListener {
            saveScheduleAndFinish(
                schedule = selectedSchedule.toSet()
            )
        }
    }
    private fun configureSkipButton(view: View) {
        val btnSkip =
            view.findViewById<Button>(
                R.id.btnSkip
            )

        btnSkip.setOnClickListener {
            saveScheduleAndFinish(
                schedule = emptySet()
            )
        }
    }

    private fun saveScheduleAndFinish(
        schedule: Set<String>
    ) {
        btnFinish.isEnabled = false
        btnFinish.text = "Saving..."

        viewLifecycleOwner.lifecycleScope.launch {
            val onlineResult =
                onboardingRepository
                    .finishOnboarding(
                        schedule = schedule
                    )

            onlineResult.onSuccess {
                userPreferences
                    .finishOnboarding(
                        schedule
                    )

                openHomeScreen()
            }.onFailure { exception ->
                btnFinish.text = "Finish"

                btnFinish.isEnabled =
                    selectedSchedule.isNotEmpty()

                Toast.makeText(
                    requireContext(),
                    exception.message
                        ?: "Unable to finish onboarding.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
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