package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class LoginAndSecurityViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val _isBiometricsEnabled = MutableStateFlow(prefsHelper.isBiometricsLogIn())
    val isBiometricsEnabled: StateFlow<Boolean> = _isBiometricsEnabled

    private val _isTwoFAEnabled = MutableStateFlow(prefsHelper.isTwoFAEnabled())
    val isTwoFAEnabled: StateFlow<Boolean> = _isTwoFAEnabled

    fun updateBiometricsEnabled(enabled: Boolean) {
        _isBiometricsEnabled.value = enabled
        prefsHelper.setBiometricsLogIn(enabled)
    }
}
