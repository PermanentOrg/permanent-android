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
    private val onEditAboutRequest = SingleLiveEvent<Void>()
    private val onEditPersonInformationRequest = SingleLiveEvent<Void>()
    private val onEditMilestonesRequest = SingleLiveEvent<Void>()
    private val onEditOnlinePresenceRequest = SingleLiveEvent<Void>()
    private val isBusy = MutableLiveData(false)
    private val showMessage = SingleLiveEvent<String>()

    fun onEditAboutBtnClick(){
        onEditAboutRequest.call()
    }

    fun onEditPersonInformationBtnClick(){
        onEditPersonInformationRequest.call()
    }

    fun onEditMilestonesBtnClick(){
        onEditMilestonesRequest.call()
    }

    fun onEditOnlinePresenceBtnClick(){
        onEditOnlinePresenceRequest.call()
    }

    fun getOnEditAboutRequest(): LiveData<Void> = onEditAboutRequest

    fun getOnEditPersonInformationRequest(): LiveData<Void> = onEditPersonInformationRequest

    fun getOnEditMilestonesRequest(): LiveData<Void> = onEditMilestonesRequest

    fun getOnEditOnlinePresenceRequest(): LiveData<Void> = onEditOnlinePresenceRequest

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun getShowMessage(): LiveData<String> = showMessage

}
