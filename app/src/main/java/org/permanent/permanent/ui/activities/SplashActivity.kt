package org.permanent.permanent.ui.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivitySplashBinding
import org.permanent.permanent.network.AuthStateManager
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.archiveOnboarding.ArchiveOnboardingActivity
import org.permanent.permanent.ui.login.BiometricsActivity
import org.permanent.permanent.ui.onboarding.OnboardingActivity
import org.permanent.permanent.viewmodels.SplashViewModel

class SplashActivity : PermanentBaseActivity() {

    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var binding: ActivitySplashBinding
    private lateinit var viewModel: SplashViewModel

    private val errorObserver = Observer<String> {
        val snackBar = Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.deepRed))
        snackBar.setTextColor(ContextCompat.getColor(this, R.color.white))
        snackBar.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        viewModel = ViewModelProvider(this)[SplashViewModel::class.java]
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        createNotificationChannel()
        prefsHelper = PreferencesHelper(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

        val authResponse = AuthorizationResponse.fromIntent(intent)
        val authException = AuthorizationException.fromIntent(intent)

        when {
            authResponse != null -> {
                AuthStateManager.getInstance(this)
                    .updateAfterAuthorization(authResponse, authException)
                viewModel.requestTokens(authResponse)
            }
            authException != null -> {
                AuthStateManager.getInstance(this)
                    .updateAfterAuthorization(authResponse, authException)
                errorObserver.onChanged(authException.errorDescription)
            }
            else -> {
                // Clear deep links that weren't consumed
                prefsHelper.saveShareLinkUrlToken("")
                prefsHelper.saveDeepLinkArchiveNr("")
                val remoteConfig = setupRemoteConfig()

                remoteConfig.fetchAndActivate().addOnCompleteListener(this) {
                    if (shouldUpdateApp(remoteConfig)) startUpdateAppActivity()
                    else if (!prefsHelper.isOnboardingCompleted()) startOnboardingActivity()
                    else if (prefsHelper.isUserSignedUpInApp() && !prefsHelper.isArchiveOnboardingSeen()) startArchiveOnboardingActivity()
                    else if (prefsHelper.isUserLoggedIn()) startBiometricsActivity()
                    else startSignUpActivity()
                }
            }
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

    private val userJustLoggedInObserver = Observer<Void> {
        if (prefsHelper.isUserSignedUpInApp() && !prefsHelper.isArchiveOnboardingSeen()) {
            startArchiveOnboardingActivity()
        } else {
            startMainActivity()
        }
    }

    private fun startOnboardingActivity() {
        startActivity(Intent(this@SplashActivity, OnboardingActivity::class.java))
        finish()
    }

    private fun startUpdateAppActivity() {
        startActivity(Intent(this@SplashActivity, UpdateAppActivity::class.java))
        finish()
    }

    private fun startMainActivity() {
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        finish()
    }

    private fun startArchiveOnboardingActivity() {
        startActivity(Intent(this@SplashActivity, ArchiveOnboardingActivity::class.java))
        finish()
    }

    private fun startBiometricsActivity() {
        startActivity(Intent(this@SplashActivity, BiometricsActivity::class.java))
        finish()
    }

    private fun startSignUpActivity() {
        startActivity(Intent(this@SplashActivity, SignUpActivity::class.java))
        finish()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            getString(R.string.default_notification_channel_id),
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    override fun connectViewModelEvents() {
        viewModel.getOnUserLoggedIn().observe(this, userJustLoggedInObserver)
        viewModel.getShowError().observe(this, errorObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnUserLoggedIn().removeObserver(userJustLoggedInObserver)
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