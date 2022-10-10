package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.models.File
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class SaveToPermanentViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val prefsHelper = PreferencesHelper(
        application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )
    private val currentArchiveThumb =
        MutableLiveData<String>(prefsHelper.getCurrentArchiveThumbURL())
    private val currentArchiveName =
        MutableLiveData<String>(prefsHelper.getCurrentArchiveFullName())
    private val onUploadRequest = SingleLiveEvent<Void>()
    private val onCancelRequest = SingleLiveEvent<Void>()

    fun onCancelBtnClick() {
        onCancelRequest.call()
    }

    fun onUploadBtnClick() {
        onUploadRequest.call()
    }

    fun getFiles(uris: ArrayList<Uri>): ArrayList<File> {
        val files = ArrayList<File>()
        for (uri in uris) files.add(File(appContext, uri))
        return files
    }

    fun getCurrentArchiveThumb(): MutableLiveData<String> = currentArchiveThumb

    fun getCurrentArchiveName(): MutableLiveData<String> = currentArchiveName

    fun getOnUploadRequest(): MutableLiveData<Void> = onUploadRequest

    fun getOnCancelRequest(): MutableLiveData<Void> = onCancelRequest
}
