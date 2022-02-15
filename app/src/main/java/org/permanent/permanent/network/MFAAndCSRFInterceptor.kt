package org.permanent.permanent.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.Constants
import org.permanent.permanent.Constants.Companion.ERROR_INVALID_CSRF
import org.permanent.permanent.Constants.Companion.ERROR_MFA_TOKEN
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.R
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.showLoginScreen
import java.io.IOException

class MFAAndCSRFInterceptor : Interceptor {

    private var prefsHelper: PreferencesHelper = PreferencesHelper(
        PermanentApplication.instance.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
    )

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val requestUrl = request.url.toString()
        val response = chain.proceed(request)

        if (requestUrl.contains(BuildConfig.BASE_API_URL)
            && !requestUrl.contains(Constants.LOGIN_URL_SUFFIX)
        ) {
            response.body?.let { responseBody ->
                val rawJson = responseBody.string()

                if (response.code == 401 || rawJson.contains(ERROR_MFA_TOKEN) || rawJson.contains(ERROR_INVALID_CSRF)) {
                    Log.i(TAG, "Requires MFA Token or CSRF is invalid, redirecting to log in")
                    prefsHelper.saveUserLoggedIn(false)
                    prefsHelper.saveBiometricsLogIn(true) // Setting back to default
                    val currentActivity = PermanentApplication.instance.currentActivity
                    currentActivity?.let {
//                        it.startActivity(Intent(it, SplashActivity::class.java))
                        it.runOnUiThread {
                            Toast.makeText(
                                it,
                                it.getString(R.string.warning_auth_mfa_token_message),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    PermanentApplication.instance.showLoginScreen()
                }
                // Re-create the response before returning it because body can be read only once
                return response
                    .newBuilder()
                    .body(rawJson.toResponseBody(responseBody.contentType()))
                    .build()
            }
        }
        return response
    }

    companion object {
        private val TAG = MFAAndCSRFInterceptor::class.java.simpleName
    }
}