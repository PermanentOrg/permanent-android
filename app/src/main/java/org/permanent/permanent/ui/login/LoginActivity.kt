package org.permanent.permanent.ui.login

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import org.permanent.permanent.R
import org.permanent.permanent.START_DESTINATION_FRAGMENT_ID_KEY
import org.permanent.permanent.databinding.ActivityLoginBinding
import org.permanent.permanent.ui.activities.PermanentBaseActivity

class LoginActivity : PermanentBaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        // NavController setup
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.loginNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        val intentExtras = intent.extras
        val startDestFragmentId = intentExtras?.getInt(START_DESTINATION_FRAGMENT_ID_KEY)
        if (startDestFragmentId != null && startDestFragmentId != 0) {
            startWithCustomDestination(startDestFragmentId)
        } else {
            navController.setGraph(R.navigation.login_navigation_graph, intent.extras)
        }
    }

    private fun startWithCustomDestination(startDestFragmentId: Int) {
        val navGraph = navController.graph
        navGraph.startDestination = startDestFragmentId
        navController.setGraph(navGraph, intent.extras)
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
