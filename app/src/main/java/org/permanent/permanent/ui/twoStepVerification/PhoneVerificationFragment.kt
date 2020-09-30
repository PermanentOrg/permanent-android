package org.permanent.permanent.ui.twoStepVerification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentVerificationPhoneBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.viewmodels.PhoneVerificationViewModel

class PhoneVerificationFragment : PermanentBaseFragment() {

    private lateinit var viewModel: PhoneVerificationViewModel
    private lateinit var binding: FragmentVerificationPhoneBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVerificationPhoneBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(PhoneVerificationViewModel::class.java)
        binding.viewModel = viewModel

        return binding.root
    }

    private val onLoggedIn = Observer<Void> {
        startMainActivity()
    }

    private val onErrorMessage = Observer<String> { errorMessage ->
        when(errorMessage) {
            //Phone update error
            Constants.ERROR_PHONE_INVALID -> Toast.makeText(
                context,
                R.string.sign_up_phone_invalid_error,
                Toast.LENGTH_LONG
            ).show()
            //Login error
            Constants.ERROR_MFA_TOKEN -> startCodeVerificationFragment()
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

    private fun startMainActivity() {
        findNavController().navigate(R.id.action_phoneVerificationFragment_to_mainActivity)
        activity?.finish()
    }

    private fun startCodeVerificationFragment() {
        this.findNavController()
            .navigate(R.id.action_phoneVerificationFragment_to_codeVerificationFragment)
    }

    override fun connectViewModelEvents() {
        viewModel.getOnLoggedIn().observe(this, onLoggedIn)
        viewModel.getOnErrorMessage().observe(this, onErrorMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnLoggedIn().removeObserver(onLoggedIn)
        viewModel.getOnErrorMessage().removeObserver(onErrorMessage)
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
