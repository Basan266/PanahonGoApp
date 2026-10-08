package com.example.weatherapp.data.repository

import com.example.weatherapp.data.remote.supabase.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import io.github.jan.supabase.auth.OtpType


class SupabaseAuthRepository {

    private val supabase =
        SupabaseClientProvider.client

    /*
     * Creates a new account using
     * name, email and password.
     */
    suspend fun signUpWithEmail(
        displayName: String,
        email: String,
        password: String
    ): Result<Unit> {
        return runCatching {
            supabase.auth.signUpWith(
                provider = Email,
                redirectUrl = EMAIL_CONFIRMATION_URL
            ) {
                this.email =
                    email.trim()

                this.password =
                    password

                data =
                    buildJsonObject {
                        put(
                            key = "display_name",
                            value = displayName.trim()
                        )
                    }
            }
        }
    }

    /*
     * Signs in an existing user using
     * email and password.
     */
    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<Unit> {
        return runCatching {
            supabase.auth.signInWith(
                provider = Email
            ) {
                this.email =
                    email.trim()

                this.password =
                    password
            }
        }
    }

    /*
     * Signs out the current Supabase user.
     */
    suspend fun signOut(): Result<Unit> {
        return runCatching {
            supabase.auth.signOut()
        }
    }


    suspend fun resendConfirmationEmail(
        email: String
    ): Result<Unit> {
        return runCatching {
            val cleanEmail =
                email.trim()

            require(
                cleanEmail.isNotEmpty()
            ) {
                "Please enter your email address."
            }

            supabase.auth.resendEmail(
                type = OtpType.Email.SIGNUP,
                email = cleanEmail
            )
        }
    }


    fun isUserSignedIn(): Boolean {
        return supabase
            .auth
            .currentSessionOrNull() != null
    }

    fun getCurrentUserId(): String? {
        return supabase
            .auth
            .currentSessionOrNull()
            ?.user
            ?.id
            ?: supabase
                .auth
                .currentUserOrNull()
                ?.id
    }

    fun getCurrentUserEmail(): String? {
        return supabase
            .auth
            .currentSessionOrNull()
            ?.user
            ?.email
            ?: supabase
                .auth
                .currentUserOrNull()
                ?.email
    }

    companion object {
        private const val EMAIL_CONFIRMATION_URL =
            "https://weatherapp-confirmation-site.vercel.app/"
    }
}