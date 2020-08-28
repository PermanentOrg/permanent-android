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

    private val onError = Observer<String> { error ->
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }

    private val onLoggedIn = Observer<Void> {
        navigateLogIn()
    }

    private val onBiometricAuthSuccess = Observer<BiometricPrompt.PromptInfo> {
        biometricPrompt.authenticate(it)
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
        viewModel.onError().observe(this, onError)
        viewModel.onLoggedIn().observe(this, onLoggedIn)
        viewModel.onBiometricAuthSuccess().observe(this, onBiometricAuthSuccess)
        viewModel.onSignUp().observe(this, onSignUp)
        viewModel.onPasswordReset().observe(this, onPasswordReset)
    }

    override fun disconnectViewModelEvents() {
        viewModel.onError().removeObserver(onError)
        viewModel.onLoggedIn().removeObserver(onLoggedIn)
        viewModel.onBiometricAuthSuccess().removeObserver(onBiometricAuthSuccess)
        viewModel.onSignUp().removeObserver(onSignUp)
        viewModel.onPasswordReset().removeObserver(onPasswordReset)
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
            viewModel.onError.value = getString(
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
            viewModel.onError.value =
                getString(R.string.login_biometric_authentication_failed_message)
        }
    }
}
