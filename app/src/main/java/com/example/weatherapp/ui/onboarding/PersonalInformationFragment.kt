package com.example.weatherapp.ui.onboarding

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.R
import com.example.weatherapp.data.local.preferences.UserPreferences
import com.example.weatherapp.data.remote.supabase.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.widget.Toast
import com.example.weatherapp.data.repository.SupabaseOnboardingRepository

class PersonalInformationFragment :
    Fragment(R.layout.fragment_personal_information) {

    private lateinit var userPreferences: UserPreferences

    private val onboardingRepository = SupabaseOnboardingRepository()

    private lateinit var etBirthday: EditText
    private lateinit var tvAge: TextView
    private lateinit var rgGender: RadioGroup
    private lateinit var btnContinue: Button

    private var selectedBirthdayMillis: Long = 0L
    private var calculatedAge: Int = 0
    private var selectedGender: String = ""

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences =
            UserPreferences(requireContext())

        bindViews(view)
        configureBirthdayPicker()
        configureGenderSelection()
        configureContinueButton()
        configureSkipButton(view)
        configureNormalScreen(view)
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
        ) { currentView, windowInsets ->

            val systemBars =
                windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            currentView.setPadding(
                originalPaddingLeft,
                originalPaddingTop + systemBars.top,
                originalPaddingRight,
                originalPaddingBottom + systemBars.bottom
            )

            windowInsets
        }

        ViewCompat.requestApplyInsets(rootView)
    }

    private fun bindViews(view: View) {
        etBirthday =
            view.findViewById(R.id.etBirthday)

        tvAge =
            view.findViewById(R.id.tvAge)

        rgGender =
            view.findViewById(R.id.rgGender)

        btnContinue =
            view.findViewById(R.id.btnContinue)
    }

    private fun configureBirthdayPicker() {
        etBirthday.setOnClickListener {
            showBirthdayPicker()
        }
    }

    private fun showBirthdayPicker() {
        val today =
            Calendar.getInstance()

        val initialDate =
            Calendar.getInstance().apply {
                add(Calendar.YEAR, -18)
            }

        val dialog =
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->

                    val birthday =
                        Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(
                                Calendar.DAY_OF_MONTH,
                                dayOfMonth
                            )
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                    selectedBirthdayMillis =
                        birthday.timeInMillis

                    calculatedAge =
                        calculateAge(
                            birthday = birthday,
                            today = today
                        )

                    displayBirthday(
                        selectedBirthdayMillis
                    )

                    tvAge.text =
                        "$calculatedAge years old"

                    validateForm()
                },
                initialDate.get(Calendar.YEAR),
                initialDate.get(Calendar.MONTH),
                initialDate.get(Calendar.DAY_OF_MONTH)
            )

        dialog.datePicker.maxDate =
            System.currentTimeMillis()

        dialog.show()
    }

    private fun displayBirthday(
        birthdayMillis: Long
    ) {
        val formatter =
            SimpleDateFormat(
                "MMMM dd, yyyy",
                Locale.getDefault()
            )

        etBirthday.setText(
            formatter.format(
                Date(birthdayMillis)
            )
        )
    }

    private fun calculateAge(
        birthday: Calendar,
        today: Calendar
    ): Int {
        var age =
            today.get(Calendar.YEAR) -
                    birthday.get(Calendar.YEAR)

        val birthdayHasNotOccurred =
            today.get(Calendar.DAY_OF_YEAR) <
                    birthday.get(Calendar.DAY_OF_YEAR)

        if (birthdayHasNotOccurred) {
            age--
        }

        return age
    }

    private fun configureGenderSelection() {
        rgGender.setOnCheckedChangeListener {
                _,
                checkedId ->

            selectedGender =
                when (checkedId) {
                    R.id.rbMale ->
                        "Male"

                    R.id.rbFemale ->
                        "Female"

                    R.id.rbNonBinary ->
                        "Non-binary"

                    R.id.rbPreferNotToSay ->
                        "Prefer not to say"

                    else ->
                        ""
                }

            validateForm()
        }
    }

    private fun validateForm() {
        val hasBirthday =
            selectedBirthdayMillis > 0L

        val hasValidAge =
            calculatedAge >= 0

        val hasGender =
            selectedGender.isNotEmpty()

        btnContinue.isEnabled =
            hasBirthday &&
                    hasValidAge &&
                    hasGender
    }

    private fun configureContinueButton() {
        btnContinue.setOnClickListener {
            saveInformationAndContinue()
        }
    }

    private fun saveInformationAndContinue() {
        val displayName =
            getSignedInUserDisplayName()

        btnContinue.isEnabled = false
        btnContinue.text = "Saving..."

        viewLifecycleOwner.lifecycleScope.launch {
            val onlineResult =
                onboardingRepository
                    .savePersonalInformation(
                        displayName = displayName,
                        birthdayMillis =
                        selectedBirthdayMillis,
                        gender = selectedGender
                    )

            onlineResult.onSuccess {
                userPreferences.savePersonalInformation(
                    displayName = displayName,
                    birthday = selectedBirthdayMillis,
                    age = calculatedAge,
                    gender = selectedGender
                )

                openActivitySelection()
            }.onFailure { exception ->
                btnContinue.text = "Continue"
                validateForm()

                Toast.makeText(
                    requireContext(),
                    exception.message
                        ?: "Unable to save your information.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun getSignedInUserDisplayName(): String {
        val currentUser =
            SupabaseClientProvider.client
                .auth
                .currentUserOrNull()

        val metadataDisplayName =
            currentUser
                ?.userMetadata
                ?.get("display_name")
                ?.jsonPrimitive
                ?.contentOrNull
                ?.trim()
                .orEmpty()

        if (metadataDisplayName.isNotBlank()) {
            return metadataDisplayName
        }

        return currentUser
            ?.email
            ?.substringBefore("@")
            ?.trim()
            .orEmpty()
    }

    private fun configureSkipButton(view: View) {
        view.findViewById<Button>(R.id.btnSkip)
            .setOnClickListener {
                openActivitySelection()
            }
    }

    private fun openActivitySelection() {
        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                ActivitySelectionFragment()
            )
            .commit()
    }
}