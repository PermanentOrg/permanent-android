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
import org.permanent.permanent.PermissionsHelper
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

        context?.let {
            val permissionHelper = PermissionsHelper()
            if (!permissionHelper.hasSMSGroupPermission(it))
            permissionHelper.requestSMSGroupPermission(this)
        }

        return binding.root
    }

    private val onVerificationSkipped = Observer<Void> {
        findNavController().navigate(R.id.action_phoneVerificationFragment_to_mainActivity)
        activity?.finish()
    }

    private val onSMSCodeSent = Observer<Void> {
        findNavController().navigate(R.id.action_phoneVerificationFragment_to_codeVerificationFragment)
    }

    private val onErrorMessage = Observer<String> { errorMessage ->
        when(errorMessage) {
            //Phone update error
            Constants.ERROR_PHONE_INVALID -> Toast.makeText(
                context,
                R.string.sign_up_phone_invalid_error,
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

    override fun connectViewModelEvents() {
        viewModel.getOnVerificationSkipped().observe(this, onVerificationSkipped)
        viewModel.getOnSMSCodeSent().observe(this, onSMSCodeSent)
        viewModel.getOnErrorMessage().observe(this, onErrorMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnVerificationSkipped().removeObserver(onVerificationSkipped)
        viewModel.getOnSMSCodeSent().removeObserver(onSMSCodeSent)
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
