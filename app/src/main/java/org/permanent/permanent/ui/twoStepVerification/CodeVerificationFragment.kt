package org.permanent.permanent.ui.twoStepVerification

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentVerificationCodeBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.viewmodels.CodeVerificationViewModel

class CodeVerificationFragment : PermanentBaseFragment() {

    private lateinit var binding: FragmentVerificationCodeBinding
    private lateinit var viewModel: CodeVerificationViewModel

    private val onErrorMessage = Observer<String> { errorMessage ->
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }
    private val onCodeVerified = Observer<Void> {
        val prefsHelper = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.let { sharedPreferences -> PreferencesHelper(sharedPreferences) }

        if (isLoginFlow() || prefsHelper!= null && prefsHelper.isPhoneVerified()) {
            startMainActivity()
        } else {
            startPhoneVerificationFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVerificationCodeBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(CodeVerificationViewModel::class.java)
        binding.viewModel = viewModel
        return binding.root
    }

    private fun isLoginFlow(): Boolean {
        return findNavController().graph.id == R.id.login_navigation
    }

    private fun startMainActivity() {
        findNavController().navigate(R.id.action_codeVerificationFragment_to_mainActivity)
        activity?.finish()
    }

    private fun startPhoneVerificationFragment() {
        findNavController().navigate(R.id.action_codeVerificationFragment_to_phoneVerificationFragment)
    }

    override fun connectViewModelEvents() {
        viewModel.getOnCodeVerified().observe(this, onCodeVerified)
        viewModel.getErrorMessage().observe(this, onErrorMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnCodeVerified().removeObserver(onCodeVerified)
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