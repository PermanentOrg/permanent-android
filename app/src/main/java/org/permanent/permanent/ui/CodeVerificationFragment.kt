package org.permanent.permanent.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.databinding.FragmentVerificationCodeBinding
import org.permanent.permanent.viewmodels.CodeVerificationViewModel

class CodeVerificationFragment : Fragment() {

    private lateinit var binding: FragmentVerificationCodeBinding
    private lateinit var verificationViewModel: CodeVerificationViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVerificationCodeBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        verificationViewModel = ViewModelProvider(this).get(CodeVerificationViewModel::class.java)
        binding.viewModel = verificationViewModel
        return binding.root
    }

}