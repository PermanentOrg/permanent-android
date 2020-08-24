package org.permanent.permanent.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.dialog_terms_of_service.view.*
import org.permanent.R
import org.permanent.databinding.ActivitySignUpBinding
import org.permanent.permanent.models.Event
import org.permanent.permanent.viewmodels.SignUpViewModel
import org.w3c.dom.Text

class SignUpActivity : PermanentBaseActivity() {
    private lateinit var viewModel: SignUpViewModel
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var displayTermsOfServiceAlertObserver: Observer<Event<Unit>>


    private val onError = Observer<String> { error ->

        when(error){
           "Password" ->{
               binding.layoutPassword.error = "Please enter your password"

           }
            "Email" -> {
                binding.layoutEmail.error = "Please enter valid email address"

            }
             "Name" -> {
                 binding.layoutFullName.error = "Please enter your full name"

             }
        }

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
        viewModel.displayTermsOfServiceTextDialog.observe(this, displayTermsOfServiceAlertObserver)
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

    private fun createObservers() {
        displayTermsOfServiceAlertObserver = Observer {
            it.getContentIfNotHandled()?.let {

                val viewDialog: View =
                    layoutInflater.inflate(R.layout.dialog_terms_of_service, null)

                val alert = AlertDialog.Builder(this)
                    .setTitle("Terms and Conditions")
                    .setView(viewDialog)
                    .create()

                val titleView = TextView(this)
                titleView.text = getString(R.string.terms_conditions_title)
                titleView.setBackgroundColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.colorAccent
                    )
                )
                titleView.setPadding(10, 30, 10, 30)
                titleView.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
                titleView.textSize = 20F
                titleView.gravity = Gravity.CENTER

                alert.setCustomTitle(titleView)
                viewDialog.webviewtermsOfService.loadUrl("https://www.permanent.org/privacy-policy/")
                viewDialog.btnAccept.setOnClickListener { _ ->
                    viewModel.makeAccount()
                    alert.dismiss()
                }
                viewDialog.btnDecline.setOnClickListener { _ ->
                    Toast.makeText(this, "Declined Terms of Service", Toast.LENGTH_SHORT).show()
                    alert.dismiss()
                }

                alert.show()

            }
        }
    }
}
