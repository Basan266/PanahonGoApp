package com.example.weatherapp.ui.auth

data class AuthUiState(
    val isLoading: Boolean = false,

    val authenticationSuccessful: Boolean = false,

    val requiresEmailVerification: Boolean = false,

    val message: String? = null,

    val errorMessage: String? = null
)