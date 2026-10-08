package com.example.weatherapp.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.R
import com.example.weatherapp.data.local.preferences.UserPreferences
import com.example.weatherapp.ui.activities.ActivitiesFragment
import com.example.weatherapp.ui.home.HomeFragment
import com.example.weatherapp.ui.map.MapDashboardFragment
import com.example.weatherapp.ui.navigation.GoogleNavigationFragment
import com.example.weatherapp.ui.onboarding.ActivitySelectionFragment
import com.example.weatherapp.ui.onboarding.PersonalInformationFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import com.example.weatherapp.data.repository.SupabaseAuthRepository
import com.example.weatherapp.data.repository.SupabaseOnboardingRepository
import com.example.weatherapp.ui.auth.SignInFragment
import com.example.weatherapp.ui.onboarding.OutdoorScheduleFragment
import com.example.weatherapp.ui.onboarding.WeatherConcernsFragment
import kotlinx.coroutines.flow.drop
import android.content.Intent
import com.example.weatherapp.data.remote.supabase.SupabaseClientProvider
import io.github.jan.supabase.auth.handleDeeplinks
import com.example.weatherapp.ui.auth.SignUpFragment
import com.example.weatherapp.ui.forecast.ForecastFragment
import com.example.weatherapp.ui.profile.ProfileFragment
import android.net.Uri
import io.github.jan.supabase.auth.auth


class MainActivity : AppCompatActivity() {

    private lateinit var userPreferences:
            UserPreferences

    private lateinit var bottomNavigationHost:
            View

    private lateinit var navHomeButton:
            View

    private lateinit var navActivitiesButton:
            View

    private lateinit var navMapButton:
            View

    private lateinit var navForecastButton:
            View

    private lateinit var navProfileButton:
            View

    private lateinit var ivNavHome:
            ImageView

    private lateinit var ivNavActivities:
            ImageView

    private lateinit var ivNavMap:
            ImageView

    private lateinit var ivNavForecast:
            ImageView

    private lateinit var ivNavProfile:
            ImageView

    private lateinit var indicatorHome:
            View

    private lateinit var indicatorActivities:
            View

    private lateinit var indicatorMap:
            View

    private lateinit var indicatorForecast:
            View

    private lateinit var indicatorProfile:
            View

    private var onboardingCompleted = false


    private val authRepository =
        SupabaseAuthRepository()

    private val onboardingRepository =
        SupabaseOnboardingRepository()


    private val selectedNavigationColor =
        Color.rgb(
            240,
            240,
            240
        )

    private val unselectedNavigationColor =
        Color.rgb(
            119,
            119,
            132
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        userPreferences =
            UserPreferences(this)

        configureSystemBars()
        initializeNavigationViews()

        hideBottomNavigation()

        configureBottomNavigation()
        observeFragmentChanges()

        val authenticationCallbackHandled =
            handleAuthenticationCallback(
                callbackIntent = intent
            )

        if (!authenticationCallbackHandled) {
            loadApplicationState(
                shouldOpenStartingScreen =
                savedInstanceState == null
            )
        }
    }

    private fun initializeNavigationViews() {
        bottomNavigationHost =
            findViewById(
                R.id.bottomNavigationHost
            )

        navHomeButton =
            findViewById(
                R.id.navHomeButton
            )

        navActivitiesButton =
            findViewById(
                R.id.navActivitiesButton
            )

        navMapButton =
            findViewById(
                R.id.navMapButton
            )

        navForecastButton =
            findViewById(
                R.id.navForecastButton
            )

        navProfileButton =
            findViewById(
                R.id.navProfileButton
            )

        ivNavHome =
            findViewById(
                R.id.ivNavHome
            )

        ivNavActivities =
            findViewById(
                R.id.ivNavActivities
            )

        ivNavMap =
            findViewById(
                R.id.ivNavMap
            )

        ivNavForecast =
            findViewById(
                R.id.ivNavForecast
            )

        ivNavProfile =
            findViewById(
                R.id.ivNavProfile
            )

        indicatorHome =
            findViewById(
                R.id.indicatorHome
            )

        indicatorActivities =
            findViewById(
                R.id.indicatorActivities
            )

        indicatorMap =
            findViewById(
                R.id.indicatorMap
            )

        indicatorForecast =
            findViewById(
                R.id.indicatorForecast
            )

        indicatorProfile =
            findViewById(
                R.id.indicatorProfile
            )
    }

    private fun configureSystemBars() {
        val rootView =
            findViewById<View>(
                R.id.mainRoot
            )

        val navigationHost =
            findViewById<View>(
                R.id.bottomNavigationHost
            )

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        window.statusBarColor =
            Color.rgb(
                32,
                32,
                32
            )

        window.navigationBarColor =
            Color.rgb(
                32,
                32,
                32
            )

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).apply {
            show(
                WindowInsetsCompat.Type.systemBars()
            )

            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val originalBottomMargin =
            (
                    navigationHost.layoutParams
                            as ViewGroup.MarginLayoutParams
                    ).bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(
            rootView
        ) { _, windowInsets ->

            val navigationBarInsets =
                windowInsets.getInsets(
                    WindowInsetsCompat.Type.navigationBars()
                )

            val layoutParams =
                navigationHost.layoutParams
                        as ViewGroup.MarginLayoutParams

            layoutParams.bottomMargin =
                originalBottomMargin +
                        navigationBarInsets.bottom

            navigationHost.layoutParams =
                layoutParams

            windowInsets
        }

        ViewCompat.requestApplyInsets(
            rootView
        )
    }

    private fun configureBottomNavigation() {
        navHomeButton.setOnClickListener {
            selectNavigation(
                R.id.navHomeButton
            )

            openMainFragment(
                HomeFragment()
            )
        }

        navActivitiesButton.setOnClickListener {
            selectNavigation(
                R.id.navActivitiesButton
            )

            openMainFragment(
                ActivitiesFragment()
            )
        }

        navMapButton.setOnClickListener {
            selectNavigation(
                R.id.navMapButton
            )

            openMainFragment(
                MapDashboardFragment()
            )
        }

        navForecastButton.setOnClickListener {
            selectNavigation(
                R.id.navForecastButton
            )

            openMainFragment(
                ForecastFragment()
            )
        }

        navProfileButton.setOnClickListener {
            selectNavigation(
                R.id.navProfileButton
            )

            openMainFragment(
                ProfileFragment()
            )
        }
    }

    private fun selectNavigation(
        selectedButtonId: Int
    ) {
        updateNavigationItem(
            imageView = ivNavHome,
            indicator = indicatorHome,
            isSelected =
            selectedButtonId ==
                    R.id.navHomeButton
        )

        updateNavigationItem(
            imageView = ivNavActivities,
            indicator = indicatorActivities,
            isSelected =
            selectedButtonId ==
                    R.id.navActivitiesButton
        )

        updateNavigationItem(
            imageView = ivNavMap,
            indicator = indicatorMap,
            isSelected =
            selectedButtonId ==
                    R.id.navMapButton
        )

        updateNavigationItem(
            imageView = ivNavForecast,
            indicator = indicatorForecast,
            isSelected =
            selectedButtonId ==
                    R.id.navForecastButton
        )

        updateNavigationItem(
            imageView = ivNavProfile,
            indicator = indicatorProfile,
            isSelected =
            selectedButtonId ==
                    R.id.navProfileButton
        )
    }

    private fun updateNavigationItem(
        imageView: ImageView,
        indicator: View,
        isSelected: Boolean
    ) {
        val color =
            if (isSelected) {
                selectedNavigationColor
            } else {
                unselectedNavigationColor
            }

        imageView.setColorFilter(
            color
        )

        indicator.visibility =
            if (isSelected) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
    }



    private fun loadApplicationState(
        shouldOpenStartingScreen: Boolean
    ) {
        lifecycleScope.launch {
            val localOnboardingCompleted =
                userPreferences
                    .onboardingCompleted
                    .first()

            if (shouldOpenStartingScreen) {
                openStartingScreen(
                    localFallback =
                    localOnboardingCompleted
                )
            } else {
                onboardingCompleted =
                    localOnboardingCompleted &&
                            authRepository
                                .isUserSignedIn()

                updateNavigationForCurrentFragment()
            }

            /*
             * Ignore the first value because it was
             * already read above. Observe only future
             * onboarding changes.
             */
            userPreferences
                .onboardingCompleted
                .drop(1)
                .collect { completed ->

                    val isSignedIn =
                        authRepository
                            .isUserSignedIn()

                    val wasCompleted =
                        onboardingCompleted

                    onboardingCompleted =
                        completed &&
                                isSignedIn

                    if (!isSignedIn) {
                        hideBottomNavigation()
                        return@collect
                    }

                    if (
                        !wasCompleted &&
                        onboardingCompleted
                    ) {
                        showBottomNavigation()

                        selectNavigation(
                            R.id.navMapButton
                        )

                        openMainFragment(
                            MapDashboardFragment()
                        )
                    } else {
                        updateNavigationForCurrentFragment()
                    }
                }
        }
    }

    private suspend fun openStartingScreen(
        localFallback: Boolean,
        authenticationConfirmed: Boolean = false
    ) {
        val isSignedIn =
            authenticationConfirmed ||
                    authRepository.isUserSignedIn()

        if (!isSignedIn) {
            onboardingCompleted = false

            hideBottomNavigation()

            openMainFragment(
                SignInFragment()
            )

            return
        }

        val profileResult =
            onboardingRepository
                .getCurrentUserProfile()

        profileResult.onSuccess { profile ->
            when {
                profile == null -> {
                    onboardingCompleted = false

                    hideBottomNavigation()

                    openMainFragment(
                        PersonalInformationFragment()
                    )
                }

                profile.onboardingCompleted -> {
                    onboardingCompleted = true

                    showBottomNavigation()

                    selectNavigation(
                        R.id.navMapButton
                    )

                    openMainFragment(
                        MapDashboardFragment()
                    )
                }

                profile.onboardingStep <= 0 -> {
                    onboardingCompleted = false

                    hideBottomNavigation()

                    openMainFragment(
                        PersonalInformationFragment()
                    )
                }

                profile.onboardingStep == 1 -> {
                    onboardingCompleted = false

                    hideBottomNavigation()

                    openMainFragment(
                        ActivitySelectionFragment()
                    )
                }

                profile.onboardingStep == 2 -> {
                    onboardingCompleted = false

                    hideBottomNavigation()

                    openMainFragment(
                        WeatherConcernsFragment()
                    )
                }

                else -> {
                    onboardingCompleted = false

                    hideBottomNavigation()

                    openMainFragment(
                        OutdoorScheduleFragment()
                    )
                }
            }
        }.onFailure { exception ->
            /*
             * Kapag walang internet, gamitin muna
             * ang existing local onboarding status.
             */
            if (localFallback) {
                onboardingCompleted = true

                showBottomNavigation()

                selectNavigation(
                    R.id.navMapButton
                )

                openMainFragment(
                    MapDashboardFragment()
                )
            } else {
                onboardingCompleted = false

                hideBottomNavigation()

                openMainFragment(
                    PersonalInformationFragment()
                )
            }

            Toast.makeText(
                this,
                exception.message
                    ?: "Unable to load your online profile.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openMainFragment(
        fragment: Fragment
    ) {
        updateNavigationVisibility(
            fragment
        )

        val currentFragment =
            supportFragmentManager
                .findFragmentById(
                    R.id.fragmentContainer
                )

        if (
            currentFragment?.javaClass ==
            fragment.javaClass
        ) {
            return
        }

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                fragment
            )
            .commit()
    }


    private fun observeFragmentChanges() {
        supportFragmentManager
            .registerFragmentLifecycleCallbacks(
                object :
                    FragmentManager.FragmentLifecycleCallbacks() {

                    override fun onFragmentResumed(
                        fragmentManager: FragmentManager,
                        fragment: Fragment
                    ) {
                        super.onFragmentResumed(
                            fragmentManager,
                            fragment
                        )

                        updateNavigationVisibility(
                            fragment
                        )
                    }
                },
                false
            )
    }

    private fun updateNavigationForCurrentFragment() {
        val currentFragment =
            supportFragmentManager
                .findFragmentById(
                    R.id.fragmentContainer
                )

        if (currentFragment != null) {
            updateNavigationVisibility(
                currentFragment
            )
        }
    }

    private fun updateNavigationVisibility(
        fragment: Fragment
    ) {
        val shouldHideNavigation =
            fragment is SignInFragment ||
                    fragment is SignUpFragment ||
                    fragment is PersonalInformationFragment ||
                    fragment is ActivitySelectionFragment ||
                    fragment is WeatherConcernsFragment ||
                    fragment is OutdoorScheduleFragment ||
                    fragment is GoogleNavigationFragment

        if (
            shouldHideNavigation ||
            !onboardingCompleted
        ) {
            hideBottomNavigation()
            return
        }

        showBottomNavigation()

        when (fragment) {
            is HomeFragment -> {
                selectNavigation(
                    R.id.navHomeButton
                )
            }

            is ActivitiesFragment -> {
                selectNavigation(
                    R.id.navActivitiesButton
                )
            }

            is MapDashboardFragment -> {
                selectNavigation(
                    R.id.navMapButton
                )
            }

            is ForecastFragment -> {
                selectNavigation(
                    R.id.navForecastButton
                )
            }

            is ProfileFragment -> {
                selectNavigation(
                    R.id.navProfileButton
                )
            }
        }
    }

    fun showBottomNavigation() {
        if (!::bottomNavigationHost.isInitialized) {
            return
        }

        val currentFragment =
            supportFragmentManager
                .findFragmentById(
                    R.id.fragmentContainer
                )

        val navigationMustStayHidden =
            currentFragment == null ||
                    currentFragment is SignInFragment ||
                    currentFragment is SignUpFragment ||
                    currentFragment is PersonalInformationFragment ||
                    currentFragment is ActivitySelectionFragment ||
                    currentFragment is WeatherConcernsFragment ||
                    currentFragment is OutdoorScheduleFragment ||
                    currentFragment is GoogleNavigationFragment ||
                    !onboardingCompleted

        bottomNavigationHost.visibility =
            if (navigationMustStayHidden) {
                View.GONE
            } else {
                View.VISIBLE
            }
    }

    fun hideBottomNavigation() {
        if (::bottomNavigationHost.isInitialized) {
            bottomNavigationHost.visibility =
                View.GONE
        }
    }

    fun completeOnboarding() {
        onboardingCompleted = true

        selectNavigation(
            R.id.navMapButton
        )

        openMainFragment(
            HomeFragment()
        )
    }

    fun continueAfterAuthentication(
        authenticationConfirmed: Boolean = false
    ) {
        lifecycleScope.launch {
            /*
             * Kapag bagong OAuth callback, huwag gamitin ang
             * onboarding status ng dating naka-login na user.
             */
            val localOnboardingCompleted =
                if (authenticationConfirmed) {
                    false
                } else {
                    userPreferences
                        .onboardingCompleted
                        .first()
                }

            openStartingScreen(
                localFallback =
                localOnboardingCompleted,

                authenticationConfirmed =
                authenticationConfirmed
            )
        }
    }




    private fun handleAuthenticationCallback(
        callbackIntent: Intent?
    ): Boolean {
        val callbackUri =
            callbackIntent?.data
                ?: return false

        if (!isAuthenticationCallback(callbackUri)) {
            return false
        }

        val callbackParameters =
            buildString {
                append(
                    callbackUri.fragment.orEmpty()
                )

                append("&")

                append(
                    callbackUri.query.orEmpty()
                )
            }

        val errorDescription =
            callbackUri.getQueryParameter(
                "error_description"
            ) ?: findCallbackParameter(
                parameters = callbackParameters,
                parameterName = "error_description"
            )

        if (!errorDescription.isNullOrBlank()) {
            Toast.makeText(
                this,
                Uri.decode(errorDescription),
                Toast.LENGTH_LONG
            ).show()

            loadApplicationState(
                shouldOpenStartingScreen = true
            )

            return true
        }

        val containsAuthenticationData =
            callbackParameters.contains(
                other = "access_token=",
                ignoreCase = true
            ) ||
                    callbackParameters.contains(
                        other = "code=",
                        ignoreCase = true
                    )

        if (!containsAuthenticationData) {
            Toast.makeText(
                this,
                "Social sign-in did not return a valid session. Please try again.",
                Toast.LENGTH_LONG
            ).show()

            loadApplicationState(
                shouldOpenStartingScreen = true
            )

            return true
        }

        try {
            SupabaseClientProvider
                .client
                .handleDeeplinks(
                    intent = callbackIntent
                ) { facebookSession ->

                    lifecycleScope.launch {
                        try {
                            /*
                             * Explicitly import at i-save ang
                             * session na galing sa Facebook.
                             */
                            SupabaseClientProvider
                                .client
                                .auth
                                .importSession(
                                    facebookSession
                                )

                            val importedSession =
                                SupabaseClientProvider
                                    .client
                                    .auth
                                    .currentSessionOrNull()

                            if (importedSession == null) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Unable to save the Facebook session.",
                                    Toast.LENGTH_LONG
                                ).show()

                                loadApplicationState(
                                    shouldOpenStartingScreen = true
                                )

                                return@launch
                            }

                            /*
                             * Alisin ang processed callback para
                             * hindi ito maproseso ulit.
                             */
                            intent.data = null

                            /*
                             * Confirmed na ang authentication at
                             * available na ang Supabase session.
                             */
                            continueAfterAuthentication(
                                authenticationConfirmed = true
                            )
                        } catch (exception: Exception) {
                            Toast.makeText(
                                this@MainActivity,
                                exception.message
                                    ?.takeIf {
                                        it.isNotBlank()
                                    }
                                    ?: "Unable to import the Facebook session.",
                                Toast.LENGTH_LONG
                            ).show()

                            loadApplicationState(
                                shouldOpenStartingScreen = true
                            )
                        }
                    }
                }
        } catch (exception: Exception) {
            Toast.makeText(
                this,
                exception.message
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: "Unable to complete Facebook sign-in.",
                Toast.LENGTH_LONG
            ).show()

            loadApplicationState(
                shouldOpenStartingScreen = true
            )
        }

        return true
    }




    private fun isAuthenticationCallback(
        uri: Uri
    ): Boolean {
        return uri.scheme.equals(
            "weatherapp",
            ignoreCase = true
        ) &&
                uri.host.equals(
                    "auth-callback",
                    ignoreCase = true
                )
    }

    private fun findCallbackParameter(
        parameters: String,
        parameterName: String
    ): String? {
        return parameters
            .split("&")
            .firstOrNull {
                it.startsWith(
                    "$parameterName=",
                    ignoreCase = true
                )
            }
            ?.substringAfter("=")
    }


    override fun onNewIntent(
        intent: Intent
    ) {
        super.onNewIntent(intent)

        setIntent(intent)

        val authenticationCallbackHandled =
            handleAuthenticationCallback(
                callbackIntent = intent
            )

        if (!authenticationCallbackHandled) {
            continueAfterAuthentication()
        }
    }
}