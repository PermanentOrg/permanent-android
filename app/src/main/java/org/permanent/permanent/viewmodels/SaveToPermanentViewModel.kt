package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.R
import org.permanent.permanent.models.File
import org.permanent.permanent.models.Record
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
    private val destinationFolderName =
        MutableLiveData(appContext.getString(R.string.save_to_permanent_mobile_uploads))
    private val onUploadRequest = SingleLiveEvent<Void>()
    private val onChangeDestinationFolderRequest = SingleLiveEvent<Void>()
    private val onCancelRequest = SingleLiveEvent<Void>()

    fun onCancelBtnClick() {
        onCancelRequest.call()
    }

    fun onUploadBtnClick() {
        onUploadRequest.call()
    }

    fun onDestinationArchiveClick() {
    }

    fun onDestinationFolderClick() {
        onChangeDestinationFolderRequest.call()
    }

    fun changeDestinationFolderTo(record: Record?) {
        if (record != null) destinationFolderName.value = record.displayName
        else destinationFolderName.value = appContext.getString(R.string.save_to_permanent_mobile_uploads)
    }

    fun getFiles(uris: ArrayList<Uri>): ArrayList<File> {
        val files = ArrayList<File>()
        for (uri in uris) files.add(File(appContext, uri))
        return files
    }

    fun getCurrentArchiveThumb(): MutableLiveData<String> = currentArchiveThumb

    fun getCurrentArchiveName(): MutableLiveData<String> = currentArchiveName

    fun getDestinationFolderName(): MutableLiveData<String> = destinationFolderName

    fun getOnUploadRequest(): MutableLiveData<Void> = onUploadRequest

    fun getOnChangeDestinationFolderRequest(): MutableLiveData<Void> =
        onChangeDestinationFolderRequest

    fun getOnCancelRequest(): MutableLiveData<Void> = onCancelRequest
}
