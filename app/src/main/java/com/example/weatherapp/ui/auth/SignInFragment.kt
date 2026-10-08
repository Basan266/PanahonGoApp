package com.example.weatherapp.ui.auth

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.WindowManager
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
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.R
import com.example.weatherapp.data.repository.SupabaseAuthRepository
import com.example.weatherapp.ui.MainActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import com.example.weatherapp.data.remote.supabase.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Facebook
import io.github.jan.supabase.auth.providers.Google



class SignInFragment :
    Fragment(R.layout.fragment_sign_in) {

    private val authRepository =
        SupabaseAuthRepository()

    private val viewModel:
            AuthViewModel by viewModels {
        AuthViewModelFactory(
            authRepository
        )
    }

    private lateinit var etEmail:
            TextInputEditText

    private lateinit var etPassword:
            TextInputEditText

    private lateinit var btnSignIn:
            MaterialButton

    private lateinit var btnGoogle:
            ImageButton

    private lateinit var btnFacebook:
            ImageButton

    private lateinit var tvForgotPassword:
            TextView

    private lateinit var tvOpenSignUp:
            TextView

    private lateinit var progressAuth:
            ProgressBar

    private var resendDialog:
            Dialog? = null

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
        configureSignIn()
        configureNavigation()
        configureOtherButtons()
        observeUiState()
    }


    private fun bindViews(
        view: View
    ) {
        etEmail =
            view.findViewById(
                R.id.etEmail
            )

        etPassword =
            view.findViewById(
                R.id.etPassword
            )

        btnSignIn =
            view.findViewById(
                R.id.btnSignIn
            )

        btnGoogle =
            view.findViewById(
                R.id.btnGoogle
            )

        btnFacebook =
            view.findViewById(
                R.id.btnFacebook
            )

        tvForgotPassword =
            view.findViewById(
                R.id.tvForgotPassword
            )

        tvOpenSignUp =
            view.findViewById(
                R.id.tvOpenSignUp
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


    private fun configureSignIn() {
        btnSignIn.setOnClickListener {
            hideMessage()

            viewModel.signIn(
                email =
                etEmail.text
                    ?.toString()
                    ?.trim()
                    .orEmpty(),

                password =
                etPassword.text
                    ?.toString()
                    .orEmpty()
            )
        }
    }

    private fun configureNavigation() {
        tvOpenSignUp.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    SignUpFragment()
                )
                .commit()
        }
    }

    private fun configureOtherButtons() {
        tvForgotPassword.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Password recovery setup is next.",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        btnFacebook.setOnClickListener {
            signInWithFacebook()
        }
    }

    private fun signInWithGoogle() {
        startSocialSignIn(
            providerName = "Google"
        ) {
            // Alisin ang lumang Supabase session.
            SupabaseClientProvider
                .client
                .auth
                .signOut()

            // Buksan ang Google authentication.
            SupabaseClientProvider
                .client
                .auth
                .signInWith(
                    Google
                ) {
                    scopes.add("email")
                    scopes.add("profile")

                    // Ipakita ang Google account selector.
                    queryParams["prompt"] =
                        "select_account"
                }
        }
    }

    private fun signInWithFacebook() {
        startSocialSignIn("Facebook") {
            // Alisin muna ang lumang Supabase session.
            SupabaseClientProvider.client.auth.signOut()

            SupabaseClientProvider.client.auth.signInWith(
                Facebook
            ) {
                scopes.add("email")

                // Piliting mag-login ulit sa Facebook.
                queryParams["auth_type"] =
                    "reauthorize"
            }
        }
    }

    private fun startSocialSignIn(
        providerName: String,
        signInAction: suspend () -> Unit
    ) {
        displayLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                signInAction()
            }.onFailure { exception ->
                if (!isAdded || view == null) {
                    return@onFailure
                }

                showMessage(
                    message =
                    exception.message
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: "Unable to sign in with $providerName.",
                    isError = true
                )
            }

            if (isAdded && view != null) {
                displayLoading(false)
            }
        }
    }

    private fun observeUiState() {
        viewModel.uiState.observe(
            viewLifecycleOwner
        ) { state ->

            displayLoading(
                state.isLoading
            )

            if (
                state.authenticationSuccessful
            ) {
                viewModel
                    .clearAuthenticationSuccess()

                openAuthenticatedScreen()
                return@observe
            }

            state.errorMessage?.let { errorMessage ->
                val emailNotConfirmed =
                    isEmailNotConfirmedError(
                        errorMessage
                    )

                viewModel.clearMessages()

                if (emailNotConfirmed) {
                    hideMessage()
                    openResendConfirmationDialog()
                } else {
                    showMessage(
                        message =
                        createFriendlyErrorMessage(
                            errorMessage
                        ),
                        isError = true
                    )
                }

                return@observe
            }

            state.message?.let { message ->
                showMessage(
                    message = message,
                    isError = false
                )

                viewModel.clearMessages()
            }
        }
    }

    private fun isEmailNotConfirmedError(
        errorMessage: String
    ): Boolean {
        return errorMessage.contains(
            other = "email not confirmed",
            ignoreCase = true
        ) ||
                errorMessage.contains(
                    other = "email_not_confirmed",
                    ignoreCase = true
                ) ||
                errorMessage.contains(
                    other = "email is not confirmed",
                    ignoreCase = true
                ) ||
                errorMessage.contains(
                    other = "confirm your email",
                    ignoreCase = true
                )
    }

    private fun createFriendlyErrorMessage(
        errorMessage: String
    ): String {
        return when {
            errorMessage.contains(
                "invalid login credentials",
                ignoreCase = true
            ) -> {
                "Incorrect email address or password."
            }

            errorMessage.contains(
                "network",
                ignoreCase = true
            ) -> {
                "Unable to connect. Please check your internet connection."
            }

            errorMessage.contains(
                "rate limit",
                ignoreCase = true
            ) -> {
                "Too many attempts. Please wait before trying again."
            }

            else -> {
                errorMessage
            }
        }
    }

    private fun openResendConfirmationDialog() {
        if (
            resendDialog?.isShowing == true
        ) {
            return
        }

        val email =
            etEmail.text
                ?.toString()
                ?.trim()
                .orEmpty()

        if (email.isEmpty()) {
            showMessage(
                message =
                "Enter your email address first.",
                isError = true
            )

            etEmail.requestFocus()
            return
        }

        if (
            !Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()
        ) {
            showMessage(
                message =
                "Enter a valid email address.",
                isError = true
            )

            etEmail.requestFocus()
            return
        }

        hideMessage()

        val dialog =
            Dialog(requireContext())

        resendDialog =
            dialog

        dialog.setContentView(
            R.layout.dialog_resend_confirmation
        )

        dialog.window?.apply {
            setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )

            addFlags(
                WindowManager.LayoutParams
                    .FLAG_DIM_BEHIND
            )

            attributes =
                attributes.apply {
                    dimAmount = 0.72f
                }
        }

        val tvConfirmationEmail =
            dialog.findViewById<TextView>(
                R.id.tvConfirmationEmail
            )

        val tvResendResult =
            dialog.findViewById<TextView>(
                R.id.tvResendResult
            )

        val progressResend =
            dialog.findViewById<ProgressBar>(
                R.id.progressResend
            )

        val btnDialogResend =
            dialog.findViewById<TextView>(
                R.id.btnDialogResend
            )

        val tvCloseDialog =
            dialog.findViewById<TextView>(
                R.id.tvCloseDialog
            )

        tvConfirmationEmail.text =
            email

        tvCloseDialog.setOnClickListener {
            dialog.dismiss()
        }

        btnDialogResend.setOnClickListener {
            resendConfirmationEmail(
                email = email,
                button = btnDialogResend,
                progress = progressResend,
                resultText = tvResendResult
            )
        }

        dialog.setOnDismissListener {
            resendDialog = null
        }

        dialog.show()

        val dialogWidth =
            (
                    resources
                        .displayMetrics
                        .widthPixels * 0.88
                    ).toInt()

        dialog.window?.setLayout(
            dialogWidth,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun resendConfirmationEmail(
        email: String,
        button: TextView,
        progress: ProgressBar,
        resultText: TextView
    ) {
        button.isEnabled = false
        button.text = "Sending..."

        progress.visibility =
            View.VISIBLE

        resultText.visibility =
            View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            val result =
                authRepository
                    .resendConfirmationEmail(
                        email = email
                    )

            progress.visibility =
                View.GONE

            resultText.visibility =
                View.VISIBLE

            result.onSuccess {
                resultText.text =
                    "A new confirmation email was sent. " +
                            "Please check your inbox and spam folder."

                resultText.setTextColor(
                    Color.parseColor(
                        "#64D98B"
                    )
                )

                button.text =
                    "Email sent"

                button.isEnabled =
                    false
            }.onFailure { exception ->
                val rawMessage =
                    exception.message.orEmpty()

                resultText.text =
                    when {
                        rawMessage.contains(
                            "over_email_send_rate_limit",
                            ignoreCase = true
                        ) -> {
                            "Email limit reached. Please wait before trying again."
                        }

                        rawMessage.contains(
                            "rate limit",
                            ignoreCase = true
                        ) -> {
                            "Please wait before requesting another email."
                        }

                        rawMessage.contains(
                            "already confirmed",
                            ignoreCase = true
                        ) -> {
                            "This email is already confirmed. You can sign in now."
                        }

                        else -> {
                            "Unable to resend the confirmation email. Please try again."
                        }
                    }

                resultText.setTextColor(
                    Color.parseColor(
                        "#FF6B6B"
                    )
                )

                button.text =
                    "Resend email"

                button.isEnabled =
                    true
            }
        }
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

        btnSignIn.isEnabled =
            !loading

        btnGoogle.isEnabled =
            !loading

        btnFacebook.isEnabled =
            !loading

        tvForgotPassword.isEnabled =
            !loading

        tvOpenSignUp.isEnabled =
            !loading

        etEmail.isEnabled =
            !loading

        etPassword.isEnabled =
            !loading
    }

    private fun showMessage(
        message: String,
        isError: Boolean
    ) {
        Toast.makeText(
            requireContext(),
            message,
            if (isError) {
                Toast.LENGTH_LONG
            } else {
                Toast.LENGTH_SHORT
            }
        ).show()
    }

    private fun hideMessage() {
        // Toast messages close automatically.
    }

    private fun openAuthenticatedScreen() {
        val mainActivity =
            requireActivity()
                    as? MainActivity

        mainActivity
            ?.continueAfterAuthentication()
    }

    override fun onDestroyView() {
        resendDialog?.dismiss()
        resendDialog = null

        super.onDestroyView()
    }
}
