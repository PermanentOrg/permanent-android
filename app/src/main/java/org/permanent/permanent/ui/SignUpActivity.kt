package org.permanent.permanent.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.dialog_terms_of_service.view.*
import org.permanent.R
import org.permanent.databinding.ActivitySignUpBinding
import org.permanent.permanent.ui.twoStepVerification.TwoStepVerificationActivity
import org.permanent.permanent.viewmodels.SignUpViewModel

class SignUpActivity : PermanentBaseActivity() {
    private lateinit var viewModel: SignUpViewModel
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var displayTermsOfServiceAlertObserver: Observer<Void>


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
        createObservers()

    }

    private fun navigateSignUp() {
        val intent = Intent(Intent(this, TwoStepVerificationActivity::class.java))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateAlreadyHaveAccount() {
        val intent = Intent(Intent(this, LoginActivity::class.java))
        startActivity(intent)
        finish()
    }



    override fun connectViewModelEvents() {
        viewModel.onError().observe(this, onError)
        viewModel.onSignedUp().observe(this, onSignedUp)
        viewModel.onAlreadyHaveAccount().observe(this, onAlreadyHaveAccount)
        viewModel.displayTermsOfServiceTextDialog()
            .observe(this, displayTermsOfServiceAlertObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.onError().removeObserver(onError)
        viewModel.onSignedUp().removeObserver(onSignedUp)
        viewModel.onAlreadyHaveAccount().removeObserver(onAlreadyHaveAccount)
        viewModel.displayTermsOfServiceTextDialog()
            .removeObserver(displayTermsOfServiceAlertObserver)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    private fun createObservers() {
        displayTermsOfServiceAlertObserver = Observer {


            val viewDialog: View =
                layoutInflater.inflate(R.layout.dialog_terms_of_service, null)

            val alert = AlertDialog.Builder(this)
                .setView(viewDialog)
                .create()

            viewDialog.webviewtermsOfService.loadUrl(getString(R.string.terms_of_service_privacy_policy_url))
            viewDialog.btnAccept.setOnClickListener { _ ->
                //viewModel.makeAccount()
                navigateSignUp()
                alert.dismiss()
            }
            viewDialog.btnDecline.setOnClickListener { _ ->
                Toast.makeText(this, R.string.sign_up_terms_declined, Toast.LENGTH_SHORT).show()
                alert.dismiss()
            }

            alert.show()

        }
    }
}
