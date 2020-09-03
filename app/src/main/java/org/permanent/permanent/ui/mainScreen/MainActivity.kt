package org.permanent.permanent.ui.mainScreen

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import org.permanent.R
import org.permanent.databinding.ActivityMainBinding
import org.permanent.databinding.NavHeaderBinding
import org.permanent.databinding.NavSettingsHeaderBinding
import org.permanent.permanent.ui.PermanentBaseActivity
import org.permanent.permanent.viewmodels.MainActivityViewModel

class MainActivity : PermanentBaseActivity(),Toolbar.OnMenuItemClickListener {

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var headerBinding: NavHeaderBinding
    private lateinit var headerSettingsBinding: NavSettingsHeaderBinding
    private lateinit var navigationController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        headerSettingsBinding = NavSettingsHeaderBinding.bind(binding.mainActivitySettingNavigationView.getHeaderView(0))
        headerSettingsBinding.viewModel = viewModel

        headerBinding = NavHeaderBinding.bind(binding.mainActivityNavigationView.getHeaderView(0))
        headerBinding.viewModel = viewModel

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainActivityNavHostFragment) as NavHostFragment
        navigationController = navHostFragment.navController

        val topLevelDestination = setOf(
            R.id.mainFragment,
            R.id.secondaryFragment
        )
        appBarConfiguration =
            AppBarConfiguration(topLevelDestination, binding.mainActivityDrawerLayout)

        binding.mainActivityNavigationView.setupWithNavController(navigationController)
        binding.mainActivitySettingNavigationView.setupWithNavController(navigationController)
        binding.mainToolbar.setupWithNavController(navigationController, appBarConfiguration)
        setUpListeners()
        binding.mainToolbar.inflateMenu(R.menu.menu_profile_settings)
        binding.mainToolbar.setOnMenuItemClickListener(this)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        binding.mainActivityDrawerLayout.openDrawer(GravityCompat.END)
        return true
    }

    private fun setUpListeners(){
        navigationController.addOnDestinationChangedListener { _, _, _ ->
            binding.mainToolbar.setNavigationIcon(R.drawable.ic_baseline_camera_24)
        }
    }

    override fun connectViewModelEvents() {

    }

    override fun disconnectViewModelEvents() {

    }

}