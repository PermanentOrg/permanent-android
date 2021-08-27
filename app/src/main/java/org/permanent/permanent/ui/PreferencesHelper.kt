package org.permanent.permanent.ui

import android.content.SharedPreferences

const val PREFS_NAME = "permanent_preferences"
const val IS_ONBOARDING_COMPLETED = "onboarding_completed"
const val IS_USER_SIGNED_UP_IN_APP = "is_user_signed_up_in_app"
const val IS_USER_LOGGED_IN = "is_user_logged_in"
const val IS_BIOMETRICS_LOG_IN = "is_biometrics_log_in"
const val IS_WELCOME_SEEN = "is_welcome_seen"
const val PREFS_ACCOUNT_ID = "preferences_user_account_id"
const val PREFS_ACCOUNT_EMAIL = "preferences_user_email"
const val PREFS_ACCOUNT_PASSWORD = "preferences_user_password"
const val PREFS_CSRF = "preferences_csrf"
const val PREFS_DEFAULT_ARCHIVE_ID = "preferences_default_archive_id"
const val PREFS_CURRENT_ARCHIVE_ID = "preferences_current_archive_id"
const val PREFS_CURRENT_ARCHIVE_NUMBER = "preferences_current_archive_number"
const val PREFS_CURRENT_ARCHIVE_FULL_NAME = "preferences_current_archive_full_name"
const val PREFS_CURRENT_ARCHIVE_THUMB_URL = "preferences_current_archive_thumb_url"
const val PREFS_SHARE_LINK_URL_TOKEN = "preferences_share_link_url_token"

class PreferencesHelper(private val sharedPreferences: SharedPreferences) {

    fun isOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean(IS_ONBOARDING_COMPLETED, false)
    }

    fun saveWelcomeDialogSeen() {
        with(sharedPreferences.edit()) {
            putBoolean(IS_WELCOME_SEEN, true)
            apply()
        }
    }

    fun isWelcomeDialogSeen(): Boolean {
        return sharedPreferences.getBoolean(IS_WELCOME_SEEN, false)
    }

    fun saveUserSignedUpInApp() {
        with(sharedPreferences.edit()) {
            putBoolean(IS_USER_SIGNED_UP_IN_APP, true)
            apply()
        }
    }

    fun isUserSignedUpInApp(): Boolean {
        return sharedPreferences.getBoolean(IS_USER_SIGNED_UP_IN_APP, false)
    }

    fun saveUserLoggedIn(isLoggedIn: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(IS_USER_LOGGED_IN, isLoggedIn)
            apply()
        }
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(IS_USER_LOGGED_IN, false)
    }

    fun saveBiometricsLogIn(isBiometricsLogIn: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(IS_BIOMETRICS_LOG_IN, isBiometricsLogIn)
            apply()
        }
    }

    fun isBiometricsLogIn(): Boolean {
        return sharedPreferences.getBoolean(IS_BIOMETRICS_LOG_IN, true)
    }

    fun saveAccountEmail(email: String) {
        with(sharedPreferences.edit()) {
            putString(PREFS_ACCOUNT_EMAIL, email)
            apply()
        }
    }

    fun getAccountEmail(): String? {
        return sharedPreferences.getString(PREFS_ACCOUNT_EMAIL, "")
    }

    fun saveAccountPass(pass: String) {
        with(sharedPreferences.edit()) {
            putString(PREFS_ACCOUNT_PASSWORD, pass)
            apply()
        }
    }

    fun getAccountPass(): String? {
        return sharedPreferences.getString(PREFS_ACCOUNT_PASSWORD, "")
    }

    fun saveCsrf(csrf: String?) {
        csrf?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_CSRF, csrf)
                apply()
            }
        }
    }

    fun getCsrf(): String? {
        return sharedPreferences.getString(PREFS_CSRF, "")
    }

    fun saveAccountId(id: Int?) {
        id?.let {
            with(sharedPreferences.edit()) {
                putInt(PREFS_ACCOUNT_ID, id)
                apply()
            }
        }
    }

    fun getAccountId(): Int {
        return sharedPreferences.getInt(PREFS_ACCOUNT_ID, 0)
    }

    fun saveDefaultArchiveId(id: Int?) {
        id?.let {
            with(sharedPreferences.edit()) {
                putInt(PREFS_DEFAULT_ARCHIVE_ID, id)
                apply()
            }
        }
    }

    fun getDefaultArchiveId(): Int {
        return sharedPreferences.getInt(PREFS_DEFAULT_ARCHIVE_ID, 0)
    }

    fun saveCurrentArchiveInfo(id: Int?, number: String?, name: String?, thumbURL: String?) {
        id?.let {
            with(sharedPreferences.edit()) {
                putInt(PREFS_CURRENT_ARCHIVE_ID, id)
                apply()
            }
        }
        number?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_CURRENT_ARCHIVE_NUMBER, number)
                apply()
            }
        }
        name?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_CURRENT_ARCHIVE_FULL_NAME, name)
                apply()
            }
        }
        thumbURL?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_CURRENT_ARCHIVE_THUMB_URL, thumbURL)
                apply()
            }
        }
    }

    fun getCurrentArchiveId(): Int {
        return sharedPreferences.getInt(PREFS_CURRENT_ARCHIVE_ID, 0)
    }

    fun getCurrentArchiveNr(): String? {
        return sharedPreferences.getString(PREFS_CURRENT_ARCHIVE_NUMBER, "")
    }

    fun getCurrentArchiveFullName(): String? {
        return sharedPreferences.getString(PREFS_CURRENT_ARCHIVE_FULL_NAME, "")
    }

    fun getCurrentArchiveThumbURL(): String? {
        return sharedPreferences.getString(PREFS_CURRENT_ARCHIVE_THUMB_URL, "")
    }

    fun saveShareLinkUrlToken(urlToken: String) {
        with(sharedPreferences.edit()) {
            putString(PREFS_SHARE_LINK_URL_TOKEN, urlToken)
            apply()
        }
    }

    fun getShareLinkUrlToken(): String? {
        return sharedPreferences.getString(PREFS_SHARE_LINK_URL_TOKEN, "")
    }
}