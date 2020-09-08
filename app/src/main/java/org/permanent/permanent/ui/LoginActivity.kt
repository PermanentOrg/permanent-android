package org.permanent.permanent.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.permanent.R
import org.permanent.databinding.ActivityLoginBinding
import org.permanent.permanent.viewmodels.LoginViewModel
import java.util.concurrent.Executor

class LoginActivity : PermanentBaseActivity() {
    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, biometricAuthCallback)
    }

    override fun connectViewModelEvents() {
        viewModel.getOnError().observe(this, onError)
        viewModel.getOnLoggedIn().observe(this, onLoggedIn)
        viewModel.getOnBiometricAuthSuccess().observe(this, onBiometricAuthSuccess)
        viewModel.getOnSignUp().observe(this, onSignUp)
        viewModel.getOnPasswordReset().observe(this, onPasswordReset)
        viewModel.getOnAuthenticationError().observe(this,onAuthenticationError)
    }

    private val onError = Observer<Int> { error ->
        val errorMessage = this.resources.getString(error)
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private val onAuthenticationError = Observer<String> { error ->
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
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
            this,
            getString(R.string.login_screen_password_reset_message),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnError().removeObserver(onError)
        viewModel.getOnLoggedIn().removeObserver(onLoggedIn)
        viewModel.getOnBiometricAuthSuccess().removeObserver(onBiometricAuthSuccess)
        viewModel.getOnSignUp().removeObserver(onSignUp)
        viewModel.getOnPasswordReset().removeObserver(onPasswordReset)
        viewModel.getOnAuthenticationError().removeObserver(onAuthenticationError)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    private fun navigateLogIn() {
        //TODO refactor with appropriate Activity
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateSignUp() {
        startActivity(Intent(this, SignUpActivity::class.java))
    }

    private val biometricAuthCallback = object : BiometricPrompt.AuthenticationCallback() {

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            viewModel.authenticationError.value = getString(
                R.string.login_screen_biometric_authentication_error_message
            ) + errString
        }

        override fun onAuthenticationSucceeded(
            result: BiometricPrompt.AuthenticationResult
        ) {
            super.onAuthenticationSucceeded(result)
            navigateLogIn()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            viewModel.authenticationError.value =
                getString(R.string.login_biometric_authentication_failed_message)
        }
    }
}
