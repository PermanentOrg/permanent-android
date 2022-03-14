package org.permanent.permanent.ui.activities

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.dialog_terms_of_service.view.*
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivitySignUpBinding
import org.permanent.permanent.ui.hideKeyboardFrom
import org.permanent.permanent.viewmodels.SignUpViewModel

const val SKIP_CODE_VERIFICATION_FRAGMENT = "skip_code_verification_fragment"

class SignUpActivity : PermanentBaseActivity() {
    private lateinit var viewModel: SignUpViewModel
    private lateinit var binding: ActivitySignUpBinding

    private val onReadyToShowTermsDialog = Observer<Void> { showTermsDialog() }

    private val onErrorMessage = Observer<String> { errorMessage ->
        when (errorMessage) {
            //Sign up error
            Constants.ERROR_ACCOUNT_DUPLICATE -> Toast.makeText(
                this,
                R.string.sign_up_email_in_use_error,
                Toast.LENGTH_LONG
            ).show()
            else -> Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    private val onSuccessMessage = Observer<String> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.paleGreen))
        snackBar.setTextColor(ContextCompat.getColor(this, R.color.green))
        val snackbarTextTextView = view.findViewById(R.id.snackbar_text) as TextView
        snackbarTextTextView.setTypeface(snackbarTextTextView.typeface, Typeface.BOLD)
        snackBar.show()
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
        hideKeyboardFrom(binding.root.windowToken)
        val viewDialog: View = layoutInflater.inflate(R.layout.dialog_terms_of_service, null)

        val alert = AlertDialog.Builder(this)
            .setView(viewDialog)
            .create()

        viewDialog.webviewtermsOfService.loadUrl(BuildConfig.TERMS_URL)
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

    override fun connectViewModelEvents() {
        viewModel.getOnSuccessMessage().observe(this, onSuccessMessage)
        viewModel.getOnErrorMessage().observe(this, onErrorMessage)
        viewModel.getOnReadyToShowTermsDialog().observe(this, onReadyToShowTermsDialog)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnSuccessMessage().removeObserver(onSuccessMessage)
        viewModel.getOnErrorMessage().removeObserver(onErrorMessage)
        viewModel.getOnReadyToShowTermsDialog().removeObserver(onReadyToShowTermsDialog)
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
