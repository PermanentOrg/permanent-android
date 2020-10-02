package org.permanent.permanent.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.databinding.DialogForgotPasswordBinding
import org.permanent.permanent.databinding.FragmentLoginBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.activities.SignUpActivity
import org.permanent.permanent.viewmodels.ForgotPasswordViewModel
import org.permanent.permanent.viewmodels.LoginFragmentViewModel
import java.util.concurrent.Executor

class LoginFragment  : PermanentBaseFragment() {
    private lateinit var fragmentViewModel: LoginFragmentViewModel
    private lateinit var dialogViewModel: ForgotPasswordViewModel
    private lateinit var binding: FragmentLoginBinding
    private lateinit var dialogBinding: DialogForgotPasswordBinding
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
        fragmentViewModel = ViewModelProvider(this).get(LoginFragmentViewModel::class.java)
        binding.viewModel = fragmentViewModel

        executor = ContextCompat.getMainExecutor(context)
        biometricPrompt = BiometricPrompt(this, executor, biometricAuthCallback)

        return binding.root
    }

    private val biometricAuthCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            fragmentViewModel.errorMessage.value = getString(
                R.string.login_screen_biometric_authentication_error_message
            ) + errString
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            startMainActivity()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            fragmentViewModel.errorMessage.value =
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
                findNavController().navigate(R.id.action_loginFragment_to_codeVerificationFragment)
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
        startMainActivity()
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
            getString(
                R.string.login_screen_password_reset_message,
                dialogViewModel.getCurrentEmail()?.value
            ),
            Toast.LENGTH_LONG
        ).show()
    }

    private val onReadyToShowForgotPassDialog = Observer<Void> {
        showForgotPassDialog()
    }

    private fun showForgotPassDialog() {
        dialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_forgot_password, null, false
        )
        dialogBinding.executePendingBindings()
        dialogViewModel = ViewModelProvider(this).get(ForgotPasswordViewModel::class.java)
        dialogBinding.viewModel = dialogViewModel
        val thisContext = context

        if (thisContext != null) {
            val alert = AlertDialog.Builder(thisContext)
                .setView(dialogBinding.root)
                .create()

            dialogBinding.btnReset.setOnClickListener {
                val email = dialogViewModel.getValidatedEmail()
                if (email != null) {
                    fragmentViewModel.resetPassword(email)
                    alert.dismiss()
                } else {
                    Toast.makeText(context, R.string.invalid_email_error, Toast.LENGTH_LONG).show()
                }
            }
            dialogBinding.btnCancel.setOnClickListener {
                alert.dismiss()
            }
            alert.show()
        }
    }

    private fun startMainActivity() {
        findNavController().navigate(R.id.action_loginFragment_to_mainActivity)
        activity?.finish()
    }

    private fun navigateSignUp() {
        startActivity(Intent(context, SignUpActivity::class.java))
    }

    override fun connectViewModelEvents() {
        fragmentViewModel.getErrorStringId().observe(this, onErrorStringId)
        fragmentViewModel.getOnLoggedIn().observe(this, onLoggedIn)
        fragmentViewModel.getOnBiometricAuthSuccess().observe(this, onBiometricAuthSuccess)
        fragmentViewModel.getOnSignUp().observe(this, onSignUp)
        fragmentViewModel.getOnPasswordReset().observe(this, onPasswordReset)
        fragmentViewModel.getOnReadyToShowForgotPassDialog().observe(this,
            onReadyToShowForgotPassDialog)
        fragmentViewModel.getErrorMessage().observe(this, onErrorMessage)
    }

    override fun disconnectViewModelEvents() {
        fragmentViewModel.getErrorStringId().removeObserver(onErrorStringId)
        fragmentViewModel.getOnLoggedIn().removeObserver(onLoggedIn)
        fragmentViewModel.getOnBiometricAuthSuccess().removeObserver(onBiometricAuthSuccess)
        fragmentViewModel.getOnSignUp().removeObserver(onSignUp)
        fragmentViewModel.getOnPasswordReset().removeObserver(onPasswordReset)
        fragmentViewModel.getOnReadyToShowForgotPassDialog().removeObserver(
            onReadyToShowForgotPassDialog)
        fragmentViewModel.getErrorMessage().removeObserver(onErrorMessage)
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