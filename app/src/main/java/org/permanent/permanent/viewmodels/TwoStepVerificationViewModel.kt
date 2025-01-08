package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.permanent.permanent.network.models.TwoFAVO
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class TwoStepVerificationViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    private val _isTwoFAEnabled = MutableStateFlow(prefsHelper.isTwoFAEnabled())
    val isTwoFAEnabled: StateFlow<Boolean> = _isTwoFAEnabled

    private val _twoFAList = MutableStateFlow(prefsHelper.getTwoFAList())
    val twoFAList: StateFlow<List<TwoFAVO>> = _twoFAList

    fun updateTwoFAList(newList: List<TwoFAVO>) {
        prefsHelper.setTwoFAList(newList)
        _twoFAList.value = newList
    }
}