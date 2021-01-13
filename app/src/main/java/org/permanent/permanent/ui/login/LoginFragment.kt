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
import org.permanent.permanent.ui.activities.SignUpActivity
import org.permanent.permanent.viewmodels.ForgotPasswordViewModel
import org.permanent.permanent.viewmodels.LoginFragmentViewModel

class LoginFragment  : PermanentBaseFragment() {
    private lateinit var fragmentViewModel: LoginFragmentViewModel
    private lateinit var dialogViewModel: ForgotPasswordViewModel
    private lateinit var binding: FragmentLoginBinding
    private lateinit var dialogBinding: DialogForgotPasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        fragmentViewModel = ViewModelProvider(this).get(LoginFragmentViewModel::class.java)
        binding.viewModel = fragmentViewModel

        return binding.root
    }

    private val onErrorStringId = Observer<Int> { errorId ->
        val errorMessage = this.resources.getString(errorId)
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
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
        val prefsHelper = PreferencesHelper(requireContext().getSharedPreferences(
            PREFS_NAME, Context.MODE_PRIVATE))
        prefsHelper.saveUserLoggedIn(true)
        startActivity(Intent(context, MainActivity::class.java))
        activity?.finish()
    }

    private val onStartSignUp = Observer<Void> {
        startActivity(Intent(context, SignUpActivity::class.java))
        activity?.finish()
    }

    private val onPasswordReset = Observer<Void> {
        Toast.makeText(
            context,
            getString(
                R.string.login_screen_password_reset_message,
                dialogViewModel.getCurrentEmail()?.value
            ),
            Toast.LENGTH_LONG
        ).show()
    }

    private val onReadyToShowForgotPassDialog = Observer<Void> {
        showForgotPassDialog()
    }

    private fun showForgotPassDialog() {
        dialogBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_forgot_password, null, false
        )
        dialogBinding.executePendingBindings()
        dialogBinding.lifecycleOwner = this
        dialogViewModel = ViewModelProvider(this).get(ForgotPasswordViewModel::class.java)
        dialogBinding.viewModel = dialogViewModel
        val thisContext = context

        if (thisContext != null) {
            val alert = AlertDialog.Builder(thisContext)
                .setView(dialogBinding.root)
                .create()

            dialogBinding.btnReset.setOnClickListener {
                val email = dialogViewModel.getValidatedEmail()
                if (email != null) {
                    fragmentViewModel.resetPassword(email)
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
        fragmentViewModel.getErrorStringId().observe(this, onErrorStringId)
        fragmentViewModel.getOnLoggedIn().observe(this, onLoggedIn)
        fragmentViewModel.getOnSignUp().observe(this, onStartSignUp)
        fragmentViewModel.getOnPasswordReset().observe(this, onPasswordReset)
        fragmentViewModel.getOnReadyToShowForgotPassDialog().observe(this,
            onReadyToShowForgotPassDialog)
        fragmentViewModel.getErrorMessage().observe(this, onErrorMessage)
    }

    override fun disconnectViewModelEvents() {
        fragmentViewModel.getErrorStringId().removeObserver(onErrorStringId)
        fragmentViewModel.getOnLoggedIn().removeObserver(onLoggedIn)
        fragmentViewModel.getOnSignUp().removeObserver(onStartSignUp)
        fragmentViewModel.getOnPasswordReset().removeObserver(onPasswordReset)
        fragmentViewModel.getOnReadyToShowForgotPassDialog().removeObserver(
            onReadyToShowForgotPassDialog)
        fragmentViewModel.getErrorMessage().removeObserver(onErrorMessage)
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