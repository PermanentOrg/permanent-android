package org.permanent.permanent.ui.login

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.dialog_terms_of_service.view.*
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentSignUpBinding
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.hideKeyboardFrom
import org.permanent.permanent.viewmodels.SignUpViewModel

class SignUpFragment : PermanentBaseFragment() {
    private lateinit var viewModel: SignUpViewModel
    private lateinit var binding: FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this)[SignUpViewModel::class.java]
        binding.viewModel = viewModel

        return binding.root
    }

    private val onReadyToShowTermsDialog = Observer<Void> { showTermsDialog() }

    private val showLoginScreenObserver = Observer<Void> {
        findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
    }

    private val onErrorMessage = Observer<String> { errorMessage ->
        when (errorMessage) {
            //Sign up error
            Constants.ERROR_ACCOUNT_DUPLICATE -> Toast.makeText(
                context, R.string.sign_up_email_in_use_error, Toast.LENGTH_LONG
            ).show()
            else -> Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    private val onSuccessMessage = Observer<String> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        context?.let {
            view.setBackgroundColor(ContextCompat.getColor(it, R.color.paleGreen))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.green))
            val snackbarTextTextView = view.findViewById(R.id.snackbar_text) as TextView
            snackbarTextTextView.setTypeface(snackbarTextTextView.typeface, Typeface.BOLD)
            snackBar.show()
        }
    }

    private fun showTermsDialog() {
        context?.hideKeyboardFrom(binding.root.windowToken)
        val viewDialog: View = layoutInflater.inflate(R.layout.dialog_terms_of_service, null)

        val alert = AlertDialog.Builder(context).setView(viewDialog).create()

        viewDialog.webviewtermsOfService.loadUrl(BuildConfig.TERMS_URL)
        viewDialog.btnAccept.setOnClickListener {
            viewModel.makeAccount()
            alert.dismiss()
        }
        viewDialog.btnDecline.setOnClickListener {
            Toast.makeText(context, R.string.sign_up_terms_declined, Toast.LENGTH_SHORT).show()
            alert.dismiss()
        }
        alert.show()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnSuccessMessage().observe(this, onSuccessMessage)
        viewModel.getOnErrorMessage().observe(this, onErrorMessage)
        viewModel.getOnReadyToShowTermsDialog().observe(this, onReadyToShowTermsDialog)
        viewModel.getShowLoginScreen().observe(this, showLoginScreenObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnSuccessMessage().removeObserver(onSuccessMessage)
        viewModel.getOnErrorMessage().removeObserver(onErrorMessage)
        viewModel.getOnReadyToShowTermsDialog().removeObserver(onReadyToShowTermsDialog)
        viewModel.getShowLoginScreen().removeObserver(showLoginScreenObserver)
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
