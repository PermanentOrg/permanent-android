package org.permanent.permanent.ui.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivitySplashBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.archiveOnboarding.ArchiveOnboardingActivity
import org.permanent.permanent.ui.computeWindowSizeClasses
import org.permanent.permanent.ui.login.AuthenticationActivity
import org.permanent.permanent.ui.login.START_DESTINATION_PAGE_VALUE_KEY
import org.permanent.permanent.ui.login.compose.AuthPage
import org.permanent.permanent.viewmodels.SplashViewModel

class SplashActivity : PermanentBaseActivity() {

    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var binding: ActivitySplashBinding
    private lateinit var viewModel: SplashViewModel
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var remoteConfigFetch: Task<Boolean>
    private var isRouted = false

    private val errorObserver = Observer<String> {
        prefsHelper.saveUserLoggedIn(false)
        prefsHelper.saveDefaultArchiveId(0)
        prefsHelper.setBiometricsLogIn(true)

        startLoginActivity()

        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup orientation
        requestedOrientation = if (resources.getBoolean(R.bool.is_tablet)) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        prefsHelper = PreferencesHelper(getSharedPreferences(PREFS_NAME, MODE_PRIVATE))
        val windowWidthSizeClass = computeWindowSizeClasses().windowWidthSizeClass
        prefsHelper.saveWindowWidthSizeClass(windowWidthSizeClass)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        viewModel = ViewModelProvider(this)[SplashViewModel::class.java]
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        createNotificationChannel()

        // Clear deep links that weren't consumed
        prefsHelper.saveShareLinkUrlToken("")
        prefsHelper.saveDeepLinkArchiveNr("")
        remoteConfig = setupRemoteConfig()
        remoteConfigFetch = remoteConfig.fetchAndActivate()
    }

    override fun onStart() {
        super.onStart()
        // Activity-scoped listeners are dropped in onStop; a finished task calls a new one at once
        remoteConfigFetch.addOnCompleteListener(this) { routeAfterRemoteConfig() }
    }

    private fun routeAfterRemoteConfig() {
        if (isRouted) return
        isRouted = true
        if (shouldUpdateApp(remoteConfig)) startUpdateAppActivity()
        else if (!prefsHelper.isUserLoggedIn()) startLoginActivity()
        else if (prefsHelper.getDefaultArchiveId() == 0) startArchiveOnboardingActivity()
        else viewModel.switchArchiveToCurrent()
    }

    private fun shouldUpdateApp(remoteConfig: FirebaseRemoteConfig): Boolean {
        // Empty until the async defaults land, so a fast fetch can return ""
        val minimumVersion = parseVersion(remoteConfig.getString(MINIMUM_APP_VERSION_KEY))
        val currentVersion = parseVersion(BuildConfig.VERSION_NAME)
        if (minimumVersion == null || currentVersion == null) return false

        for (i in 0..2) {
            if (minimumVersion[i] != currentVersion[i]) return minimumVersion[i] > currentVersion[i]
        }
        return false
    }

    private fun parseVersion(version: String): List<Int>? {
        val parts = version.split(".").map { it.toIntOrNull() ?: return null }
        return if (parts.size >= 3) parts else null
    }

    private fun setupRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = Firebase.remoteConfig

        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        // FOR DEVELOPMENT PURPOSE ONLY
        // The default minimum fetch interval is 12 hours
//        val configSettings = remoteConfigSettings {
//            minimumFetchIntervalInSeconds = 30
//        }
//        remoteConfig.setConfigSettingsAsync(configSettings)
        return remoteConfig
    }

    private val onArchiveSwitchedToCurrentObserver = Observer<Void?> {
        startBiometricsFragment()
    }

    private fun startUpdateAppActivity() {
        startActivity(Intent(this@SplashActivity, UpdateAppActivity::class.java))
        finish()
    }

    private fun startArchiveOnboardingActivity() {
        startActivity(Intent(this@SplashActivity, ArchiveOnboardingActivity::class.java))
        finish()
    }

    private fun startBiometricsFragment() {
        val intent = Intent(this@SplashActivity, AuthenticationActivity::class.java)
        intent.putExtra(START_DESTINATION_PAGE_VALUE_KEY, AuthPage.BIOMETRICS.value)
        startActivity(intent)
        finish()
    }

    private fun startLoginActivity() {
        startActivity(Intent(this@SplashActivity, AuthenticationActivity::class.java))
        finish()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            getString(R.string.default_notification_channel_id),
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )
    }

    override fun connectViewModelEvents() {
        viewModel.getOnArchiveSwitchedToCurrent().observe(this, onArchiveSwitchedToCurrentObserver)
        viewModel.getShowError().observe(this, errorObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnArchiveSwitchedToCurrent().removeObserver(onArchiveSwitchedToCurrentObserver)
        viewModel.getShowError().removeObserver(errorObserver)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }

    companion object {
        private const val MINIMUM_APP_VERSION_KEY = "min_app_version_android"
    }
}