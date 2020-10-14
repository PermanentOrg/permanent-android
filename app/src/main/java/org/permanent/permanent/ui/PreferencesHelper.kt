package org.permanent.permanent.ui

import android.content.SharedPreferences

const val PREFS_NAME = "permanent_preferences"
const val IS_ONBOARDING_COMPLETED = "onboarding_completed"
const val IS_USER_LOGGED_IN = "is_user_logged_in"
const val IS_WELCOME_SEEN = "is_welcome_seen"
const val PREFS_SAVED_ACCOUNT_ID = "preferences_saved_account_id"
const val PREFS_SAVED_EMAIL = "preferences_saved_email"
const val PREFS_SAVED_CSRF = "preferences_saved_csrf"

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

    fun saveAccountId(id: String) {
        with(sharedPreferences.edit()) {
            putString(PREFS_SAVED_ACCOUNT_ID, id)
            apply()
        }
    }

    fun getAccountId(): String? {
        return sharedPreferences.getString(PREFS_SAVED_ACCOUNT_ID, "")
    }

    fun saveEmail(email: String) {
        with(sharedPreferences.edit()) {
            putString(PREFS_SAVED_EMAIL, email)
            apply()
        }
    }

    fun getEmail(): String? {
        return sharedPreferences.getString(PREFS_SAVED_EMAIL, "")
    }

    fun saveCsrf(csrf: String?) {
        csrf?.let {
            with(sharedPreferences.edit()) {
                putString(PREFS_SAVED_CSRF, csrf)
                apply()
            }
        }
    }

    fun getCsrf(): String? {
        return sharedPreferences.getString(PREFS_SAVED_CSRF, "")
    }
}