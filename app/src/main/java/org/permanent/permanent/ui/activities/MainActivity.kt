package org.permanent.permanent.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.dialog_welcome.view.*
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.START_DESTINATION_FRAGMENT_ID_KEY
import org.permanent.permanent.databinding.ActivityMainBinding
import org.permanent.permanent.databinding.NavMainHeaderBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.login.LoginActivity
import org.permanent.permanent.viewmodels.MainViewModel

class MainActivity : PermanentBaseActivity(), Toolbar.OnMenuItemClickListener {

    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var headerAccountBinding: NavMainHeaderBinding
    //    private lateinit var headerSettingsBinding: NavSettingsHeaderBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration

    private val onLoggedOut = Observer<Void> {
        prefsHelper.saveUserLoggedIn(false)
        prefsHelper.saveBiometricsLogIn(true) // Setting back to default
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    private val onErrorMessage = Observer<String> { errorMessage ->
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        // Drawer left and right headers binding
//        headerAccountBinding = NavMainHeaderBinding.bind(binding.accountNavView.getHeaderView(0))
//        headerAccountBinding.viewModel = viewModel
//        headerSettingsBinding = NavSettingsHeaderBinding.bind(binding.settingsNavigationView.getHeaderView(0))
//        headerSettingsBinding.viewModel = viewModel

        // NavController setup
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // ActionBar & appBarConfig setup
        setSupportActionBar(binding.toolbar)
        val topLevelDestinations = setOf(
            R.id.myFilesFragment,
            R.id.sharesFragment,
            R.id.membersFragment,
            R.id.activityFeedFragment,
            R.id.invitationsFragment,
            R.id.accountInfoFragment,
            R.id.securityFragment
        )
        appBarConfig = AppBarConfiguration(topLevelDestinations, binding.drawerLayout)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig)
//        binding.toolbar.inflateMenu(R.menu.menu_toolbar_settings)
//        binding.toolbar.setOnMenuItemClickListener(this)

        val intentExtras = intent.extras
        val startDestFragmentId = intentExtras?.getInt(START_DESTINATION_FRAGMENT_ID_KEY)
        if (startDestFragmentId != null && startDestFragmentId != 0) {
            val navGraph = navController.graph
            navGraph.startDestination = startDestFragmentId
            navController.setGraph(navGraph, intentExtras)
        }

        // NavViews setup
        binding.accountNavView.setupWithNavController(navController)
        binding.accountNavView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.logout -> viewModel.logout()
                R.id.storage -> {
                    binding.drawerLayout.closeDrawers()
                    navigateToAddStorage()
                }
                else -> {
                    menuItem.onNavDestinationSelected(navController)
                    binding.drawerLayout.closeDrawers()
                }
            }
            true
        }
//        binding.settingsNavigationView.setupWithNavController(navigationController)

        prefsHelper = PreferencesHelper(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
        if (!prefsHelper.isWelcomeDialogSeen()) {
            showWelcomeDialog()
        }

        if (!isGooglePlayServicesAvailable(this))
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
    }

    private fun navigateToAddStorage() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(Constants.URL_ADD_STORAGE)
        startActivity(intent)
    }

    // Toolbar back press
    override fun onSupportNavigateUp(): Boolean {
        return when(navController.currentDestination?.id) {
            R.id.manageLinkFragment -> {
                navController.popBackStack(R.id.shareLinkFragment, true)
                true
            }
            else -> navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
        }
    }

    // On settings icon click
    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
//        binding.mainActivityDrawerLayout.openDrawer(GravityCompat.END)
        return true
    }

    private fun showWelcomeDialog() {
        val viewDialog: View = layoutInflater.inflate(R.layout.dialog_welcome, null)

        val alert = AlertDialog.Builder(this)
            .setView(viewDialog)
            .create()

        viewDialog.ivBtnClose.setOnClickListener {
            prefsHelper.setWelcomeDialogSeen()
            alert.dismiss()
        }
        viewDialog.btnStartPreserving.setOnClickListener {
            prefsHelper.setWelcomeDialogSeen()
            alert.dismiss()
        }
        alert.show()
    }

    private fun isGooglePlayServicesAvailable(activity: Activity): Boolean {
        val googleApiAvailability: GoogleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show()
            }
            return false
        }
        return true
    }

    override fun connectViewModelEvents() {
        viewModel.getOnLoggedOut().observe(this, onLoggedOut)
        viewModel.getErrorMessage().observe(this, onErrorMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnLoggedOut().removeObserver(onLoggedOut)
        viewModel.getErrorMessage().removeObserver(onErrorMessage)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
        if (!isGooglePlayServicesAvailable(this))
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }
}