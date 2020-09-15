package org.permanent.permanent.ui.twoStepVerification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.databinding.FragmentTwoStepVerificationCodeBinding

class TwoStepVerificationCodeFragment : Fragment() {

    private lateinit var binding: FragmentTwoStepVerificationCodeBinding
    private lateinit var viewModel: TwoStepVerificationCodeViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTwoStepVerificationCodeBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(TwoStepVerificationCodeViewModel::class.java)
        binding.viewModel = viewModel
        return binding.root
    }

}