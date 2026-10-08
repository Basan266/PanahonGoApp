package com.example.weatherapp.ui.profile

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.weatherapp.R
import com.example.weatherapp.data.repository.SupabaseAuthRepository
import com.example.weatherapp.data.repository.SupabaseOnboardingRepository
import com.example.weatherapp.ui.home.AvatarAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.fragment.app.FragmentManager
import com.example.weatherapp.data.remote.supabase.SupabaseClientProvider
import com.example.weatherapp.ui.MainActivity
import com.example.weatherapp.ui.auth.SignInFragment
import io.github.jan.supabase.auth.auth

class ProfileFragment :
    Fragment(R.layout.fragment_profile) {

    private val onboardingRepository =
        SupabaseOnboardingRepository()

    private val authRepository =
        SupabaseAuthRepository()

    private var currentAvatarSeed: String? =
        null

    private lateinit var ivProfileAvatar:
            ImageView

    private lateinit var tvProfileName:
            TextView

    private lateinit var tvProfileEmail:
            TextView

    private lateinit var tvProfileBirthday:
            TextView

    private lateinit var tvProfileAge:
            TextView

    private lateinit var tvProfileGender:
            TextView

    private lateinit var chipGroupActivities:
            ChipGroup

    private lateinit var chipGroupWeatherConcerns:
            ChipGroup

    private lateinit var chipGroupOutdoorSchedule:
            ChipGroup

    private lateinit var btnSignOut:
            MaterialButton

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        bindViews(view)
        configureNormalScreen(view)
        configureAvatarClick()
        configureSignOutButton()
        loadProfile()
    }

    private fun bindViews(
        view: View
    ) {
        ivProfileAvatar =
            view.findViewById(
                R.id.ivProfileAvatar
            )

        tvProfileName =
            view.findViewById(
                R.id.tvProfileName
            )

        tvProfileEmail =
            view.findViewById(
                R.id.tvProfileEmail
            )

        tvProfileBirthday =
            view.findViewById(
                R.id.tvProfileBirthday
            )

        tvProfileAge =
            view.findViewById(
                R.id.tvProfileAge
            )

        tvProfileGender =
            view.findViewById(
                R.id.tvProfileGender
            )

        chipGroupActivities =
            view.findViewById(
                R.id.chipGroupActivities
            )

        chipGroupWeatherConcerns =
            view.findViewById(
                R.id.chipGroupWeatherConcerns
            )

        chipGroupOutdoorSchedule =
            view.findViewById(
                R.id.chipGroupOutdoorSchedule
            )

        btnSignOut =
            view.findViewById(
                R.id.btnSignOut
            )
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
        ) { view, windowInsets ->

            val systemBars =
                windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                originalPaddingLeft +
                        systemBars.left,

                originalPaddingTop +
                        systemBars.top,

                originalPaddingRight +
                        systemBars.right,

                originalPaddingBottom +
                        systemBars.bottom
            )

            windowInsets
        }

        ViewCompat.requestApplyInsets(
            rootView
        )
    }

    private fun configureAvatarClick() {
        ivProfileAvatar.setOnClickListener {
            showAvatarPicker()
        }
    }


    private fun configureSignOutButton() {
        btnSignOut.setOnClickListener {
            showSignOutConfirmation()
        }
    }

    private fun showSignOutConfirmation() {
        AlertDialog.Builder(
            requireContext()
        )
            .setTitle(
                "Sign out"
            )
            .setMessage(
                "Are you sure you want to sign out?"
            )
            .setNegativeButton(
                "Cancel"
            ) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(
                "Sign out"
            ) { dialog, _ ->
                dialog.dismiss()
                signOutUser()
            }
            .show()
    }

    private fun signOutUser() {
        btnSignOut.isEnabled =
            false

        btnSignOut.text =
            "Signing out..."

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                SupabaseClientProvider
                    .client
                    .auth
                    .signOut()
            }.onSuccess {
                if (
                    !isAdded ||
                    view == null
                ) {
                    return@onSuccess
                }

                openSignInScreen()
            }.onFailure { error ->
                if (
                    !isAdded ||
                    view == null
                ) {
                    return@onFailure
                }

                btnSignOut.isEnabled =
                    true

                btnSignOut.text =
                    "Sign Out"

                Log.e(
                    "ProfileFragment",
                    "Unable to sign out",
                    error
                )

                Toast.makeText(
                    requireContext(),
                    error.message
                        ?: "Unable to sign out. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun openSignInScreen() {
        val mainActivity =
            activity as? MainActivity

        mainActivity
            ?.hideBottomNavigation()

        parentFragmentManager
            .popBackStack(
                null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )

        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                SignInFragment()
            )
            .commit()
    }



    private fun loadProfile() {
        tvProfileEmail.text =
            authRepository
                .getCurrentUserEmail()
                ?: "Email unavailable"

        viewLifecycleOwner.lifecycleScope.launch {
            onboardingRepository
                .getCurrentUserProfile()
                .onSuccess { profile ->

                    if (
                        profile == null ||
                        !isAdded ||
                        view == null
                    ) {
                        return@onSuccess
                    }

                    tvProfileName.text =
                        profile.displayName
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: "WeatherApp User"

                    tvProfileGender.text =
                        profile.gender
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: "Not provided"

                    displayBirthdayAndAge(
                        birthdayValue =
                        profile.birthday
                    )

                    displayPreferences(
                        activities =
                        profile.selectedActivities,

                        concerns =
                        profile.weatherConcerns,

                        schedule =
                        profile.outdoorSchedule
                    )

                    currentAvatarSeed =
                        profile.avatarSeed

                    loadAvatar(
                        avatarStyle =
                        profile.avatarStyle,

                        avatarSeed =
                        profile.avatarSeed
                    )
                }
                .onFailure { error ->

                    if (
                        !isAdded ||
                        view == null
                    ) {
                        return@onFailure
                    }

                    Log.e(
                        "ProfileFragment",
                        "Unable to load profile",
                        error
                    )

                    Toast.makeText(
                        requireContext(),
                        error.message
                            ?: "Unable to load profile.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun displayBirthdayAndAge(
        birthdayValue: String?
    ) {
        if (birthdayValue.isNullOrBlank()) {
            tvProfileBirthday.text =
                "Not provided"

            tvProfileAge.text =
                "Not provided"

            return
        }

        runCatching {
            LocalDate.parse(
                birthdayValue
            )
        }.onSuccess { birthday ->
            val birthdayFormatter =
                DateTimeFormatter.ofPattern(
                    "MMMM dd, yyyy",
                    Locale.getDefault()
                )

            tvProfileBirthday.text =
                birthday.format(
                    birthdayFormatter
                )

            val age =
                Period.between(
                    birthday,
                    LocalDate.now()
                ).years

            tvProfileAge.text =
                "$age years old"
        }.onFailure {
            tvProfileBirthday.text =
                birthdayValue

            tvProfileAge.text =
                "Not available"
        }
    }

    private fun displayPreferences(
        activities: List<String>,
        concerns: List<String>,
        schedule: List<String>
    ) {
        displayPreferenceChips(
            chipGroup =
            chipGroupActivities,

            values =
            activities,

            iconProvider =
            ::getActivityIcon,

            emptyMessage =
            "No activities selected"
        )

        displayPreferenceChips(
            chipGroup =
            chipGroupWeatherConcerns,

            values =
            concerns,

            iconProvider =
            ::getConcernIcon,

            emptyMessage =
            "No weather concerns selected"
        )

        displayPreferenceChips(
            chipGroup =
            chipGroupOutdoorSchedule,

            values =
            schedule,

            iconProvider =
            ::getScheduleIcon,

            emptyMessage =
            "No schedule selected"
        )
    }

    private fun displayPreferenceChips(
        chipGroup: ChipGroup,
        values: List<String>,
        iconProvider: (String) -> String,
        emptyMessage: String
    ) {
        chipGroup.removeAllViews()

        val cleanedValues =
            values
                .map {
                    it.trim()
                }
                .filter {
                    it.isNotBlank()
                }
                .distinct()

        if (cleanedValues.isEmpty()) {
            addPreferenceChip(
                chipGroup =
                chipGroup,

                label =
                emptyMessage,

                icon =
                "—",

                isEmpty =
                true
            )

            return
        }

        cleanedValues.forEach { value ->
            addPreferenceChip(
                chipGroup =
                chipGroup,

                label =
                value,

                icon =
                iconProvider(value),

                isEmpty =
                false
            )
        }
    }

    private fun addPreferenceChip(
        chipGroup: ChipGroup,
        label: String,
        icon: String,
        isEmpty: Boolean
    ) {
        val density =
            resources.displayMetrics.density

        val chip =
            Chip(requireContext()).apply {
                text =
                    "$icon  $label"

                isClickable =
                    false

                isCheckable =
                    false

                isFocusable =
                    false

                chipCornerRadius =
                    18f * density

                chipMinHeight =
                    36f * density

                chipStartPadding =
                    10f * density

                chipEndPadding =
                    10f * density

                textStartPadding =
                    0f

                textEndPadding =
                    0f

                chipBackgroundColor =
                    ColorStateList.valueOf(
                        Color.parseColor(
                            if (isEmpty) {
                                "#242424"
                            } else {
                                "#303030"
                            }
                        )
                    )

                chipStrokeColor =
                    ColorStateList.valueOf(
                        Color.parseColor(
                            if (isEmpty) {
                                "#3A3A3A"
                            } else {
                                "#4A4A4A"
                            }
                        )
                    )

                chipStrokeWidth =
                    1f * density

                setTextColor(
                    Color.parseColor(
                        if (isEmpty) {
                            "#888888"
                        } else {
                            "#F2F2F2"
                        }
                    )
                )

                textSize =
                    12f
            }

        chipGroup.addView(
            chip
        )
    }

    private fun getActivityIcon(
        activity: String
    ): String {
        return when (
            activity
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
                "⛳"

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
                "🏃"
        }
    }

    private fun getConcernIcon(
        concern: String
    ): String {
        return when (
            concern
                .trim()
                .lowercase(Locale.US)
        ) {
            "rain" ->
                "🌧️"

            "extreme heat" ->
                "🌡️"

            "strong wind" ->
                "💨"

            "high humidity" ->
                "💧"

            "uv exposure" ->
                "☀️"

            "air quality" ->
                "🍃"

            "thunderstorms" ->
                "⛈️"

            "poor visibility" ->
                "🌫️"

            else ->
                "⚠️"
        }
    }

    private fun getScheduleIcon(
        schedule: String
    ): String {
        val normalizedSchedule =
            schedule
                .trim()
                .lowercase(Locale.US)

        return when {
            normalizedSchedule.contains(
                "early morning"
            ) -> {
                "🌅"
            }

            normalizedSchedule.contains(
                "morning"
            ) -> {
                "☀️"
            }

            normalizedSchedule.contains(
                "afternoon"
            ) -> {
                "🌤️"
            }

            normalizedSchedule.contains(
                "evening"
            ) -> {
                "🌆"
            }

            normalizedSchedule.contains(
                "night"
            ) -> {
                "🌙"
            }

            normalizedSchedule.contains(
                "weekend"
            ) -> {
                "📅"
            }

            else -> {
                "🕐"
            }
        }
    }

    private fun showAvatarPicker() {
        val dialogView =
            layoutInflater.inflate(
                R.layout.dialog_avatar_picker,
                null
            )

        val rvAvatars =
            dialogView.findViewById<RecyclerView>(
                R.id.rvAvatars
            )

        val btnClose =
            dialogView.findViewById<TextView>(
                R.id.btnCloseAvatarPicker
            )

        lateinit var dialog:
                AlertDialog

        val avatarAdapter =
            AvatarAdapter { selectedSeed ->
                saveSelectedAvatar(
                    avatarSeed =
                    selectedSeed,

                    dialog =
                    dialog
                )
            }

        rvAvatars.layoutManager =
            GridLayoutManager(
                requireContext(),
                3
            )

        rvAvatars.adapter =
            avatarAdapter

        avatarAdapter.submitAvatars(
            seeds =
            createAvatarSeeds(),

            currentSeed =
            currentAvatarSeed
        )

        dialog =
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

        dialog.window
            ?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
    }

    private fun createAvatarSeeds():
            List<String> {

        return (1..60).map { number ->
            "weather-avatar-" +
                    number
                        .toString()
                        .padStart(
                            length = 2,
                            padChar = '0'
                        )
        }
    }

    private fun saveSelectedAvatar(
        avatarSeed: String,
        dialog: AlertDialog
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            onboardingRepository
                .saveSelectedAvatar(
                    avatarSeed
                )
                .onSuccess {

                    if (
                        !isAdded ||
                        view == null
                    ) {
                        return@onSuccess
                    }

                    currentAvatarSeed =
                        avatarSeed

                    loadAvatar(
                        avatarStyle =
                        "toon-head",

                        avatarSeed =
                        avatarSeed
                    )

                    Toast.makeText(
                        requireContext(),
                        "Avatar updated.",
                        Toast.LENGTH_SHORT
                    ).show()

                    dialog.dismiss()
                }
                .onFailure { error ->

                    if (
                        !isAdded ||
                        view == null
                    ) {
                        return@onFailure
                    }

                    Log.e(
                        "ProfileFragment",
                        "Unable to save avatar",
                        error
                    )

                    Toast.makeText(
                        requireContext(),
                        error.message
                            ?: "Unable to update avatar.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun loadAvatar(
        avatarStyle: String,
        avatarSeed: String?
    ) {
        if (avatarSeed.isNullOrBlank()) {
            return
        }

        val encodedStyle =
            URLEncoder.encode(
                avatarStyle,
                "UTF-8"
            )

        val encodedSeed =
            URLEncoder.encode(
                avatarSeed,
                "UTF-8"
            )

        val avatarUrl =
            "https://api.dicebear.com/10.x/" +
                    "$encodedStyle/svg" +
                    "?seed=$encodedSeed"

        val imageLoader =
            ImageLoader.Builder(
                requireContext()
            )
                .components {
                    add(
                        SvgDecoder.Factory()
                    )
                }
                .build()

        val imageRequest =
            ImageRequest.Builder(
                requireContext()
            )
                .data(
                    avatarUrl
                )
                .crossfade(
                    true
                )
                .target(
                    ivProfileAvatar
                )
                .build()

        imageLoader.enqueue(
            imageRequest
        )
    }
}