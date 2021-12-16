package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper


class PublicProfileViewModel(application: Application) : ObservableAndroidViewModel(application)  {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val onEditAboutRequest = MutableLiveData<Void>()
    private val onEditPersonInformationRequest = MutableLiveData<Void>()
    private val onEditOnlinePresenceRequest = MutableLiveData<Void>()
    private val onEditMilestonesRequest = MutableLiveData<Void>()
    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()

    fun onEditAboutBtnClick(){

    }

    fun onEditPersonInformationBtnClick(){

    }

    fun onEditOnlinePresenceBtnClick(){

    }

    fun onEditMilestonesBtnClick(){

    }

    fun getOnEditAboutRequest(): LiveData<Void> = onEditAboutRequest

    fun getOnEditPersonInformationRequest(): LiveData<Void> = onEditPersonInformationRequest

    fun getOnEditOnlinePresenceRequest(): LiveData<Void> = onEditOnlinePresenceRequest

    fun getOnEditMilestonesRequest(): LiveData<Void> = onEditMilestonesRequest

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage

}
