package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class PublicViewModel(application: Application) : ObservableAndroidViewModel(application)  {
    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val profileBannerThumb =
        MutableLiveData<String>(prefsHelper.getPublicRecordThumbURL2000())
    private val currentArchiveThumb =
        MutableLiveData<String>(prefsHelper.getCurrentArchiveThumbURL())
    private val currentArchiveName = prefsHelper.getCurrentArchiveFullName()
    private val isBusy = MutableLiveData(false)


    fun getProfileBannerThumb(): MutableLiveData<String> = profileBannerThumb

    fun getCurrentArchiveThumb(): MutableLiveData<String> = currentArchiveThumb

    fun getCurrentArchiveName(): String? = currentArchiveName

    fun getIsBusy(): MutableLiveData<Boolean> = isBusy

    fun refreshBannerAndProfilePhotos() {
        profileBannerThumb.value = prefsHelper.getPublicRecordThumbURL2000()
        currentArchiveThumb.value = prefsHelper.getCurrentArchiveThumbURL()
    }
}