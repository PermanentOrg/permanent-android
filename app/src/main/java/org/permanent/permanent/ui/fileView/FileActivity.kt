package org.permanent.permanent.ui.fileView

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivityFileBinding
import org.permanent.permanent.models.FileSessionData
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.activities.PermanentBaseActivity
import org.permanent.permanent.ui.computeWindowSizeClasses

class FileActivity : PermanentBaseActivity() {

    private lateinit var binding: ActivityFileBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration
    private lateinit var prefsHelper: PreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Let content draw under system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_file)
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        prefsHelper = PreferencesHelper(getSharedPreferences(PREFS_NAME, MODE_PRIVATE))
        val windowWidthSizeClass = computeWindowSizeClasses().windowWidthSizeClass
        prefsHelper.saveWindowWidthSizeClass(windowWidthSizeClass)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fileRoot)) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

            view.setPadding(0, statusBarHeight, 0, navBarHeight)
            insets
        }

        // ActionBar & appBarConfig setup
        setSupportActionBar(binding.fileToolbar)

        // NavController setup
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fileNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        navController.setGraph(R.navigation.file_navigation_graph, intent.extras)

        appBarConfig = AppBarConfiguration(navController.graph)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig)

        setToolbarAndStatusBarColor(R.color.black)
    }

    // Toolbar back press
    override fun onSupportNavigateUp(): Boolean {
        return when(navController.currentDestination?.id) {
            R.id.filesContainerFragment, R.id.fileMetadataFragment -> {
                this@FileActivity.finish()
                true
            }
            R.id.shareLinkFragment -> {
                navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
                setToolbarAndStatusBarColor(R.color.black)
                true
            }
            else -> navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
        }
    }

    fun setToolbarAndStatusBarColor(colorId: Int) {
        binding.fileToolbar.setBackgroundColor(ContextCompat.getColor(this, colorId))
        window.statusBarColor = ContextCompat.getColor(this, colorId)
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

    override fun onDestroy() {
        super.onDestroy()
        FileSessionData.records = null
    }
}