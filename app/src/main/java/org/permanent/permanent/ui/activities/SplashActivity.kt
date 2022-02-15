package org.permanent.permanent.ui.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivitySplashBinding
import org.permanent.permanent.network.AuthStateManager
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.login.LoginActivity
import org.permanent.permanent.ui.onboarding.OnboardingActivity
import org.permanent.permanent.ui.twoStepVerification.TwoStepVerificationActivity
import org.permanent.permanent.viewmodels.SplashViewModel

class SplashActivity : PermanentBaseActivity() {

    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var binding: ActivitySplashBinding
    private lateinit var viewModel: SplashViewModel
    private var isLoginFlow = false

    private val errorObserver = Observer<String> {
        val snackBar = Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.deepRed))
        snackBar.setTextColor(ContextCompat.getColor(this, R.color.white))
        snackBar.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        viewModel = ViewModelProvider(this).get(SplashViewModel::class.java)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        createNotificationChannel()
        prefsHelper = PreferencesHelper(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
        prefsHelper.saveShareLinkUrlToken("")

        if (!prefsHelper.getSkipTwoStepVerification()) {
            val intent = Intent(this@SplashActivity, TwoStepVerificationActivity::class.java)
            intent.putExtra(SKIP_CODE_VERIFICATION_FRAGMENT, true)
            startActivity(intent)
            finish()
        } else {
            val authResponse = AuthorizationResponse.fromIntent(intent)
            val authException = AuthorizationException.fromIntent(intent)

            when {
                authResponse != null -> {
                    AuthStateManager.getInstance(this).updateAfterAuthorization(authResponse, authException)
                    isLoginFlow = true
                    viewModel.requestTokens(authResponse)
                }
                authException != null -> {
                    AuthStateManager.getInstance(this).updateAfterAuthorization(authResponse, authException)
                    isLoginFlow = true
                    errorObserver.onChanged(authException.errorDescription)
                }
                else -> {
                    isLoginFlow = false
                    viewModel.verifyIsUserLoggedIn()
                }
            }
        }
    }

    private val loggedInResponseObserver = Observer<Boolean> { isLoggedIn ->
        if (!isLoggedIn) {
            if (!prefsHelper.isOnboardingCompleted()) {
                startActivity(Intent(this@SplashActivity, OnboardingActivity::class.java))
                finish()
            } else {
                startSignUpActivity()
            }
        } else if (isLoginFlow) { // User just loggedIn, no need for biometrics
            startMainActivity()
        } else { // User was loggedIn, show biometrics
            startLoginActivity()
        }
    }

    private fun startSignUpActivity() {
        val intent = Intent(this@SplashActivity, SignUpActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startMainActivity() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startLoginActivity() {
        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            getString(R.string.default_notification_channel_id),
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    override fun connectViewModelEvents() {
        viewModel.getOnLoggedInResponse().observe(this, loggedInResponseObserver)
        viewModel.getShowError().observe(this, errorObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnLoggedInResponse().removeObserver(loggedInResponseObserver)
        viewModel.getShowError().removeObserver(errorObserver)
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