package org.permanent.permanent.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import org.permanent.permanent.Constants.Companion.REQUEST_CODE_GOOGLE_API_AVAILABILITY
import org.permanent.permanent.R
import org.permanent.permanent.RECIPIENT_ARCHIVE_NAME_KEY
import org.permanent.permanent.RECIPIENT_ARCHIVE_NR_KEY
import org.permanent.permanent.START_DESTINATION_FRAGMENT_ID_KEY
import org.permanent.permanent.databinding.ActivityMainBinding
import org.permanent.permanent.databinding.DialogTitleTextTwoButtonsBinding
import org.permanent.permanent.databinding.NavMainHeaderBinding
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.AccountEventAction
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.archives.PARCELABLE_ARCHIVE_KEY
import org.permanent.permanent.ui.computeWindowSizeClasses
import org.permanent.permanent.ui.login.AuthenticationActivity
import org.permanent.permanent.ui.public.LocationSearchFragment
import org.permanent.permanent.ui.public.PublicFolderFragment
import org.permanent.permanent.ui.settings.compose.SettingsMenuScreen
import org.permanent.permanent.ui.shares.RECORD_ID_TO_NAVIGATE_TO_KEY
import org.permanent.permanent.viewmodels.MainViewModel
import org.permanent.permanent.viewmodels.SettingsMenuViewModel


class MainActivity : PermanentBaseActivity(), Toolbar.OnMenuItemClickListener {

    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var viewModel: MainViewModel
    private lateinit var settingsMenuViewModel: SettingsMenuViewModel
    lateinit var binding: ActivityMainBinding
    private lateinit var headerMainBinding: NavMainHeaderBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration
    private var isSubmenuVisible = false

    private val onArchiveSwitched = Observer<Void?> {
        startWithCustomDestination(false)
    }

    private val onViewProfile = Observer<Void?> {
        navController.navigateUp()
        navController.navigate(R.id.publicFragment)
        binding.drawerLayout.closeDrawers()
    }

    private val onErrorMessage = Observer<String> { errorMessage ->
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private val onDestinationChangedListener =
        NavController.OnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.editAboutFragment, R.id.editArchiveInformationFragment, R.id.onlinePresenceListFragment, R.id.milestoneListFragment -> {
                    binding.toolbar.menu?.findItem(R.id.settingsItem)?.isVisible = false
                    binding.toolbar.menu?.findItem(R.id.doneItem)?.isVisible = false
                }

                R.id.addEditOnlinePresenceFragment, R.id.addEditMilestoneFragment -> {
                    binding.toolbar.menu?.findItem(R.id.settingsItem)?.isVisible = false
                    binding.toolbar.menu?.findItem(R.id.plusItem)?.isVisible = false
                    binding.toolbar.menu?.findItem(R.id.doneItem)?.isVisible = false
                }

                R.id.publicFragment -> {
                    binding.toolbar.menu?.findItem(R.id.settingsItem)?.isVisible = true
                    binding.toolbar.menu?.findItem(R.id.plusItem)?.isVisible = false
                    binding.toolbar.menu?.findItem(R.id.moreItem)?.isVisible = false
                }

                R.id.publicFolderFragment -> {
                    binding.toolbar.menu?.findItem(R.id.settingsItem)?.isVisible = false
                    binding.toolbar.menu?.findItem(R.id.moreItem)?.isVisible = true
                }

                R.id.legacyLoadingFragment, R.id.storageMenuFragment, R.id.addStorageFragment, R.id.giftStorageFragment, R.id.redeemCodeFragment, R.id.loginAndSecurityFragment, R.id.changePasswordFragment, R.id.twoStepVerificationFragment -> {
                    binding.toolbar.menu?.findItem(R.id.settingsItem)?.isVisible = false
                }

                R.id.introFragment, R.id.statusFragment, R.id.legacyContactFragment, R.id.archiveStewardFragment -> {
                    binding.toolbar.menu?.findItem(R.id.settingsItem)?.isVisible = false
                    binding.toolbar.menu?.findItem(R.id.closeItem)?.isVisible = true
                }

                else -> {
                    binding.toolbar.menu?.findItem(R.id.settingsItem)?.isVisible = true
                    binding.toolbar.menu?.findItem(R.id.closeItem)?.isVisible = false
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup orientation
        requestedOrientation = if (resources.getBoolean(R.bool.is_tablet)) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        prefsHelper = PreferencesHelper(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
        val windowWidthSizeClass = computeWindowSizeClasses().windowWidthSizeClass
        prefsHelper.saveWindowWidthSizeClass(windowWidthSizeClass)

        // MainActivity binding
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        settingsMenuViewModel = ViewModelProvider(this)[SettingsMenuViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        // Left drawer header binding
        headerMainBinding = NavMainHeaderBinding.bind(binding.mainNavigationView.getHeaderView(0))
        headerMainBinding.executePendingBindings()
        headerMainBinding.lifecycleOwner = this
        headerMainBinding.viewModel = viewModel

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
            R.id.archiveSettings,
            R.id.manageTagsFragment,
            R.id.membersFragment,
            R.id.activityFeedFragment,
            R.id.invitationsFragment,
            R.id.accountFragment,
            R.id.publicFilesFragment,
            R.id.publicGalleryFragment
        )
        appBarConfig = AppBarConfiguration(topLevelDestinations, binding.drawerLayout)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig)

        // Toolbar Settings menu click listener
        binding.toolbar.setOnMenuItemClickListener(this)

        // NavViews setup
        binding.mainNavigationView.setupWithNavController(navController)
        binding.mainNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.archiveSettings -> {
                    isSubmenuVisible = !isSubmenuVisible
                    setSubmenuVisibility(isSubmenuVisible)
                    setArchiveSettingsIcon(menuItem, isSubmenuVisible)
                    true
                }

                R.id.archiveStewardFragment -> {
                    val bundle = bundleOf(PARCELABLE_ARCHIVE_KEY to viewModel.getCurrentArchive())
                    navController.navigate(R.id.archiveStewardFragment, bundle)
                    binding.drawerLayout.closeDrawers()
                }
                // Handle other menu items here, if necessary
                else -> {
                    menuItem.onNavDestinationSelected(navController)
                    binding.drawerLayout.closeDrawers()
                }
            }
            true
        }

        // Archive menu setup
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
                if (drawerView.id == binding.mainNavigationView.id) {
                    viewModel.updateCurrentArchiveHeader()
                    viewModel.sendEvent(
                        AccountEventAction.OPEN_ARCHIVE_MENU, mapOf("page" to "Archive Menu")
                    )
                }
            }

            override fun onDrawerClosed(drawerView: View) {
            }

            override fun onDrawerStateChanged(newState: Int) {
            }
        })

        // Account menu setup
        binding.bottomSheetComposeView.setContent {
            val showBottomSheet by settingsMenuViewModel.showBottomSheet.observeAsState(false)

            MaterialTheme {
                if (showBottomSheet) {
                    SettingsMenuScreen(
                        viewModel = settingsMenuViewModel,
                        onDismiss = { settingsMenuViewModel.closeAccountMenuSheet() },
                        onAccountClick = {
                            navController.navigate(R.id.accountFragment)
                            settingsMenuViewModel.closeAccountMenuSheet()
                        },
                        onStorageClick = {
                            navController.navigate(R.id.storageMenuFragment)
                            settingsMenuViewModel.closeAccountMenuSheet()
                        },
                        onMyArchivesClick = {
                            navController.navigate(R.id.archivesFragment)
                            settingsMenuViewModel.closeAccountMenuSheet()
                        },
                        onInvitationsClick = {
                            navController.navigate(R.id.invitationsFragment)
                            settingsMenuViewModel.closeAccountMenuSheet()
                        },
                        onActivityFeedClick = {
                            navController.navigate(R.id.activityFeedFragment)
                            settingsMenuViewModel.closeAccountMenuSheet()
                        },
                        onLoginAndSecurityClick = {
                            navController.navigate(R.id.loginAndSecurityFragment)
                            settingsMenuViewModel.closeAccountMenuSheet()
                        },
                        onLegacyPlanningClick = {
                            navController.navigate(R.id.legacyLoadingFragment)
                            settingsMenuViewModel.closeAccountMenuSheet()
                        },
                        onSignOutClick = {
                            settingsMenuViewModel.deleteDeviceToken()
                            settingsMenuViewModel.closeAccountMenuSheet()
                        }
                    )
                }
            }

            // Sync visibility with state
            LaunchedEffect(showBottomSheet) {
                binding.bottomSheetComposeView.visibility = if (showBottomSheet) View.VISIBLE else View.GONE
            }
        }

        // Intent handling
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                handleSendFile(intent) // Handle single file being sent
            }

            Intent.ACTION_SEND_MULTIPLE -> {
                handleSendMultipleFiles(intent) // Handle multiple files being sent
            }

            else -> {
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
            }
        }

        if (!isGooglePlayServicesAvailable(this)) GoogleApiAvailability.getInstance()
            .makeGooglePlayServicesAvailable(this)
    }

    private fun setSubmenuVisibility(visible: Boolean) {
        val manageTagsItem = binding.mainNavigationView.menu.findItem(R.id.manageTagsFragment)
        val manageMembersItem = binding.mainNavigationView.menu.findItem(R.id.membersFragment)
        val legacyPlanning = binding.mainNavigationView.menu.findItem(R.id.archiveStewardFragment)

        manageTagsItem.isVisible = visible
        manageMembersItem.isVisible = visible
        legacyPlanning.isVisible =
            visible && viewModel.getCurrentArchive().accessRole == AccessRole.OWNER
    }

    private fun setArchiveSettingsIcon(menuItem: MenuItem, submenuVisible: Boolean) {
        val archiveSettingsRightIcon =
            menuItem.actionView?.findViewById<ImageView>(R.id.ivRightIcon)
        val icon =
            if (submenuVisible) R.drawable.ic_arrow_drop_up_white else R.drawable.ic_arrow_drop_down_white
        archiveSettingsRightIcon?.setImageResource(icon)
    }

    private fun handleSendFile(intent: Intent) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
            startDestinationWithArgs(arrayListOf(it))
        }
    }

    private fun handleSendMultipleFiles(intent: Intent) {
        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let {
            startDestinationWithArgs(it)
        }
    }

    private fun startDestinationWithArgs(it: List<Parcelable>) {
        val bundle = bundleOf(SAVE_TO_PERMANENT_FILE_URIS_KEY to it)
        navController.setGraph(navController.graph, bundle)
    }

    private fun startWithCustomDestination(removeRecordId: Boolean) {
        val intentExtras = intent.extras
        val startDestFragmentId = intentExtras?.getInt(START_DESTINATION_FRAGMENT_ID_KEY)
        if (startDestFragmentId != null && startDestFragmentId != 0) {
            if (removeRecordId) intentExtras.remove(RECORD_ID_TO_NAVIGATE_TO_KEY)
            val navGraph = navController.graph
            navGraph.startDestination = startDestFragmentId
            navController.setGraph(navGraph, intentExtras)
        }
    }

    private fun showArchiveSwitchDialog(recipientArchiveNr: String?) {
        val archiveName = intent.extras?.getString(RECIPIENT_ARCHIVE_NAME_KEY)
        val dialogBinding: DialogTitleTextTwoButtonsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this), R.layout.dialog_title_text_two_buttons, null, false
        )
        val alert = android.app.AlertDialog.Builder(this).setView(dialogBinding.root).create()

        dialogBinding.tvTitle.text = getString(R.string.dialog_switch_archive_title, archiveName)
        dialogBinding.tvText.text = getString(R.string.dialog_switch_archive_text, archiveName)
        dialogBinding.btnPositive.setOnClickListener {
            viewModel.switchCurrentArchiveTo(recipientArchiveNr)
            alert.dismiss()
        }
        dialogBinding.btnNegative.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_settings, menu)
        return true
    }


    // On Toolbar menu item click
    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
        when (menuItem?.itemId) {
            R.id.moreItem, R.id.doneItem -> sendEventToFragment()
            R.id.closeItem -> navController.navigate(R.id.myFilesFragment)
            else -> settingsMenuViewModel.openAccountMenuSheet()
        }
        return true
    }

    private fun sendEventToFragment() {
        val currentFragment =
            supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.fragments?.first()
        if (currentFragment is PublicFolderFragment) currentFragment.onMoreItemClick()
        else if (currentFragment is LocationSearchFragment) currentFragment.onDoneItemClick()
    }

    // Toolbar back press
    override fun onSupportNavigateUp(): Boolean {
        return when (navController.currentDestination?.id) {
            R.id.publicFolderFragment -> {
                val publicFolderFragment =
                    supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.fragments?.first() as PublicFolderFragment
                if (publicFolderFragment.onNavigateUp()) true
                else navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
            }

            else -> navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
        }
    }

    private fun isGooglePlayServicesAvailable(activity: Activity): Boolean {
        val googleApiAvailability: GoogleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(
                    activity, status, REQUEST_CODE_GOOGLE_API_AVAILABILITY
                )?.show()
            }
            return false
        }
        return true
    }

    private val onLoggedOut = Observer<Void?> {
        startActivity(Intent(this, AuthenticationActivity::class.java))
        this.finish()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnArchiveSwitched().observe(this, onArchiveSwitched)
        viewModel.getOnViewProfile().observe(this, onViewProfile)
        viewModel.getErrorMessage().observe(this, onErrorMessage)
        settingsMenuViewModel.getOnLoggedOut().observe(this, onLoggedOut)
        settingsMenuViewModel.getErrorMessage().observe(this, onErrorMessage)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnArchiveSwitched().removeObserver(onArchiveSwitched)
        viewModel.getOnViewProfile().removeObserver(onViewProfile)
        viewModel.getErrorMessage().removeObserver(onErrorMessage)
        settingsMenuViewModel.getOnLoggedOut().removeObserver(onLoggedOut)
        settingsMenuViewModel.getErrorMessage().removeObserver(onErrorMessage)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
        if (!isGooglePlayServicesAvailable(this)) GoogleApiAvailability.getInstance()
            .makeGooglePlayServicesAvailable(this)
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    companion object {
        const val SAVE_TO_PERMANENT_FILE_URIS_KEY = "save_to_permanent_file_uris_key"
        const val SPACE_TOTAL_KEY = "space_total_key"
        const val SPACE_LEFT_KEY = "space_left_key"
        const val SPACE_USED_PERCENTAGE_KEY = "space_used_percentage_key"
    }
}