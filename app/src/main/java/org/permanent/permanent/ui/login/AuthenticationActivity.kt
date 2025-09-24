package org.permanent.permanent.ui.login

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivityAuthenticationBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.activities.PermanentBaseActivity
import org.permanent.permanent.ui.computeWindowSizeClasses

class AuthenticationActivity : PermanentBaseActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
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

        // Allow content to draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Required flag for drawing system bar backgrounds
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        // Apply insets manually
        val rootView = findViewById<View>(R.id.rootLayout)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
            insets
        }

        // NavController setup
        val startPage = intent.getIntExtra(START_DESTINATION_PAGE_VALUE_KEY, -1)
        val args = bundleOf(START_DESTINATION_PAGE_VALUE_KEY to startPage)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.authenticationNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        navController.setGraph(R.navigation.authentication_navigation_graph, args)
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
