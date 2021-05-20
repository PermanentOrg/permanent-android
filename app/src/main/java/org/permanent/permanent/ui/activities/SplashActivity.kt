package org.permanent.permanent.ui.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivitySplashBinding
import org.permanent.permanent.ui.IS_USER_LOGGED_IN
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.login.LoginActivity
import org.permanent.permanent.ui.onboarding.OnboardingActivity
import org.permanent.permanent.viewmodels.SplashViewModel

class SplashActivity : PermanentBaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        viewModel = ViewModelProvider(this).get(SplashViewModel::class.java)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

//        getFCMDeviceToken()
//        val keys = intent.extras?.keySet()
//        if (!keys.isNullOrEmpty()) {
//            keys.forEach { key ->
//                when (intent.extras!![key]) {
//                    is Long -> Log.e("SplashActivity","$key = ${intent.getLongExtra(key, 0L)}")
//                    is Int -> Log.e("SplashActivity","$key = ${intent.getIntExtra(key, 0)}")
//                    is String -> Log.e("SplashActivity","$key = ${intent.getStringExtra(key)}")
//                    is Boolean -> Log.e("SplashActivity","$key = ${intent.getBooleanExtra(key, false)}")
//                    else -> println("unknown Type")
//                }
//            }
//        }
        createNotificationChannel()
        viewModel.verifyIsUserLoggedIn()
    }

    private val loggedInResponseObserver = Observer<Boolean> { isLoggedIn ->
        val prefsHelper = PreferencesHelper(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

        prefsHelper.saveUserLoggedIn(isLoggedIn)

        if (!isLoggedIn && !prefsHelper.isOnboardingCompleted()) {
            startActivity(Intent(this@SplashActivity, OnboardingActivity::class.java))
            finish()
        } else {
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            intent.putExtra(IS_USER_LOGGED_IN, isLoggedIn)
            startActivity(intent)
            finish()
        }
    }

    private fun getFCMDeviceToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("SplashActivity", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            Log.d("SplashActivity", "FCM device token: $token")
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.default_notification_channel_id),
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    override fun connectViewModelEvents() {
        viewModel.getOnLoggedInResponse().observe(this, loggedInResponseObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnLoggedInResponse().removeObserver(loggedInResponseObserver)
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }
}