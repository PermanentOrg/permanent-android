package org.permanent.permanent.ui

import android.content.SharedPreferences

const val PREFS_NAME = "permanent_preferences"
const val IS_ONBOARDING_COMPLETED = "onboarding_completed"
const val IS_WELCOME_SEEN = "is_welcome_seen"
const val PREFS_SAVED_ACCOUNT_ID = "preferences_saved_account_id"
const val PREFS_SAVED_EMAIL = "preferences_saved_email"
const val PREFS_IS_PHONE_VERIFIED = "preferences_is_phone_verified"
const val PREFS_SAVED_CSRF = "preferences_saved_csrf"

class PreferencesHelper(private val sharedPreferences: SharedPreferences) {

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

    fun savePhoneVerified() {
        with(sharedPreferences.edit()) {
            putBoolean(PREFS_IS_PHONE_VERIFIED, true)
            apply()
        }
    }

    fun isPhoneVerified(): Boolean {
        return sharedPreferences.getBoolean(PREFS_IS_PHONE_VERIFIED, false)
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