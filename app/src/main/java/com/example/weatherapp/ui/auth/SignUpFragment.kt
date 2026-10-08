package com.example.weatherapp.ui.auth

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.weatherapp.R
import com.example.weatherapp.data.repository.SupabaseAuthRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SignUpFragment :
    Fragment(R.layout.fragment_sign_up) {

    private val viewModel:
            AuthViewModel by viewModels {
        AuthViewModelFactory(
            SupabaseAuthRepository()
        )
    }

    private lateinit var etName:
            TextInputEditText

    private lateinit var etEmail:
            TextInputEditText

    private lateinit var etPassword:
            TextInputEditText

    private lateinit var etConfirmPassword:
            TextInputEditText

    private lateinit var cbTerms:
            CheckBox

    private lateinit var btnCreateAccount:
            MaterialButton

    private lateinit var btnGoogle:
            ImageButton

    private lateinit var btnFacebook:
            ImageButton

    private lateinit var tvOpenSignIn:
            TextView

    private lateinit var tvAuthMessage:
            TextView

    private lateinit var progressAuth:
            ProgressBar

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
        configureCreateAccount()
        configureNavigation()
        configureSocialButtons()
        observeUiState()
    }

    private fun bindViews(
        view: View
    ) {
        etName =
            view.findViewById(
                R.id.etName
            )

        etEmail =
            view.findViewById(
                R.id.etEmail
            )

        etPassword =
            view.findViewById(
                R.id.etPassword
            )

        etConfirmPassword =
            view.findViewById(
                R.id.etConfirmPassword
            )

        cbTerms =
            view.findViewById(
                R.id.cbTerms
            )

        btnCreateAccount =
            view.findViewById(
                R.id.btnCreateAccount
            )

        btnGoogle =
            view.findViewById(
                R.id.btnGoogle
            )

        btnFacebook =
            view.findViewById(
                R.id.btnFacebook
            )

        tvOpenSignIn =
            view.findViewById(
                R.id.tvOpenSignIn
            )

        tvAuthMessage =
            view.findViewById(
                R.id.tvAuthMessage
            )

        progressAuth =
            view.findViewById(
                R.id.progressAuth
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


    private fun configureCreateAccount() {
        btnCreateAccount.setOnClickListener {
            hideMessage()

            viewModel.signUp(
                displayName =
                etName.text
                    ?.toString()
                    .orEmpty(),

                email =
                etEmail.text
                    ?.toString()
                    .orEmpty(),

                password =
                etPassword.text
                    ?.toString()
                    .orEmpty(),

                confirmPassword =
                etConfirmPassword.text
                    ?.toString()
                    .orEmpty(),

                acceptedTerms =
                cbTerms.isChecked
            )
        }
    }

    private fun configureNavigation() {
        tvOpenSignIn.setOnClickListener {
            openSignInScreen()
        }
    }

    private fun configureSocialButtons() {
        btnGoogle.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Google Sign-In setup is next.",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnFacebook.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Facebook Sign-In setup is next.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private fun observeUiState() {
        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->

            displayLoading(
                state.isLoading
            )

            state.errorMessage?.let { errorMessage ->
                showMessage(
                    message = errorMessage,
                    isError = true
                )

                viewModel.clearMessages()
                return@observe
            }

            state.message?.let { message ->
                val accountWasCreated =
                    message.contains(
                        other = "Account created",
                        ignoreCase = true
                    )

                viewModel.clearMessages()

                if (accountWasCreated) {
                    clearSignUpForm()

                    Toast.makeText(
                        requireContext(),
                        "Account created. Confirm your email, then sign in.",
                        Toast.LENGTH_LONG
                    ).show()

                    openSignInScreen()
                    return@observe
                }

                showMessage(
                    message = message,
                    isError = false
                )

                return@observe
            }

            if (state.authenticationSuccessful) {
                viewModel.clearAuthenticationSuccess()

                clearSignUpForm()
                openSignInScreen()
            }
        }
    }


    private fun clearSignUpForm() {
        etName.text?.clear()
        etEmail.text?.clear()
        etPassword.text?.clear()
        etConfirmPassword.text?.clear()

        cbTerms.isChecked = false
    }



    private fun displayLoading(
        loading: Boolean
    ) {
        progressAuth.visibility =
            if (loading) {
                View.VISIBLE
            } else {
                View.GONE
            }

        btnCreateAccount.isEnabled =
            !loading

        btnGoogle.isEnabled =
            !loading

        btnFacebook.isEnabled =
            !loading

        etName.isEnabled =
            !loading

        etEmail.isEnabled =
            !loading

        etPassword.isEnabled =
            !loading

        etConfirmPassword.isEnabled =
            !loading

        cbTerms.isEnabled =
            !loading
    }

    private fun showMessage(
        message: String,
        isError: Boolean
    ) {
        tvAuthMessage.visibility =
            View.VISIBLE

        tvAuthMessage.text =
            message

        tvAuthMessage.setTextColor(
            if (isError) {
                Color.parseColor("#FF6B6B")
            } else {
                Color.parseColor("#64D98B")
            }
        )
    }

    private fun hideMessage() {
        tvAuthMessage.visibility =
            View.GONE

        tvAuthMessage.text =
            ""
    }

    private fun openSignInScreen() {
        parentFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                SignInFragment()
            )
            .commit()
    }
}