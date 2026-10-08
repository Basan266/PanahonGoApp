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
import kotlinx.coroutines.launch
import android.widget.Toast
import com.example.weatherapp.data.repository.SupabaseOnboardingRepository

class WeatherConcernsFragment :
    Fragment(R.layout.fragment_weather_concerns) {

    private lateinit var userPreferences: UserPreferences

    private val onboardingRepository = SupabaseOnboardingRepository()

    private lateinit var btnContinue: Button

    private val selectedConcerns = mutableSetOf<String>()

    private val concernButtons =
        mutableMapOf<ToggleButton, String>()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())
        btnContinue = view.findViewById(R.id.btnContinue)

        concernButtons.putAll(
            mapOf(
                view.findViewById<ToggleButton>(
                    R.id.btnRain
                ) to "Rain",

                view.findViewById<ToggleButton>(
                    R.id.btnExtremeHeat
                ) to "Extreme heat",

                view.findViewById<ToggleButton>(
                    R.id.btnStrongWind
                ) to "Strong wind",

                view.findViewById<ToggleButton>(
                    R.id.btnHumidity
                ) to "High humidity",

                view.findViewById<ToggleButton>(
                    R.id.btnUvExposure
                ) to "UV exposure",

                view.findViewById<ToggleButton>(
                    R.id.btnAirQuality
                ) to "Air quality",

                view.findViewById<ToggleButton>(
                    R.id.btnThunderstorms
                ) to "Thunderstorms",

                view.findViewById<ToggleButton>(
                    R.id.btnVisibility
                ) to "Poor visibility"
            )
        )

        configureConcernButtons()
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


    private fun configureConcernButtons() {
        concernButtons.forEach { (button, concern) ->
            button.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    selectedConcerns.add(concern)
                } else {
                    selectedConcerns.remove(concern)
                }

                btnContinue.isEnabled =
                    selectedConcerns.isNotEmpty()
            }
        }
    }

    private fun configureContinueButton() {
        btnContinue.setOnClickListener {
            saveConcernsAndContinue(
                concerns = selectedConcerns
            )
        }
    }

    private fun configureSkipButton(view: View) {
        val btnSkip =
            view.findViewById<Button>(
                R.id.btnSkip
            )

        btnSkip.setOnClickListener {
            saveConcernsAndContinue(
                concerns = emptySet()
            )
        }
    }

    private fun saveConcernsAndContinue(
        concerns: Set<String>
    ) {
        btnContinue.isEnabled = false
        btnContinue.text = "Saving..."

        viewLifecycleOwner.lifecycleScope.launch {
            val onlineResult =
                onboardingRepository
                    .saveWeatherConcerns(
                        concerns = concerns
                    )

            onlineResult.onSuccess {
                userPreferences
                    .saveWeatherConcerns(
                        concerns
                    )

                openNextScreen()
            }.onFailure { exception ->
                btnContinue.text = "Continue"

                btnContinue.isEnabled =
                    selectedConcerns.isNotEmpty()

                Toast.makeText(
                    requireContext(),
                    exception.message
                        ?: "Unable to save weather concerns.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun openNextScreen() {
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                OutdoorScheduleFragment()
            )
            .commit()
    }
}