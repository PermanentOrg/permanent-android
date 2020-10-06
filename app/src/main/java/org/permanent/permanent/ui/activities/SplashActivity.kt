package org.permanent.permanent.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivitySplashBinding
import org.permanent.permanent.ui.login.LoginActivity
import org.permanent.permanent.ui.onboarding.OnboardingActivity
import org.permanent.permanent.viewmodels.SplashViewModel

class SplashActivity : PermanentBaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        viewModel = ViewModelProvider(this).get(SplashViewModel::class.java)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.checkIsUserLoggedIn()
    }

    private val loggedInResponseObserver = Observer<Boolean> { isLoggedIn ->
        if (isLoggedIn) startMainActivity()
        else if (!viewModel.isOnboardingCompleted(this)) startOnboardingActivity()
        else startLoginActivity()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnLoggedInResponse().observe(this, loggedInResponseObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnLoggedInResponse().removeObserver(loggedInResponseObserver)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    private fun startOnboardingActivity() {
        startActivity(Intent(this@SplashActivity, OnboardingActivity::class.java))
        finish()
    }

    private fun startMainActivity() {
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }

    private fun startLoginActivity() {
        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
        finish()
    }
}