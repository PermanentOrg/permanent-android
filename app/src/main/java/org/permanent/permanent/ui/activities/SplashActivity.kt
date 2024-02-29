package org.permanent.permanent.ui.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.START_DESTINATION_FRAGMENT_ID_KEY
import org.permanent.permanent.databinding.ActivitySplashBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.archiveOnboarding.ArchiveOnboardingActivity
import org.permanent.permanent.ui.login.LoginActivity
import org.permanent.permanent.ui.onboarding.OnboardingActivity
import org.permanent.permanent.viewmodels.SplashViewModel

class SplashActivity : PermanentBaseActivity() {

    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var binding: ActivitySplashBinding
    private lateinit var viewModel: SplashViewModel

    private val errorObserver = Observer<String> {
        prefsHelper.saveUserLoggedIn(false)
        prefsHelper.saveDefaultArchiveId(0)
        prefsHelper.saveBiometricsLogIn(true) // Setting back to default

        startLoginActivity()

        Toast.makeText(
            this,
            it,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup orientation
        requestedOrientation = if (resources.getBoolean(R.bool.is_tablet)) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        viewModel = ViewModelProvider(this)[SplashViewModel::class.java]
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        createNotificationChannel()
        prefsHelper = PreferencesHelper(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

        // Clear deep links that weren't consumed
        prefsHelper.saveShareLinkUrlToken("")
        prefsHelper.saveDeepLinkArchiveNr("")
        val remoteConfig = setupRemoteConfig()

        remoteConfig.fetchAndActivate().addOnCompleteListener(this) {
            if (shouldUpdateApp(remoteConfig)) startUpdateAppActivity()
            else if (!prefsHelper.isOnboardingCompleted()) startOnboardingActivity()
            else if (!prefsHelper.isUserLoggedIn()) startLoginActivity()
            else if (prefsHelper.getDefaultArchiveId() == 0) startArchiveOnboardingActivity()
            else viewModel.switchArchiveToCurrent()
        }
    }

    private fun shouldUpdateApp(remoteConfig: FirebaseRemoteConfig): Boolean {
        var shouldUpdateApp = false
        val minimumVersionSplit = remoteConfig.getString(MINIMUM_APP_VERSION_KEY).split(".")
        val currentVersionSplit = BuildConfig.VERSION_NAME.split(".")

        val remoteMajor = minimumVersionSplit[0].toInt()
        val remoteMinor = minimumVersionSplit[1].toInt()
        val remotePatch = minimumVersionSplit[2].toInt()
        val currentMajor = currentVersionSplit[0].toInt()
        val currentMinor = currentVersionSplit[1].toInt()
        val currentPatch = currentVersionSplit[2].toInt()

        if (remoteMajor > currentMajor) {
            shouldUpdateApp = true
        } else if (remoteMajor == currentMajor && remoteMinor > currentMinor) {
            shouldUpdateApp = true
        } else if (remoteMinor == currentMinor && remotePatch > currentPatch) {
            shouldUpdateApp = true
        }

        return shouldUpdateApp
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

    private fun startOnboardingActivity() {
        startActivity(Intent(this@SplashActivity, OnboardingActivity::class.java))
        finish()
    }

    private fun startUpdateAppActivity() {
        startActivity(Intent(this@SplashActivity, UpdateAppActivity::class.java))
        finish()
    }

    private fun startArchiveOnboardingActivity() {
        prefsHelper.saveArchiveOnboardingDoneInApp(true)
        startActivity(Intent(this@SplashActivity, ArchiveOnboardingActivity::class.java))
        finish()
    }

    private fun startBiometricsFragment() {
        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
        intent.putExtra(START_DESTINATION_FRAGMENT_ID_KEY, R.id.biometricsFragment)
        startActivity(intent)
        finish()
    }

    private fun startLoginActivity() {
        startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
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