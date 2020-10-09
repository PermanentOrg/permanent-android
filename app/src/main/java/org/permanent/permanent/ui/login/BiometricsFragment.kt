package org.permanent.permanent.ui.login

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.infinum.goldfinger.Goldfinger
import co.infinum.goldfinger.MissingHardwareException
import co.infinum.goldfinger.NoEnrolledFingerprintException
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentBiometricsBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.viewmodels.BiometricsViewModel

class BiometricsFragment : PermanentBaseFragment() {

    private lateinit var viewModel: BiometricsViewModel
    private lateinit var binding: FragmentBiometricsBinding
    private var prefsHelper: PreferencesHelper? = null
    private var goldfinger: Goldfinger? = null
    private val onBiometricAuthSuccess = Observer<Void> {
        startMainActivity()
    }
    private val onBiometricsUnregistered = Observer<Void> {
        showOpenSettingsQuestionDialog()
    }
    private val onErrorMessage = Observer<String> { errorMessage ->
        when (errorMessage) {
            Constants.ERROR_SERVER_ERROR,
            Constants.ERROR_NO_API_KEY -> Toast.makeText(
                context,
                R.string.server_error,
                Toast.LENGTH_LONG
            ).show()
            else -> Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }
    private val onErrorStringId = Observer<Int> { errorId ->
        val errorMessage = this.resources.getString(errorId)
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }
    private val onLoggedOut = Observer<Void> {
        findNavController().navigate(R.id.action_biometricsFragment_to_loginFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefsHelper = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.let { sharedPreferences -> PreferencesHelper(sharedPreferences) }
        goldfinger = context?.let { Goldfinger.Builder(it).build() }

        if(skipLogin()) {
            startMainActivity()
            return
        }
        if(skipBiometrics()) startLoginFragment()
    }

    private fun skipLogin(): Boolean {
        return prefsHelper != null
                && prefsHelper!!.isUserLoggedIn()
                && goldfinger != null
                && !goldfinger!!.canAuthenticate()
                && !goldfinger!!.hasFingerprintHardware()
    }

    private fun skipBiometrics(): Boolean {
        return prefsHelper != null && !prefsHelper!!.isUserLoggedIn()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBiometricsBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(BiometricsViewModel::class.java)
        binding.viewModel = viewModel
        initClickListeners()

        return binding.root
    }

    private fun initClickListeners() {
        binding.btnUseBiometrics.setOnClickListener {
            authenticateUser()
        }
        binding.btnUseCredentials.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun authenticateUser() {
        goldfinger?.authenticate(buildPromptParams(), object : Goldfinger.Callback {
            override fun onError(exception: Exception) {
                when (exception) {
                    is NoEnrolledFingerprintException -> viewModel.handleResult(Goldfinger.Reason.NO_BIOMETRICS)
                    is MissingHardwareException -> viewModel.handleResult(Goldfinger.Reason.HW_NOT_PRESENT)
                    else -> exception.message?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                }
            }
            override fun onResult(result: Goldfinger.Result) {
                if (result.type() == Goldfinger.Type.SUCCESS)
                    viewModel.handleResult(Goldfinger.Reason.AUTHENTICATION_SUCCESS)
                else if (result.type() != Goldfinger.Type.INFO)
                    viewModel.handleResult(result.reason())
            }
        })
    }

    private fun buildPromptParams(): Goldfinger.PromptParams {
        return Goldfinger.PromptParams.Builder(this)
            .title(R.string.login_biometric_title)
            .description(R.string.login_biometric_message)
            .deviceCredentialsAllowed(true)
            .negativeButtonText(R.string.cancel_button)
            .build()
    }

    private fun startMainActivity() {
        findNavController().navigate(R.id.action_biometricsFragment_to_mainActivity)
    }

    private fun startLoginFragment() {
        findNavController().navigate(R.id.action_biometricsFragment_to_loginFragment)
    }

    private fun showOpenSettingsQuestionDialog() {
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(getString(R.string.login_biometric_error_no_biometrics_enrolled_title))
                setMessage(getString(R.string.login_biometric_error_no_biometrics_enrolled_message))
                setPositiveButton(R.string.yes_button) { _, _ ->
                    startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS)) }
                setNegativeButton(R.string.cancel_button) { _, _ -> }
            }
            builder.create()
        }
        alertDialog?.show()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnBiometricAuthSuccess().observe(this, onBiometricAuthSuccess)
        viewModel.getOnBiometricsUnregistered().observe(this, onBiometricsUnregistered)
        viewModel.getOnLoggedOut().observe(this, onLoggedOut)
        viewModel.getErrorMessage().observe(this, onErrorMessage)
        viewModel.getErrorStringId().observe(this, onErrorStringId)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnBiometricAuthSuccess().removeObserver(onBiometricAuthSuccess)
        viewModel.getOnBiometricsUnregistered().removeObserver(onBiometricsUnregistered)
        viewModel.getOnLoggedOut().removeObserver(onLoggedOut)
        viewModel.getErrorMessage().removeObserver(onErrorMessage)
        viewModel.getErrorStringId().removeObserver(onErrorStringId)
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
