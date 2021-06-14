package org.permanent.permanent.network

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.permanent.permanent.Constants.Companion.ERROR_MFA_TOKEN
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.login.LoginActivity
import java.io.IOException

class RequiresMFAInterceptor : Interceptor {

    private var prefsHelper: PreferencesHelper = PreferencesHelper(
        PermanentApplication.instance.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
    )

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val response = chain.proceed(request)
        if (response.code == 200 && response.message == ERROR_MFA_TOKEN) {
            Log.i(TAG, "Requires MFA Token, redirecting to log in")
            prefsHelper.saveUserLoggedIn(false)
            prefsHelper.saveBiometricsLogIn(true) // Setting back to default
            val currentActivity = PermanentApplication.instance.currentActivity
            currentActivity?.startActivity(
                Intent(
                    currentActivity,
                    LoginActivity::class.java
                )
            )
            currentActivity?.let {
                Toast.makeText(
                    it,
                    "Session expired. Please re-authenticate",
                    Toast.LENGTH_SHORT
                ).show()
            }

            return response
        }
        return response
    }

    companion object {
        private val TAG = RequiresMFAInterceptor::class.java.simpleName
    }
}