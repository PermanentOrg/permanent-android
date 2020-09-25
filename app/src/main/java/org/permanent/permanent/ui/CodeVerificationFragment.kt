package org.permanent.permanent.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.databinding.FragmentVerificationCodeBinding
import org.permanent.permanent.ui.activities.MainActivity
import org.permanent.permanent.viewmodels.CodeVerificationViewModel

class CodeVerificationFragment : PermanentBaseFragment() {

    private lateinit var binding: FragmentVerificationCodeBinding
    private lateinit var viewModel: CodeVerificationViewModel

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

    private val onCodeVerified = Observer<Void> {
        navigateCodeVerified()
    }

    private val onErrorMessage = Observer<String> { errorMessage ->
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun navigateCodeVerified() {
        startActivity(Intent(context, MainActivity::class.java))
        activity?.finish()
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