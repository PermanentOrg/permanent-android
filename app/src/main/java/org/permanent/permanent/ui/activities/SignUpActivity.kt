package org.permanent.permanent.ui.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.dialog_terms_of_service.view.*
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivitySignUpBinding
import org.permanent.permanent.ui.login.LoginActivity
import org.permanent.permanent.ui.twoStepVerification.TwoStepVerificationActivity
import org.permanent.permanent.viewmodels.SignUpViewModel

class SignUpActivity : PermanentBaseActivity() {
    private lateinit var viewModel: SignUpViewModel
    private lateinit var binding: ActivitySignUpBinding

    private val onSignedUp = Observer<Void> { startTwoStepActivity() }
    private val onReadyToShowTermsDialog = Observer<Void> { showTermsDialog() }
    private val onAlreadyHaveAccount = Observer<Void> { startLoginActivity() }
    private val onErrorMessage = Observer<String> { errorMessage ->
        when(errorMessage) {
            Constants.ERROR_ACCOUNT_DUPLICATE -> Toast.makeText(
                this,
                R.string.sign_up_email_in_use_error,
                Toast.LENGTH_LONG
            ).show()
            else -> Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    private fun showTermsDialog() {
        val viewDialog: View = layoutInflater.inflate(R.layout.dialog_terms_of_service, null)

        val alert = AlertDialog.Builder(this)
            .setView(viewDialog)
            .create()

        viewDialog.webviewtermsOfService.loadUrl(Constants.URL_PRIVACY_POLICY)
        viewDialog.btnAccept.setOnClickListener {
            viewModel.makeAccount()
            alert.dismiss()
        }
        viewDialog.btnDecline.setOnClickListener {
            Toast.makeText(this, R.string.sign_up_terms_declined, Toast.LENGTH_SHORT).show()
            alert.dismiss()
        }
        alert.show()
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startTwoStepActivity() {
        val intent = Intent(this, TwoStepVerificationActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnErrorMessage().observe(this, onErrorMessage)
        viewModel.getOnSignedUp().observe(this, onSignedUp)
        viewModel.getOnReadyToShowTermsDialog().observe(this, onReadyToShowTermsDialog)
        viewModel.getOnAlreadyHaveAccount().observe(this, onAlreadyHaveAccount)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnErrorMessage().removeObserver(onErrorMessage)
        viewModel.getOnSignedUp().removeObserver(onSignedUp)
        viewModel.getOnReadyToShowTermsDialog().removeObserver(onReadyToShowTermsDialog)
        viewModel.getOnAlreadyHaveAccount().removeObserver(onAlreadyHaveAccount)
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
