package com.example.weatherapp.ui.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.repository.SupabaseAuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository:
    SupabaseAuthRepository
) : ViewModel() {

    private val _uiState =
        MutableLiveData(
            AuthUiState()
        )

    val uiState: LiveData<AuthUiState> =
        _uiState

    fun signUp(
        displayName: String,
        email: String,
        password: String,
        confirmPassword: String,
        acceptedTerms: Boolean
    ) {
        val validationError =
            validateSignUp(
                displayName = displayName,
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                acceptedTerms = acceptedTerms
            )

        if (validationError != null) {
            showError(validationError)
            return
        }

        _uiState.value =
            AuthUiState(
                isLoading = true
            )

        viewModelScope.launch {
            val result =
                authRepository
                    .signUpWithEmail(
                        displayName =
                        displayName.trim(),

                        email =
                        email.trim(),

                        password =
                        password
                    )

            result.onSuccess {
                val hasActiveSession =
                    authRepository
                        .isUserSignedIn()

                if (hasActiveSession) {
                    _uiState.value =
                        AuthUiState(
                            authenticationSuccessful =
                            true,

                            message =
                            "Account created successfully."
                        )
                } else {
                    _uiState.value =
                        AuthUiState(
                            requiresEmailVerification =
                            true,

                            message =
                            "Account created. Check your email to verify your account."
                        )
                }
            }.onFailure { exception ->
                _uiState.value =
                    AuthUiState(
                        isLoading = false,

                        errorMessage =
                        getSignUpErrorMessage(
                            exception
                        )
                    )
            }
        }
    }

    fun signIn(
        email: String,
        password: String
    ) {
        val validationError =
            validateSignIn(
                email = email,
                password = password
            )

        if (validationError != null) {
            showError(validationError)
            return
        }

        _uiState.value =
            AuthUiState(
                isLoading = true
            )

        viewModelScope.launch {
            val result =
                authRepository
                    .signInWithEmail(
                        email =
                        email.trim(),

                        password =
                        password
                    )

            result.onSuccess {
                _uiState.value =
                    AuthUiState(
                        isLoading = false,

                        authenticationSuccessful =
                        true,

                        message =
                        "Signed in successfully."
                    )
            }.onFailure { exception ->
                _uiState.value =
                    AuthUiState(
                        isLoading = false,

                        errorMessage =
                        getSignInErrorMessage(
                            exception
                        )
                    )
            }
        }
    }

    fun hasActiveSession(): Boolean {
        return authRepository
            .isUserSignedIn()
    }

    fun clearMessages() {
        val currentState =
            _uiState.value
                ?: AuthUiState()

        _uiState.value =
            currentState.copy(
                message = null,
                errorMessage = null
            )
    }

    fun clearAuthenticationSuccess() {
        val currentState =
            _uiState.value
                ?: AuthUiState()

        _uiState.value =
            currentState.copy(
                authenticationSuccessful =
                false
            )
    }

    private fun validateSignUp(
        displayName: String,
        email: String,
        password: String,
        confirmPassword: String,
        acceptedTerms: Boolean
    ): String? {
        if (displayName.trim().isEmpty()) {
            return "Enter your name."
        }

        if (!isValidEmail(email)) {
            return "Enter a valid email address."
        }

        if (
            password.length <
            MINIMUM_PASSWORD_LENGTH
        ) {
            return "Password must contain at least " +
                    "$MINIMUM_PASSWORD_LENGTH characters."
        }

        if (password != confirmPassword) {
            return "Passwords do not match."
        }

        if (!acceptedTerms) {
            return "You must accept the Terms and Conditions."
        }

        return null
    }

    private fun validateSignIn(
        email: String,
        password: String
    ): String? {
        if (!isValidEmail(email)) {
            return "Enter a valid email address."
        }

        if (password.isEmpty()) {
            return "Enter your password."
        }

        return null
    }

    private fun isValidEmail(
        email: String
    ): Boolean {
        return Patterns.EMAIL_ADDRESS
            .matcher(
                email.trim()
            )
            .matches()
    }

    private fun showError(
        message: String
    ) {
        _uiState.value =
            AuthUiState(
                isLoading = false,
                errorMessage = message
            )
    }

    private fun getSignUpErrorMessage(
        exception: Throwable
    ): String {
        val rawMessage =
            exception.message.orEmpty()

        return when {
            rawMessage.contains(
                "over_email_send_rate_limit",
                ignoreCase = true
            ) ||
                    rawMessage.contains(
                        "email rate limit",
                        ignoreCase = true
                    ) -> {
                "Too many confirmation emails were requested. " +
                        "Please wait up to one hour before trying again."
            }

            rawMessage.contains(
                "already registered",
                ignoreCase = true
            ) ||
                    rawMessage.contains(
                        "user_already_exists",
                        ignoreCase = true
                    ) -> {
                "This email address is already registered. " +
                        "Please sign in instead."
            }

            rawMessage.contains(
                "network",
                ignoreCase = true
            ) ||
                    rawMessage.contains(
                        "unable to resolve host",
                        ignoreCase = true
                    ) -> {
                "Unable to connect. Please check your internet connection."
            }

            else -> {
                "Unable to create your account. Please try again."
            }
        }
    }

    private fun getSignInErrorMessage(
        exception: Throwable
    ): String {
        val rawMessage =
            exception.message.orEmpty()

        return when {
            rawMessage.contains(
                "email not confirmed",
                ignoreCase = true
            ) ||
                    rawMessage.contains(
                        "email_not_confirmed",
                        ignoreCase = true
                    ) ||
                    rawMessage.contains(
                        "email is not confirmed",
                        ignoreCase = true
                    ) -> {
                "email_not_confirmed"
            }

            rawMessage.contains(
                "invalid login credentials",
                ignoreCase = true
            ) ||
                    rawMessage.contains(
                        "invalid_credentials",
                        ignoreCase = true
                    ) -> {
                "Incorrect email address or password."
            }

            rawMessage.contains(
                "over_email_send_rate_limit",
                ignoreCase = true
            ) ||
                    rawMessage.contains(
                        "rate limit",
                        ignoreCase = true
                    ) -> {
                "Too many attempts. Please wait before trying again."
            }

            rawMessage.contains(
                "network",
                ignoreCase = true
            ) ||
                    rawMessage.contains(
                        "unable to resolve host",
                        ignoreCase = true
                    ) -> {
                "Unable to connect. Please check your internet connection."
            }

            else -> {
                "Unable to sign in. Please check your email and password."
            }
        }
    }

    companion object {
        private const val MINIMUM_PASSWORD_LENGTH =
            8
    }
}