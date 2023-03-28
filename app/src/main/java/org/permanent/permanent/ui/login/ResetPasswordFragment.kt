package org.permanent.permanent.ui.login

import android.content.Context
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
import org.permanent.permanent.R
import org.permanent.permanent.databinding.FragmentResetPasswordBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.viewmodels.ResetPasswordViewModel

class ResetPasswordFragment : PermanentBaseFragment() {
    private lateinit var viewModel: ResetPasswordViewModel
    private lateinit var binding: FragmentResetPasswordBinding
    private lateinit var prefsHelper: PreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this)[ResetPasswordViewModel::class.java]
        binding.viewModel = viewModel

        prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )

        return binding.root
    }

    private val onPasswordReset = Observer<Void> {
        val message = getString(
            R.string.security_password_update_success
        )
        val snackBar = Snackbar.make(binding.root, message, SNACKBAR_DURATION)
        val view: View = snackBar.view
        context?.let {
            view.setBackgroundColor(ContextCompat.getColor(it, R.color.deepGreen))
            snackBar.setTextColor(ContextCompat.getColor(it, R.color.paleGreen))
        }
        val snackbarTextTextView = view.findViewById(R.id.snackbar_text) as TextView
        snackbarTextTextView.setTypeface(snackbarTextTextView.typeface, Typeface.BOLD)
        snackBar.show()

        onBackToSignIn.onChanged(null)
    }

    private val onBackToSignIn = Observer<Void> {
        findNavController().popBackStack()
    }

    private val onErrorMessage = Observer<String> { errorMessage ->
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnPasswordReset().observe(this, onPasswordReset)
        viewModel.getOnBackToSignIn().observe(this, onBackToSignIn)
        viewModel.getErrorMessage().observe(this, onErrorMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnPasswordReset().removeObserver(onPasswordReset)
        viewModel.getOnBackToSignIn().removeObserver(onBackToSignIn)
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

    companion object {
        const val SNACKBAR_DURATION = 4000
    }
}