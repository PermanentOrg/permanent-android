package org.permanent.permanent.ui.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.dialog_welcome.view.*
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivityMainBinding
import org.permanent.permanent.databinding.NavMainHeaderBinding
import org.permanent.permanent.databinding.NavSettingsHeaderBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.viewmodels.MainViewModel

class MainActivity : PermanentBaseActivity(), Toolbar.OnMenuItemClickListener {

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var headerBinding: NavMainHeaderBinding
    private lateinit var headerSettingsBinding: NavSettingsHeaderBinding
    private lateinit var navigationController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

//        headerSettingsBinding = NavSettingsHeaderBinding.bind(binding.settingsNavigationView.getHeaderView(0))
//        headerSettingsBinding.viewModel = viewModel

        headerBinding = NavMainHeaderBinding.bind(binding.accountNavigationView.getHeaderView(0))
        headerBinding.viewModel = viewModel

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainActivityNavHostFragment) as NavHostFragment
        navigationController = navHostFragment.navController

        val topLevelDestination = setOf(
            R.id.myFilesFragment,
            R.id.sharesFragment
        )
        appBarConfiguration =
            AppBarConfiguration(topLevelDestination, binding.mainActivityDrawerLayout)

        binding.accountNavigationView.setupWithNavController(navigationController)
//        binding.settingsNavigationView.setupWithNavController(navigationController)
        binding.mainToolbar.setupWithNavController(navigationController, appBarConfiguration)
        setUpListeners()
//        binding.mainToolbar.inflateMenu(R.menu.menu_toolbar_settings)
//        binding.mainToolbar.setOnMenuItemClickListener(this)

        sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefsHelper = PreferencesHelper(sharedPrefs)

        if (!prefsHelper.isWelcomeDialogSeen()) {
            showWelcomeDialog()
        }
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

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        binding.mainActivityDrawerLayout.openDrawer(GravityCompat.END)
        return true
    }

    private fun setUpListeners(){
        navigationController.addOnDestinationChangedListener { _, _, _ ->
            binding.mainToolbar.setNavigationIcon(R.drawable.ic_account_circle_white)
        }
    }

    override fun connectViewModelEvents() {

    }

    override fun disconnectViewModelEvents() {

    }
}