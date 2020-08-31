package org.permanent.permanent.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import org.permanent.R
import org.permanent.databinding.ActivityMainBinding

class MainActivity : PermanentBaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navigationController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainActivityNavHostFragment) as NavHostFragment
        navigationController = navHostFragment.navController
        binding.mainActivityNavigationView.setupWithNavController(navigationController)

        val topLevelDestination = setOf(
            R.id.mainFragment,
            R.id.secondaryFragment
        )
        appBarConfiguration =
            AppBarConfiguration(topLevelDestination, binding.mainActivityDrawerLayout)

        binding.mainToolbar.setupWithNavController(navigationController, appBarConfiguration)
    }

    override fun connectViewModelEvents() {
        TODO("Not yet implemented")
    }

    override fun disconnectViewModelEvents() {
        TODO("Not yet implemented")
    }
}