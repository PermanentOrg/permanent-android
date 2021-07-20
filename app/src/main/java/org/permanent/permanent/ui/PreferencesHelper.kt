package org.permanent.permanent.ui

import android.content.SharedPreferences

const val PREFS_NAME = "permanent_preferences"
const val IS_ONBOARDING_COMPLETED = "onboarding_completed"
const val IS_USER_LOGGED_IN = "is_user_logged_in"
const val IS_BIOMETRICS_LOG_IN = "is_biometrics_log_in"
const val IS_WELCOME_SEEN = "is_welcome_seen"
const val IS_USER_SIGNED_UP_IN_APP = "is_user_signed_up_in_app"
const val PREFS_USER_ACCOUNT_ID = "preferences_user_account_id"
const val PREFS_USER_EMAIL = "preferences_user_email"
const val PREFS_USER_PASSWORD = "preferences_user_password"
const val PREFS_CSRF = "preferences_csrf"
const val PREFS_ARCHIVE_ID = "preferences_archive_id"
const val PREFS_ROOT_ARCHIVE_NR = "preferences_root_archive_nr"
const val PREFS_ACCOUNT_FULL_NAME = "preferences_account_full_name"
const val PREFS_ARCHIVE_FULL_NAME = "preferences_archive_full_name"
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

    fun saveUserAccountId(id: Int?) {
        id?.let {
            with(sharedPreferences.edit()) {
                putInt(PREFS_USER_ACCOUNT_ID, id)
                apply()
            }
        }
    }

    fun getUserAccountId(): Int {
        return sharedPreferences.getInt(PREFS_USER_ACCOUNT_ID, 0)
    }

    fun saveUserEmail(email: String) {
        with(sharedPreferences.edit()) {
            putString(PREFS_USER_EMAIL, email)
            apply()
        }
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(PREFS_USER_EMAIL, "")
    }

    fun saveUserPass(pass: String) {
        with(sharedPreferences.edit()) {
            putString(PREFS_USER_PASSWORD, pass)
            apply()
        }
    }

    fun getUserPass(): String? {
        return sharedPreferences.getString(PREFS_USER_PASSWORD, "")
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

    fun saveArchiveId(id: Int?) {
        id?.let {
            with(sharedPreferences.edit()) {
                putInt(PREFS_ARCHIVE_ID, id)
                apply()
            }
        }
    }

    fun getArchiveId(): Int {
        return sharedPreferences.getInt(PREFS_ARCHIVE_ID, 0)
    }

    fun saveRootArchiveNr(archiveNr: String?) {
        archiveNr?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_ROOT_ARCHIVE_NR, archiveNr)
                apply()
            }
        }
    }

    fun getUserArchiveNr(): String {
        return sharedPreferences.getString(PREFS_ROOT_ARCHIVE_NR, "")
            ?.substringBefore("-") + "-0000"
    }

    fun saveAccountFullName(name: String?) {
        name?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_ACCOUNT_FULL_NAME, name)
                apply()
            }
        }
    }

    fun getAccountFullName(): String? {
        return sharedPreferences.getString(PREFS_ACCOUNT_FULL_NAME, "")
    }

    fun saveArchiveFullName(name: String?) {
        name?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_ARCHIVE_FULL_NAME, name)
                apply()
            }
        }
    }

    fun getArchiveFullName(): String? {
        return sharedPreferences.getString(PREFS_ARCHIVE_FULL_NAME, "")
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