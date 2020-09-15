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
import org.permanent.permanent.databinding.FragmentTwoStepVerificationPhoneBinding

class TwoStepVerificationPhoneFragment : Fragment() {

    private lateinit var viewModel: TwoStepVerificationPhoneViewModel
    private lateinit var binding: FragmentTwoStepVerificationPhoneBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentTwoStepVerificationPhoneBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(TwoStepVerificationPhoneViewModel::class.java)
        binding.viewModel = viewModel

        viewModel.onSkipTwoStep().observe(viewLifecycleOwner, onSkipTwoStepObserver)
        viewModel.onSubmit().observe(viewLifecycleOwner, onSubmitObserver)

        return binding.root


    }

    private val onSkipTwoStepObserver = Observer<Void> {
        navigateSkip()
    }

    private val onSubmitObserver = Observer<Void> {
        navigateSubmit()
    }

    private fun navigateSubmit() {
        this.findNavController().navigate(R.id.action_twoStepVerificationPhoneFragment_to_twoStepVerificationCodeFragment)
    }

    private fun navigateSkip() {
        findNavController().navigate(R.id.action_twoStepVerificationPhoneFragment_to_mainActivity,)
        activity?.finish()
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onPause() {
        super.onPause()
    }
}