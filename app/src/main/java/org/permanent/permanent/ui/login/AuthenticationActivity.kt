package org.permanent.permanent.ui.login

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivityLoginBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.activities.PermanentBaseActivity
import org.permanent.permanent.ui.computeWindowSizeClasses
import org.permanent.permanent.ui.login.compose.AuthPage

class AuthenticationActivity : PermanentBaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var navController: NavController
    private lateinit var prefsHelper: PreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup orientation
        requestedOrientation = if (resources.getBoolean(R.bool.is_tablet)) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        prefsHelper = PreferencesHelper(getSharedPreferences(PREFS_NAME, MODE_PRIVATE))
        val windowWidthSizeClass = computeWindowSizeClasses().windowWidthSizeClass
        prefsHelper.saveWindowWidthSizeClass(windowWidthSizeClass)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        // NavController setup
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.authenticationNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        val intentExtras = intent.extras
        val startDestPageVal = intentExtras?.getInt(START_DESTINATION_PAGE_VALUE_KEY)
        if (startDestPageVal != null && startDestPageVal != 0 && startDestPageVal == AuthPage.BIOMETRICS.value) {
            val navGraph = navController.graph
            navGraph.startDestination = R.id.biometricsFragment
            navController.setGraph(navGraph, intent.extras)
        } else {
            navController.setGraph(R.navigation.authentication_navigation_graph, intent.extras)
        }
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    override fun onBackPressed() {}
}
