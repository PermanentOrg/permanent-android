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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_title_text_two_buttons.view.*
import kotlinx.android.synthetic.main.dialog_welcome.view.*
import org.permanent.permanent.*
import org.permanent.permanent.Constants.Companion.REQUEST_CODE_GOOGLE_API_AVAILABILITY
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivityMainBinding
import org.permanent.permanent.databinding.NavMainHeaderBinding
import org.permanent.permanent.databinding.NavSettingsHeaderBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.public.LocationSearchFragment
import org.permanent.permanent.ui.public.PublicFolderFragment
import org.permanent.permanent.ui.shares.RECORD_ID_TO_NAVIGATE_TO_KEY
import org.permanent.permanent.viewmodels.MainViewModel

class MainActivity : PermanentBaseActivity(), Toolbar.OnMenuItemClickListener {

    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var headerMainBinding: NavMainHeaderBinding
    private lateinit var headerSettingsBinding: NavSettingsHeaderBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration

    private val onArchiveSwitched = Observer<Void> {
        startWithCustomDestination(false)
    }

    private val onViewProfile = Observer<Void> {
        navController.navigateUp()
        navController.navigate(R.id.publicFragment)
        binding.drawerLayout.closeDrawers()
    }

    private val onLoggedOut = Observer<Void> {
        prefsHelper.saveUserLoggedIn(false)
        prefsHelper.saveBiometricsLogIn(true) // Setting back to default
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }

    private val onErrorMessage = Observer<String> { errorMessage ->
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private val onDestinationChangedListener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.editAboutFragment, R.id.editArchiveInformationFragment,
                R.id.onlinePresenceListFragment, R.id.milestonesListFragment -> {
                    toolbar?.menu?.findItem(R.id.settingsItem)?.isVisible = false
                    toolbar?.menu?.findItem(R.id.doneItem)?.isVisible = false
                }
                R.id.addEditOnlinePresenceFragment, R.id.editMilestoneFragment -> {
                    toolbar?.menu?.findItem(R.id.settingsItem)?.isVisible = false
                    toolbar?.menu?.findItem(R.id.plusItem)?.isVisible = false
                }
                R.id.publicFragment -> {
                    toolbar?.menu?.findItem(R.id.settingsItem)?.isVisible = true
                    toolbar?.menu?.findItem(R.id.plusItem)?.isVisible = false
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefsHelper = PreferencesHelper(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
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

        // NavController setupOnDestinationChangedListener
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener(onDestinationChangedListener)

        // Toolbar & ActionBar & AppBarConfiguration setup
        setSupportActionBar(binding.toolbar)
        val topLevelDestinations = setOf(
            R.id.archivesFragment,
            R.id.myFilesFragment,
            R.id.sharesFragment,
            R.id.membersFragment,
            R.id.activityFeedFragment,
            R.id.invitationsFragment,
            R.id.accountFragment,
            R.id.publicFilesFragment,
            R.id.publicFragment,
            R.id.publicGalleryFragment,
            R.id.securityFragment
        )
        appBarConfig = AppBarConfiguration(topLevelDestinations, binding.drawerLayout)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig)
        // Toolbar Settings menu click listener
        binding.toolbar.setOnMenuItemClickListener(this)

        // Custom start destination fragment from notification
        val intentExtras = intent.extras
        val startDestFragmentId = intentExtras?.getInt(START_DESTINATION_FRAGMENT_ID_KEY)
        if (startDestFragmentId != null && startDestFragmentId != 0) {
            val recipientArchiveNr = intentExtras.getString(RECIPIENT_ARCHIVE_NR_KEY)
            if (prefsHelper.getCurrentArchiveNr() != recipientArchiveNr) {
                showArchiveSwitchDialog(recipientArchiveNr)
                startWithCustomDestination(true)
            } else {
                startWithCustomDestination(false)
            }
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
                R.id.contactSupport -> {
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

        if (prefsHelper.isUserSignedUpInApp() && !prefsHelper.isWelcomeDialogSeen()) {
            showWelcomeDialog()
        }

        if (!isGooglePlayServicesAvailable(this))
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
    }

    private fun showArchiveSwitchDialog(recipientArchiveNr: String?) {
        val archiveName = intent.extras?.getString(RECIPIENT_ARCHIVE_NAME_KEY)
        val viewDialog: View = layoutInflater.inflate(R.layout.dialog_title_text_two_buttons, null)
        val alert = android.app.AlertDialog.Builder(this).setView(viewDialog).create()

        viewDialog.tvTitle.text = getString(R.string.dialog_switch_archive_title, archiveName)
        viewDialog.tvText.text = getString(R.string.dialog_switch_archive_text, archiveName)
        viewDialog.btnPositive.setOnClickListener {
            viewModel.switchCurrentArchiveTo(recipientArchiveNr)
            alert.dismiss()
        }
        viewDialog.btnNegative.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    private fun startWithCustomDestination(removeRecordId: Boolean) {
        val intentExtras = intent.extras
        val startDestFragmentId = intentExtras?.getInt(START_DESTINATION_FRAGMENT_ID_KEY)
        if (startDestFragmentId != null && startDestFragmentId != 0) {
            if (removeRecordId) intentExtras.remove(RECORD_ID_TO_NAVIGATE_TO_KEY)
            val navGraph = navController.graph
            navGraph.setStartDestination(startDestFragmentId)
            navController.setGraph(navGraph, intentExtras)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_settings, menu)
        return true
    }


    // On Toolbar menu item click
    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
        when (menuItem?.itemId) {
            R.id.moreItem, R.id.doneItem -> sendEventToFragment()
            else -> binding.drawerLayout.openDrawer(GravityCompat.END) // settings item
        }
        return true
    }

    private fun sendEventToFragment() {
        val currentFragment = supportFragmentManager.primaryNavigationFragment?.childFragmentManager
            ?.fragments?.first()
        if (currentFragment is PublicFolderFragment) currentFragment.onMoreItemClick()
        else if (currentFragment is LocationSearchFragment) currentFragment.onDoneItemClick()
    }

    // Toolbar back press
    override fun onSupportNavigateUp(): Boolean {
        return when (navController.currentDestination?.id) {
            R.id.linkSettingsFragment -> {
                navController.popBackStack(R.id.shareLinkFragment, true)
                true
            }
            R.id.publicFolderFragment -> {
                val publicFolderFragment =
                    supportFragmentManager.primaryNavigationFragment?.childFragmentManager
                        ?.fragments?.first() as PublicFolderFragment
                if (publicFolderFragment.onNavigateUp()) true
                else navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
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
                )?.show()
            }
            return false
        }
        return true
    }

    override fun connectViewModelEvents() {
        viewModel.getOnArchiveSwitched().observe(this, onArchiveSwitched)
        viewModel.getOnViewProfile().observe(this, onViewProfile)
        viewModel.getOnLoggedOut().observe(this, onLoggedOut)
        viewModel.getErrorMessage().observe(this, onErrorMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnArchiveSwitched().removeObserver(onArchiveSwitched)
        viewModel.getOnViewProfile().removeObserver(onViewProfile)
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