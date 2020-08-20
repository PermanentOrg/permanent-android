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

    private val onBiometricAuthError = Observer<String> { error ->
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }

    private val onSignUp = Observer<Void> {
        navigateSignUp()
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
        viewModel.onBiometricAuthError().observe(this, onBiometricAuthError)
        viewModel.onSignUp().observe(this, onSignUp)
    }

    override fun disconnectViewModelEvents() {
        viewModel.onError().removeObserver(onError)
        viewModel.onLoggedIn().removeObserver(onLoggedIn)
        viewModel.onBiometricAuthSuccess().removeObserver(onBiometricAuthSuccess)
        viewModel.onBiometricAuthError().removeObserver(onBiometricAuthError)
        viewModel.onSignUp().removeObserver(onSignUp)
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
        //TODO refactor with appropriate Activity
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private val biometricAuthCallback = object : BiometricPrompt.AuthenticationCallback() {

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            viewModel.onBiometricAuthError.value = "Authentication error: $errString"
        }

        override fun onAuthenticationSucceeded(
            result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            navigateLogIn()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            viewModel.onBiometricAuthError.value = "Authentication failed"
        }
    }
}
