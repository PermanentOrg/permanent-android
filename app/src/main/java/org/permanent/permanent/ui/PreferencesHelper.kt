package org.permanent.permanent.ui

import android.content.SharedPreferences

const val PREFS_NAME = "permanent_preferences"
const val IS_ONBOARDING_COMPLETED = "onboarding_completed"
const val IS_USER_LOGGED_IN = "is_user_logged_in"
const val IS_WELCOME_SEEN = "is_welcome_seen"
const val PREFS_USER_ACCOUNT_ID = "preferences_user_account_id"
const val PREFS_USER_EMAIL = "preferences_user_email"
const val PREFS_CSRF = "preferences_csrf"
const val PREFS_ARCHIVE_ID = "preferences_archive_id"
const val PREFS_ROOT_ARCHIVE_NR = "preferences_root_archive_nr"
const val PREFS_USER_FULL_NAME = "preferences_user_full_name"

class PreferencesHelper(private val sharedPreferences: SharedPreferences) {

    fun isOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean(IS_ONBOARDING_COMPLETED, false)
    }

    fun setWelcomeDialogSeen() {
        with(sharedPreferences.edit()) {
            putBoolean(IS_WELCOME_SEEN, true)
            apply()
        }
    }

    fun isWelcomeDialogSeen(): Boolean {
        return sharedPreferences.getBoolean(IS_WELCOME_SEEN, false)
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

    fun getEmail(): String? {
        return sharedPreferences.getString(PREFS_USER_EMAIL, "")
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

    fun saveUserFullName(name: String?) {
        name?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_USER_FULL_NAME, name)
                apply()
            }
        }
    }

    fun getUserFullName(): String? {
        return sharedPreferences.getString(PREFS_USER_FULL_NAME, "")
    }
}