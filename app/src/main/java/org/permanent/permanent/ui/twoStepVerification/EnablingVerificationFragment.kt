package org.permanent.permanent.ui.twoStepVerification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentVerificationEnablingBinding
import org.permanent.permanent.viewmodels.EnablingVerificationViewModel

class EnablingVerificationFragment : Fragment() {

    private lateinit var verificationViewModel: EnablingVerificationViewModel
    private lateinit var binding: FragmentVerificationEnablingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentVerificationEnablingBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        verificationViewModel = ViewModelProvider(this).get(EnablingVerificationViewModel::class.java)
        binding.viewModel = verificationViewModel

        verificationViewModel.onSkipTwoStep().observe(viewLifecycleOwner, onSkipTwoStepObserver)
        verificationViewModel.onSubmit().observe(viewLifecycleOwner, onSubmitObserver)

        return binding.root
    }

    private val onSkipTwoStepObserver = Observer<Void> {
        navigateSkip()
    }

    private val onSubmitObserver = Observer<Void> {
        navigateSubmit()
    }

    private fun navigateSubmit() {
        this.findNavController().navigate(R.id.action_enablingVerificationFragment_to_codeVerificationFragment)
    }

    private fun navigateSkip() {
        findNavController().navigate(R.id.action_enablingVerificationFragment_to_mainActivity,)
        activity?.finish()
    }
}