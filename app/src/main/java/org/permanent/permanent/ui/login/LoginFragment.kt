package org.permanent.permanent.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.databinding.DialogForgotPasswordBinding
import org.permanent.permanent.databinding.FragmentLoginBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.activities.MainActivity
import org.permanent.permanent.ui.archiveOnboarding.ArchiveOnboardingActivity
import org.permanent.permanent.viewmodels.ForgotPasswordViewModel
import org.permanent.permanent.viewmodels.LoginFragmentViewModel

class LoginFragment : PermanentBaseFragment() {
    private lateinit var viewModel: LoginFragmentViewModel
    private lateinit var dialogViewModel: ForgotPasswordViewModel
    private lateinit var binding: FragmentLoginBinding
    private lateinit var dialogBinding: DialogForgotPasswordBinding
    private lateinit var prefsHelper: PreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this)[LoginFragmentViewModel::class.java]
        binding.viewModel = viewModel

        prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )

        return binding.root
    }

    private val onErrorMessage = Observer<String> { errorMessage ->
        when (errorMessage) {
            Constants.ERROR_MFA_TOKEN -> {
                findNavController().navigate(R.id.action_loginFragment_to_codeVerificationFragment)
            }
            else -> Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    private val onLoggedIn = Observer<Void> {
        startActivity(Intent(context, MainActivity::class.java))
        activity?.finish()
    }

    private val userMissingDefaultArchiveObserver = Observer<Void> {
        prefsHelper.saveArchiveOnboardingDoneInApp(true)
        startActivity(Intent(context, ArchiveOnboardingActivity::class.java))
        activity?.finish()
    }

    private val onStartSignUp = Observer<Void> {
        findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
    }

    private val onPasswordReset = Observer<Void> {
        Toast.makeText(
            context, getString(
                R.string.login_screen_password_reset_message,
                dialogViewModel.getCurrentEmail().value
            ), Toast.LENGTH_LONG
        ).show()
    }

    private val onReadyToShowForgotPassDialog = Observer<Void> {
        showForgotPassDialog()
    }

    private fun showForgotPassDialog() {
        dialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.dialog_forgot_password, null, false
        )
        dialogBinding.executePendingBindings()
        dialogBinding.lifecycleOwner = this
        dialogViewModel = ViewModelProvider(this)[ForgotPasswordViewModel::class.java]
        dialogBinding.viewModel = dialogViewModel
        val thisContext = context

        if (thisContext != null) {
            val alert = AlertDialog.Builder(thisContext).setView(dialogBinding.root).create()

            dialogBinding.btnReset.setOnClickListener {
                val email = dialogViewModel.getValidatedEmail()
                if (email != null) {
                    viewModel.resetPassword(email)
                    alert.dismiss()
                }
            }
            dialogBinding.btnCancel.setOnClickListener {
                alert.dismiss()
            }
            alert.show()
        }
    }

    override fun connectViewModelEvents() {
        viewModel.getOnLoggedIn().observe(this, onLoggedIn)
        viewModel.getOnUserMissingDefaultArchive().observe(this, userMissingDefaultArchiveObserver)
        viewModel.getOnSignUp().observe(this, onStartSignUp)
        viewModel.getOnPasswordReset().observe(this, onPasswordReset)
        viewModel.getOnReadyToShowForgotPassDialog().observe(
            this, onReadyToShowForgotPassDialog
        )
        viewModel.getErrorMessage().observe(this, onErrorMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnLoggedIn().removeObserver(onLoggedIn)
        viewModel.getOnUserMissingDefaultArchive().removeObserver(userMissingDefaultArchiveObserver)
        viewModel.getOnSignUp().removeObserver(onStartSignUp)
        viewModel.getOnPasswordReset().removeObserver(onPasswordReset)
        viewModel.getOnReadyToShowForgotPassDialog().removeObserver(
            onReadyToShowForgotPassDialog
        )
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