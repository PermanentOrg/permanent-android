package org.permanent.permanent.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.dialog_welcome.view.*
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.Constants.Companion.REQUEST_CODE_GOOGLE_API_AVAILABILITY
import org.permanent.permanent.R
import org.permanent.permanent.START_DESTINATION_FRAGMENT_ID_KEY
import org.permanent.permanent.databinding.ActivityMainBinding
import org.permanent.permanent.databinding.NavMainHeaderBinding
import org.permanent.permanent.databinding.NavSettingsHeaderBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.login.LoginActivity
import org.permanent.permanent.viewmodels.MainViewModel

class MainActivity : PermanentBaseActivity(), Toolbar.OnMenuItemClickListener {

    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var headerMainBinding: NavMainHeaderBinding
    private lateinit var headerSettingsBinding: NavSettingsHeaderBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration

    private val onManageArchives = Observer<Void> {
        navController.navigateUp()
        navController.navigate(R.id.archivesFragment)
        binding.drawerLayout.closeDrawers()
    }

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
        // MainActivity binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        // Left drawer header binding
        headerMainBinding =
            NavMainHeaderBinding.bind(binding.mainNavigationView.getHeaderView(0))
        headerMainBinding.executePendingBindings()
        headerMainBinding.lifecycleOwner = this
        headerMainBinding.viewModel = viewModel

        // Right drawer header binding
        headerSettingsBinding =
            NavSettingsHeaderBinding.bind(binding.settingsNavigationView.getHeaderView(0))
        headerSettingsBinding.executePendingBindings()
        headerSettingsBinding.lifecycleOwner = this
        headerSettingsBinding.viewModel = viewModel

        // NavController setup
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // Toolbar & ActionBar & AppBarConfiguration setup
        setSupportActionBar(binding.toolbar)
        val topLevelDestinations = setOf(
            R.id.archivesFragment,
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
        // Toolbar Settings menu click listener
        binding.toolbar.setOnMenuItemClickListener(this)

        // Custom start destination fragment
        val intentExtras = intent.extras
        val startDestFragmentId = intentExtras?.getInt(START_DESTINATION_FRAGMENT_ID_KEY)
        if (startDestFragmentId != null && startDestFragmentId != 0) {
            val navGraph = navController.graph
            navGraph.startDestination = startDestFragmentId
            navController.setGraph(navGraph, intentExtras)
        }

        // NavViews setup
        binding.mainNavigationView.setupWithNavController(navController)
        binding.settingsNavigationView.setupWithNavController(navController)
        binding.settingsNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.storage -> {
                    binding.drawerLayout.closeDrawers()
                    navigateOnWebTo(BuildConfig.ADD_STORAGE_URL)
                    // Returning 'false' to not remain the item selected on resuming
                    return@setNavigationItemSelectedListener false
                }
                R.id.help -> {
                    binding.drawerLayout.closeDrawers()
                    navigateOnWebTo(BuildConfig.HELP_URL)
                    // Returning 'false' to not remain the item selected on resuming
                    return@setNavigationItemSelectedListener false
                }
                R.id.logOut -> viewModel.deleteDeviceToken()
                else -> {
                    menuItem.onNavDestinationSelected(navController)
                    binding.drawerLayout.closeDrawers()
                }
            }
            true
        }
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
                if (drawerView.id == binding.mainNavigationView.id) {
                    viewModel.updateCurrentArchiveHeader()
                } else if (drawerView.id == binding.settingsNavigationView.id) {
                    viewModel.updateUsedStorage()
                }
            }

            override fun onDrawerClosed(drawerView: View) {
            }

            override fun onDrawerStateChanged(newState: Int) {
            }
        })

        prefsHelper = PreferencesHelper(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
        if (prefsHelper.isUserSignedUpInApp() && !prefsHelper.isWelcomeDialogSeen()) {
            showWelcomeDialog()
        }

        if (!isGooglePlayServicesAvailable(this))
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_settings, menu)
        return true
    }

    // On Settings menu click
    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
        binding.drawerLayout.openDrawer(GravityCompat.END)
        return true
    }

    // Toolbar back press
    override fun onSupportNavigateUp(): Boolean {
        return when (navController.currentDestination?.id) {
            R.id.manageLinkFragment -> {
                navController.popBackStack(R.id.shareLinkFragment, true)
                true
            }
            else -> navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
        }
    }

    private fun navigateOnWebTo(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun showWelcomeDialog() {
        val viewDialog: View = layoutInflater.inflate(R.layout.dialog_welcome, null)

        val alert = AlertDialog.Builder(this)
            .setView(viewDialog)
            .create()

        viewDialog.tvWelcomeTitle.text =
            getString(R.string.welcome_title, prefsHelper.getCurrentArchiveFullName())
        viewDialog.ivBtnClose.setOnClickListener {
            prefsHelper.saveWelcomeDialogSeen()
            alert.dismiss()
        }
        viewDialog.btnStartPreserving.setOnClickListener {
            prefsHelper.saveWelcomeDialogSeen()
            alert.dismiss()
        }
        alert.show()
    }

    private fun isGooglePlayServicesAvailable(activity: Activity): Boolean {
        val googleApiAvailability: GoogleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(
                    activity,
                    status,
                    REQUEST_CODE_GOOGLE_API_AVAILABILITY
                ).show()
            }
            return false
        }
        return true
    }

    override fun connectViewModelEvents() {
        viewModel.getOnManageArchives().observe(this, onManageArchives)
        viewModel.getOnLoggedOut().observe(this, onLoggedOut)
        viewModel.getErrorMessage().observe(this, onErrorMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnManageArchives().removeObserver(onManageArchives)
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