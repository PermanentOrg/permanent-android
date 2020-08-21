package org.permanent.permanent.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.permanent.R
import org.permanent.databinding.ActivitySignUpBinding
import org.permanent.permanent.viewmodels.SignUpViewModel

class SignUpActivity : PermanentBaseActivity() {
    private lateinit var viewModel: SignUpViewModel
    private lateinit var binding: ActivitySignUpBinding

    private val onError = Observer<String> { error ->
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }

    private val onSignedUp = Observer<Void> {
        navigateSignUp()
    }

    private val onAlreadyHaveAccount = Observer<Void> {
        navigateAlreadyHaveAccount()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    private fun navigateSignUp() {
        //TODO refactor with appropriate Activity
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateAlreadyHaveAccount() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun connectViewModelEvents() {
        viewModel.onError().observe(this, onError)
        viewModel.onSignedUp().observe(this, onSignedUp)
        viewModel.onAlreadyHaveAccount().observe(this, onAlreadyHaveAccount)
    }

    override fun disconnectViewModelEvents() {
        viewModel.onError().removeObserver(onError)
        viewModel.onSignedUp().removeObserver(onSignedUp)
        viewModel.onAlreadyHaveAccount().removeObserver(onAlreadyHaveAccount)
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
