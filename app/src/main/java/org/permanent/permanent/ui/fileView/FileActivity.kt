package org.permanent.permanent.ui.fileView

import android.graphics.Color
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivityFileBinding
import org.permanent.permanent.network.models.FileData
import org.permanent.permanent.ui.activities.PermanentBaseActivity
import org.permanent.permanent.viewmodels.FileViewModel

class FileActivity : PermanentBaseActivity() {

    private lateinit var viewModel: FileViewModel
    private lateinit var binding: ActivityFileBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(FileViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_file)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        // ActionBar & appBarConfig setup
        setSupportActionBar(binding.fileToolbar)

        // NavController setup
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fileNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        val intentExtras = intent.extras
        navController.setGraph(R.navigation.file_navigation_graph, intentExtras)

        appBarConfig = AppBarConfiguration(navController.graph)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig)

        window.statusBarColor = Color.BLACK
        supportActionBar?.title = intentExtras
            ?.getParcelable<FileData>(PARCELABLE_FILE_DATA_KEY)?.displayName
    }

    // Toolbar back press
    override fun onSupportNavigateUp(): Boolean {
        this@FileActivity.finish()
        return true
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
        window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
    }
}