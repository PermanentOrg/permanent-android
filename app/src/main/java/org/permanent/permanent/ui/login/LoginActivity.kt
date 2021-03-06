package org.permanent.permanent.ui.login

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivityLoginBinding
import org.permanent.permanent.ui.IS_USER_LOGGED_IN
import org.permanent.permanent.ui.activities.PermanentBaseActivity
import org.permanent.permanent.viewmodels.LoginActivityViewModel

class LoginActivity : PermanentBaseActivity() {
    private lateinit var activityViewModel: LoginActivityViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityViewModel = ViewModelProvider(this).get(LoginActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = activityViewModel
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
        val isUserLoggedIn = intent.extras?.getBoolean(IS_USER_LOGGED_IN)
        val navController = binding.loginNavHostFragment.findNavController()
        if (isUserLoggedIn != null
            && isUserLoggedIn
            && navController.currentDestination?.id == R.id.loginFragment)
            navController.navigate(R.id.action_loginFragment_to_biometricsFragment)
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    override fun onBackPressed() {}
}
