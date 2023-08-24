package org.permanent.permanent.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.EventType
import org.permanent.permanent.EventsManager
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentVerificationCodeBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.archiveOnboarding.ArchiveOnboardingActivity
import org.permanent.permanent.viewmodels.CodeVerificationViewModel

class CodeVerificationFragment : PermanentBaseFragment() {

    private lateinit var binding: FragmentVerificationCodeBinding
    private lateinit var viewModel: CodeVerificationViewModel
    private var smsVerificationCodeHelper: SmsVerificationCodeHelper? = null
    private lateinit var prefsHelper: PreferencesHelper

    private val onErrorMessage = Observer<String> { errorMessage ->
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

    private val onCodeVerified = Observer<Void> {
        when {
            isLoginFlow() -> {
                val defaultArchiveId = prefsHelper.getDefaultArchiveId()
                if (defaultArchiveId == 0) startArchiveOnboardingActivity()
                else viewModel.getDefaultArchive(defaultArchiveId)
            }
            viewModel.isSmsCodeFlow -> startMainActivity()
            else -> startPhoneVerificationFragment()
        }
    }

    private val onLoggedIn = Observer<Void> {
        logEvents()
        startMainActivity()
    }

    private val onSmsCodeReceived = Observer<String> {
        binding.etVerificationCode.setText(it)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentVerificationCodeBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this)[CodeVerificationViewModel::class.java]
        binding.viewModel = viewModel

        prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )

        return binding.root
    }

    private fun isLoginFlow(): Boolean {
        return findNavController().graph.id == R.id.login_navigation
    }

    private fun startArchiveOnboardingActivity() {
        prefsHelper.saveArchiveOnboardingDoneInApp(true)
        startActivity(Intent(context, ArchiveOnboardingActivity::class.java))
        activity?.finish()
    }

    private fun startMainActivity() {
        findNavController().navigate(R.id.action_codeVerificationFragment_to_mainActivity)
        activity?.finish()
    }

    private fun startPhoneVerificationFragment() {
        findNavController().navigate(R.id.action_codeVerificationFragment_to_phoneVerificationFragment)
    }

    private fun logEvents() {
        EventsManager(requireContext()).setUserProfile(prefsHelper.getAccountId(), prefsHelper.getAccountEmail())
        EventsManager(requireContext()).sendToMixpanel(EventType.SignIn)
    }

    override fun connectViewModelEvents() {
        viewModel.getOnCodeVerified().observe(this, onCodeVerified)
        viewModel.getOnLoggedIn().observe(this, onLoggedIn)
        viewModel.getErrorMessage().observe(this, onErrorMessage)
        smsVerificationCodeHelper?.getCode()?.observe(this, onSmsCodeReceived)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnCodeVerified().removeObserver(onCodeVerified)
        viewModel.getOnLoggedIn().removeObserver(onLoggedIn)
        viewModel.getErrorMessage().removeObserver(onErrorMessage)
        smsVerificationCodeHelper?.getCode()?.removeObserver(onSmsCodeReceived)
    }

    override fun onResume() {
        super.onResume()
        if (isSmsCodeFlow()) {
            viewModel.isSmsCodeFlow = true
            smsVerificationCodeHelper = context?.let { SmsVerificationCodeHelper(it) }
            smsVerificationCodeHelper?.registerReceiver()
        }
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.isSmsCodeFlow) smsVerificationCodeHelper?.unregisterReceiver()
        disconnectViewModelEvents()
    }

    private fun isSmsCodeFlow(): Boolean {
        return findNavController().previousBackStackEntry?.destination?.id == R.id.phoneVerificationFragment
    }
}
