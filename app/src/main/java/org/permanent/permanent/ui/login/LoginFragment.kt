package org.permanent.permanent.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentLoginBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.activities.SignUpActivity
import org.permanent.permanent.ui.activities.MainActivity
import org.permanent.permanent.viewmodels.LoginFragmentViewModel
import java.util.concurrent.Executor

class LoginFragment  : PermanentBaseFragment() {
    private lateinit var viewModel: LoginFragmentViewModel
    private lateinit var binding: FragmentLoginBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(LoginFragmentViewModel::class.java)
        binding.viewModel = viewModel

        executor = ContextCompat.getMainExecutor(context)
        biometricPrompt = BiometricPrompt(this, executor, biometricAuthCallback)

        return binding.root
    }

    private val biometricAuthCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            viewModel.errorMessage.value = getString(
                R.string.login_screen_biometric_authentication_error_message) + errString
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            navigateLogIn()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            viewModel.errorMessage.value =
                getString(R.string.login_biometric_authentication_failed_message)
        }
    }

    private val onErrorStringId = Observer<Int> { errorId ->
        val errorMessage = this.resources.getString(errorId)
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

    private val onErrorMessage = Observer<String> { errorMessage ->
        when (errorMessage) {
            Constants.ERROR_MFA_TOKEN -> {
                this.findNavController().navigate(
                    R.id.action_loginFragment_to_codeVerificationFragment)
            }
            Constants.ERROR_UNKNOWN_SIGNIN -> Toast.makeText(
                context,
                R.string.login_bad_credentials,
                Toast.LENGTH_LONG
            ).show()
            Constants.ERROR_SERVER_ERROR -> Toast.makeText(
                context,
                R.string.server_error,
                Toast.LENGTH_LONG
            ).show()
            Constants.ERROR_NO_API_KEY -> Toast.makeText(
                context,
                R.string.no_api_key,
                Toast.LENGTH_LONG
            ).show()
            else -> Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    private val onLoggedIn = Observer<Void> {
        navigateLogIn()
    }

    private val onBiometricAuthSuccess = Observer<Void> {
        val biometricBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.login_biometric_title))
            .setSubtitle(getString(R.string.login_biometric_subtitle))
            .setNegativeButtonText(getString(R.string.login_biometric_negative_button))
            .build()
        biometricPrompt.authenticate(biometricBuilder)
    }

    private val onSignUp = Observer<Void> {
        navigateSignUp()
    }

    private val onPasswordReset = Observer<Void> {
        Toast.makeText(
            context,
            getString(R.string.login_screen_password_reset_message),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun navigateLogIn() {
        startActivity(Intent(context, MainActivity::class.java))
        activity?.finish()
    }

    private fun navigateSignUp() {
        startActivity(Intent(context, SignUpActivity::class.java))
    }

    override fun connectViewModelEvents() {
        viewModel.getErrorStringId().observe(this, onErrorStringId)
        viewModel.getOnLoggedIn().observe(this, onLoggedIn)
        viewModel.getOnBiometricAuthSuccess().observe(this, onBiometricAuthSuccess)
        viewModel.getOnSignUp().observe(this, onSignUp)
        viewModel.getOnPasswordReset().observe(this, onPasswordReset)
        viewModel.getErrorMessage().observe(this, onErrorMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getErrorStringId().removeObserver(onErrorStringId)
        viewModel.getOnLoggedIn().removeObserver(onLoggedIn)
        viewModel.getOnBiometricAuthSuccess().removeObserver(onBiometricAuthSuccess)
        viewModel.getOnSignUp().removeObserver(onSignUp)
        viewModel.getOnPasswordReset().removeObserver(onPasswordReset)
        viewModel.getErrorMessage().removeObserver(onErrorMessage)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }
}